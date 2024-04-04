package iljin.framework.ebid.bid.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.BidCustDto;
import iljin.framework.ebid.bid.dto.BidItemSpecDto;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.dto.BidProgressDto;
import iljin.framework.ebid.bid.dto.BidProgressFileDto;
import iljin.framework.ebid.bid.dto.CoUserInfoDto;
import iljin.framework.ebid.bid.dto.ItemDto;
import iljin.framework.ebid.bid.dto.SubmitHistDto;
import iljin.framework.ebid.bid.entity.TBiDetailMatCust;
import iljin.framework.ebid.bid.entity.TBiDetailMatCustTemp;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.math.BigDecimal;
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
				+ ",		tcu2.user_name AS opener_id "
				+ ",		tcu2.user_email AS opener_email "
				+ "FROM t_bi_info_mat tbim "
				+ "LEFT OUTER JOIN t_co_user tcu1 "
				+ "	ON tbim.create_user = tcu1.user_id "
				+ "LEFT OUTER JOIN t_co_user tcu2 "
				+ "	ON tbim.est_opener = tcu2.user_id "
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
				queryList.setParameter("bidNo", CommonUtils.getString(params.get("bidNo")));
				queryTotal.setParameter("bidNo", CommonUtils.getString(params.get("bidNo")));
			}
			if (!StringUtils.isEmpty(params.get("bidName"))) {
				queryList.setParameter("bidName", CommonUtils.getString(params.get("bidName")));
				queryTotal.setParameter("bidName", CommonUtils.getString(params.get("bidName")));
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
			
			String biNo = CommonUtils.getString(params.get("biNo"));
			
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
			queryMain.setParameter("biNo", biNo);
			
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
			
			detailDto.setBidAuth(!StringUtils.isEmpty(userInfoDto.getBidauth()));
			detailDto.setOpenAuth(!StringUtils.isEmpty(userInfoDto.getOpenauth()));
			
			// ************ 데이터 검색 -- 입찰참가업체 ************
			StringBuilder sbCustData = new StringBuilder(
				  "select	tbimc.BI_NO "
				+ ",		tbimc.CUST_CODE "
				+ ",		tccm.CUST_NAME "
				+ ",		tccm.PRES_NAME "
				+ ",		tcc.CODE_NAME as ESMT_CURR "
				+ ",		DATE_FORMAT(tbimc.SUBMIT_DATE, '%Y-%m-%d %H:%i') as SUBMIT_DATE "
				+ ",		(select tccu.USER_NAME from t_co_cust_user tccu where tccu.CUST_CODE = tbimc.CUST_CODE AND tccu.USER_TYPE = '1' LIMIT 1) AS DAMDANG_NAME "
				+ ",		tbimc.ESMT_YN "
				+ ",		tbimc.ESMT_AMT "
				+ ",		tbimc.ETC_B_FILE as ETC_FILE "
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
			queryCust.setParameter("biNo", biNo);
			
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
					queryCustSpec.setParameter("biNo", biNo);
					queryCustSpec.setParameter("custCode", custDto.getCustCode());
					
					List<BidItemSpecDto> specDto = new JpaResultMapper().list(queryCustSpec, BidItemSpecDto.class);
					
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
				querySpecFile.setParameter("biNo", biNo);
				
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
				querySpecInput.setParameter("biNo", biNo);
				
				List<BidItemSpecDto> specInput = new JpaResultMapper().list(querySpecInput, BidItemSpecDto.class);
				
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
			queryFile.setParameter("biNo", biNo);
			
			List<BidProgressFileDto> fileData = new JpaResultMapper().list(queryFile, BidProgressFileDto.class);
			
			detailDto.setFileList(fileData);
			
			resultBody.setData(detailDto);
			
		}catch(Exception e) {
			log.error("statusDetail error : {}", e);
			resultBody.setCode("999");
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
			
			String biNo = CommonUtils.getString(params.get("biNo"));
	
			StringBuilder sbList = new StringBuilder(
					"UPDATE	t_bi_info_mat " 
				+	"set	ing_tag = 'A7' "
				+	",		why_a7 = :reason "
				+	",		update_date = sysdate() "
				+	",		update_user = :userId "
				+	"WHERE bi_no = :biNo "
			);
	
			Query queryList = entityManager.createNativeQuery(sbList.toString());
			queryList.setParameter("biNo", biNo);
			queryList.setParameter("reason", CommonUtils.getString(params.get("reason")));
			queryList.setParameter("userId", userId);
			int rowsUpdated = queryList.executeUpdate();
			
			//입찰 hist 입력
			this.bidHist(biNo);
			
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
			
			try {
				bidProgressService.updateEmail(params);
			}catch(Exception e) {
				log.error("bidFailure sendMail error : {}", e);
			}
		}catch(Exception e) {
			log.error("bidFailure error : {}", e);
			resultBody.setCode("999");
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
			
			String biNo = CommonUtils.getString(params.get("biNo"));
			
			//복호화 대상 협력사
			StringBuilder sbCustList = new StringBuilder(
					"SELECT	tbimc.BI_NO "
				+	",		tbimc.CUST_CODE "
				+	",		tbimc.FILE_ID "
				+	",		tbimc.ENC_QUTN "
				+	",		tbimc.ENC_ESMT_SPEC "
				+	",		tbim.INS_MODE "
				+	",		tbimc.BI_ORDER "
				+	"FROM	t_bi_info_mat_cust tbimc " 
				+	"INNER JOIN	t_bi_info_mat tbim "
				+	"	ON	tbimc.BI_NO = tbim.BI_NO "
				+	"WHERE	tbimc.bi_no = :biNo "
				+	"AND	tbimc.ESMT_YN = '2' "
			);
			
			Query queryCustList = entityManager.createNativeQuery(sbCustList.toString());
			queryCustList.setParameter("biNo", biNo);
			queryCustList.executeUpdate();
			
			List<BidCustDto> custList = new JpaResultMapper().list(queryCustList, BidCustDto.class);
			
			for(BidCustDto custDto : custList) {
				
				//총 견적금액 복호화 (encQutn -> encQutn)
				String encQutn = custDto.getEncQutn();
				
				
				
				
				
				
				
				//복호화 후 업데이트
				StringBuilder sbCust = new StringBuilder(
						"UPDATE	t_bi_info_mat_cust " 
				+		"set	ESMT_AMT = :esmtAmt "
				+		",		SUBMIT_DATE = sysdate() "
				+		",		UPDATE_DATE = sysdate() "
				+		",		UPDATE_USER = :userId "
				+		"WHERE bi_no = :biNo "
				+		"AND CUST_CODE = :custCode "
				);
				
				Query queryCust = entityManager.createNativeQuery(sbCust.toString());
				queryCust.setParameter("esmtAmt", encQutn);
				queryCust.setParameter("userId", userId);
				queryCust.setParameter("biNo", custDto.getBiNo());
				queryCust.setParameter("custCode", custDto.getCustCode());
				queryCust.executeUpdate();
				
				//협력사 입찰 temp 테이블 insert
				this.insertBiInfoMatCustTemp(custDto.getBiNo(), custDto.getCustCode());
				
				//직접입력일 경우 협력사 직접입력 테이블 insert
				if(custDto.getInsMode().equals("2")) {
					//직접입력 정보 복호화(encEsmtSpec -> encEsmtSpec)
					String encEsmtSpec = custDto.getEncEsmtSpec();
					
					
					
					
					
					
					
					if(!encEsmtSpec.equals("")) {
						//직접입력 복호화
						String[] esmtSpecArr = encEsmtSpec.split("‡");
						
						for(String esmtSpec : esmtSpecArr) {
							
							String[] info = esmtSpec.split("=");
							
							//입찰 직접입력 테이블(t_bi_detail_mat_cust)에 insert
							TBiDetailMatCust tBiDetailMatCust = new TBiDetailMatCust();
							tBiDetailMatCust.setBiNo(custDto.getBiNo());
							tBiDetailMatCust.setSeq(CommonUtils.getInt(info[0]));
							tBiDetailMatCust.setCustCode(CommonUtils.getInt(custDto.getCustCode()));
							tBiDetailMatCust.setEsmtUc(new BigDecimal(info[1]));
							
							entityManager.persist(tBiDetailMatCust);
							
							//입찰 직접입력 테이블 차수(t_bi_detail_mat_cust_temp)에 insert
							TBiDetailMatCustTemp tBiDetailMatCustTemp = new TBiDetailMatCustTemp();
							tBiDetailMatCustTemp.setBiNo(custDto.getBiNo());
							tBiDetailMatCustTemp.setSeq(CommonUtils.getInt(info[0]));
							tBiDetailMatCustTemp.setCustCode(CommonUtils.getInt(custDto.getCustCode()));
							tBiDetailMatCustTemp.setBiOrder(custDto.getBiOrder());
							tBiDetailMatCustTemp.setEsmtUc(new BigDecimal(info[1]));
							
							entityManager.persist(tBiDetailMatCustTemp);
							
						}
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
			queryMain.setParameter("biNo", biNo);
			queryMain.executeUpdate();
			
			//입찰 이력 업데이트
			this.bidHist(biNo);
			
			//로그
			Map<String, String> logParams = new HashMap<>();
			logParams.put("msg", "[본사] 개찰");
			logParams.put("biNo", biNo);
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

	/**
	 * 낙찰
	 * @param params
	 * @return
	 */
	@Transactional
	public ResultBody bidSucc(@RequestBody Map<String, Object> params) {

		ResultBody resultBody = new ResultBody();
		
		try {
			UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
			String userId = userOptional.get().getUserId();
			
			String biNo = CommonUtils.getString(params.get("biNo"));
			
			StringBuilder sbList = new StringBuilder( // 입찰 업데이트
					  "UPDATE t_bi_info_mat "
					+ "SET	ing_tag = 'A5'"
					+ ",	update_user = :userId "
					+ ",	update_date = sysdate() "
					+ ",	add_accept = :succDetail "
					+ ",	succ_amt = (select ESMT_AMT from t_bi_info_mat_cust tbimc where BI_NO = :biNo and CUST_CODE = :succCust) "
					+ "where bi_no = :biNo ");
			
			Query queryList = entityManager.createNativeQuery(sbList.toString());
			queryList.setParameter("userId", userId);
			queryList.setParameter("succDetail", CommonUtils.getString(params.get("succDetail")));
			queryList.setParameter("succCust", CommonUtils.getString(params.get("succCust")));
			queryList.setParameter("biNo", biNo);
	
			queryList.executeUpdate();
			
			//입찰 hist 테이블 insert
			this.bidHist((String) params.get("biNo"));
	
			// 낙찰 업체정보 업데이트
			StringBuilder sbCust = new StringBuilder(
					"UPDATE t_bi_info_mat_cust SET succ_yn ='Y', update_user = :userId, update_date = sysdate() " +
							"where bi_no = :biNo and cust_code = :custCode");
			Query custQuery = entityManager.createNativeQuery(sbCust.toString());
			custQuery.setParameter("userId", userId);
			custQuery.setParameter("biNo", biNo);
			custQuery.setParameter("custCode", CommonUtils.getString(params.get("succCust")));
	
			custQuery.executeUpdate();
			
			//업체정보차수 업데이트
			StringBuilder sbList3 = new StringBuilder(
					"UPDATE t_bi_info_mat_cust_temp SET succ_yn ='Y', update_user = :userId, update_date = sysdate() " +
							"where bi_no = :biNo and cust_code = :custCode");
			Query queryList3 = entityManager.createNativeQuery(sbList3.toString());
			queryList3.setParameter("userId", userId);
			queryList3.setParameter("biNo", biNo);
			queryList3.setParameter("custCode", CommonUtils.getString(params.get("succCust")));
	
			queryList3.executeUpdate();
			
			//로그 입력
			Map<String, String> logParams = new HashMap<>();
			logParams.put("msg", "[본사] 낙찰");
			logParams.put("biNo", biNo);
			logParams.put("userId", userId);
			bidProgressService.updateLog(logParams);
			
			Map<String, String> mailParams = new HashMap<>();
			mailParams.put("biNo", biNo);
			mailParams.put("type", "succ");
			mailParams.put("biName", CommonUtils.getString(params.get("biName")));
			mailParams.put("reason", CommonUtils.getString(params.get("succDetail")));
			bidProgressService.updateEmail(mailParams);
			
		}catch(Exception e) {
			log.error("bidSucc error : {}", e);
			resultBody.setCode("999");
			resultBody.setMsg("낙찰 처리중 오류가 발생했습니다.");	
		}

		return resultBody;
	}
	
	/**
	 * 입찰 hist 입력
	 * @param biNo
	 */
	public void bidHist(String biNo) {
		
		if(!StringUtils.isEmpty(biNo)) {
			StringBuilder sbHist = new StringBuilder( // 입찰 hist 업데이트
				"INSERT into t_bi_info_mat_hist ("
				+ "BI_NO, BI_NAME, BI_MODE, INS_MODE, BID_JOIN_SPEC, SPECIAL_COND, SUPPLY_COND, SPOT_DATE, SPOT_AREA, SUCC_DECI_METH, BID_OPEN_DATE, AMT_BASIS, "
				+ "BD_AMT, SUCC_AMT, EST_START_DATE, EST_CLOSE_DATE, EST_OPENER, EST_BIDDER, EST_OPEN_DATE, OPEN_ATT1, OPEN_ATT1_SIGN, OPEN_ATT2, OPEN_ATT2_SIGN, "
				+ "ING_TAG, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE, ITEM_CODE, GONGO_ID, PAY_COND, WHY_A3, WHY_A7, BI_OPEN, INTERRELATED_CUST_CODE, "
				+ "REAL_AMT, ADD_ACCEPT, MAT_DEPT, MAT_PROC, MAT_CLS, MAT_FACTORY, MAT_FACTORY_LINE, MAT_FACTORY_CNT "
				+ ") select "
				+ "BI_NO, BI_NAME, BI_MODE, INS_MODE, BID_JOIN_SPEC, SPECIAL_COND, SUPPLY_COND, SPOT_DATE, SPOT_AREA, SUCC_DECI_METH, BID_OPEN_DATE, AMT_BASIS, "
				+ "BD_AMT, SUCC_AMT, EST_START_DATE, EST_CLOSE_DATE, EST_OPENER, EST_BIDDER, EST_OPEN_DATE, OPEN_ATT1, OPEN_ATT1_SIGN, OPEN_ATT2, OPEN_ATT2_SIGN, "
				+ "ING_TAG, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE, ITEM_CODE, GONGO_ID, PAY_COND, WHY_A3, WHY_A7, BI_OPEN, INTERRELATED_CUST_CODE, "
				+ "REAL_AMT, ADD_ACCEPT, MAT_DEPT, MAT_PROC, MAT_CLS, MAT_FACTORY, MAT_FACTORY_LINE, MAT_FACTORY_CNT "
				+ "from t_bi_info_mat tbim "
				+ "where tbim.BI_NO = :biNo"
			);
	
			Query histQuery = entityManager.createNativeQuery(sbHist.toString());
			histQuery.setParameter("biNo", biNo);
	
			histQuery.executeUpdate();
		}
	}
	
	/**
	 * 재입찰
	 * @param params
	 * @return
	 */
	@Transactional
	@SuppressWarnings({ "unchecked" })
	public ResultBody rebid(@RequestBody Map<String, Object> params) {

		ResultBody resultBody = new ResultBody();
		
		try {
			UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
			String userId = userOptional.get().getUserId();
	
			StringBuilder sbMain = new StringBuilder( // 입찰 업데이트
					  "UPDATE	t_bi_info_mat "
					+ "SET		EST_CLOSE_DATE = :estCloseDate "
					+ ",		WHY_A3 = :whyA3 "
					+ ",		ING_TAG = 'A3' "
					+ ",		UPDATE_DATE = sysdate() "
					+ ",		UPDATE_USER = :userId "
					+ ",		BI_MODE = 'A' "
					+ "WHERE	bi_no = :biNo");
	
			Query queryMain = entityManager.createNativeQuery(sbMain.toString());
			queryMain.setParameter("estCloseDate", (String) params.get("estCloseDate"));
			queryMain.setParameter("whyA3", (String) params.get("whyA3"));
			queryMain.setParameter("biNo", (String) params.get("biNo"));
			queryMain.setParameter("userId", userId);
	
			queryMain.executeUpdate();
	
			//입찰 hist 테이블 insert
			this.bidHist((String) params.get("biNo"));
			
			//재입찰 대상 초기화
			StringBuilder sbReCust = new StringBuilder(
				  "UPDATE	t_bi_info_mat_cust "
				+ "SET		REBID_ATT = 'N' "
				+ "WHERE	BI_NO = :biNo "
			);
			
			Query queryReCust = entityManager.createNativeQuery(sbReCust.toString());
			queryReCust.setParameter("biNo", (String) params.get("biNo"));
			queryReCust.executeUpdate();
			
			//협력사 상세내역 삭제
			StringBuilder sbCustDetailDel = new StringBuilder(
				  "DELETE FROM t_bi_detail_mat_cust "
				+ "WHERE BI_NO = :biNo "
			);
			
			Query queryCustDetailDel = entityManager.createNativeQuery(sbCustDetailDel.toString());
			queryCustDetailDel.setParameter("biNo", (String) params.get("biNo"));
			queryCustDetailDel.executeUpdate();
			
			//협력사 재입찰대상만 업데이트
			StringBuilder sbCustUpdate = new StringBuilder(
				  "UPDATE t_bi_info_mat_cust "
				+ "SET		REBID_ATT = 'Y' "
				+ ",		ESMT_YN = '0' "
				+ ",		ESMT_CURR = NULL "
				+ ",		ESMT_AMT = 0 "
				+ ",		ENC_QUTN = '0' "
				+ ",		ENC_ESMT_SPEC = NULL "
				+ ",		FILE_ID = NULL "
				+ ",		SUBMIT_DATE = NULL "
				+ ",		FILE_HASH_VALUE = NULL "
				+ ",		UPDATE_USER = :userId "
				+ ",		UPDATE_DATE = sysdate() "
				+ "WHERE	BI_NO = :biNo "
				+ "AND		CUST_CODE IN ( :custCode ) "
			);
			
			ArrayList<Integer> reCustList = (ArrayList<Integer>) params.get("reCustList");
			
			Query queryCustUpdate = entityManager.createNativeQuery(sbCustUpdate.toString());
			queryCustUpdate.setParameter("biNo", (String) params.get("biNo"));
			queryCustUpdate.setParameter("custCode", reCustList);
			queryCustUpdate.setParameter("userId", userId);
			queryCustUpdate.executeUpdate();
			
			//로그 입력
			Map<String, String> logParams = new HashMap<>();
			logParams.put("msg", "[본사] 재입찰");
			logParams.put("biNo", (String) params.get("biNo"));
			bidProgressService.updateLog(logParams);
			
			//메일 발송
			Map<String, String> mailParams = new HashMap<>();
			mailParams.put("biNo", (String) params.get("biNo"));
			mailParams.put("type", "rebid");
			mailParams.put("biName", (String) params.get("biName"));
			mailParams.put("reason", (String) params.get("whyA3"));
			bidProgressService.updateEmail(mailParams);
	
		}catch(Exception e) {
			log.error("rebid error : {}", e);
			resultBody.setCode("999");
			resultBody.setMsg("재입찰 처리 중 오류가 발생했습니다.");	
		}
		
		return resultBody;
	}
	
	/**
	 * 입찰 협력업체 차수 등록
	 */
	public void insertBiInfoMatCustTemp(String biNo, Integer custCode) {
		if(!StringUtils.isEmpty(biNo) && !StringUtils.isEmpty(custCode)) {
			StringBuilder sbHist = new StringBuilder( // 입찰 hist 업데이트
				  "insert into t_bi_info_mat_cust_temp( "
				+ "BI_NO, CUST_CODE, BI_ORDER, REBID_ATT, ESMT_YN, ESMT_AMT, SUCC_YN "
				+ ", ENC_QUTN, ENC_ESMT_SPEC, FILE_ID, SUBMIT_DATE, CREATE_USER, CREATE_DATE "
				+ ", UPDATE_USER, UPDATE_DATE, ESMT_CURR, ETC_B_FILE, FILE_HASH_VALUE, ETC_B_FILE_PATH "
				+ ") select  "
				+ "BI_NO, CUST_CODE, BI_ORDER, REBID_ATT, ESMT_YN, ESMT_AMT, SUCC_YN "
				+ ", ENC_QUTN, ENC_ESMT_SPEC, FILE_ID, SUBMIT_DATE, CREATE_USER, CREATE_DATE "
				+ ", UPDATE_USER, UPDATE_DATE, ESMT_CURR, ETC_B_FILE, FILE_HASH_VALUE, ETC_B_FILE_PATH "
				+ "from t_bi_info_mat_cust tbimc "
				+ "where tbimc.BI_NO = :biNo "
				+ "and tbimc.CUST_CODE = :custCode "
			);
	
			Query histQuery = entityManager.createNativeQuery(sbHist.toString());
			histQuery.setParameter("biNo", biNo);
			histQuery.setParameter("custCode", custCode);
	
			histQuery.executeUpdate();
		}
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
