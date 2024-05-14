package iljin.framework.ebid.custom.service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.map.HashedMap;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.custom.dto.TCoCustMasterDto;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.PagaUtils;
import iljin.framework.ebid.etc.util.common.consts.DB;
import iljin.framework.ebid.etc.util.common.file.FileService;
import iljin.framework.ebid.etc.util.common.mail.service.MailService;
import iljin.framework.ebid.etc.util.common.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustService {
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	private FileService fileService;
	@Autowired
	private MailService mailService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private MessageService messageService;
	@Autowired
	private GeneralDao generalDao;
	
	@SuppressWarnings({ "rawtypes", "unused" })
	public Page custList(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();

		Page listPage = generalDao.selectGernalListPage("cust.selectTCoCustList", params);
		
		return listPage;

	}

	public Page otherCustList(Map<String, Object> params) {
		StringBuilder sbCount = new StringBuilder("SELECT count(1)\n");
		StringBuilder sbList = new StringBuilder(
				"SELECT	a.cust_code\n"
				+ ",	a.cust_name\n"
				+ ",	case\n"
				+ "			when (select ifnull(ITEM_NAME, '') from t_co_item where item_code = a.CUST_TYPE2) != ''\n"
				+ "			then CONCAT((select CONCAT('1. ', item_name, '<br/>') from t_co_item x where x.item_code = a.cust_type1) , IFNULL((select CONCAT('2. ', item_name) from t_co_item x where x.item_code = a.cust_type2), ''))\n"
				+ "			else (select CONCAT('1. ', item_name) from t_co_item where ITEM_CODE = a.CUST_TYPE1)\n"
				+ "		end as cust_type1\n"
				+ ",	case\n"
				+ "			when ifnull(regnum, '') != ''\n"
				+ "			then CONCAT(SUBSTR(regnum, 1, 3), '-', SUBSTR(regnum, 4, 2), '-', SUBSTR(regnum, 6, 5))\n"
				+ "			else regnum\n"
				+ "		end as regnum\n"
				+ ",	pres_name\n"
				+ ",	(\n"
				+ "			select GROUP_CONCAT(interrelated_nm separator '<br/>')\n"
				+ "			from t_co_cust_ir x\n"
				+ "			,		t_co_interrelated y\n"
				+ "			where x.cust_code = a.cust_code\n"
				+ "			and x.interrelated_cust_code = y.interrelated_cust_code\n"
				+ "		) as interrelated_nm\n"
		);
		
		StringBuilder sbFrom = new StringBuilder(
				"FROM	t_co_cust_master a\n"
				+ "WHERE	a.CERT_YN = 'Y'\n"
				+ "AND	a.cust_code NOT IN\n"
				+ "(\n"
				+ "	select tcci.cust_code\n"
				+ "	from t_co_cust_ir tcci\n"
				+ "	where tcci.interrelated_cust_code = :custCode\n"
				+ ")\n"
		);
		

        if (!StringUtils.isEmpty(params.get("custType"))) {
        	sbFrom.append("AND	(a.cust_type1 = :custType OR a.cust_type2 = :custType)\n");
        }
        if (!StringUtils.isEmpty(params.get("custName"))) {
        	sbFrom.append("AND	cust_name like concat('%',:custName,'%')\n");
        }
        sbList.append(sbFrom);
        sbList.append("order by create_date desc");
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        sbCount.append(sbFrom);
        Query queryTotal = entityManager.createNativeQuery(sbCount.toString());

        queryList.setParameter("custCode", params.get("custCode"));
        queryTotal.setParameter("custCode", params.get("custCode"));

        if (!StringUtils.isEmpty(params.get("custType"))) {
            queryList.setParameter("custType", params.get("custType"));
            queryTotal.setParameter("custType", params.get("custType"));
        }
        if (!StringUtils.isEmpty(params.get("custName"))) {
            queryList.setParameter("custName", params.get("custName"));
            queryTotal.setParameter("custName", params.get("custName"));
        }

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, TCoCustMasterDto.class);

        BigInteger count = (BigInteger) queryTotal.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }
	
    public TCoCustMasterDto custDetailForCust(String id) {
        StringBuilder sb = new StringBuilder(" SELECT a.cust_code \n" +
                "     , cust_name \n" +
                "     , (SELECT GROUP_CONCAT(interrelated_nm SEPARATOR '<br/>') FROM t_co_cust_ir x, t_co_interrelated y WHERE x.cust_code = a.cust_code AND x.interrelated_cust_code = y.interrelated_cust_code) AS interrelated_nm\n" +
                "     , cust_type1, cust_type2\n" +
                "     , (SELECT item_name FROM t_co_item x WHERE x.item_code = a.cust_type1) AS cust_type_nm1\n" +
                "     , (SELECT item_name FROM t_co_item x WHERE x.item_code = a.cust_type2) AS cust_type_nm2\n" +
                "     , CONCAT(SUBSTR(regnum, 1, 3), '-', SUBSTR(regnum, 4, 2), '-', SUBSTR(regnum, 6, 5)) AS regnum\n" +
                "     , SUBSTR(regnum, 1, 3) AS regnum1\n" +
                "     , SUBSTR(regnum, 4, 2) AS regnum2\n" +
                "     , SUBSTR(regnum, 6, 5) AS regnum3\n" +
                "     , pres_name \n" +
                "     , CONCAT(SUBSTR(pres_jumin_no, 1, 6), '-', SUBSTR(pres_jumin_no, 7, 7)) AS pres_jumin_no\n" +
                "     , SUBSTR(pres_jumin_no, 1, 6) AS pres_jumin_no1\n" +
                "     , SUBSTR(pres_jumin_no, 7, 7) AS pres_jumin_no2\n" +
                "     , capital\n" +
                "     , found_year \n" +
                "     , tel\n" +
                "     , fax\n" +
                "     , zipcode \n" +
                "     , addr \n" +
                "     , addr_detail\n" +
                "     , regnum_file\n" +
                "     , regnum_path\n" +
                "     , b_file\n" +
                "     , b_file_path\n" +
                "     , cert_yn \n" +
                "     , etc \n" +
                "     , b.user_name \n" +
                "     , b.user_email \n" +
                "     , b.user_id \n" +
                "     , b.user_hp \n" +
                "     , b.user_tel \n" +
                "     , b.user_buseo \n" +
                "     , b.user_position \n" +
                "  FROM t_co_cust_master a\n" +
                "     , t_co_cust_user   b\n" +
                " WHERE a.cust_code = b.cust_code\n" +
                "   AND b.user_type = '1'" +
                "   AND a.cust_code   = :custCode" +
                " LIMIT 1");
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter("custCode", id);
        TCoCustMasterDto data = new JpaResultMapper().uniqueResult(query, TCoCustMasterDto.class);
        return data;
    }
	
	@SuppressWarnings("unchecked")
	public ResultBody custDetail(String id) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		Map<String, Object> params = new HashedMap();
		params.put("custCode", id);
		
		Map<String, Object> custObj = (Map<String, Object>) generalDao.selectGernalObject("cust.selectTCoCustDetail", params);
		
		resultBody.setData(custObj);
		return resultBody;
	}
	
    public TCoCustMasterDto custDetailForInter(String id) {
        StringBuilder sb = new StringBuilder(" SELECT a.cust_code \n" +
                "     , cust_name \n" +
                "     , (SELECT interrelated_nm FROM t_co_interrelated x WHERE x.interrelated_cust_code = c.interrelated_cust_code) AS interrelated_nm\n" +
                "     , cust_type1, cust_type2\n" +
                "     , (SELECT item_name FROM t_co_item x WHERE x.item_code = a.cust_type1) AS cust_type_nm1\n" +
                "     , (SELECT item_name FROM t_co_item x WHERE x.item_code = a.cust_type2) AS cust_type_nm2\n" +
                "     , CASE\n"+
                "     		WHEN IFNULL(regnum, '') = ''\n"+
                "     		THEN regnum\n"+
                "     		ELSE CONCAT(SUBSTR(regnum, 1, 3), '-', SUBSTR(regnum, 4, 2), '-', SUBSTR(regnum, 6, 5))\n"+
                "     	END as regnum\n"+
                "     , SUBSTR(regnum, 1, 3) AS regnum1\n" +
                "     , SUBSTR(regnum, 4, 2) AS regnum2\n" +
                "     , SUBSTR(regnum, 6, 5) AS regnum3\n" +
                "     , pres_name \n" +
                "     , CASE\n"+
                "     		WHEN IFNULL(pres_jumin_no, '') = ''\n"+
                "     		THEN pres_jumin_no\n"+
                "     		ELSE CONCAT(SUBSTR(pres_jumin_no, 1, 6), '-', SUBSTR(pres_jumin_no, 7, 7))\n"+
                "     	END as pres_jumin_no\n"+
                "     , SUBSTR(pres_jumin_no, 1, 6) AS pres_jumin_no1\n" +
                "     , SUBSTR(pres_jumin_no, 7, 7) AS pres_jumin_no2\n" +
                "     , capital\n" +
                "     , found_year \n" +
                "     , tel\n" +
                "     , fax\n" +
                "     , zipcode \n" +
                "     , addr \n" +
                "     , addr_detail\n" +
                "     , regnum_file\n" +
                "     , regnum_path\n" +
                "     , b_file\n" +
                "     , b_file_path\n" +
                "     , cert_yn \n" +
                "     , etc \n" +
                "     , cust_level \n" +
                "     , cust_valuation \n" +
                "     , care_content \n" +
                "     , b.user_name \n" +
                "     , b.user_email \n" +
                "     , b.user_id \n" +
                "     , b.user_hp \n" +
                "     , b.user_tel \n" +
                "     , b.user_buseo \n" +
                "     , b.user_position \n" +
                "  FROM t_co_cust_master a\n" +
                "     , t_co_cust_user   b\n" +
                "     , t_co_cust_ir     c\n" +
                " WHERE a.cust_code = b.cust_code\n" +
                "   AND a.cust_code = c.cust_code\n" +
                "   AND b.user_type = '1'\n" +
                "   AND c.interrelated_cust_code = :interrelatedCustCode\n" +
                "   AND a.cust_code   = :custCode\n" +
                " LIMIT 1");
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter("custCode", id);
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        query.setParameter("interrelatedCustCode", user.getCustCode());
        TCoCustMasterDto data = new JpaResultMapper().uniqueResult(query, TCoCustMasterDto.class);
        return data;
    }
	// 업체 승인
	@Transactional
	public void approval(Map<String, Object> params, CustomUserDetails user) throws Exception {
		params.put("userId", user.getUsername());
		params.put("certYn", "Y");
		// 업체 승인 처리
		generalDao.updateGernal("cust.updateTCoCustMasterCert", params);

		// 업체 이력 등록
		insertHistory(params);

		// 업체 승인 메일 저장 처리
		mailService.saveMailInfo("[일진그룹 e-bidding] 회원가입 승인",
				"[" + user.getCustName() + "] 계열사에서 [" + params.get("custName") + "] 업체 승인처리 되었습니다.\n"
						+ "<b>e-bidding 시스템</b>에 로그인하고 입찰업무를 처리해 주십시오\n"
						+ "입찰 업무는 로그인 후 하단에 입찰업무 안내를 참고하시거나 공지메뉴의 매뉴얼을 참조해 주십시오\n" + "\n" + "감사합니다.\n",
				(String) params.get("userEmail"));

		try {
			// 업체 승인 문자 발송
			messageService.send("일진그룹", CommonUtils.getString(params.get("userHp")).replaceAll("-", ""), (String) params.get("userName"),
					"[일진그룹 전자입찰시스템] 요청하신 일진그룹 전자입찰 시스템 회원가입이 승인되었습니다.");
		} catch (Exception e) {
			log.error("approval send message error : {}", e);
		}
	}

	@Transactional
	public void back(Map<String, Object> params, CustomUserDetails user) throws Exception {
		params.put("userId", user.getUsername());
		params.put("certYn", "D");
		// 업체 반려 처리
		generalDao.updateGernal("cust.updateTCoCustMasterCert", params);

		// 업체 이력 등록
		insertHistory(params);

		// 업체 삭제 처리
		generalDao.deleteGernal("cust.updateTCoCustMasterCert", params);
		// 매핑 삭제 처리
		generalDao.deleteGernal("cust.deleteTCoCustIr", params);
		// 업체 사용자 삭제 처리
		generalDao.deleteGernal("cust.deleteTCoCustUser", params);
		
		// 반려 메일 저장 처리
		mailService.saveMailInfo("[일진그룹 e-bidding] 회원가입 반려",
				"[" + user.getCustName() + "] 계열사에서 [" + params.get("custName") + "] 업체 반려처리 되었습니다.\n"
				+ "아래 반려 사유를 확인해 주십시오\n" + "\n" + "감사합니다.\n" + "\n" + "- 반려사유\n" + params.get("etc"),
				(String) params.get("userEmail"));
	}
	
    @Transactional
    public ResultBody del(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
        StringBuilder sbQuery = new StringBuilder(" UPDATE t_co_cust_master SET cert_yn = 'D', etc = :etc, update_user = :userId, update_date = now() WHERE cust_code = :custCode LIMIT 1");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("etc", params.get("etc"));
        query.setParameter("custCode", params.get("custCode"));
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        query.setParameter("userId", user.getUsername());
        query.executeUpdate();

        // 협력사 이력 등록
//        insertHistory(params);

        // 사용자 삭제 처리
        sbQuery = new StringBuilder(" UPDATE t_co_cust_user SET update_user = :userId, update_date = now(), use_yn = 'N' WHERE cust_code = :custCode");
        query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("userId", user.getUsername());
        query.setParameter("custCode", params.get("custCode"));
        query.executeUpdate();
        return resultBody;
    }
    @Transactional
    public ResultBody insert(Map<String, Object> params, MultipartFile regnumFile, MultipartFile bFile) {
        ResultBody resultBody = new ResultBody();
        try {
            if (regnumFile != null) {
                params.put("regnumPath", fileService.uploadFile(regnumFile));
                params.put("regnumFile", regnumFile.getOriginalFilename());
            }
            if (bFile != null) {
                params.put("bFilePath", fileService.uploadFile(bFile, "N"));
                params.put("bFile", bFile.getOriginalFilename());
            }
        } catch (IOException e) {
            resultBody.setCode("UPLOAD");
            resultBody.setMsg("파일 업로드시 오류가 발생했습니다.");
            return resultBody;
        }
        
        CustomUserDetails user = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        StringBuilder sbQuery = new StringBuilder(" INSERT INTO t_co_cust_master (cust_type1, cust_type2, cust_name, regnum, pres_name, pres_jumin_no, tel, fax, zipcode, addr, addr_detail, capital, found_year, cert_yn, etc, create_user, create_date, update_user, update_date, interrelated_cust_code, b_file, b_file_path, regnum_file, regnum_path)" +
                " VALUES (:custType1, :custType2, :custName, :regnum, :presName, :presJuminNo, :tel, :fax, :zipcode, :addr, :addrDetail, :capital, :foundYear, :certYn, :etc, :userId, now(), :userId, now(), :interrelatedCustCode, :bFile, :bFilePath, :regnumFile, :regnumPath)");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("custType1",		params.get("custType1"));
        query.setParameter("custType2",		params.get("custType2"));
        query.setParameter("custName",		params.get("custName"));
        query.setParameter("regnum",		CommonUtils.getString(params.get("regnum1"))+CommonUtils.getString(params.get("regnum2"))+CommonUtils.getString(params.get("regnum3")));
        query.setParameter("presName",		params.get("presName"));
        query.setParameter("presJuminNo",	CommonUtils.getString(params.get("presJuminNo1"))+CommonUtils.getString(params.get("presJuminNo2")));
        query.setParameter("tel",			CommonUtils.getString(params.get("tel")).replaceAll("-", ""));
        query.setParameter("fax",			CommonUtils.getString(params.get("fax")).replaceAll("-", ""));
        query.setParameter("zipcode",		params.get("zipcode"));
        query.setParameter("addr",			params.get("addr"));
        query.setParameter("addrDetail",	params.get("addrDetail"));
        query.setParameter("capital",		CommonUtils.getString(params.get("capital")).replaceAll(",", ""));
        query.setParameter("foundYear",		params.get("foundYear"));
        query.setParameter("certYn",		user == null ? "N" : "Y"); // 승인요청 : 승인
        query.setParameter("etc",			"");
        query.setParameter("interrelatedCustCode",	params.get("interrelatedCustCode"));
        query.setParameter("bFile",			params.get("bFile"));
        query.setParameter("bFilePath",		params.get("bFilePath"));
        query.setParameter("regnumFile",	params.get("regnumFile"));
        query.setParameter("regnumPath",	params.get("regnumPath"));
        query.setParameter("userId",		user == null ? "" : user.getUsername());
        query.executeUpdate();

        query = entityManager.createNativeQuery("SELECT LAST_INSERT_ID()");
        BigInteger custCode = (BigInteger) query.getSingleResult();

        // 협력사 이력 등록
//        insertHistory(params);

        // 계열사_협력사_매핑 등록
        sbQuery = new StringBuilder(" INSERT INTO t_co_cust_ir (cust_code, interrelated_cust_code) VALUES (:custCode, :interrelatedCustCode)");
        query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("custCode", custCode.intValue());
        query.setParameter("interrelatedCustCode", user == null ? params.get("interrelatedCustCode") : user.getCustCode());
        query.executeUpdate();

        // 관리자 등록 처리
        sbQuery = new StringBuilder(" INSERT INTO t_co_cust_user (user_id, cust_code, user_pwd, user_name, user_tel, user_hp, user_email, user_type, user_buseo, user_position, create_user, create_date, update_user, update_date, pwd_chg_date, use_yn)" +
                " VALUES (:userId, :custCode, :userPwd, :userName, :userTel, :userHp, :userEmail, :userType, :userBuseo, :userPosition, :updUserId, now(), :updUserId, now(), now(), :useYn)");
        query = entityManager.createNativeQuery(sbQuery.toString());
        String userHp = CommonUtils.getString(params.get("userHp")).replaceAll("-", "");
        query.setParameter("userId",		params.get("userId"));
        query.setParameter("custCode",		custCode.intValue());
        query.setParameter("userPwd",		passwordEncoder.encode((String) params.get("userPwd")));
        query.setParameter("userName",		params.get("userName"));
        query.setParameter("userTel",		CommonUtils.getString(params.get("userTel")).replaceAll("-", ""));
        query.setParameter("userHp",		userHp);
        query.setParameter("userEmail",		params.get("userEmail"));
        query.setParameter("userType",		"1"); // 관리자
        query.setParameter("userBuseo",		params.get("userBuseo"));
        query.setParameter("userPosition",	params.get("userPosition"));
        query.setParameter("updUserId",		user == null ? "" : user.getUsername());
        query.setParameter("useYn", "Y");
        query.executeUpdate();

        if (user == null) {

            String title = "[일진그룹 e-bidding] 신규업체 승인 요청";
            String content = "[" + params.get("custName") + "] 신규업체 승인 요청이 왔습니다.\n" +
                    "e-bidding 시스템에 로그인하고 업체정보의 업체승인 페이지에서 \n" +
                    "업체 정보를 확인하십시오\n" +
                    "처리는 3일 이내 처리해야 합니다..\n" +
                    "\n" +
                    "감사합니다.";

            sbQuery = new StringBuilder(" SELECT user_email FROM t_co_user WHERE user_auth = '2' AND use_yn = 'Y' AND interrelated_cust_code = :interrelatedCustCode");
            query = entityManager.createNativeQuery(sbQuery.toString());
            query.setParameter("interrelatedCustCode", params.get("interrelatedCustCode"));
            List<String> list = (List<String>) query.getResultList();

            // 회원가입 승인요청 메일 저장 처리
            for (String userEmail : list) {
                mailService.saveMailInfo(title, content, userEmail);
            }
        } else {
            // 회원가입 승인 메일 저장 처리
            mailService.saveMailInfo("[일진그룹 e-bidding] 회원가입 승인", "[" + user.getCustName() + "] 계열사에서 [" + params.get("custName") + "] 업체 승인처리 되었습니다.\n" +
                    "<b>e-bidding 시스템</b>에 로그인하고 입찰업무를 처리해 주십시오\n" +
                    "입찰 업무는 로그인 후 하단에 입찰업무 안내를 참고하시거나 공지메뉴의 매뉴얼을 참조해 주십시오\n" +
                    "\n" +
                    "감사합니다.\n", (String) params.get("userEmail"));
            
            
			try{
				messageService.send("일진그룹", userHp, (String) params.get("userName"), "[일진그룹 전자입찰시스템] 요청하신 일진그룹 전자입찰 시스템 회원가입이 승인되었습니다.");
			}catch(Exception e) {
				log.error("insert send message error : {}", e);
			}
           
        }
        return resultBody;
    }
    @Transactional
    public ResultBody update(Map<String, Object> params, MultipartFile regnumFile, MultipartFile bFile) {
        ResultBody resultBody = new ResultBody();
        try {
            if (regnumFile != null) {
                params.put("regnumPath", fileService.uploadFile(regnumFile));
                params.put("regnumFile", regnumFile.getOriginalFilename());
            }
            if (bFile != null) {
                params.put("bfilePath", fileService.uploadFile(bFile, "N"));
                params.put("bfile", bFile.getOriginalFilename());
            }
        } catch (IOException e) {
            resultBody.setCode("UPLOAD");
            resultBody.setMsg("파일 업로드시 오류가 발생했습니다.");
            return resultBody;
        }

        StringBuilder sbQuery = new StringBuilder(" UPDATE t_co_cust_master SET cust_name = :custName, regnum = :regnum, pres_name = :presName, pres_jumin_no = :presJuminNo, tel = :tel, fax = :fax, zipcode = :zipcode, addr = :addr, addr_detail = :addrDetail, capital = :capital" +
                " , found_year = :foundYear, update_user = :userId, update_date = now(), b_file = :bFile, b_file_path = :bFilePath, regnum_file = :regnumFile, regnum_path = :regnumPath WHERE cust_code = :custCode");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("custName",		params.get("custName"));
        query.setParameter("regnum",		CommonUtils.getString(params.get("regnum1"))+CommonUtils.getString(params.get("regnum2"))+CommonUtils.getString(params.get("regnum3")));
        query.setParameter("presName",		params.get("presName"));
        query.setParameter("presJuminNo",	CommonUtils.getString(params.get("presJuminNo1"))+CommonUtils.getString(params.get("presJuminNo2")));
        query.setParameter("tel",			CommonUtils.getString(params.get("tel")).replaceAll("-", ""));
        query.setParameter("fax",			CommonUtils.getString(params.get("fax")).replaceAll("-", ""));
        query.setParameter("zipcode",		params.get("zipcode"));
        query.setParameter("addr",			params.get("addr"));
        query.setParameter("addrDetail",	params.get("addrDetail"));
        query.setParameter("capital",		CommonUtils.getString(params.get("capital")).replaceAll(",", ""));
        query.setParameter("foundYear",		params.get("foundYear"));
        query.setParameter("bFile",			params.get("bfile"));
        query.setParameter("bFilePath",		params.get("bfilePath"));
        query.setParameter("regnumFile",	params.get("regnumFile"));
        query.setParameter("regnumPath",	params.get("regnumPath"));
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        query.setParameter("userId",		user.getUsername());
        query.setParameter("custCode",		params.get("custCode"));
        query.executeUpdate();

        // 협력사 이력 등록
//        insertHistory(params);

        // 계열사 사용자 수정만 해당
        if ("inter".equals(user.getCustType())) {
            // 계열사_협력사_매핑 수정
        	sbQuery = new StringBuilder("INSERT INTO t_co_cust_ir (cust_code, interrelated_cust_code, cust_level, cust_valuation, care_content, cert_date) VALUES (:custCode, :interrelatedCustCode, :custLevel, :custValuation, :careContent, now())\n"
        			+" ON DUPLICATE KEY UPDATE cust_level = :custLevel, cust_valuation = :custValuation, care_content = :careContent, cert_date = now()");
            query = entityManager.createNativeQuery(sbQuery.toString());
            query.setParameter("custLevel", params.get("custLevel"));
            query.setParameter("custValuation", params.get("custValuation"));
            query.setParameter("careContent", params.get("careContent"));
            query.setParameter("custCode", params.get("custCode"));
            query.setParameter("interrelatedCustCode", user.getCustCode());
            query.executeUpdate();

            sbQuery = new StringBuilder(" UPDATE t_co_cust_user SET user_name = :userName, user_tel = :userTel, user_hp = :userHp, user_email = :userEmail, user_buseo = :userBuseo, user_position = :userPosition, update_user = :updUserId, update_date = now() WHERE user_id = :userId");
            query = entityManager.createNativeQuery(sbQuery.toString());
            query.setParameter("userName",		params.get("userName"));
            query.setParameter("userTel",		CommonUtils.getString(params.get("userTel")).replaceAll("-", ""));
            query.setParameter("userHp",		CommonUtils.getString(params.get("userHp")).replaceAll("-", ""));
            query.setParameter("userEmail",		params.get("userEmail"));
            query.setParameter("userBuseo",		params.get("userBuseo"));
            query.setParameter("userPosition",	params.get("userPosition"));
            query.setParameter("updUserId",		user.getUsername());
            query.setParameter("userId",		params.get("userId"));
            query.executeUpdate();
        }
        return resultBody;
    }
    public ResultBody idcheck(Map<String, Object> params) throws Exception {
        ResultBody resultBody = new ResultBody();
        
        int cnt = CommonUtils.getInt(generalDao.selectGernalCount(DB.QRY_SELECT_DUP_USER_CNT, params));
        if (cnt > 0) {
            resultBody.setCode("DUP"); // 아이디중복됨
        }
        return resultBody;
    }
    public ResultBody pwdcheck(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!((BCryptPasswordEncoder) passwordEncoder).matches((String) params.get("userPwd"), user.getPassword())) {
            resultBody.setCode("notmatch");
        }
        return resultBody;
    }

	// 업체 이력 insert
	public void insertHistory(Map<String, Object> params) throws Exception {
		generalDao.insertGernal("cust.insertTCoCustHistory", params);
	}
}
