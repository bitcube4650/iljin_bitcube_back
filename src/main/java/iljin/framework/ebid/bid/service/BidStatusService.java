package iljin.framework.ebid.bid.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.common.consts.DB;
//import iljin.framework.ebid.etc.util.common.certificate.service.CertificateService;
import iljin.framework.ebid.etc.util.common.message.MessageService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BidStatusService {
	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private TCoUserRepository tCoUserRepository;
	
	@Autowired
	private GeneralDao generalDao;

	@Autowired
	private BidProgressService bidProgressService;
	
//	@Autowired
//	private CertificateService certificateService;

	@Autowired
	private MessageService messageService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
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
		params.put("interrelatedCustCode", interrelatedCode);
		
		Page listPage = generalDao.selectGernalListPage(DB.QRY_SELECT_EBID_STATUS_LIST, params);
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
		
		Map<String, Object> detailObj = (Map<String, Object>) generalDao.selectGernalObject(DB.QRY_SELECT_EBID_STATUS_DETAIL, params);
		
		// ************ 로그인 당사자 개찰권한, 낙찰권한 확인 ************
		
		detailObj.put("bid_Auth", CommonUtils.getString(detailObj.get("estBidderId")).equals(userId));
		detailObj.put("open_Auth", CommonUtils.getString(detailObj.get("estOpenerId")).equals(userId));
		
		// ************ 데이터 검색 -- 입찰참가업체 ************
		
		List<Object> custData = generalDao.selectGernalList(DB.QRY_SELECT_EBID_STATUS_JOIN_CUST_LIST, params);
		
		//내역방식이 직접등록일 경우
		if(CommonUtils.getString(detailObj.get("insMode")).equals("2")) {
			for(Object custObj : custData) {
				Map<String, Object> custObjMap = (Map<String, Object>) custObj;
				
				Map<String, Object> innerParams = new HashMap<String, Object>();
				innerParams.put("biNo", params.get("biNo"));
				innerParams.put("custCode", custObjMap.get("custCode"));
				List<Object> specObj = generalDao.selectGernalList(DB.QRY_SELECT_EBID_STATUS_JOIN_CUST_SPEC, innerParams);
				
				custObjMap.put("bid_Spec", specObj);
			}
		}
		
		detailObj.put("cust_List", custData);
		
		// ************ 데이터 검색 -- 세부내역 ************
		if(CommonUtils.getString(detailObj.get("insMode")).equals("1")) {		//내역방식이 파일등록일 경우
			ArrayList<String> fileFlagArr = new ArrayList<String>();
			fileFlagArr.add("K");
			
			Map<String, Object> innerParams = new HashMap<String, Object>();
			innerParams.put("biNo", params.get("biNo"));
			innerParams.put("fileFlag", fileFlagArr);
			List<Object> specfile = generalDao.selectGernalList(DB.QRY_SELECT_EBID_STATUS_DETAIL_FILE, innerParams);
			
			detailObj.put("spec_File", specfile);
			
		}else if(CommonUtils.getString(detailObj.get("insMode")).equals("2")) {		//내역방식이 직접입력일 경우
			List<Object> specInput = generalDao.selectGernalList(DB.QRY_SELECT_EBID_STATUS_DETAIL_SPEC, params);
			
			detailObj.put("spec_Input", specInput);
			
		}
		
		// ************ 데이터 검색 -- 첨부파일 ************
		ArrayList<String> fileFlagArr = new ArrayList<String>();
		fileFlagArr.add("0");
		fileFlagArr.add("1");
		
		Map<String, Object> innerParams = new HashMap<String, Object>();
		innerParams.put("biNo", params.get("biNo"));
		innerParams.put("fileFlag", fileFlagArr);
		List<Object> fileData = generalDao.selectGernalList(DB.QRY_SELECT_EBID_STATUS_DETAIL_FILE, innerParams);
		
		detailObj.put("file_List", fileData);
		
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
		generalDao.selectGernalList(DB.QRY_UPDATE_EBID_STATUS, innerParams);
		
		//입찰 hist 입력
		this.bidHist(biNo);
		
		//로그입력
		this.insertTBiLog("[본사] 유찰", biNo, userId);
		
		//메일 전송
		try {
			List<Object> list = null;
			if(biMode.equals("A")) {
				list = generalDao.selectGernalList(DB.QRY_SELECT_EBID_BI_MODE_A_SEND_INFO, params);
			}else if(biMode.equals("B")) {
				list = generalDao.selectGernalList(DB.QRY_SELECT_EBID_BI_MODE_B_SEND_INFO, params);
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
	@SuppressWarnings({ "unchecked" })
	public ResultBody bidOpening(Map<String, String> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		//입찰 메인 테이블 업데이트
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		String biNo = CommonUtils.getString(params.get("biNo"));
		
		params.put("userId", userId);
		
		//복호화 대상 협력사
		List<Object> custList = generalDao.selectGernalList(DB.QRY_SELECT_DECRYPT_EBID_CUST_LIST, params);
		
		for(Object obj : custList) {
			Map<String, Object> custDto = (Map<String, Object>) obj;
			//복호화
			String encQutn = null;//파일입력
			String encEsmtSpec = null;//직접입력
			String decryptData = null;//복호화 할 데이터(파일입력 방식은 encQutn, 직접입력 방식은 encEsmtSpec)
			
			if(custDto.get("encQutn") != null) {
				encQutn = CommonUtils.getString(custDto.get("encQutn"));
			};
			
			if(custDto.get("encEsmtSpec") != null) {
				encEsmtSpec = CommonUtils.getString(custDto.get("encEsmtSpec"));
			};
			
			//파일입력 방식은 encQutn, 직접입력 방식은 encEsmtSpec 복호화
			if(CommonUtils.getString(custDto.get("insMode")).equals("1")) {//파일등록 방식
				decryptData = encQutn;
			}else{//직접입력 방식
				decryptData = encEsmtSpec;
			}
			
			//만약 데이터가 없으면 continue
			if(decryptData == null || decryptData.equals("")) {
				continue;
			}
			
			/* tradeSign 라이센스 문제로 복호화 및 데이터 검증 부분 주석처리
			 * 
			================================================복호화 및 검증 주석 시작========================================== 
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
			
			================================================복호화 및 검증 주석 끝==========================================
			*/
			
			//원래 암호화된 금액 복호화 후 데이터 검증된 결과가 fixedResult에 나와야 하는데 복호화 부분 제거로 new ResultBody()로 넣어줌 
			//====================================================================================================
			ResultBody fixedResult = new ResultBody();
			fixedResult.setData(decryptData);
			//====================================================================================================
			
			
			if(fixedResult.getCode().equals("ERROR")) {//복호화 한 데이터 검증 실패
				return fixedResult;
			}else {//검증 성공
				decryptData = (String) fixedResult.getData();
				decryptData = decryptData.replaceAll(",", "");// 금액에서 , 빼기
				
				if(CommonUtils.getString(custDto.get("insMode")).equals("2")) {//직접입력 방식인 경우
					//직접입력 총 견적액 구하기
					String[] esmtSpecArr = decryptData.split("\\$");//정규표현식에서 메타 문자로 사용되기 때문에 \\를 붙여줘야함
					
					//각 항목의 가격을 더해서 총 견적액 계산
					int specTotal = 0;
					for(String esmtSpec : esmtSpecArr) {
						String[] info = esmtSpec.split("=");
						Map<String, Object> innerParams = new HashMap<String, Object>();
						innerParams.put("biNo", custDto.get("biNo"));
						innerParams.put("seq", CommonUtils.getInt(info[0])+1);
						innerParams.put("custCode", CommonUtils.getInt(custDto.get("custCode")));
						innerParams.put("esmtUc", new BigDecimal(info[1]));
						innerParams.put("biOrder", custDto.get("biOrder"));
						
						//입찰 직접입력 테이블(t_bi_detail_mat_cust)에 insert
						generalDao.insertGernal(DB.QRY_INSERT_T_BI_DETAIL_MAT_CUST, params);
						
						//입찰 직접입력 이력 테이블(t_bi_detail_mat_cust_temp)에 insert
						generalDao.insertGernal(DB.QRY_INSERT_T_BI_DETAIL_MAT_CUST_TEMP, params);
						
						int itemPrice = Integer.parseInt(info[1]);
						specTotal += itemPrice;
						
					}
					
					decryptData = String.valueOf(specTotal);
				}
				
				
			}
			//데이터 검증 끝
			
			//총견적액 업데이트
			Map<String, Object> innerParams = new HashMap<String, Object>();
			innerParams.put("esmtAmt", decryptData);
			innerParams.put("userId", userId);
			innerParams.put("biNo", custDto.get("biNo"));
			innerParams.put("custCode", custDto.get("custCode"));
			
			generalDao.updateGernal(DB.QRY_UPDATE_OPEN_EBID_T_BI_INFO_MAT_CUST, innerParams);
			
			//협력사 입찰 temp 테이블 insert
			this.insertBiInfoMatCustTemp(CommonUtils.getString(custDto.get("biNo")), CommonUtils.getInt(custDto.get("custCode")));
		
		}
		
		//입찰 메인 업데이트
		generalDao.updateGernal(DB.QRY_UPDATE_OPEN_EBID_T_BI_INFO_MAT, params);
		
		//입찰 이력 업데이트
		this.bidHist(biNo);
		
		//로그입력
		this.insertTBiLog("[본사] 개찰", biNo, userId);
		
		return resultBody;
	}

	/**
	 * 낙찰
	 * @param params
	 * @return
	 */
	@Transactional
	@SuppressWarnings({ "unchecked" })
	public ResultBody bidSucc(@RequestBody Map<String, Object> params) throws Exception {

		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		
		String biNo = CommonUtils.getString(params.get("biNo"));
		Map<String, Object> biInfo = this.selectTBiInfoMatInfomation(biNo, "bi_mode");
		String biMode = CommonUtils.getString(biInfo.get("biMode"));
		
		params.put("userId", userId);
		
		generalDao.updateGernal(DB.QRY_UPDATE_EBID_SUCCESS_T_BI_INFO_MAT, params);
		
		
		//입찰 hist 테이블 insert
		this.bidHist((String) params.get("biNo"));

		// 낙찰 업체정보 업데이트
		generalDao.updateGernal(DB.QRY_UPDATE_EBID_SUCCESS_T_BI_INFO_MAT_CUST, params);
		
		//업체정보차수 업데이트
		generalDao.updateGernal(DB.QRY_UPDATE_EBID_SUCCESS_T_BI_INFO_MAT_CUST_TEMP, params);
		
		//로그입력
		this.insertTBiLog("[본사] 낙찰", biNo, userId);
		
		//메일, 문자 전송
		try {
			List<Object> list = null;
			ArrayList<Integer> arr = new ArrayList<Integer>();
			arr.add(CommonUtils.getInt(params.get("succCust")));
			params.put("custList", arr);
			
			if(biMode.equals("A")) {
				list = generalDao.selectGernalList(DB.QRY_SELECT_EBID_BI_MODE_A_SEND_INFO_CUST_LIST, params);
				
			}else if(biMode.equals("B")) {
				list = generalDao.selectGernalList(DB.QRY_SELECT_EBID_BI_MODE_B_SEND_INFO_CUST_LIST, params);
			}
			
			if(list.size() != 0) {
				Map<String, Object> emailParam = new HashMap<String, Object>();
				emailParam.put("type", "succ");
				emailParam.put("biName", params.get("biName"));
				emailParam.put("reason", params.get("succDetail"));
				emailParam.put("sendList", list);
				emailParam.put("biNo", biNo);
				
				bidProgressService.updateEmail(emailParam);
				
				//문자
				for(Object obj : list) {
					Map<String, Object> map = (Map<String, Object>) obj;
					messageService.send("일진그룹", CommonUtils.getString(map.get("userHp")), CommonUtils.getString(map.get("userName")), "[일진그룹 전자입찰시스템] 참여하신 입찰에("+biNo+") 낙찰되었습니다.\r\n확인바랍니다.", biNo);
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
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("biNo", biNo);
			
			generalDao.insertGernal(DB.QRY_INSERT_T_BI_INFO_MAT_HIST, params);
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
		Map<String, Object> biInfo = this.selectTBiInfoMatInfomation(biNo, "bi_mode");
		String biMode = CommonUtils.getString(biInfo.get("biMode"));
		
		params.put("userId", userId);
		
		//입찰 재입찰 상태로 변경
		generalDao.updateGernal(DB.QRY_UPDATE_REBID_T_BI_INFO_MAT, params);

		//입찰 hist 테이블 insert
		this.bidHist(biNo);
		
		//재입찰 대상 초기화
		generalDao.updateGernal(DB.QRY_UPDATE_REBID_ATT_N, params);
		
		//협력사 상세내역 삭제
		generalDao.deleteGernal(DB.QRY_DELETE_T_BI_DETAIL_MAT_CUST_CUST_CODE, params);
		
		//협력사 재입찰대상만 업데이트
		generalDao.updateGernal(DB.QRY_UPDATE_REBID_T_BI_INFO_MAT_CUST_CUST_CODE, params);
		
		//로그입력
		this.insertTBiLog("[본사] 재입찰", biNo, userId);
				
		//메일, 문자 전송
		try {
			List<Object> list = null;
			params.put("custList", params.get("reCustList"));
			
			if(biMode.equals("A")) {
				list = generalDao.selectGernalList(DB.QRY_SELECT_EBID_BI_MODE_A_SEND_INFO_CUST_LIST, params);
			}else if(biMode.equals("B")) {
				list = generalDao.selectGernalList(DB.QRY_SELECT_EBID_BI_MODE_B_SEND_INFO_CUST_LIST, params);
			}
			
			if(list.size() != 0) {
				Map<String, Object> emailParam = new HashMap<String, Object>();
				emailParam.put("type", "rebid");
				emailParam.put("biName", params.get("biName"));
				emailParam.put("reason", params.get("whyA3"));
				emailParam.put("sendList", list);
				emailParam.put("biNo", biNo);
				
				bidProgressService.updateEmail(emailParam);
				
				//문자
				for(Object obj : list) {
					Map<String, Object> map = (Map<String, Object>) obj;
					messageService.send("일진그룹", CommonUtils.getString(map.get("userHp")), CommonUtils.getString(map.get("userName")), "[일진그룹 전자입찰시스템] 일진씨앤에스에서 재입찰을 공고하였습니다.\r\n확인바랍니다.", biNo);
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
	public void insertBiInfoMatCustTemp(String biNo, Integer custCode) throws Exception {
		if(!StringUtils.isEmpty(biNo) && !StringUtils.isEmpty(custCode)) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("biNo", biNo);
			params.put("custCode", custCode);
			generalDao.insertGernal(DB.QRY_INSERT_T_BI_INFO_MAT_CUST_TEMP, params);
		}
	}
	
	/**
	 * 제출이력
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public ResultBody submitHist(@RequestBody Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		Page listPage = generalDao.selectGernalListPage(DB.QRY_SELECT_T_BI_INFO_MAT_CUST_TEMP_CUST_CODE, params);
		resultBody.setData(listPage);
		
		return resultBody;
	}

	/**
	 * 입회자 서명
	 * @param params
	 * @return
	 */
	@Transactional
	public ResultBody attSign(@RequestBody Map<String, Object> params) throws Exception{

		ResultBody resultBody = new ResultBody();
		
		String userId = CommonUtils.getString(params.get("attSignId"));
		String password = CommonUtils.getString(params.get("attPw"));
		String dbPassword = "";
		Optional<TCoUser> userOptional = tCoUserRepository.findById(userId);
		if (userOptional.isPresent()) {
			dbPassword = userOptional.get().getUserPwd();
		}
		
		String whoAtt = CommonUtils.getString(params.get("whoAtt"));
		if(!whoAtt.equals("1") && !whoAtt.equals("2")) {
			log.error("attSign error : whoAtt = {}" , whoAtt);
			resultBody.setCode("fail");
			return resultBody;
		}

		Boolean pwCheck = ((BCryptPasswordEncoder) passwordEncoder).matches(password, dbPassword);
		
		if(pwCheck) {
			generalDao.updateGernal(DB.QRY_UPDATE_OPEN_ATT_SIGN, params);
			
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
		
		Map<String, Object> resultMap = (Map<String, Object>) generalDao.selectGernalObject(DB.QRY_SELECT_T_BI_INFO_MAT_INFOMATION, params);
		
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
		generalDao.insertGernal(DB.QRY_BID_STATUS_INSERT_T_BI_LOG, logParams);
	}
}
