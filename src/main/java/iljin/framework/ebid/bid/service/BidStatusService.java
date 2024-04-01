package iljin.framework.ebid.bid.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.BidCustDto;
import iljin.framework.ebid.bid.dto.BidCompleteSpecDto;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.dto.BidProgressDto;
import iljin.framework.ebid.bid.dto.BidProgressFileDto;
import iljin.framework.ebid.bid.dto.BidProgressTableDto;
import iljin.framework.ebid.bid.dto.CoUserInfoDto;
import iljin.framework.ebid.bid.dto.ItemDto;
import iljin.framework.ebid.bid.dto.SubmitHistDto;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.PagaUtils;
import iljin.framework.ebid.etc.util.common.file.FileService;
import lombok.extern.slf4j.Slf4j;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class BidStatusService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TCoUserRepository tCoUserRepository;
    @Autowired
    Util util;

    @Autowired
    private FileService fileService;

    @Autowired
    private BidProgressService bidProgressService;

    @Value("${file.upload.directory}")
    private String uploadDirectory;

	/**
	 * 입찰진행 리스트
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResultBody statuslist(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody(); 
			
		try {
			UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
			String userId = userOptional.get().getUserId();
			String interrelatedCode = userOptional.get().getInterrelatedCustCode();
			String userAuth = userOptional.get().getUserAuth();
			
			StringBuilder sbCount = new StringBuilder(
				" select count(1) from t_bi_info_mat tbim "
			);
			StringBuilder sbList = new StringBuilder(
				  "select	tbim.bi_no"
				+ ",		tbim.bi_name "
				+ ",		DATE_FORMAT(tbim.est_start_date, '%Y-%m-%d %H:%i') AS est_start_date "
				+ ",		DATE_FORMAT(tbim.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date "
				+ ",		tbim.bi_mode "
				+ ",		tbim.ins_mode "
				+ ",		CASE	WHEN tbim.ing_tag = 'A1' AND tbim.est_close_date < sysdate() "
				+ "					THEN '입찰공고(개찰대상)' "
				+ "					WHEN tbim.ing_tag = 'A1' "
				+ "					THEN '입찰공고' "
				+ "					WHEN tbim.ing_tag = 'A3' "
				+ "					THEN '입찰공고(재)' "
				+ "					ELSE '개찰' "
				+ "			END AS ing_tag "
				+ ",		tcu1.user_name AS cuser "
				+ ",		tcu1.user_email AS cuser_email "
				+ ",		tcu2.user_name AS gongo_id "
				+ ",		tcu2.user_email AS gongo_email "
				+ ",		tbim.interrelated_cust_code "
				+ "FROM t_bi_info_mat tbim "
				+ "LEFT JOIN t_co_user tcu1 "
				+ "	ON tbim.create_user = tcu1.user_id "
				+ "LEFT JOIN t_co_user tcu2 "
				+ "	ON tbim.gongo_id = tcu2.user_id "
			);
			
			if (userAuth.equals("4")) {
				
				String addStr = "inner join t_co_user_interrelated tcui "
							+ "	on tbim.INTERRELATED_CUST_CODE = tcui.INTERRELATED_CUST_CODE "
							+ "	and tcui.USER_ID = :userId ";
				
				sbCount.append(addStr);
				sbList.append(addStr);
			}
			
			//조회조건
			StringBuilder sbWhere = new StringBuilder();
			
			sbWhere.append("where 1=1 ");
			
			//입찰번호
			if (!StringUtils.isEmpty(params.get("bidNo"))) {
				sbWhere.append(" and tbim.bi_no = :bidNo ");
			}
			
			//입찰명
			if (!StringUtils.isEmpty(params.get("bidName"))) {
				sbWhere.append(" and tbim.bi_name like concat('%',:bidName,'%') ");
			}
			
			//진행상태
			Boolean rebidYn = (Boolean) params.get("rebidYn");			//입찰공고(재입찰포함)
			Boolean dateOverYn = (Boolean) params.get("dateOverYn");	//입찰공고(개찰대상)
			Boolean openBidYn = (Boolean) params.get("openBidYn");		//입찰공고(개찰)
			
			if(rebidYn && !dateOverYn && !openBidYn) {
				sbWhere.append(" and tbim.ing_tag in ( 'A1', 'A3' )");
				sbWhere.append(" and tbim.est_close_date > sysdate() ");
			}else if(!rebidYn && dateOverYn && !openBidYn) {
				sbWhere.append(" and tbim.ing_tag in ( 'A1', 'A3' )");
				sbWhere.append(" and tbim.est_close_date < sysdate() ");
			}else if(!rebidYn && !dateOverYn && openBidYn) {
				sbWhere.append(" and tbim.ing_tag in ( 'A2' )");
			}else if(rebidYn && dateOverYn && !openBidYn) {
				sbWhere.append(" and tbim.ing_tag in ( 'A1', 'A3' )");
			}else if(rebidYn && !dateOverYn && openBidYn) {
				sbWhere.append(" and ((	tbim.ing_tag in ( 'A1', 'A3' ) and tbim.est_close_date > sysdate() ) or tbim.ing_tag in ( 'A2' ))");
			}else if(!rebidYn && dateOverYn && openBidYn) {
				sbWhere.append(" and ((	tbim.ing_tag in ( 'A1', 'A3' ) and tbim.est_close_date < sysdate() ) or tbim.ing_tag in ( 'A2' ))");
			}else {
				sbWhere.append(" and tbim.ing_tag in ( 'A1', 'A2', 'A3' )");
			}
	
			if (!userAuth.equals("4")) {
				sbWhere.append("and tbim.interrelated_cust_code = :interrelatedCustCode ");
			}
			
			sbWhere.append(
					"and ( tbim.create_user = :userId "
				+	"	or tbim.open_att1 = :userId " 
				+	"	or tbim.open_att2 = :userId " 
				+	"	or tbim.gongo_id = :userId " 
				+	"	or tbim.est_bidder = :userId " 
				+	"	or tbim.est_opener = :userId ) "
			);
			
			sbList.append(sbWhere);
			sbCount.append(sbWhere);
	
			Query queryList = entityManager.createNativeQuery(sbList.toString());
			Query queryTotal = entityManager.createNativeQuery(sbCount.toString());
	
			if (!StringUtils.isEmpty(params.get("bidNo"))) {
				queryList.setParameter("bidNo", params.get("bidNo"));
				queryTotal.setParameter("bidNo", params.get("bidNo"));
			}
			if (!StringUtils.isEmpty(params.get("bidName"))) {
				queryList.setParameter("bidName", params.get("bidName"));
				queryTotal.setParameter("bidName", params.get("bidName"));
			}
			if (userAuth.equals("1") || userAuth.equals("2") || userAuth.equals("3")) {
				queryList.setParameter("interrelatedCustCode", interrelatedCode);
				queryTotal.setParameter("interrelatedCustCode", interrelatedCode);
			}
			
			queryList.setParameter("userId", userId);
			queryTotal.setParameter("userId", userId);
			
			Pageable pageable = PagaUtils.pageable(params);
			queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
			List list = new JpaResultMapper().list(queryList, BidProgressDto.class);
	
			BigInteger count = (BigInteger) queryTotal.getSingleResult();
			Page listPage = new PageImpl(list, pageable, count.intValue());
			resultBody.setData(listPage);
			
		}catch(Exception e) {
			log.error("statuslist list error : {}", e);
			resultBody.setCode("999");
			resultBody.setMsg("입찰 진행 리스트를 가져오는것을 실패하였습니다.");	
		}
		
		return resultBody;
	}
	
	/**
	 * 입찰진행 상세
	 * @param param
	 * @return
	 */
	public ResultBody statusDetail(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		try {
			UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
			String userId = userOptional.get().getUserId();
			
			BidProgressDetailDto detailDto = null;
			
			
			// ************ 데이터 검색 -- 입찰참가업체, 세부내역, 첨부파일 제외 ************
			StringBuilder sbMainData = new StringBuilder(
				  "select	tbim.BI_NO "
				+ ",		tbim.BI_NAME "
				+ ",		tci.ITEM_NAME "
				+ ",		tbim.BI_MODE "
				+ ",		tbim.BID_JOIN_SPEC "
				+ ",		tbim.SPECIAL_COND "
				+ ",		DATE_FORMAT(tbim.SPOT_DATE, '%Y-%m-%d %H:%i') as SPOT_DATE "
				+ ",		tbim.SPOT_AREA "
				+ ",		tcc.CODE_NAME as SUCC_DECI_METH "
				+ ",		tbim.AMT_BASIS "
				+ ",		tbim.PAY_COND "
				+ ",		tbim.BD_AMT "
				+ ",		tbim.CREATE_USER "
				+ ",		tcu.USER_NAME as DAMDANG_NAME "
				+ ",		DATE_FORMAT(tbim.EST_START_DATE, '%Y-%m-%d %H:%i') as EST_START_DATE "
				+ ",		DATE_FORMAT(tbim.EST_CLOSE_DATE, '%Y-%m-%d %H:%i') as EST_CLOSE_DATE "
				+ ",		tbim.EST_CLOSE_DATE < sysdate() as EST_CLOSE_CHECK "
				+ ",		tcu3.USER_NAME as EST_OPENER "
				+ ",		tcu4.USER_NAME as EST_BIDDER "
				+ ",		tcu.USER_NAME as GONGO_NAME "
				+ ",		tcu1.USER_NAME as OPEN_ATT1 "
				+ ",		tcu2.USER_NAME as OPEN_ATT2 "
				+ ",		tbim.INS_MODE "
				+ ",		tbim.SUPPLY_COND "
				+ ",		tbim.WHY_A3 "
				+ ",		tbim.ING_TAG "
				+ ",		tbim.INTERRELATED_CUST_CODE "
				+ ",		codeMd.CODE_NAME as MAT_DEPT "
				+ ",		codeMp.CODE_NAME as MAT_PROC "
				+ ",		codeMc.CODE_NAME as MAT_CLS "
				+ ",		tbim.MAT_FACTORY "
				+ ",		tbim.MAT_FACTORY_LINE "
				+ ",		tbim.MAT_FACTORY_CNT "
				+ "from t_bi_info_mat tbim "
				+ "left outer join t_co_user tcu "
				+ "	on tbim.GONGO_ID = tcu.USER_ID "
				+ "left outer join t_co_user tcu1 "
				+ "	on tbim.OPEN_ATT1 = tcu1.USER_ID "
				+ "left outer join t_co_user tcu2 "
				+ "	on tbim.OPEN_ATT2 = tcu2.USER_ID "
				+ "left outer join t_co_user tcu3 "
				+ "	on tbim.EST_OPENER = tcu3.USER_ID "
				+ "left outer join t_co_user tcu4 "
				+ "	on tbim.EST_BIDDER = tcu4.USER_ID "
				+ "left outer join t_co_item tci  "
				+ "	on tbim.ITEM_CODE = tci.ITEM_CODE "
				+ "left outer join t_co_code tcc  "
				+ "	on tbim.SUCC_DECI_METH = tcc.CODE_VAL "
				+ " and tcc.COL_CODE = 'T_CO_SUCC_METHOD' "
				+ "left outer join t_co_code codeMd "
				+ "	on tbim.MAT_DEPT = codeMd.CODE_VAL "
				+ "	and codeMd.COL_CODE = 'MAT_DEPT' "
				+ "left outer join t_co_code codeMp "
				+ "	on tbim.MAT_PROC = codeMp.CODE_VAL "
				+ "	and codeMp.COL_CODE = 'MAT_PROC' "
				+ "left outer join t_co_code codeMc "
				+ "	on tbim.MAT_CLS = codeMc.CODE_VAL "
				+ "	and codeMc.COL_CODE = 'MAT_CLS' "
			);
			
			//조건문 쿼리 삽입
			StringBuilder sbMainWhere = new StringBuilder();
			sbMainWhere.append("where tbim.BI_NO = :biNo");
			sbMainData.append(sbMainWhere);
			
			//쿼리 실행
			Query queryMain = entityManager.createNativeQuery(sbMainData.toString());
			
			//조건 대입
			queryMain.setParameter("biNo", params.get("biNo"));
			
			detailDto = new JpaResultMapper().uniqueResult(queryMain, BidProgressDetailDto.class);
			
			// ************ 로그인 당사자 개찰권한, 낙찰권한 확인 ************
			StringBuilder sbAuthData = new StringBuilder(
				  "select	tcu.OPENAUTH "
				+ ",		tcu.BIDAUTH "
				+ "from t_co_user tcu "
				+ "where tcu.USER_ID = :userId "
			);
			
			//쿼리 실행
			Query queryAuth = entityManager.createNativeQuery(sbAuthData.toString());
			
			//조건 대입
			queryAuth.setParameter("userId", userId);
			
			CoUserInfoDto userInfoDto = new JpaResultMapper().uniqueResult(queryAuth, CoUserInfoDto.class);
			
			detailDto.setBidAuth(Boolean.parseBoolean(userInfoDto.getBidauth()));
			detailDto.setOpenAuth(Boolean.parseBoolean(userInfoDto.getOpenauth()));
			
			// ************ 데이터 검색 -- 입찰참가업체 ************
			StringBuilder sbCustData = new StringBuilder(
				  "select	tbimc.BI_NO "
				+ ",		cast(tbimc.CUST_CODE as char) as CUST_CODE "
				+ ",		tccm.CUST_NAME "
				+ ",		tccm.PRES_NAME "
				+ ",		tcc.CODE_NAME as ESMT_CURR "
				+ ",		DATE_FORMAT(tbimc.SUBMIT_DATE, '%Y-%m-%d %H:%i') as SUBMIT_DATE "
				+ ",		(select tccu.USER_NAME from t_co_cust_user tccu where tccu.CUST_CODE = tbimc.CUST_CODE AND tccu.USER_TYPE = '1' LIMIT 1) AS DAMDANG_NAME "
				+ ",		tbimc.ESMT_YN "
				+ ",		tbimc.ETC_B_FILE_PATH as ETC_PATH "
				+ "from t_bi_info_mat_cust tbimc "
				+ "inner join t_co_cust_master tccm "
				+ "	on tbimc.CUST_CODE = tccm.CUST_CODE "
				+ "left outer join t_co_code tcc "
				+ "	on tcc.COL_CODE = 'T_CO_RATE' "
				+ "	and tbimc.ESMT_CURR = tcc.CODE_VAL "
			);
			
			//조건문 쿼리 삽입
			StringBuilder sbCustWhere = new StringBuilder();
			sbCustWhere.append("where tbimc.BI_NO = :biNo ");
			sbCustData.append(sbCustWhere);
			
			//정렬
			sbCustData.append("order by field(tbimc.SUCC_YN, 'Y', 'N') ");
			
			//쿼리 실행
			Query queryCust = entityManager.createNativeQuery(sbCustData.toString());
			
			//조건 대입
			queryCust.setParameter("biNo", params.get("biNo"));
			
			List<BidCustDto> custData = new JpaResultMapper().list(queryCust, BidCustDto.class);
			
			//내역방식이 직접등록일 경우
			if(detailDto.getInsMode().equals("2")) {
				for(BidCustDto custDto : custData) {
					StringBuilder sbCustSpec = new StringBuilder(
						  "select	cast(tbdmc.CUST_CODE as char) as CUST_CODE "
						+ ",		tbsm.NAME "
						+ ",		tbsm.SSIZE "
						+ ",		tbsm.UNITCODE "
						+ ",		tbsm.ORDER_QTY "
						+ ",		tbdmc.ESMT_UC "
						+ "from t_bi_detail_mat_cust tbdmc "
						+ "inner join t_bi_spec_mat tbsm "
						+ "	on tbdmc.BI_NO = tbsm.BI_NO "
						+ "	and tbdmc.SEQ = tbsm.SEQ "
					);
					
					//조건문 쿼리 삽입
					StringBuilder sbCustSpecWhere = new StringBuilder();
					sbCustSpecWhere.append("where tbdmc.BI_NO = :biNo ");
					sbCustSpecWhere.append("and tbdmc.CUST_CODE = :custCode ");
					
					sbCustSpec.append(sbCustSpecWhere);
					
					//쿼리 실행
					Query queryCustSpec = entityManager.createNativeQuery(sbCustSpec.toString());
					
					//조건 대입
					queryCustSpec.setParameter("biNo", params.get("biNo"));
					queryCustSpec.setParameter("custCode", custDto.getCustCode());
					
					List<BidCompleteSpecDto> specDto = new JpaResultMapper().list(queryCustSpec, BidCompleteSpecDto.class);
					
					custDto.setBidSpec(specDto);
				}
			}
			
			detailDto.setCustList(custData);
			
			// ************ 데이터 검색 -- 세부내역 ************
			if(detailDto.getInsMode().equals("1")) {		//내역방식이 파일등록일 경우
				StringBuilder sbSpecFile = new StringBuilder(
						  "select	tbu.FILE_FLAG "
						+ ",		tbu.FILE_NM "
						+ ",		tbu.FILE_PATH "
						+ "from t_bi_upload tbu "
						+ "where tbu.USE_YN = 'Y' "
						+ "and tbu.FILE_FLAG in ('K') "
				);
			
				//조건문 쿼리 삽입
				StringBuilder sbSpecFileWhere = new StringBuilder();
				sbSpecFileWhere.append("and tbu.BI_NO = :biNo ");
				sbSpecFile.append(sbSpecFileWhere);
				
				//쿼리 실행
				Query querySpecFile = entityManager.createNativeQuery(sbSpecFile.toString());
				
				//조건 대입
				querySpecFile.setParameter("biNo", params.get("biNo"));
				
				List<BidProgressFileDto> specfile = new JpaResultMapper().list(querySpecFile, BidProgressFileDto.class);
				
				detailDto.setSpecFile(specfile);
				
			}else if(detailDto.getInsMode().equals("2")) {		//내역방식이 직접입력일 경우
				StringBuilder sbSpecInput = new StringBuilder(
						 "select	tbsm.NAME "
						+ ",		tbsm.SSIZE "
						+ ",		tbsm.UNITCODE "
						+ ",		tbsm.ORDER_UC "
						+ ",		tbsm.ORDER_QTY "
						+ "from t_bi_spec_mat tbsm "
				);
			
				//조건문 쿼리 삽입
				StringBuilder sbSpecInputWhere = new StringBuilder();
				sbSpecInputWhere.append("where tbsm.BI_NO = :biNo ");
				sbSpecInput.append(sbSpecInputWhere);
				
				//정렬
				sbSpecInput.append("order by tbsm.SEQ ");
				
				//쿼리 실행
				Query querySpecInput = entityManager.createNativeQuery(sbSpecInput.toString());
				
				//조건 대입
				querySpecInput.setParameter("biNo", params.get("biNo"));
				
				List<BidCompleteSpecDto> specInput = new JpaResultMapper().list(querySpecInput, BidCompleteSpecDto.class);
				
				detailDto.setSpecInput(specInput);
			}
			
			// ************ 데이터 검색 -- 첨부파일 ************
			StringBuilder sbFileData = new StringBuilder(
				  "select	tbu.FILE_FLAG "
				+ ",		tbu.FILE_NM "
				+ ",		tbu.FILE_PATH "
				+ "from t_bi_upload tbu "
				+ "where tbu.USE_YN = 'Y' "
				+ "and tbu.FILE_FLAG in ('0','1') "
			);
		
			//조건문 쿼리 삽입
			StringBuilder sbFileWhere = new StringBuilder();
			sbFileWhere.append("and tbu.BI_NO = :biNo ");
			sbFileData.append(sbFileWhere);
			
			//정렬
			sbFileData.append("order by field(tbu.FILE_FLAG, '1', '0') ");
			
			//쿼리 실행
			Query queryFile = entityManager.createNativeQuery(sbFileData.toString());
			
			//조건 대입
			queryFile.setParameter("biNo", params.get("biNo"));
			
			List<BidProgressFileDto> fileData = new JpaResultMapper().list(queryFile, BidProgressFileDto.class);
			
			detailDto.setFileList(fileData);
			
			resultBody.setData(detailDto);
			
		}catch(Exception e) {
			log.error("statusDetail error : {}", e);
			resultBody.setStatus(999);
			resultBody.setMsg("입찰진행 상세 데이터를 가져오는것을 실패하였습니다.");
		}
		
		return resultBody;
	}
	
	/**
	 * 유찰처리
	 * @param params
	 * @return
	 */
	@Transactional
	public ResultBody bidFailure(Map<String, String> params) {
		
		ResultBody resultBody = new ResultBody();
		
		try {
			UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
			String userId = userOptional.get().getUserId();
			
			String biNo = params.get("biNo");
	
			StringBuilder sbList = new StringBuilder(
					"UPDATE	t_bi_info_mat " 
				+	"set	ing_tag = 'A7' "
				+	",		why_a7 = :reason "
				+	"WHERE bi_no = :biNo "
			);
	
			Query queryList = entityManager.createNativeQuery(sbList.toString());
			queryList.setParameter("biNo", biNo);
			queryList.setParameter("reason", (String) params.get("reason"));
			int rowsUpdated = queryList.executeUpdate();
	
			if (rowsUpdated > 0) {
				Map<String, String> logParams = new HashMap<>();
				logParams.put("msg", "[본사] 유찰");
				logParams.put("biNo", biNo);
				logParams.put("userId", userId);
				try {
					bidProgressService.updateLog(logParams);
				}catch(Exception e) {
					log.error("bidFailure updateLog error : {}", e);
				}
			}
	
			bidProgressService.updateEmail(params);
		}catch(Exception e) {
			log.error("bidFailure error : {}", e);
			resultBody.setStatus(999);
			resultBody.setMsg("유찰 처리중 오류가 발생했습니다.");
		}
		
		return resultBody;
	}
	

	/**
	 * 개찰
	 * @param params : (String) biNo
	 * @return
	 */
	@Transactional
	public ResultBody bidOpening(Map<String, String> params) {
		ResultBody resultBody = new ResultBody();
		
		try {
			//입찰 메인 테이블 업데이트
			UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
			String userId = userOptional.get().getUserId();
			
			//복호화 대상 협력사
			StringBuilder sbCustList = new StringBuilder(
					"SELECT	tbimc.BI_NO "
				+	",		tbimc.CUST_CODE "
				+	",		tbimc.FILE_ID "
				+	",		tbimc.ENC_QUTN "
				+	",		tbimc.ENC_ESMT_SPEC "
				+	",		tbim.INS_MODE "
				+	"FROM	t_bi_info_mat_cust tbimc " 
				+	"INNER JOIN	t_bi_info_mat tbim "
				+	"	ON	tbimc.BI_NO = tbim.BI_NO "
				+	"WHERE	tbimc.bi_no = :biNo "
				+	"AND	tbimc.ESMT_YN = '2' "
			);
			
			Query queryCustList = entityManager.createNativeQuery(sbCustList.toString());
			queryCustList.setParameter("biNo", params.get("biNo"));
			queryCustList.executeUpdate();
			
			List<BidCustDto> custList = new JpaResultMapper().list(queryCustList, BidCustDto.class);
			
			for(BidCustDto custDto : custList) {
				
				//복호화한 값 입력
				String fileId = null;
				String esmtAmt = null;
				ArrayList<Object> esmtSpec = new ArrayList<Object>();
				
				//견적금액 복호화
				
				
				//내역방식이 파일 등록일 경우
				if(custDto.getInsMode().equals("1")) {
					//파일 복호화
					
				//내역방식이 직접입력일 경우
				}else if(custDto.getInsMode().equals("2")) {
					//직접입력 정보 복호화
					
				}
				
				//복호화 후 업데이트
				StringBuilder sbCust = new StringBuilder(
						"UPDATE	t_bi_info_mat_cust " 
				+		"set	ESMT_AMT = :esmtAmt "
				);
				
				//파일등록일 경우
				if(custDto.getInsMode().equals("1")) {
					sbCust.append(
						",		FILE_ID = :fileId "
					);
					
				}
				
				sbCust.append(
						",		SUBMIT_DATE = sysdate() "
					+	",		UPDATE_DATE = sysdate() "
					+	",		UPDATE_USER = :userId "
					+	"WHERE bi_no = :biNo "
				);
				
				Query queryCust = entityManager.createNativeQuery(sbCust.toString());
				queryCust.setParameter("fileId", fileId);
				queryCust.setParameter("esmtAmt", esmtAmt);
				queryCust.setParameter("userId", userId);
				queryCust.setParameter("biNo", custDto.getBiNo());
				queryCust.executeUpdate();
				
				//직접입력일 경우 협력사 직접입력 테이블 insert
				if(custDto.getInsMode().equals("2")) {
					
					for(Object obj : esmtSpec) {
						
						String seq = null;
						String esmtUc = null;
						
						StringBuilder sbCustDetail = new StringBuilder(
								"INSERT INTO t_bi_detail_mat_cust (bi_no,cust_code,seq,esmt_uc) VALUES (:biNo, :custCode, :seq, :esmtUc)"
						);
						Query queryCustDetail = entityManager.createNativeQuery(sbCustDetail.toString());
						queryCustDetail.setParameter("biNo", custDto.getBiNo());
						queryCustDetail.setParameter("custCode", custDto.getCustCode());
						queryCustDetail.setParameter("seq", seq);
						queryCustDetail.setParameter("esmtUc", esmtUc);
						queryCustDetail.executeUpdate();
					}
				}
			}
			
			//입찰 메인 업데이트
			StringBuilder sbMain = new StringBuilder(
					"UPDATE	t_bi_info_mat " 
				+	"set	ING_TAG = 'A2' "
				+	",		EST_OPEN_DATE = sysdate() "
				+	",		UPDATE_DATE = sysdate() "
				+	",		UPDATE_USER = :userId "
				+	"WHERE bi_no = :biNo "
			);
	
			Query queryMain = entityManager.createNativeQuery(sbMain.toString());
			queryMain.setParameter("userId", userId);
			queryMain.setParameter("biNo", params.get("biNo"));
			queryMain.executeUpdate();
			
			//로그
			Map<String, String> logParams = new HashMap<>();
			logParams.put("msg", "[본사] 개찰");
			logParams.put("biNo", params.get("biNo"));
			logParams.put("userId", userId);
			try {
				bidProgressService.updateLog(logParams);
			}catch(Exception e) {
				log.error("bidOpening updateLog error : {}", e);
			}
		}catch(Exception e) {
			log.error("bidOpening error : {}", e);
			resultBody.setCode("999");
			resultBody.setMsg("개찰 처리중 오류가 발생했습니다.");	
		}
		
		return resultBody;
	}


    public Page submitHist(@RequestBody Map<String, Object> params) {
        String biNo = (String) params.get("biNo");
        String custCode = (String) params.get("custCode");

        StringBuilder sbCount = new StringBuilder("");
        StringBuilder sbList = new StringBuilder("");

        StringBuilder ins = new StringBuilder(
                "SELECT ins_mode from t_bi_info_mat where bi_no = :biNo");
        Query insList = entityManager.createNativeQuery(ins.toString());
        insList.setParameter("biNo", biNo);
        String insMode = (String) insList.getSingleResult();

        if (insMode.equals("1")) {
            sbCount.append(
                    "SELECT count(1) from t_bi_info_mat_cust_temp where bi_no = :biNo and cust_code = :custCode");
            sbList.append(
                    "SELECT '1' AS insMode, bi_order, esmt_curr, esmt_amt, DATE_FORMAT(submit_date, '%Y-%m-%d %H:%i') AS submit_date from t_bi_info_mat_cust_temp "
                            +
                            "where bi_no = :biNo and cust_code = :custCode");
        }

        else if (insMode.equals("2")) {
            sbCount.append(
                    "SELECT count(1) from t_bi_detail_mat_cust_temp a, t_bi_info_mat_cust b " +
                            "where a.bi_no = :biNo and a.cust_code = :custCode and (a.bi_no =b.bi_no and a.cust_code = b.cust_code)");
            sbList.append(
                    "SELECT '2' AS insMode, a.bi_order AS bi_order, 'KRW' AS esmt_curr, a.esmt_uc AS esmt_amt, DATE_FORMAT(b.submit_date, '%Y-%m-%d %H:%i') AS submit_date "
                            +
                            "from t_bi_detail_mat_cust_temp a, t_bi_info_mat_cust b " +
                            "where a.bi_no = :biNo and a.cust_code = :custCode and (a.bi_no =b.bi_no and a.cust_code = b.cust_code)");
        }
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        Query queryCountList = entityManager.createNativeQuery(sbCount.toString());
        queryList.setParameter("biNo", biNo);
        queryCountList.setParameter("biNo", biNo);
        queryList.setParameter("custCode", custCode);
        queryCountList.setParameter("custCode", custCode);

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, SubmitHistDto.class);

        BigInteger count = (BigInteger) queryCountList.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    @Transactional
    public ResultBody rebid(@RequestBody Map<String, Object> params) { // rebid 페이지에서 disabled 조건인 칼럼 모두 제외

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();

        StringBuilder sbList = new StringBuilder( // 입찰 업데이트
                "UPDATE t_bi_info_mat SET bi_name = :biName,  " +
                        "bid_join_spec = :bidJoinSpec, special_cond = :specialCond, supply_cond = :supplyCond, " +
                        "spot_date = STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), spot_area = :spotArea, " +
                        "succ_deci_meth = :succDeciMethCode, amt_basis = :amtBasis, bd_amt = :bdAmt, " +
                        "update_user = :userId, update_date = sysdate(), pay_cond = :payCond, ing_tag = 'A3', why_a3 = :whyA3 "
                        +
                        "WHERE bi_no = :biNo");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biName", (String) params.get("biName"));
        queryList.setParameter("bidJoinSpec", (String) params.get("bidJoinSpec"));
        queryList.setParameter("specialCond", (String) params.get("specialCond"));
        queryList.setParameter("supplyCond", (String) params.get("supplyCond"));
        queryList.setParameter("spotDate", (String) params.get("spotDate"));
        queryList.setParameter("spotArea", (String) params.get("spotArea"));
        queryList.setParameter("succDeciMethCode", (String) params.get("succDeciMethCode"));
        queryList.setParameter("amtBasis", (String) params.get("amtBasis"));
        queryList.setParameter("bdAmt", params.get("bdAmt"));
        queryList.setParameter("userId", userId);
        queryList.setParameter("payCond", (String) params.get("payCond"));
        queryList.setParameter("whyA3", (String) params.get("whyA3"));
        queryList.setParameter("biNo", (String) params.get("biNo"));

        queryList.executeUpdate();

        StringBuilder sbList1 = new StringBuilder( // 입찰 hist 업데이트
                "INSERT into t_bi_info_mat_hist (bi_no, bi_name, bi_mode, ins_mode, bid_join_spec, special_cond, supply_cond, spot_date, "
                        +
                        "spot_area, succ_deci_meth, amt_basis, bd_amt, est_start_date, est_close_date, est_opener, est_bidder, "
                        +
                        "open_att1, open_att2, ing_tag, update_user, update_date, item_code, " +
                        "gongo_id, pay_cond, bi_open, mat_dept, mat_proc, mat_cls, mat_factory, mat_factory_line, mat_factory_cnt, why_a3) "
                        +
                        "values (:biNo, :biName, :biModeCode, :insModeCode, :bidJoinSpec, :specialCond, :supplyCond, " +
                        "STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), :spotArea, :succDeciMethCode, :amtBasis, :bdAmt, "
                        +
                        "est_start_date =STR_TO_DATE(:estStartDate, '%Y-%m-%d %H:%i'), est_close_date =STR_TO_DATE(:estCloseDate, '%Y-%m-%d %H:%i'), :estOpenerCode, :estBidderCode, "
                        +
                        ":openAtt1Code, :openAtt2Code, 'A3', :userId, sysdate(), :itemCode, :gongoIdCode, :payCond, 'N', :matDept, :matProc, :matCls, :matFactory, "
                        +
                        ":matFactoryLine, :matFactoryCnt, :whyA3)");

        Query queryList1 = entityManager.createNativeQuery(sbList1.toString());
        queryList1.setParameter("biNo", (String) params.get("biNo"));
        queryList1.setParameter("biName", (String) params.get("biName"));
        queryList1.setParameter("biModeCode", (String) params.get("biModeCode"));
        queryList1.setParameter("insModeCode", (String) params.get("insModeCode"));
        queryList1.setParameter("bidJoinSpec", (String) params.get("bidJoinSpec"));
        queryList1.setParameter("specialCond", (String) params.get("specialCond"));
        queryList1.setParameter("supplyCond", (String) params.get("supplyCond"));
        queryList1.setParameter("spotDate", (String) params.get("spotDate"));
        queryList1.setParameter("spotArea", (String) params.get("spotArea"));
        queryList1.setParameter("succDeciMethCode", (String) params.get("succDeciMethCode"));
        queryList1.setParameter("amtBasis", (String) params.get("amtBasis"));
        queryList1.setParameter("bdAmt", params.get("bdAmt"));
        queryList1.setParameter("estStartDate", (String) params.get("estStartDate"));
        queryList1.setParameter("estCloseDate", (String) params.get("estCloseDate"));
        queryList1.setParameter("estOpenerCode", (String) params.get("estOpenerCode"));
        queryList1.setParameter("estBidderCode", (String) params.get("estBidderCode"));
        queryList1.setParameter("openAtt1Code", (String) params.get("openAtt1Code"));
        queryList1.setParameter("openAtt2Code", (String) params.get("openAtt2Code"));
        queryList1.setParameter("userId", userId);
        queryList1.setParameter("itemCode", (String) params.get("itemCode"));
        queryList1.setParameter("gongoIdCode", (String) params.get("gongoIdCode"));
        queryList1.setParameter("payCond", (String) params.get("payCond"));
        queryList1.setParameter("matDept", (String) params.get("matDept"));
        queryList1.setParameter("matProc", (String) params.get("matProc"));
        queryList1.setParameter("matCls", (String) params.get("matCls"));
        queryList1.setParameter("matFactory", (String) params.get("matFactory"));
        queryList1.setParameter("matFactoryLine", (String) params.get("matFactoryLine"));
        queryList1.setParameter("matFactoryCnt", (String) params.get("matFactoryCnt"));
        queryList1.setParameter("whyA3", (String) params.get("whyA3"));

        int rowsUpdated = queryList1.executeUpdate();
        if (rowsUpdated > 0) {
            Map<String, String> logParams = new HashMap<>();
            logParams.put("msg", "[본사] 재입찰");
            logParams.put("biNo", (String) params.get("biNo"));
            bidProgressService.updateLog(logParams);

            Map<String, String> mailParams = new HashMap<>();
            mailParams.put("biNo", (String) params.get("biNo"));
            mailParams.put("type", (String) params.get("type"));
            mailParams.put("biName", (String) params.get("biName"));
            mailParams.put("reason", (String) params.get("whyA7"));
            mailParams.put("interNm", (String) params.get("interNm"));
            bidProgressService.updateEmail(mailParams);
        }

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    @Transactional
    public ResultBody rebidCust(@RequestBody List<Map<String, Object>> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String userId = principal.getUsername();
        if (params.size() > 0) {
            String biNo = (String) params.get(0).get("biNo");

            for (Map<String, Object> data : params) {
                StringBuilder sbList = new StringBuilder(
                        "UPDATE t_bi_info_mat_cust SET rebid_att = 'Y', esmt_yn = '0', esmt_amt = 0, update_user = :userId, update_date = sysdate() "
                                +
                                "where bi_no = :biNo and cust_code = :custCode");
                Query queryList = entityManager.createNativeQuery(sbList.toString());
                queryList.setParameter("userId", userId);
                queryList.setParameter("biNo", biNo);
                queryList.setParameter("custCode", (String) data.get("custCode"));
                queryList.executeUpdate();
            }
        }

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    public List<ItemDto> itemlist(@RequestBody Map<String, Object> params) {
        StringBuilder itemlist = new StringBuilder(
                "SELECT a.bi_no, a.seq, a.order_qty, a.name, a.ssize, a.unitcode, b.esmt_uc, b.cust_code " +
                        "from t_bi_spec_mat a, t_bi_detail_mat_cust b " +
                        "where a.bi_no = :biNo and b.cust_code = :custCode " +
                        "and (a.bi_no = b.bi_no and a.seq = b.seq)");
        Query itemlistQ = entityManager.createNativeQuery(itemlist.toString());
        itemlistQ.setParameter("biNo", (String) params.get("biNo"));
        itemlistQ.setParameter("custCode", (String) params.get("custCode"));
        return new JpaResultMapper().list(itemlistQ, ItemDto.class);
    }

    public ResultBody bidSucc(@RequestBody Map<String, Object> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String userId = principal.getUsername();

        StringBuilder sbList = new StringBuilder( // 입찰 업데이트
                "UPDATE t_bi_info_mat SET ing_tag = 'A5', update_user = :userId, update_date = sysdate(), add_aceept = :reason, "
                        +
                        "succ_amt = :esmtAmt where bi_no = :biNo ");
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("userId", userId);
        queryList.setParameter("reason", (String) params.get("reason"));
        queryList.setParameter("esmtAmt", params.get("esmtAmt"));
        queryList.setParameter("biNo", (String) params.get("biNo"));

        queryList.executeUpdate();

        StringBuilder sbList1 = new StringBuilder( // 입찰 hist 업데이트
                "INSERT into t_bi_info_mat_hist (bi_no, bi_name, bi_mode, ins_mode, bid_join_spec, special_cond, supply_cond, spot_date, "
                        +
                        "spot_area, succ_deci_meth, amt_basis, bd_amt, est_start_date, est_close_date, est_opener, est_bidder, "
                        +
                        "open_att1, open_att2, ing_tag, update_user, update_date, item_code, " +
                        "gongo_id, pay_cond, bi_open, mat_dept, mat_proc, mat_cls, mat_factory, mat_factory_line, mat_factory_cnt, add_accept, succ_amt) "
                        +
                        "values (:biNo, :biName, :biModeCode, :insModeCode, :bidJoinSpec, :specialCond, :supplyCond, " +
                        "STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), :spotArea, :succDeciMethCode, :amtBasis, :bdAmt, "
                        +
                        "est_start_date =STR_TO_DATE(:estStartDate, '%Y-%m-%d %H:%i'), est_close_date =STR_TO_DATE(:estCloseDate, '%Y-%m-%d %H:%i'), :estOpenerCode, :estBidderCode, "
                        +
                        ":openAtt1Code, :openAtt2Code, 'A5', :userId, sysdate(), :itemCode, :gongoIdCode, :payCond, 'Y', :matDept, :matProc, :matCls, :matFactory, "
                        +
                        ":matFactoryLine, :matFactoryCnt, :reason, :esmtAmt)");

        Query queryList1 = entityManager.createNativeQuery(sbList1.toString());
        queryList1.setParameter("biNo", (String) params.get("biNo"));
        queryList1.setParameter("biName", (String) params.get("biName"));
        queryList1.setParameter("biModeCode", (String) params.get("biModeCode"));
        queryList1.setParameter("insModeCode", (String) params.get("insModeCode"));
        queryList1.setParameter("bidJoinSpec", (String) params.get("bidJoinSpec"));
        queryList1.setParameter("specialCond", (String) params.get("specialCond"));
        queryList1.setParameter("supplyCond", (String) params.get("supplyCond"));
        queryList1.setParameter("spotDate", (String) params.get("spotDate"));
        queryList1.setParameter("spotArea", (String) params.get("spotArea"));
        queryList1.setParameter("succDeciMethCode", (String) params.get("succDeciMethCode"));
        queryList1.setParameter("amtBasis", (String) params.get("amtBasis"));
        queryList1.setParameter("bdAmt", params.get("bdAmt"));
        queryList1.setParameter("estStartDate", (String) params.get("estStartDate"));
        queryList1.setParameter("estCloseDate", (String) params.get("estCloseDate"));
        queryList1.setParameter("estOpenerCode", (String) params.get("estOpenerCode"));
        queryList1.setParameter("estBidderCode", (String) params.get("estBidderCode"));
        queryList1.setParameter("openAtt1Code", (String) params.get("openAtt1Code"));
        queryList1.setParameter("openAtt2Code", (String) params.get("openAtt2Code"));
        queryList1.setParameter("userId", userId);
        queryList1.setParameter("itemCode", (String) params.get("itemCode"));
        queryList1.setParameter("gongoIdCode", (String) params.get("gongoIdCode"));
        queryList1.setParameter("payCond", (String) params.get("payCond"));
        queryList1.setParameter("matDept", (String) params.get("matDept"));
        queryList1.setParameter("matProc", (String) params.get("matProc"));
        queryList1.setParameter("matCls", (String) params.get("matCls"));
        queryList1.setParameter("matFactory", (String) params.get("matFactory"));
        queryList1.setParameter("matFactoryLine", (String) params.get("matFactoryLine"));
        queryList1.setParameter("matFactoryCnt", (String) params.get("matFactoryCnt"));
        queryList1.setParameter("reason", (String) params.get("reason"));
        queryList1.setParameter("esmtAmt", params.get("esmtAmt"));

        queryList1.executeUpdate();

        StringBuilder sbList2 = new StringBuilder( // 업체정보 업데이트
                "UPDATE t_bi_info_mat_cust SET succ_yn ='Y', update_user = :userId, update_date = sysdate() " +
                        "where bi_no = :biNo and cust_code = :custCode");
        Query queryList2 = entityManager.createNativeQuery(sbList2.toString());
        queryList2.setParameter("userId", userId);
        queryList2.setParameter("biNo", (String) params.get("biNo"));
        queryList2.setParameter("custCode", (String) params.get("custCode"));

        queryList2.executeUpdate();

        StringBuilder sbList3 = new StringBuilder( // 업체정보차수 업데이트
                "UPDATE t_bi_info_mat_cust_temp SET succ_yn ='Y', update_user = :userId, update_date = sysdate() " +
                        "where bi_no = :biNo and cust_code = :custCode");
        Query queryList3 = entityManager.createNativeQuery(sbList3.toString());
        queryList3.setParameter("userId", userId);
        queryList3.setParameter("biNo", (String) params.get("biNo"));
        queryList3.setParameter("custCode", (String) params.get("custCode"));

        int q = queryList3.executeUpdate();
        if (q > 0) {
            Map<String, String> logParams = new HashMap<>();
            logParams.put("msg", "[본사] 낙찰");
            logParams.put("biNo", (String) params.get("biNo"));
            bidProgressService.updateLog(logParams);

            Map<String, String> mailParams = new HashMap<>();
            mailParams.put("biNo", (String) params.get("biNo"));
            mailParams.put("type", (String) params.get("type"));
            mailParams.put("biName", (String) params.get("biName"));
            mailParams.put("reason", (String) params.get("reason"));
            mailParams.put("interNm", (String) params.get("interNm"));
            bidProgressService.updateEmail(mailParams);
        }

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    public void updateSign(@RequestBody Map<String, Object> params) {
        StringBuilder sbList = new StringBuilder(
            "UPDATE t_bi_info_mat SET ");
        if((Boolean) params.get("att1")){
            sbList.append("open_att1_sign = 'Y' ");
        }    
        if((Boolean) params.get("att1") && (Boolean) params.get("att2")){
            sbList.append(",");
        }  
        if((Boolean) params.get("att2")){
            sbList.append("open_att2_sign = 'Y' ");
        }
        sbList.append("where bi_no = :biNo");
        
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biNo", (String) params.get("biNo"));
        queryList.executeUpdate();
    }

}
