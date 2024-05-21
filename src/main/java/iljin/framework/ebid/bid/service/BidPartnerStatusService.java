package iljin.framework.ebid.bid.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.custom.entity.TCoCustUser;
import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.common.certificate.service.CertificateService;
import iljin.framework.ebid.etc.util.common.file.FileService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BidPartnerStatusService {

    @Autowired
    private FileService fileService;
    
    @Autowired
    private CertificateService certificateService;
    
    @Autowired
    private TCoCustUserRepository tCoCustUserRepository; 
    
    @Autowired
    GeneralDao generalDao;

	/**
	 * 협력사 입찰진행
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public ResultBody statuslist(@RequestBody Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoCustUser> userOptional = tCoCustUserRepository.findById(principal.getUsername());
		int custCode = userOptional.get().getCustCode();
		
		params.put("custCode", custCode);
		Page listPage = generalDao.selectGernalListPage("bidStatus.selectPartnerEbidStatusList", params);
		resultBody.setData(listPage);
		
		return resultBody;
	}

	/**
	 * 협력사 공고확인 처리
	 * @param params
	 * @param user
	 * @return
	 */
	@Transactional
	public ResultBody checkBid(@RequestBody Map<String, Object> params) throws Exception{
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoCustUser> userOptional = tCoCustUserRepository.findById(principal.getUsername());
		
		String userId = userOptional.get().getUserId();
		int custCode = userOptional.get().getCustCode();//협력사 번호 
		params.put("userId", userId);
		params.put("custCode", custCode);
		
		generalDao.insertGernal("bidStatus.updateEbidTBiInfoMatCustConfirm", params);
		
		//로그
		Map<String, String> logParams = new HashMap<>();
		logParams.put("msg", "[업체]공고확인");
		logParams.put("biNo", CommonUtils.getString(params.get("biNo")));
		logParams.put("userId", userId);
		generalDao.insertGernal("bidStatus.insertTBiLog", logParams);
		
		return resultBody;
	}

	/**
	 * 입찰진행 상세
	 * @param param
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public ResultBody bidStatusDetail(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoCustUser> userOptional = tCoCustUserRepository.findById(principal.getUsername());
		int custCode = userOptional.get().getCustCode();
		params.put("custCode", custCode);
		
		Map<String, Object> detailMap = (Map<String, Object>) generalDao.selectGernalObject("bidStatus.selectPartnerEbidStatusDetail", params);
		
		// ************ 데이터 검색 -- 세부내역 ************
		if(CommonUtils.getString(detailMap.get("insMode")).equals("1")) {		//내역방식이 파일등록일 경우
			
			ArrayList<String> fileFlagArr = new ArrayList<String>();
			fileFlagArr.add("K");
			
			Map<String, Object> innerParams = new HashMap<String, Object>();
			innerParams.put("biNo", params.get("biNo"));
			innerParams.put("fileFlag", fileFlagArr);
			List<Object> specfile = generalDao.selectGernalList("bidStatus.selectEbidStatusDetailFile", innerParams);
			
			detailMap.put("spec_File", specfile);
			
		}else if(CommonUtils.getString(detailMap.get("insMode")).equals("2")) {		//내역방식이 직접입력일 경우
			List<Object> specInput = generalDao.selectGernalList("bidStatus.selectEbidStatusDetailSpec", params);
			
			detailMap.put("spec_Input", specInput);
		}
		
		// ************ 데이터 검색 -- 첨부파일 ************
		ArrayList<String> fileFlagArr = new ArrayList<String>();
		fileFlagArr.add("1");
		
		Map<String, Object> innerParams = new HashMap<String, Object>();
		innerParams.put("biNo", params.get("biNo"));
		innerParams.put("fileFlag", fileFlagArr);
		List<Object> fileList = generalDao.selectGernalList("bidStatus.selectEbidStatusDetailFile", innerParams);
		
		detailMap.put("file_list", fileList);
		
		resultBody.setData(detailMap);
			
		return resultBody;
	}
	
	/**
	 * 견적금액 단위 코드값
	 * @return
	 */
	public ResultBody currList() throws Exception {
		ResultBody resultBody = new ResultBody();
		
		List<Object> list = generalDao.selectGernalList("bidStatus.selectCodeRateList", null);
		resultBody.setData(list);
		
		return resultBody;
	}

	/**
	 * 투찰
	 * @param params
	 * @param detailFile
	 * @param etcFile
	 * @param user
	 * @return
	 */
	@Transactional
	@SuppressWarnings({ "unchecked" })
	public ResultBody bidSubmitting(@RequestBody Map<String, Object> params, MultipartFile detailFile, MultipartFile etcFile, CustomUserDetails user) throws Exception {

		ResultBody resultBody = new ResultBody();
		String userId = user.getUsername();
		int custCode = Integer.parseInt(user.getCustCode());//협력사 번호 
		String interrelatedCustCode = "";//입찰에 해당하는 계열사 번호
		String biNo = CommonUtils.getString(params.get("biNo"));//입찰번호
		String insModeCode = CommonUtils.getString(params.get("insModeCode"));//입력방식
		String amt = "";//견적금액
		LocalDateTime currentDate = LocalDateTime.now();//update 또는 insert 되는 현재시점
		
		//파일입력
		String fileName = "";//견적내역파일 이름
		String filePath = "";//견적내역파일 경로
		
		String etcFileName = "";//기타파일 이름
		String etcFilePath = "";//기타파일 경로
		
		//입찰정보를 조회하여 입찰을 한 계열사 조회
		Map<String, Object> infoParams = new HashMap<String, Object>();
		infoParams.put("columns", "EST_START_DATE, EST_CLOSE_DATE, INTERRELATED_CUST_CODE ");
		infoParams.put("biNo", biNo);
		Map<String, Object> biInfo = (Map<String, Object>) generalDao.selectGernalObject("bidStatus.selectTBiInfoMatInfomation", infoParams);
		
		if(biInfo != null) {
			
			//제출 시작일시, 마감일시 체크
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
			String estStartDateStr = CommonUtils.getString(biInfo.get("estStartDate"));
			LocalDateTime estStartDate = LocalDateTime.parse(estStartDateStr, formatter);
//			String estCloseDateStr = CommonUtils.getString(biInfo.get("estCloseDate"));
//			LocalDateTime estCloseDate = LocalDateTime.parse(estCloseDateStr, formatter);
			
			if(currentDate.isBefore(estStartDate)) {
				resultBody.setCode("LESSTIME");
				resultBody.setStatus(999);
				
				return resultBody;
			}
			
			//암호화에 필요한 계열사코드
			interrelatedCustCode = CommonUtils.getString(biInfo.get("interrelatedCustCode"));
			
		}
		
		//서명된 견적금액 데이터
		if (!StringUtils.isEmpty(params.get("amt"))) {
			amt = CommonUtils.getString(params.get("amt"));//총 견적금액

			if(resultBody.getCode().equals("ERROR")) {
				return resultBody;
			}
		}
		
		try {
			
			//파일입력방식인 경우 파일 업로드
			if(insModeCode.equals("1")) {
				//견적내역파일(필수)
				fileName = detailFile.getOriginalFilename();
				filePath = fileService.uploadFile(detailFile);//업로드
			}
			
			if(etcFile != null) {
				//기타파일
				etcFileName = etcFile.getOriginalFilename();
				etcFilePath = fileService.uploadFile(etcFile);//업로드
			}
		
		}catch(Exception e) {
			log.error("bidSubmitting fileUpload error : {}", e);
			resultBody.setCode("ERROR");
			resultBody.setStatus(999);
			
			return resultBody;
		}
			
		/* ===========================================암호화 주석 시작==========================================
		 * tradeSign 라이센스가 없음으로 암호화 부분 주석 처리
		 
		 
		//암호화
		try {
			
			if(amt != null && !amt.equals("")) {
				
				//입찰한 계열사의 인증서로 암호화
				ResultBody encryptResult = certificateService.encryptData(amt, interrelatedCustCode);//견적액 envelope 암호화
				if(encryptResult.getCode().equals("ERROR")) {//암호화 실패

					return encryptResult;
					
				}else {//암호화 성공
					amt = (String) encryptResult.getData();
				}
			}
			

		}catch(Exception e){
			log.error("encrypting error : {} ", e);
			resultBody.setCode("ERROR");
			resultBody.setStatus(999);
			
			return resultBody;
		}
		===========================================암호화 주석 끝==========================================
		*/
		
		//입찰파일테이블(t_bi_upload)에 insert
		if(insModeCode.equals("1")) {
			Map<String, Object> fileParams = new HashMap<String, Object>();
			
			fileParams.put("biNo", biNo);
			fileParams.put("fileFlag", "C");//(K : 세부내역, 0 : 첨부대내용, 1 : 첨부대외용, C : 업체투찰파일)
			fileParams.put("custCode", custCode);
			fileParams.put("fileNm", fileName);
			fileParams.put("filePath", filePath);
			fileParams.put("useYn", "Y");
			
			int fileId = generalDao.insertGernal("insertTBiUploadC", fileParams);
			
			params.put("fileId", fileId);
			
		}
	
		//입찰협력업체(t_bi_info_mat_cust)에 반영
		params.put("custCode", custCode);
		params.put("amt", amt);
		params.put("userId", userId);

		params.put("etcFileName", etcFileName);
		params.put("etcFilePath", etcFilePath);
		generalDao.insertGernal("bidStatus.mergedTBiInfoMatCust", params);
		
		
		//로그 남기기
		try {
			Map<String, String> logParams = new HashMap<>();
			logParams.put("msg", "[업체]견적제출");
			logParams.put("biNo", CommonUtils.getString(params.get("biNo")));
			logParams.put("userId", userId);
			generalDao.insertGernal("bidStatus.insertTBiLog", logParams);
		}catch(Exception e) {
			log.error("bidSubmitting log error : {}", e);
		}
			
		return resultBody;
	}
	
}
