package iljin.framework.ebid.custom.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.custom.dto.TCoCustMasterDto;
import iljin.framework.ebid.custom.dto.TCoUserDto;
import iljin.framework.ebid.etc.util.PagaUtils;
import iljin.framework.ebid.etc.util.common.file.FileService;
import iljin.framework.ebid.etc.util.common.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

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

    public Page custList(Map<String, Object> params) {
        StringBuilder sbCount = new StringBuilder(" select count(1) from t_co_cust_master x where 1 = 1");
        StringBuilder sbList = new StringBuilder(" SELECT cust_code \n" +
                "     , cust_name \n" +
                "     , (SELECT item_name FROM t_co_item x WHERE x.item_code = a.cust_type1) AS cust_type1\n" +
                "     , CONCAT(SUBSTR(regnum, 1, 3), '-', SUBSTR(regnum, 4, 2), '-', SUBSTR(regnum, 6, 5)) AS regnum\n" +
                "     , pres_name \n" +
                "     , (SELECT user_name FROM t_co_cust_user x WHERE x.cust_code = a.cust_code AND x.user_type = '1' LIMIT 1) AS user_name\n" +
                "     , DATE_FORMAT(create_date, '%Y-%m-%d %H:%i') AS create_date \n" +
                "  FROM t_co_cust_master a\n" +
                " WHERE 1 = 1");
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("certYn"))) {
            sbWhere.append(" AND cert_yn = :certYn");
        }
        if (!StringUtils.isEmpty(params.get("custName"))) {
            sbWhere.append(" AND cust_name like concat('%',:custName,'%')");
        }
        if (!StringUtils.isEmpty(params.get("custTypeCode1"))) {
            sbWhere.append(" AND cust_type1 = :custTypeCode1");
        }
        sbList.append(sbWhere);
        sbList.append(" order by create_date desc");
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        sbCount.append(sbWhere);
        Query queryTotal = entityManager.createNativeQuery(sbCount.toString());

        if (!StringUtils.isEmpty(params.get("certYn"))) {
            queryList.setParameter("certYn", params.get("certYn"));
            queryTotal.setParameter("certYn", params.get("certYn"));
        }
        if (!StringUtils.isEmpty(params.get("custName"))) {
            queryList.setParameter("custName", params.get("custName"));
            queryTotal.setParameter("custName", params.get("custName"));
        }
        if (!StringUtils.isEmpty(params.get("custTypeCode1"))) {
            queryList.setParameter("custTypeCode1", params.get("custTypeCode1"));
            queryTotal.setParameter("custTypeCode1", params.get("custTypeCode1"));
        }

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, TCoCustMasterDto.class);

        BigInteger count = (BigInteger) queryTotal.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    public Page otherCustList(Map<String, Object> params) {
        StringBuilder sbCount = new StringBuilder(" SELECT count(1) FROM t_co_cust_master a WHERE interrelated_cust_code != :custCode");
        StringBuilder sbList = new StringBuilder(" SELECT cust_code \n" +
                "     , cust_name \n" +
                "     , CONCAT((SELECT CONCAT('1. ', item_name, '<br/>') FROM t_co_item x WHERE x.item_code = a.cust_type1)\n" +
                "     , (SELECT CONCAT('2. ', item_name) FROM t_co_item x WHERE x.item_code = a.cust_type2)) AS cust_type1\n" +
                "     , CONCAT(SUBSTR(regnum, 1, 3), '-', SUBSTR(regnum, 4, 2), '-', SUBSTR(regnum, 6, 5)) AS regnum\n" +
                "     , pres_name \n" +
                "     , (SELECT GROUP_CONCAT(interrelated_nm SEPARATOR '<br/>') FROM t_co_cust_ir x, t_co_interrelated y WHERE x.cust_code = a.cust_code AND x.interrelated_cust_code = y.interrelated_cust_code) AS interrelated_nm\n" +
                "  FROM t_co_cust_master a\n" +
                " WHERE interrelated_cust_code != :custCode");
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("custType"))) {
            sbWhere.append(" AND (cust_type1 = :custType OR cust_type2 = :custType)");
        }
        if (!StringUtils.isEmpty(params.get("custName"))) {
            sbWhere.append(" AND cust_name like concat('%',:custName,'%')");
        }
        sbList.append(sbWhere);
        sbList.append(" order by create_date desc");
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        sbCount.append(sbWhere);
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

    public TCoCustMasterDto custDetail(String id) {
        StringBuilder sb = new StringBuilder(" SELECT a.cust_code \n" +
                "     , cust_name \n" +
                "     , (SELECT interrelated_nm FROM t_co_interrelated x WHERE x.interrelated_cust_code = a.interrelated_cust_code) AS interrelated_nm\n" +
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
    @Transactional
    public ResultBody approval(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
        StringBuilder sbQuery = new StringBuilder(" UPDATE t_co_cust_master SET cert_yn = 'Y', update_user = :userId, update_date = now() WHERE cust_code = :custCode LIMIT 1");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("custCode", params.get("custCode"));
        CustomUserDetails principal = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        query.setParameter("userId", principal.getUsername());
        query.executeUpdate();

        // 협력사 이력 등록
        insertHistory(params.get("custCode"));

        // 회원가입 승인 메일 저장 처리
        mailService.saveMailInfo("[일진그룹 e-bidding] 회원가입 승인", "안녕하십니까\n" +
                "일진그룹 전자입찰 e-bidding 입니다.\n" +
                "\n" +
                "["+principal.getCustName()+"] 계열사에서 ["+params.get("custName")+"] 업체 승인처리 되었습니다.\n" +
                "<b>e-bidding 시스템</b>에 로그인하고 입찰업무를 처리해 주십시오\n" +
                "입찰 업무는 로그인 후 하단에 입찰업무 안내를 참고하시거나 공지메뉴의 매뉴얼을 참조해 주십시오\n" +
                "\n" +
                "감사합니다.\n", (String) params.get("userEmail"));
        return resultBody;
    }
    @Transactional
    public ResultBody del(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
        StringBuilder sbQuery = new StringBuilder(" UPDATE  t_co_cust_master SET cert_yn = 'D', etc = :etc, update_user = :userId, update_date = now() WHERE cust_code = :custCode LIMIT 1");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("etc", params.get("etc"));
        query.setParameter("custCode", params.get("custCode"));
        CustomUserDetails principal = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        query.setParameter("userId", principal.getUsername());
        query.executeUpdate();

        // 협력사 이력 등록
        insertHistory(params.get("custCode"));

        // 회원가입 반려 메일 저장 처리
        mailService.saveMailInfo("[일진그룹 e-bidding] 회원가입 반려", "안녕하십니까\n" +
                "일진그룹 전자입찰 e-bidding 입니다.\n" +
                "\n" +
                "["+principal.getCustName()+"] 계열사에서 ["+params.get("custName")+"] 업체 반려처리 되었습니다.\n" +
                "아래 반려 사유를 확인해 주십시오\n" +
                "\n" +
                "감사합니다.\n" +
                "\n" +
                "- 반려사유\n" +
                params.get("etc"), (String) params.get("userEmail"));
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
                params.put("bFilePath", fileService.uploadFile(bFile));
                params.put("bFile", bFile.getOriginalFilename());
            }
        } catch (IOException e) {
            resultBody.setCode("UPLOAD");
            resultBody.setMsg("파일 업로드시 오류가 발생했습니다.");
            return resultBody;
        }

        StringBuilder sbQuery = new StringBuilder(" INSERT INTO t_co_cust_master (cust_type1, cust_type2, cust_name, regnum, pres_name, pres_jumin_no, tel, fax, zipcode, addr, addr_detail, capital, found_year, cert_yn, etc, create_user, create_date, update_user, update_date, interrelated_cust_code, b_file, b_file_path, regnum_file, regnum_path)" +
                " VALUES (:custType1, :custType2, :custName, :regnum, :presName, :presJuminNo, :tel, :fax, :zipcode, :addr, :addrDetail, :capital, :foundYear, :certYn, :etc, :userId, now(), :userId, now(), :interrelatedCustCode, :bFile, :bFilePath, :regnumFile, :regnumPath)");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("custType1", params.get("custType1"));
        query.setParameter("custType2", params.get("custType2"));
        query.setParameter("custName", params.get("custName"));
        query.setParameter("regnum", params.get("regnum1").toString()+params.get("regnum2").toString()+params.get("regnum3").toString());
        query.setParameter("presName", params.get("presName"));
        query.setParameter("presJuminNo", params.get("presJuminNo1").toString()+params.get("presJuminNo2").toString());
        query.setParameter("tel", params.get("tel"));
        query.setParameter("fax", params.get("fax"));
        query.setParameter("zipcode", params.get("zipcode"));
        query.setParameter("addr", params.get("addr"));
        query.setParameter("addrDetail", params.get("addrDetail"));
        query.setParameter("capital", params.get("capital"));
        query.setParameter("foundYear", params.get("foundYear"));
        query.setParameter("certYn", "Y");
        query.setParameter("etc", "");
        query.setParameter("interrelatedCustCode", params.get("interrelatedCustCode"));
        query.setParameter("bFile", params.get("bFile"));
        query.setParameter("bFilePath", params.get("bFilePath"));
        query.setParameter("regnumFile", params.get("regnumFile"));
        query.setParameter("regnumPath", params.get("regnumPath"));
        CustomUserDetails principal = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        query.setParameter("userId", principal.getUsername());
        query.executeUpdate();

        query = entityManager.createNativeQuery("SELECT LAST_INSERT_ID()");
        BigInteger custCode = (BigInteger) query.getSingleResult();

        // 협력사 이력 등록
        insertHistory(custCode.intValue());

        // 관리자 등록 처리
        sbQuery = new StringBuilder(" INSERT INTO t_co_cust_user (user_id, cust_code, user_pwd, user_name, user_tel, user_hp, user_email, user_type, user_buseo, user_position, create_user, create_date, update_user, update_date, pwd_chg_date, use_yn)" +
                " VALUES (:userId, :custCode, :userPwd, :userName, :userTel, :userHp, :userEmail, :userType, :userBuseo, :userPosition, :updUserId, now(), :updUserId, now(), now(), :useYn)");
        query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("userId", params.get("userId"));
        query.setParameter("custCode", custCode.intValue());
        query.setParameter("userPwd", params.get("userPwd"));
        query.setParameter("userName", params.get("userName"));
        query.setParameter("userTel", params.get("userTel"));
        query.setParameter("userHp", params.get("userHp"));
        query.setParameter("userEmail", params.get("userEmail"));
        query.setParameter("userType", "1"); // 관리자
        query.setParameter("userBuseo", params.get("userBuseo"));
        query.setParameter("userPosition", params.get("userPosition"));
        query.setParameter("updUserId", principal.getUsername());
        query.setParameter("useYn", "Y");
        query.executeUpdate();

        // 회원가입 승인 메일 저장 처리
        mailService.saveMailInfo("[일진그룹 e-bidding] 회원가입 승인", "안녕하십니까\n" +
                "일진그룹 전자입찰 e-bidding 입니다.\n" +
                "\n" +
                "["+principal.getCustName()+"] 계열사에서 ["+params.get("custName")+"] 업체 승인처리 되었습니다.\n" +
                "<b>e-bidding 시스템</b>에 로그인하고 입찰업무를 처리해 주십시오\n" +
                "입찰 업무는 로그인 후 하단에 입찰업무 안내를 참고하시거나 공지메뉴의 매뉴얼을 참조해 주십시오\n" +
                "\n" +
                "감사합니다.\n", (String) params.get("userEmail"));

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
                params.put("bFilePath", fileService.uploadFile(bFile));
                params.put("bFile", bFile.getOriginalFilename());
            }
        } catch (IOException e) {
            resultBody.setCode("UPLOAD");
            resultBody.setMsg("파일 업로드시 오류가 발생했습니다.");
            return resultBody;
        }

        StringBuilder sbQuery = new StringBuilder(" UPDATE t_co_cust_master SET cust_name = :custName, regnum = :regnum, pres_name = :presName, pres_jumin_no = :presJuminNo, tel = :tel, fax = :fax, zipcode = :zipcode, addr = :addr, addr_detail = :addrDetail, capital = :capital" +
                " , found_year = :foundYear, update_user = :userId, update_date = now(), b_file = :bFile, b_file_path = :bFilePath, regnum_file = :regnumFile, regnum_path = :regnumPath WHERE cust_code = :custCode");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("custName", params.get("custName"));
        query.setParameter("regnum", params.get("regnum1").toString()+params.get("regnum2").toString()+params.get("regnum3").toString());
        query.setParameter("presName", params.get("presName"));
        query.setParameter("presJuminNo", params.get("presJuminNo1").toString()+params.get("presJuminNo2").toString());
        query.setParameter("tel", params.get("tel"));
        query.setParameter("fax", params.get("fax"));
        query.setParameter("zipcode", params.get("zipcode"));
        query.setParameter("addr", params.get("addr"));
        query.setParameter("addrDetail", params.get("addrDetail"));
        query.setParameter("capital", params.get("capital"));
        query.setParameter("foundYear", params.get("foundYear"));
        query.setParameter("bFile", params.get("bFile"));
        query.setParameter("bFilePath", params.get("bFilePath"));
        query.setParameter("regnumFile", params.get("regnumFile"));
        query.setParameter("regnumPath", params.get("regnumPath"));
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        query.setParameter("userId", principal.getUsername());
        query.setParameter("custCode", params.get("custCode"));
        query.executeUpdate();

        // 협력사 이력 등록
        insertHistory(params.get("custCode"));

        sbQuery = new StringBuilder(" UPDATE t_co_cust_user SET user_name = :userName, user_tel = :userTel, user_hp = :userHp, user_email = :userEmail, user_buseo = :userBuseo, user_position = :userPosition, update_user = :updUserId, update_date = now() WHERE user_id = :userId");
        query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("userName", params.get("userName"));
        query.setParameter("userTel", params.get("userTel"));
        query.setParameter("userHp", params.get("userHp"));
        query.setParameter("userEmail", params.get("userEmail"));
        query.setParameter("userBuseo", params.get("userBuseo"));
        query.setParameter("userPosition", params.get("userPosition"));
        query.setParameter("updUserId", principal.getUsername());
        query.setParameter("userId", params.get("userId"));
        query.executeUpdate();
        return resultBody;
    }

    private void insertHistory(Object custCode) {
        StringBuilder sbQuery = new StringBuilder(" INSERT INTO t_co_cust_master_hist\n" +
                "      (cust_code, cust_type1, cust_type2, cust_name, regnum, pres_name, pres_jumin_no, tel, fax, zipcode, addr, addr_detail, capital, found_year, cert_yn, etc, create_user, create_date, update_user, update_date, interrelated_cust_code, b_file, b_file_path, regnum_file, regnum_path)\n" +
                "SELECT cust_code, cust_type1, cust_type2, cust_name, regnum, pres_name, pres_jumin_no, tel, fax, zipcode, addr, addr_detail, capital, found_year, cert_yn, etc, create_user, create_date, update_user, update_date, interrelated_cust_code, b_file, b_file_path, regnum_file, regnum_path\n" +
                "  FROM t_co_cust_master \n" +
                " WHERE cust_code = :custCode");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("custCode", custCode);
        query.executeUpdate();
    }
}
