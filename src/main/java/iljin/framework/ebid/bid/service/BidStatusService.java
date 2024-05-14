package iljin.framework.ebid.bid.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.BidCustDto;
import iljin.framework.ebid.bid.dto.SendDto;
import iljin.framework.ebid.bid.dto.SubmitHistDto;
import iljin.framework.ebid.bid.entity.TBiDetailMatCust;
import iljin.framework.ebid.bid.entity.TBiDetailMatCustTemp;
import iljin.framework.ebid.bid.entity.TBiInfoMat;
import iljin.framework.ebid.bid.repository.TBiInfoMatRepository;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.PagaUtils;
import iljin.framework.ebid.etc.util.common.certificate.service.CertificateService;
import iljin.framework.ebid.etc.util.common.message.MessageService;
import lombok.extern.slf4j.Slf4j;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
	private GeneralDao generalDao;

	@Autowired
	private TBiInfoMatRepository tBiInfoMatRepository;
	
	@Autowired
	private BidProgressService bidProgressService;
	
	@Autowired
	private CertificateService certificateService;

	@Autowired
	private MessageService messageService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Value("${file.upload.directory}")
	private String uploadDirectory;

	/**
	 * 입찰진행 리스트
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public ResultBody statuslist(@RequestBody Map<String, Object> params) throws Exception{
		ResultBody resultBody = new ResultBody(); 
			
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		String interrelatedCode = userOptional.get().getInterrelatedCustCode();
		String userAuth = userOptional.get().getUserAuth();
		
		params.put("userAuth", userAuth);
		params.put("userId", userId);
		params.put("interrelatedCode", interrelatedCode);
		
		Page listPage = generalDao.selectGernalListPage("bidStatus.selectEbidStatusList", params);
		resultBody.setData(listPage);
		
		resultBody.setData(listPage);
		
		return resultBody;
	}
	
	/**
	 * 입찰진행 상세
	 * @param param
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public ResultBody statusDetail(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		params.put("userId", userId);
		
		Map<String, Object> detailObj = (Map<String, Object>) generalDao.selectGernalObject("bidStatus.selectEbidStatusDetail", params);
		
		// ************ 로그인 당사자 개찰권한, 낙찰권한 확인 ************
		
		detailObj.put("bidAuth", CommonUtils.getString(detailObj.get("estBidderId")).equals(userId));
		detailObj.put("openAuth", CommonUtils.getString(detailObj.get("estOpenerId")).equals(userId));
		
		// ************ 데이터 검색 -- 입찰참가업체 ************
		
		List<Object> custData = generalDao.selectGernalList("bidStatus.selectEbidStatusJoinCustList", params);
		
		//내역방식이 직접등록일 경우
		if(CommonUtils.getString(detailObj.get("insMode")).equals("2")) {
			for(Object custObj : custData) {
				Map<String, Object> custObjMap = (Map<String, Object>) custObj;
				
				Map<String, Object> innerParams = new HashMap<String, Object>();
				innerParams.put("biNo", params.get("biNo"));
				innerParams.put("custCode", custObjMap.get("custCode"));
				List<Object> specObj = generalDao.selectGernalList("bidStatus.selectEbidStatusJoinCustSpec", innerParams);
				
				custObjMap.put("bidSpec", specObj);
			}
		}
		
		detailObj.put("custList", custData);
		
		// ************ 데이터 검색 -- 세부내역 ************
		if(CommonUtils.getString(detailObj.get("insMode")).equals("1")) {		//내역방식이 파일등록일 경우
			ArrayList<String> fileFlagArr = new ArrayList<String>();
			fileFlagArr.add("K");
			
			Map<String, Object> innerParams = new HashMap<String, Object>();
			innerParams.put("biNo", params.get("biNo"));
			innerParams.put("fileFlag", fileFlagArr);
			List<Object> specfile = generalDao.selectGernalList("bidStatus.selectEbidStatusDetailFile", innerParams);
			
			detailObj.put("specFile", specfile);
			
		}else if(CommonUtils.getString(detailObj.get("insMode")).equals("2")) {		//내역방식이 직접입력일 경우
			List<Object> specInput = generalDao.selectGernalList("bidStatus.selectEbidStatusDetailSpec", params);
			
			detailObj.put("specInput", specInput);
			
		}
		
		// ************ 데이터 검색 -- 첨부파일 ************
		ArrayList<String> fileFlagArr = new ArrayList<String>();
		fileFlagArr.add("0");
		fileFlagArr.add("1");
		
		Map<String, Object> innerParams = new HashMap<String, Object>();
		innerParams.put("biNo", params.get("biNo"));
		innerParams.put("fileFlag", fileFlagArr);
		List<Object> fileData = generalDao.selectGernalList("bidStatus.selectEbidStatusDetailFile", innerParams);
		
		detailObj.put("fileList", fileData);
		
		resultBody.setData(detailObj);
		
		return resultBody;
	}
	
	/**
	 * 유찰처리
	 * @param params
	 * @return
	 */
	@Transactional
	public ResultBody bidFailure(Map<String, String> params) throws Exception{
		
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		
		String biNo = CommonUtils.getString(params.get("biNo"));
		Map<String, Object> biInfo = this.selectTBiInfoMatInfomation(biNo, "bi_mode");
		String biMode = CommonUtils.getString(biInfo.get("biMode"));
		
		Map<String, Object> innerParams = new HashMap<String, Object>();
		innerParams.put("biNo", biNo);
		innerParams.put("ingTag", "A7");
		innerParams.put("whyA7", params.get("reason"));
		innerParams.put("userId", userId);
		generalDao.selectGernalList("bidStatus.updateEbidStatus", innerParams);
		
		//입찰 hist 입력
		this.bidHist(biNo);
		
		//로그입력
		this.insertTBiLog("[본사] 유찰", biNo, userId);
		
		//메일 전송
		try {
			List<Object> list = null;
			if(biMode.equals("A")) {
				list = generalDao.selectGernalList("bidStatus.selectEbidBiModeASendInfo", params);
			}else if(biMode.equals("B")) {
				list = generalDao.selectGernalList("bidStatus.selectEbidBiModeBSendInfo", params);
			}
			
			if(list.size() != 0) {
				Map<String, Object> emailParam = new HashMap<String, Object>();
				emailParam.put("type", "fail");
				emailParam.put("biName", params.get("biName"));
				emailParam.put("reason", params.get("reason"));
				emailParam.put("sendList", list);
				emailParam.put("biNo", biNo);
				
				bidProgressService.updateEmail(emailParam);
			}
		}catch(Exception e) {
			log.error("bidFailure sendMail error : {}", e);
		}
		
		return resultBody;
	}
	

	/**
	 * 개찰
	 * @param params : (String) biNo
	 * @return
	 */
	@Transactional
	public ResultBody bidOpening(Map<String, String> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		//입찰 메인 테이블 업데이트
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		String interrelatedCustCode = userOptional.get().getInterrelatedCustCode();
		String biNo = CommonUtils.getString(params.get("biNo"));
		String certPwd = CommonUtils.getString(params.get("certPwd"));
		
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
			+	"AND	tbimc.BI_ORDER = (select MAX(BI_ORDER) from t_bi_info_mat_cust where BI_NO = :biNo) "
		);
		
		Query queryCustList = entityManager.createNativeQuery(sbCustList.toString());
		queryCustList.setParameter("biNo", biNo);
		queryCustList.executeUpdate();
		
		List<BidCustDto> custList = new JpaResultMapper().list(queryCustList, BidCustDto.class);
		
		for(BidCustDto custDto : custList) {
			//복호화
			String encQutn = null;//파일입력
			String encEsmtSpec = null;//직접입력
			String decryptData = null;//복호화 할 데이터(파일입력 방식은 encQutn, 직접입력 방식은 encEsmtSpec)
			
			if(custDto.getEncQutn() != null) {
				encQutn = custDto.getEncQutn();
			};
			
			if(custDto.getEncEsmtSpec() != null) {
				encEsmtSpec = custDto.getEncEsmtSpec();
			};
			
			//파일입력 방식은 encQutn, 직접입력 방식은 encEsmtSpec 복호화
			if(custDto.getInsMode().equals("1")) {//파일등록 방식
				decryptData = encQutn;
			}else{//직접입력 방식
				decryptData = encEsmtSpec;
			}
			
			//만약 데이터가 없으면 continue
			if(decryptData == null || decryptData.equals("")) {
				continue;
			}
			
			//복호화 시작
			ResultBody decryptResult = certificateService.decryptData(decryptData, interrelatedCustCode, certPwd);
			if(decryptResult.getCode().equals("ERROR")) {//복호화 실패
				
				//이전 인증서로 다시 복호화 시도
				String preCertPath = "pre/" + interrelatedCustCode;
				ResultBody decryptResult2 = certificateService.decryptData(decryptData, preCertPath, certPwd);
				
				if(decryptResult2.getCode().equals("ERROR")) {//2차 시도 복호화 실패
					return decryptResult2;
					
				}else{//2차 시도 복호화 성공
					decryptData = (String) decryptResult2.getData();
				}
				
			}else {//복호화 성공
				decryptData = (String) decryptResult.getData();
				
			}
			//복호화 끝
			
			//데이터 검증
			ResultBody fixedResult = certificateService.signDataFix(decryptData);
			
			if(fixedResult.getCode().equals("ERROR")) {//복호화 한 데이터 검증 실패
				return fixedResult;
			}else {//검증 성공
				decryptData = (String) fixedResult.getData();
				decryptData = decryptData.replaceAll(",", "");// 금액에서 , 빼기
				
				if(custDto.getInsMode().equals("2")) {//직접입력 방식인 경우
					//직접입력 총 견적액 구하기
					String[] esmtSpecArr = decryptData.split("\\$");//정규표현식에서 메타 문자로 사용되기 때문에 \\를 붙여줘야함
					
					//각 항목의 가격을 더해서 총 견적액 계산
					int specTotal = 0;
					for(String esmtSpec : esmtSpecArr) {
						String[] info = esmtSpec.split("=");
						//입찰 직접입력 테이블(t_bi_detail_mat_cust)에 insert
						TBiDetailMatCust tBiDetailMatCust = new TBiDetailMatCust();
						tBiDetailMatCust.setBiNo(custDto.getBiNo());
						tBiDetailMatCust.setSeq(CommonUtils.getInt(info[0])+1);
						tBiDetailMatCust.setCustCode(CommonUtils.getInt(custDto.getCustCode()));
						tBiDetailMatCust.setEsmtUc(new BigDecimal(info[1]));
						
						entityManager.persist(tBiDetailMatCust);
						
						//입찰 직접입력 테이블 차수(t_bi_detail_mat_cust_temp)에 insert
						TBiDetailMatCustTemp tBiDetailMatCustTemp = new TBiDetailMatCustTemp();
						tBiDetailMatCustTemp.setBiNo(custDto.getBiNo());
						tBiDetailMatCustTemp.setSeq(CommonUtils.getInt(info[0])+1);
						tBiDetailMatCustTemp.setCustCode(CommonUtils.getInt(custDto.getCustCode()));
						tBiDetailMatCustTemp.setBiOrder(custDto.getBiOrder());
						tBiDetailMatCustTemp.setEsmtUc(new BigDecimal(info[1]));
						
						entityManager.persist(tBiDetailMatCustTemp);
						
						
						int itemPrice = Integer.parseInt(info[1]);
						specTotal += itemPrice;
						
						
					}
					
					decryptData = String.valueOf(specTotal);
				}
				
				
			}
			//데이터 검증 끝
			
			//총견적액 업데이트
			StringBuilder sbCust = new StringBuilder(
					"UPDATE	t_bi_info_mat_cust " 
			+		"set	ESMT_AMT = :esmtAmt "
			+		",		UPDATE_DATE = sysdate() "
			+		",		UPDATE_USER = :userId "
			+		"WHERE bi_no = :biNo "
			+		"AND CUST_CODE = :custCode "
			);
			
			Query queryCust = entityManager.createNativeQuery(sbCust.toString());
			queryCust.setParameter("esmtAmt", decryptData);
			queryCust.setParameter("userId", userId);
			queryCust.setParameter("biNo", custDto.getBiNo());
			queryCust.setParameter("custCode", custDto.getCustCode());
			queryCust.executeUpdate();
			
			//협력사 입찰 temp 테이블 insert
			this.insertBiInfoMatCustTemp(custDto.getBiNo(), custDto.getCustCode());
		
		}
		
		//입찰 메인 업데이트
		StringBuilder sbMain = new StringBuilder(
				"UPDATE	t_bi_info_mat " 
			+	"set	ING_TAG = 'A2' "
			+	",		EST_OPEN_DATE = sysdate() "
			+	",		BI_OPEN = 'Y' "
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
		
		return resultBody;
	}

	/**
	 * 낙찰
	 * @param params
	 * @return
	 */
	@Transactional
	public ResultBody bidSucc(@RequestBody Map<String, Object> params) throws Exception {

		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		
		String biNo = CommonUtils.getString(params.get("biNo"));
		String biMode = "";
		Optional<TBiInfoMat> optionData = tBiInfoMatRepository.findById(biNo);
		if(optionData.isPresent()) {
			biMode = optionData.get().getBiMode();
		}
		
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
		
		//메일, 문자 전송
		try {
			StringBuilder sbMail = new StringBuilder("");
			
			if(biMode.equals("A")) {
				sbMail.append(
					  "select	tccu.USER_EMAIL  "
					+ ",		a.from_email "
					+ ",		REGEXP_REPLACE(tccu.USER_HP , '[^0-9]+', '') as USER_HP "
					+ ",		tccu.USER_NAME "
					+ "from "
					+ "( "
					+ "	select	jb.datas as user_id "
					+ "	,		tcu.USER_EMAIL as from_email "
					+ "	from t_bi_info_mat_cust tbimc "
					+ "	inner join json_table( "
					+ "		replace(json_array(tbimc.USEMAIL_ID), ',', '\",\"'), "
					+ "		'$[*]' columns (datas varchar(50) path '$') "
					+ "	) jb "
					+ "	inner join t_bi_info_mat tbim "
					+ "		on tbimc.bi_no = tbim.bi_no "
					+ "	left outer join t_co_user tcu "
					+ "		on tbim.create_user = tcu.user_id "
					+ "	where tbimc.bi_no = :biNo "
					+ "	and tbimc.cust_code = :succCust "
					+ ") a "
					+ "inner join t_co_cust_user tccu "
					+ "	on a.user_id = tccu.user_id "
					+ "	and tccu.USE_YN = 'Y' "
					+ "group by tccu.USER_EMAIL "	
				);
			}else if(biMode.equals("B")) {
				sbMail.append(
					"select	tccu.user_email "
					+ ",	tcu.user_email as from_email "
					+ ",	REGEXP_REPLACE(tccu.USER_HP , '[^0-9]+', '') as USER_HP "
					+ ",	tccu.USER_NAME "
					+ "from t_bi_info_mat_cust tbimc "
					+ "inner join t_co_cust_master tccm "
					+ "	on tbimc.cust_code = tccm.cust_code "
					+ "inner join t_co_cust_user tccu "
					+ "	on tccm.cust_code = tccu.cust_code "
					+ "	and tccu.USE_YN = 'Y' "
					+ "inner join t_bi_info_mat tbim "
					+ "	on tbimc.bi_no = tbim.bi_no "
					+ "left outer join t_co_user tcu "
					+ "	on tbim.create_user = tcu.user_id "
					+ "where tbimc.bi_no = :biNo "
					+ "and tbimc.cust_code = :succCust "
				);
			}
			
			//쿼리 실행
			Query queryMail = entityManager.createNativeQuery(sbMail.toString());
			//조건 대입
			queryMail.setParameter("biNo", biNo);
			queryMail.setParameter("succCust", CommonUtils.getString(params.get("succCust")));
			List<SendDto> sendList = new JpaResultMapper().list(queryMail, SendDto.class);
			
			if(sendList.size() != 0) {
				Map<String, Object> emailParam = new HashMap<String, Object>();
				emailParam.put("type", "succ");
				emailParam.put("biName", params.get("biName"));
				emailParam.put("reason", params.get("succDetail"));
				emailParam.put("sendList", sendList);
				emailParam.put("biNo", biNo);
				
				bidProgressService.updateEmail(emailParam);
				
				//문자
				for(SendDto dto : sendList) {
					messageService.send("일진그룹", dto.getUserHp(), dto.getUserName(), "[일진그룹 전자입찰시스템] 참여하신 입찰에("+biNo+") 낙찰되었습니다.\r\n확인바랍니다.", biNo);
				}
				
			}
			
		}catch(Exception e) {
			log.error("bidSucc sendMsg error : {}", e);
		}

		return resultBody;
	}
	
	/**
	 * 입찰 hist 입력
	 * @param biNo
	 */
	public void bidHist(String biNo) throws Exception{
		
		if(!StringUtils.isEmpty(biNo)) {
//			StringBuilder sbHist = new StringBuilder( // 입찰 hist 업데이트
//				"INSERT into t_bi_info_mat_hist ("
//				+ "BI_NO, BI_NAME, BI_MODE, INS_MODE, BID_JOIN_SPEC, SPECIAL_COND, SUPPLY_COND, SPOT_DATE, SPOT_AREA, SUCC_DECI_METH, BID_OPEN_DATE, AMT_BASIS, "
//				+ "BD_AMT, SUCC_AMT, EST_START_DATE, EST_CLOSE_DATE, EST_OPENER, EST_BIDDER, EST_OPEN_DATE, OPEN_ATT1, OPEN_ATT1_SIGN, OPEN_ATT2, OPEN_ATT2_SIGN, "
//				+ "ING_TAG, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE, ITEM_CODE, GONGO_ID, PAY_COND, WHY_A3, WHY_A7, BI_OPEN, INTERRELATED_CUST_CODE, "
//				+ "REAL_AMT, ADD_ACCEPT, MAT_DEPT, MAT_PROC, MAT_CLS, MAT_FACTORY, MAT_FACTORY_LINE, MAT_FACTORY_CNT "
//				+ ") select "
//				+ "BI_NO, BI_NAME, BI_MODE, INS_MODE, BID_JOIN_SPEC, SPECIAL_COND, SUPPLY_COND, SPOT_DATE, SPOT_AREA, SUCC_DECI_METH, BID_OPEN_DATE, AMT_BASIS, "
//				+ "BD_AMT, SUCC_AMT, EST_START_DATE, EST_CLOSE_DATE, EST_OPENER, EST_BIDDER, EST_OPEN_DATE, OPEN_ATT1, OPEN_ATT1_SIGN, OPEN_ATT2, OPEN_ATT2_SIGN, "
//				+ "ING_TAG, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE, ITEM_CODE, GONGO_ID, PAY_COND, WHY_A3, WHY_A7, BI_OPEN, INTERRELATED_CUST_CODE, "
//				+ "REAL_AMT, ADD_ACCEPT, MAT_DEPT, MAT_PROC, MAT_CLS, MAT_FACTORY, MAT_FACTORY_LINE, MAT_FACTORY_CNT "
//				+ "from t_bi_info_mat tbim "
//				+ "where tbim.BI_NO = :biNo"
//			);
//			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("biNo", biNo);
			
			generalDao.insertGernal("bidStatus.insertTBiInfoMatHist", params);
//	
//			Query histQuery = entityManager.createNativeQuery(sbHist.toString());
//			histQuery.setParameter("biNo", biNo);
//	
//			histQuery.executeUpdate();
		}
	}
	
	/**
	 * 재입찰
	 * @param params
	 * @return
	 */
	@Transactional
	@SuppressWarnings({ "unchecked" })
	public ResultBody rebid(@RequestBody Map<String, Object> params) throws Exception {

		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		
		String biNo = CommonUtils.getString(params.get("biNo"));
		String biMode = "";
		Optional<TBiInfoMat> optionData = tBiInfoMatRepository.findById(biNo);
		if(optionData.isPresent()) {
			biMode = optionData.get().getBiMode();
		}
		
		StringBuilder sbMain = new StringBuilder( // 입찰 업데이트
				  "UPDATE	t_bi_info_mat "
				+ "SET		EST_CLOSE_DATE = :estCloseDate "
				+ ",		WHY_A3 = :whyA3 "
				+ ",		ING_TAG = 'A3' "
				+ ",		EST_OPEN_DATE = NULL "
				+ ",		UPDATE_DATE = sysdate() "
				+ ",		UPDATE_USER = :userId "
				+ ",		OPEN_ATT1_SIGN = 'N' "
				+ ",		OPEN_ATT2_SIGN = 'N' "
				+ ",		BI_MODE = 'A' "
				+ ",		BI_OPEN = 'N' "
				+ "WHERE	bi_no = :biNo");

		Query queryMain = entityManager.createNativeQuery(sbMain.toString());
		queryMain.setParameter("estCloseDate", CommonUtils.getString(params.get("estCloseDate")));
		queryMain.setParameter("whyA3", CommonUtils.getString(params.get("whyA3")));
		queryMain.setParameter("biNo", biNo);
		queryMain.setParameter("userId", userId);

		queryMain.executeUpdate();

		//입찰 hist 테이블 insert
		this.bidHist(biNo);
		
		//재입찰 대상 초기화
		StringBuilder sbReCust = new StringBuilder(
			  "UPDATE	t_bi_info_mat_cust "
			+ "SET		REBID_ATT = 'N' "
			+ "WHERE	BI_NO = :biNo "
		);
		
		Query queryReCust = entityManager.createNativeQuery(sbReCust.toString());
		queryReCust.setParameter("biNo", biNo);
		queryReCust.executeUpdate();
		
		//협력사 상세내역 삭제
		
		ArrayList<Integer> reCustList = (ArrayList<Integer>) params.get("reCustList");
		
		StringBuilder sbCustDetailDel = new StringBuilder(
			  "DELETE FROM t_bi_detail_mat_cust "
			+ "WHERE BI_NO = :biNo "
			+ "AND	CUST_CODE IN ( :custCode )"
		);
		
		Query queryCustDetailDel = entityManager.createNativeQuery(sbCustDetailDel.toString());
		queryCustDetailDel.setParameter("biNo", biNo);
		queryCustDetailDel.setParameter("custCode", reCustList);
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
			+ ",		BI_ORDER = (select MAX(BI_ORDER)+1 from t_bi_info_mat_cust where BI_NO = :biNo) "
			+ "WHERE	BI_NO = :biNo "
			+ "AND		CUST_CODE IN ( :custCode ) "
		);
		
		Query queryCustUpdate = entityManager.createNativeQuery(sbCustUpdate.toString());
		queryCustUpdate.setParameter("biNo", biNo);
		queryCustUpdate.setParameter("custCode", reCustList);
		queryCustUpdate.setParameter("userId", userId);
		queryCustUpdate.executeUpdate();
		
		//로그 입력
		Map<String, String> logParams = new HashMap<>();
		logParams.put("msg", "[본사] 재입찰");
		logParams.put("biNo", biNo);
		bidProgressService.updateLog(logParams);
		
		//메일, 문자 전송
		try {
			StringBuilder sbMail = new StringBuilder("");
			
			if(biMode.equals("A")) {
				sbMail.append(
					  "select	tccu.USER_EMAIL "
					+ ",		a.from_email "
					+ ",		REGEXP_REPLACE(tccu.USER_HP , '[^0-9]+', '') as USER_HP "
					+ ",		tccu.USER_NAME "
					+ "from "
					+ "( "
					+ "	select	jb.datas as user_id "
					+ "	,		tcu.USER_EMAIL as from_email "
					+ "	from t_bi_info_mat_cust tbimc "
					+ "	inner join json_table( "
					+ "		replace(json_array(tbimc.USEMAIL_ID), ',', '\",\"'), "
					+ "		'$[*]' columns (datas varchar(50) path '$') "
					+ "	) jb "
					+ "	inner join t_bi_info_mat tbim "
					+ "		on tbimc.bi_no = tbim.bi_no "
					+ "	left outer join t_co_user tcu "
					+ "		on tbim.create_user = tcu.user_id "
					+ "	where tbimc.bi_no = :biNo "
					+ "	and tbimc.cust_code IN ( :custCode ) "
					+ ") a "
					+ "inner join t_co_cust_user tccu "
					+ "	on a.user_id = tccu.user_id "
					+ "	and tccu.USE_YN = 'Y' "
					+ "group by tccu.USER_EMAIL "	
				);
			}else if(biMode.equals("B")) {
				sbMail.append(
					"select	tccu.user_email "
					+ ",	tcu.user_email as from_email "
					+ ",	REGEXP_REPLACE(tccu.USER_HP , '[^0-9]+', '') as USER_HP "
					+ ",	tccu.USER_NAME "
					+ "from t_bi_info_mat_cust tbimc "
					+ "inner join t_co_cust_master tccm "
					+ "	on tbimc.cust_code = tccm.cust_code "
					+ "inner join t_co_cust_user tccu "
					+ "	on tccm.cust_code = tccu.cust_code "
					+ "	and tccu.USE_YN = 'Y' "
					+ "inner join t_bi_info_mat tbim "
					+ "	on tbimc.bi_no = tbim.bi_no "
					+ "left outer join t_co_user tcu "
					+ "	on tbim.create_user = tcu.user_id "
					+ "where tbimc.bi_no = :biNo "
					+ "and tccu.CUST_CODE IN ( :custCode ) "
				);
			}
			
			//쿼리 실행
			Query queryMail = entityManager.createNativeQuery(sbMail.toString());
			//조건 대입
			queryMail.setParameter("biNo", biNo);
			queryMail.setParameter("custCode", reCustList);
			List<SendDto> sendList = new JpaResultMapper().list(queryMail, SendDto.class);
			
			if(sendList.size() != 0) {
				Map<String, Object> emailParam = new HashMap<String, Object>();
				emailParam.put("type", "rebid");
				emailParam.put("biName", params.get("biName"));
				emailParam.put("reason", params.get("whyA3"));
				emailParam.put("sendList", sendList);
				emailParam.put("biNo", biNo);
				
				bidProgressService.updateEmail(emailParam);
				
				//문자
				for(SendDto dto : sendList) {
					messageService.send("일진그룹", dto.getUserHp(), dto.getUserName(), "[일진그룹 전자입찰시스템] 일진씨앤에스에서 재입찰을 공고하였습니다.\r\n확인바랍니다.", biNo);
				}
			}
		}catch(Exception e) {
			log.error("rebid sendMail error : {}", e);
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
				+ ", UPDATE_USER, UPDATE_DATE, ESMT_CURR, ETC_B_FILE, FILE_HASH_VALUE, ETC_B_FILE_PATH, USEMAIL_ID "
				+ ") select  "
				+ "BI_NO, CUST_CODE, BI_ORDER, REBID_ATT, ESMT_YN, ESMT_AMT, SUCC_YN "
				+ ", ENC_QUTN, ENC_ESMT_SPEC, FILE_ID, SUBMIT_DATE, CREATE_USER, CREATE_DATE "
				+ ", UPDATE_USER, UPDATE_DATE, ESMT_CURR, ETC_B_FILE, FILE_HASH_VALUE, ETC_B_FILE_PATH, USEMAIL_ID "
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
	
	/**
	 * 제출이력
	 * @param params
	 * @return
	 */
	public ResultBody submitHist(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		String biNo = CommonUtils.getString(params.get("biNo"));
		String custCode = CommonUtils.getString(params.get("custCode"));

		StringBuilder sbCount = new StringBuilder(
				  "SELECT count(1) "
				+ "from t_bi_info_mat_cust_temp "
				+ "where bi_no = :biNo "
				+ "and cust_code = :custCode "
		);
		StringBuilder sbList = new StringBuilder(
				  "SELECT	tbimct.bi_order "
				+ ",		tbimct.esmt_curr "
				+ ",		tbimct.esmt_amt "
				+ ",		DATE_FORMAT(tbimct.submit_date, '%Y-%m-%d %H:%i') AS submit_date "
				+ "from t_bi_info_mat_cust_temp tbimct "
				+ "left outer join t_co_code tcc "
				+ "	on tcc.COL_CODE = 'T_CO_RATE' "
				+ "	and tbimct.ESMT_CURR = tcc.CODE_VAL "
				+ "where tbimct.bi_no = :biNo "
				+ "and tbimct.cust_code = :custCode "
				+ "order by tbimct.bi_order asc ");

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
		Page listPage = new PageImpl(list, pageable, count.intValue());
		
		resultBody.setData(listPage);
		
		return resultBody;
	}

	/**
	 * 입회자 서명
	 * @param params
	 * @return
	 */
	@Transactional
	public ResultBody attSign(@RequestBody Map<String, Object> params) {

		ResultBody resultBody = new ResultBody();
		
		String userId = CommonUtils.getString(params.get("attSignId"));
		String password = CommonUtils.getString(params.get("attPw"));
		String dbPassword = "";
		Optional<TCoUser> userOptional = tCoUserRepository.findById(userId);
		if (userOptional.isPresent()) {
			dbPassword = userOptional.get().getUserPwd();
		}

		Boolean pwCheck = ((BCryptPasswordEncoder) passwordEncoder).matches(password, dbPassword);
		
		if(pwCheck) {
			String whoAtt = CommonUtils.getString(params.get("whoAtt"));
			String biNo = CommonUtils.getString(params.get("biNo"));
			
			if(!whoAtt.equals("1") && !whoAtt.equals("2")) {
				log.error("attSign error : whoAtt = {}" , whoAtt);
				resultBody.setCode("fail");
				return resultBody;
			}
			
			StringBuilder sbMain = new StringBuilder(
				  "UPDATE t_bi_info_mat SET "
			);
			
			if(whoAtt.equals("1")) {
				sbMain.append(
					"OPEN_ATT1_SIGN = 'Y' "
				);
			} else if(whoAtt.equals("2")) {
				sbMain.append(
					"OPEN_ATT2_SIGN = 'Y' "
				);
			}
			
			sbMain.append("WHERE BI_NO = :biNo ");
			
			Query query = entityManager.createNativeQuery(sbMain.toString());
			query.setParameter("biNo", biNo);
			query.executeUpdate();
			
		}else {
			resultBody.setCode("inValid");
		}
		
		return resultBody;
	}
	
	/**
	 * t_bi_info_mat 에서 필요한 컬럼 정보 가져오기
	 * @param biNo
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
	public Map<String, Object> selectTBiInfoMatInfomation(String biNo, String columns) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("biNo", biNo);
		params.put("columns", columns);
		
		Map<String, Object> resultMap = (Map<String, Object>) generalDao.selectGernalObject("bidStatus.selectTBiInfoMatInfomation", params);
		
		return resultMap;
	}
	
	/**
	 * t_bi_log 입력
	 * @param msg
	 * @param biNo
	 * @param userId
	 * @throws Exception
	 */
	public void insertTBiLog(String msg, String biNo, String userId) throws Exception {
		Map<String, String> logParams = new HashMap<>();
		logParams.put("msg", msg);
		logParams.put("biNo", biNo);
		logParams.put("userId", userId);
		generalDao.insertGernal("bidStatus.insertTBiLog", logParams);
	}
}
