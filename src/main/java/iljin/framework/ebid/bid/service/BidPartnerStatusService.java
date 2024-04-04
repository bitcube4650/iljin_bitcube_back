package iljin.framework.ebid.bid.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.BidCompleteDetailDto;
import iljin.framework.ebid.bid.dto.BidItemSpecDto;
import iljin.framework.ebid.bid.dto.BidProgressDto;
import iljin.framework.ebid.bid.dto.BidProgressFileDto;
import iljin.framework.ebid.bid.dto.CurrDto;
import iljin.framework.ebid.bid.entity.TBiInfoMatCust;
import iljin.framework.ebid.bid.entity.TBiInfoMatCustID;
import iljin.framework.ebid.bid.entity.TBiInfoMatCustTemp;
import iljin.framework.ebid.bid.entity.TBiInfoMatCustTempID;
import iljin.framework.ebid.bid.entity.TBiLog;
import iljin.framework.ebid.bid.entity.TBiUpload;
import iljin.framework.ebid.bid.repository.TBiInfoMatCustRepository;
import iljin.framework.ebid.bid.repository.TBiInfoMatCustTempRepository;
import iljin.framework.ebid.custom.entity.TCoCustUser;
import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
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
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
@Slf4j
public class BidPartnerStatusService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    Util util;

    @Autowired
    private FileService fileService;
    
    @Autowired
    private TCoCustUserRepository tCoCustUserRepository; 
    
    @Autowired
    private TBiInfoMatCustRepository tBiInfoMatCustRepository;
    
    @Autowired
    private TBiInfoMatCustTempRepository tBiInfoMatCustTempRepository;

    @Value("${file.upload.directory}")
    private String uploadDirectory;

	/**
	 * 협력사 입찰진행
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResultBody statuslist(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		try {
			UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Optional<TCoCustUser> userOptional = tCoCustUserRepository.findById(principal.getUsername());
			int custCode = userOptional.get().getCustCode();
			
			StringBuilder sbCount = new StringBuilder(
					  "select count(1) "
					+ "from t_bi_info_mat tbim "
					+ "inner join t_co_cust_ir tcci "
					+ "	on tbim.INTERRELATED_CUST_CODE = tcci.INTERRELATED_CUST_CODE "
					+ "	and tcci.CUST_CODE = :custCode "
					+ "left outer join t_bi_info_mat_cust tbimc "
					+ "	on tbim.BI_NO = tbimc.BI_NO "
					+ "	and tbimc.CUST_CODE = :custCode "
			);
			StringBuilder sbList = new StringBuilder(
					  "select	tbim.BI_NO "
					+ ",		tbim.BI_NAME "
					+ ",		DATE_FORMAT(tbim.EST_START_DATE, '%Y-%m-%d %H:%i') AS est_start_date "
					+ ",		DATE_FORMAT(tbim.EST_CLOSE_DATE, '%Y-%m-%d %H:%i') AS est_close_date "
					+ ",		tbim.BI_MODE "
					+ ",		tbim.ING_TAG "
					+ ",		tbim.INS_MODE "
					+ ",		tcu.USER_NAME as DAMDANG_NAME "
					+ ",		tcu.USER_EMAIL as DAMDANG_EMAIL "
					+ ",		tbimc.ESMT_YN "
					+ ",		'' as temp "
					+ "from t_bi_info_mat tbim "
					+ "inner join t_co_cust_ir tcci "
					+ "	on tbim.INTERRELATED_CUST_CODE = tcci.INTERRELATED_CUST_CODE "
					+ "	and tcci.CUST_CODE = :custCode "
					+ "left outer join t_bi_info_mat_cust tbimc "
					+ "	on tbim.BI_NO = tbimc.BI_NO "
					+ "	and tbimc.CUST_CODE = :custCode "
					+ "left outer join t_co_user tcu "
					+ "	on tbim.CREATE_USER = tcu.USER_ID "
			);
			
			StringBuilder sbWhere = new StringBuilder();
			sbWhere.append("where 1=1 ");
			if (!StringUtils.isEmpty(params.get("bidNo"))) {
				sbWhere.append("and tbim.BI_NO = :bidNo ");
			}
	
			if (!StringUtils.isEmpty(params.get("bidName"))) {
				sbWhere.append("and tbim.BI_NAME like concat('%',:bidName,'%') ");
			}
			
			Boolean bidModeA = (Boolean) params.get("bidModeA");
			Boolean bidModeB = (Boolean) params.get("bidModeB");
			
			if (bidModeA && !bidModeB) {
				sbWhere.append("and tbim.BI_MODE = 'A' ");
			}else if(!bidModeA && bidModeB) {
				sbWhere.append("and tbim.BI_MODE = 'B' ");
			}else {
				sbWhere.append("and tbim.BI_MODE IN ('A', 'B') ");
			}
			
			Boolean noticeYn = (Boolean) params.get("noticeYn");
			Boolean participateYn = (Boolean) params.get("participateYn");
			Boolean rebidYn = (Boolean) params.get("rebidYn");
			
			if (noticeYn && !participateYn && !rebidYn) {
				sbWhere.append(
					  "and tbim.ING_TAG = 'A1' "
					+ "and (tbimc.ESMT_YN IS NULL or tbimc.ESMT_YN IN ('0', '1')) "
				);
			}else if(!noticeYn && participateYn && !rebidYn) {
				sbWhere.append(
					  "and tbim.ING_TAG = 'A1' "
					+ "and tbimc.ESMT_YN IN ('2') "
				);
			}else if(!noticeYn && !participateYn && rebidYn) {
				sbWhere.append(
					  "and tbim.ING_TAG = 'A3' "
					+ "and (tbimc.ESMT_YN IS NULL or tbimc.ESMT_YN IN ('0', '1')) "
				);
			}else if(noticeYn && participateYn && !rebidYn) {
				sbWhere.append(
					  "and tbim.ING_TAG = 'A1' "
				);
			}else if(noticeYn && !participateYn && rebidYn) {
				sbWhere.append(
					  "and tbim.ING_TAG IN ( 'A1', 'A3' ) "
					+ "and (tbimc.ESMT_YN IS NULL or tbimc.ESMT_YN IN ('0', '1')) "
				);
			}else if(!noticeYn && participateYn && rebidYn) {
				sbWhere.append(
					  "and ((tbim.ING_TAG = 'A1' and tbimc.ESMT_YN = '2') OR (tbim.ING_TAG = 'A3' and (tbimc.ESMT_YN IS NULL or tbimc.ESMT_YN IN ('0', '1'))) ) "
				);
			}else {
				sbWhere.append("and tbim.ING_TAG IN ('A1', 'A3') ");
			}
	
			sbList.append(sbWhere);
			sbCount.append(sbWhere);
	
			Query queryList = entityManager.createNativeQuery(sbList.toString());
			Query queryTotal = entityManager.createNativeQuery(sbCount.toString());
			queryList.setParameter("custCode", custCode);
			queryTotal.setParameter("custCode", custCode);
	
			if (!StringUtils.isEmpty(params.get("bidNo"))) {
				queryList.setParameter("bidNo", params.get("bidNo"));
				queryTotal.setParameter("bidNo", params.get("bidNo"));
			}
			if (!StringUtils.isEmpty(params.get("bidName"))) {
				queryList.setParameter("bidName", params.get("bidName"));
				queryTotal.setParameter("bidName", params.get("bidName"));
			}
	
			Pageable pageable = PagaUtils.pageable(params);
			queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
			List list = new JpaResultMapper().list(queryList, BidProgressDto.class);
	
			BigInteger count = (BigInteger) queryTotal.getSingleResult();
			Page listPage = new PageImpl(list, pageable, count.intValue());
			
			resultBody.setData(listPage);
			
		}catch(Exception e) {
			log.error("bidPartnerStatusService statuslist list error : {}", e);
			resultBody.setCode("999");
			resultBody.setMsg("입찰 진행 리스트를 가져오는것을 실패하였습니다.");	
		}
		return resultBody;
	}

	/**
	 * 협력사 공고확인 처리
	 * @param params
	 * @param user
	 * @return
	 */
	@Transactional
	public ResultBody checkBid(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoCustUser> userOptional = tCoCustUserRepository.findById(principal.getUsername());
		
		String userId = userOptional.get().getUserId();
		
		String biNo = CommonUtils.getString(params.get("biNo"));//입찰번호
		int custCode = userOptional.get().getCustCode();//협력사 번호 
		LocalDateTime currentDate = LocalDateTime.now();//update 되는 현재시점
		
		TBiInfoMatCustID tBiInfoMatCustId = new TBiInfoMatCustID();
		
		tBiInfoMatCustId.setBiNo(biNo);
		tBiInfoMatCustId.setCustCode(custCode);
		
		//입찰협력사(t_bi_info_mat_cust) 조회
		Optional<TBiInfoMatCust> optional = tBiInfoMatCustRepository.findById(tBiInfoMatCustId);
		
		if(optional.isPresent()) {//데이터가 있는 경우(지명경쟁 or 이미입찰했던 경우)
			
			//업체공고확인 처리
			TBiInfoMatCust tBiInfoMatCust = optional.get();
			String esmtYn = tBiInfoMatCust.getEsmtYn();
			if(esmtYn.equals("0") || esmtYn.equals("") || esmtYn == null) {//공고확인을 안한상태
				tBiInfoMatCust.setEsmtYn("1");//( 업체투찰 flag  0-업체지정, 1-업체공고확인, 2-업체투찰)
//				tBiInfoMatCust.setRebidAtt("N");//재투찰여부
				tBiInfoMatCust.setEsmtAmt(null);
				tBiInfoMatCust.setSuccYn("N");
				tBiInfoMatCust.setUpdateUser(userId);
				tBiInfoMatCust.setUpdateDate(currentDate);
				tBiInfoMatCust.setSubmitDate(null);
				tBiInfoMatCust.setFileId(null);
				tBiInfoMatCust.setEncQutn(null);
				tBiInfoMatCust.setEncEsmtSpec(null);
				tBiInfoMatCust.setFileHashValue(null);
				tBiInfoMatCust.setEtcBFile(null);
				tBiInfoMatCust.setEtcBFilePath(null);
				
				//로그 남기기
				TBiLog tBiLog = new TBiLog();
				
				tBiLog.setBiNo(biNo);
				tBiLog.setUserId(userId);
				tBiLog.setLogText("[업체]공고확인");
				tBiLog.setCreateDate(currentDate);
				
				entityManager.persist(tBiLog);
				
			}
			
		}
	
		return resultBody;
	}

	/**
	 * 입찰진행 상세
	 * @param param
	 * @return
	 */
	public ResultBody bidStatusDetail(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoCustUser> userOptional = tCoCustUserRepository.findById(principal.getUsername());
		int custCode = userOptional.get().getCustCode();
		
		BidCompleteDetailDto detailDto = null;
		
		String biNo = CommonUtils.getString(params.get("biNo"));
		
		try {
			// ************ 데이터 검색 -- 입찰참가업체, 세부내역, 첨부파일 제외 ************
			StringBuilder sbMainData = new StringBuilder(
				  "select	tbim.BI_NO "
				+ ",		tbim.BI_NAME "
				+ ",		tci.ITEM_NAME "
				+ ",		tbim.BI_MODE "
				+ ",		tcc.CODE_NAME as SUCC_DECI_METH "
				+ ",		tbim.BID_JOIN_SPEC "
				+ ",		DATE_FORMAT(tbim.SPOT_DATE, '%Y-%m-%d %H:%i') as SPOT_DATE "
				+ ",		tbim.SPOT_AREA "
				+ ",		tbim.SPECIAL_COND "
				+ ",		tbim.SUPPLY_COND "
				+ ",		tbim.AMT_BASIS "
				+ ",		tbim.PAY_COND "
				+ ",		tcu.USER_NAME as DAMDANG_NAME "
				+ ",		tcu.DEPT_NAME"
				+ ",		tbim.WHY_A3 "
				+ ",		tbim.WHY_A7 "
				+ ",		DATE_FORMAT(tbim.EST_START_DATE, '%Y-%m-%d %H:%i') as EST_START_DATE "
				+ ",		DATE_FORMAT(tbim.EST_CLOSE_DATE, '%Y-%m-%d %H:%i') as EST_CLOSE_DATE "
				+ ",		tbim.INS_MODE "
				+ ",		tbim.ADD_ACCEPT "
				+ ",		tbim.ING_TAG "
				+ ",		IFNULL((select REBID_ATT from t_bi_info_mat_cust tbimc where tbimc.BI_NO = :biNo and tbimc.CUST_CODE = :custCode), 'N') as CUST_REBID_YN "
				+ ",		IFNULL((select ESMT_YN from t_bi_info_mat_cust tbimc where tbimc.BI_NO = :biNo and tbimc.CUST_CODE = :custCode), '1') as CUST_ESMT_YN "
				+ ",		IFNULL((select DATE_FORMAT(tbimc.UPDATE_DATE, '%Y-%m-%d %H:%i') from t_bi_info_mat_cust tbimc where tbimc.BI_NO = :biNo and tbimc.CUST_CODE = :custCode), '') as CUST_ESMT_UPDATE_DATE "
				+ "from t_bi_info_mat tbim "
				+ "left outer join t_co_user tcu "
				+ "	on tbim.GONGO_ID = tcu.USER_ID "
				+ "left outer join t_co_item tci  "
				+ "	on tbim.ITEM_CODE = tci.ITEM_CODE "
				+ "left outer join t_co_code tcc  "
				+ "	on tbim.SUCC_DECI_METH = tcc.CODE_VAL "
				+ " and tcc.COL_CODE = 'T_CO_SUCC_METHOD'"
			);
			
			//조건문 쿼리 삽입
			StringBuilder sbMainWhere = new StringBuilder();
			sbMainWhere.append("where tbim.BI_NO = :biNo ");
			sbMainData.append(sbMainWhere);
			
			//쿼리 실행
			Query queryMain = entityManager.createNativeQuery(sbMainData.toString());
			
			//조건 대입
			queryMain.setParameter("biNo", biNo);
			queryMain.setParameter("custCode", custCode);
			
			detailDto = new JpaResultMapper().uniqueResult(queryMain, BidCompleteDetailDto.class);
			
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
						+ ",		tbsm.ORDER_QTY "
						+ ",		tbsm.SEQ "
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
				+ "and tbu.FILE_FLAG = '1' "
			);
		
			//조건문 쿼리 삽입
			StringBuilder sbFileWhere = new StringBuilder();
			sbFileWhere.append("and tbu.BI_NO = :biNo ");
			sbFileData.append(sbFileWhere);
			
			//쿼리 실행
			Query queryFile = entityManager.createNativeQuery(sbFileData.toString());
			
			//조건 대입
			queryFile.setParameter("biNo", biNo);
			
			List<BidProgressFileDto> fileData = new JpaResultMapper().list(queryFile, BidProgressFileDto.class);
			
			detailDto.setFileList(fileData);
			
			resultBody.setData(detailDto);
			
		}catch(Exception e) {
			log.error("bidStatusDetail error : {}", e);
			resultBody.setCode("999");
			resultBody.setMsg("입찰진행 상세 데이터를 가져오는것을 실패하였습니다.");
		}
		
		return resultBody;
	}
	
	/**
	 * 견적금액 단위 코드값
	 * @return
	 */
	public ResultBody currList() {
		ResultBody resultBody = new ResultBody();
		
		try {
			StringBuilder currlist = new StringBuilder(
					"SELECT code_val, code_name from t_co_code where col_code = 'T_CO_RATE' order by SORT_NO asc ");
			Query currlistQ = entityManager.createNativeQuery(currlist.toString());
			List<CurrDto> list = new JpaResultMapper().list(currlistQ, CurrDto.class);
			
			resultBody.setData(list);
		}catch(Exception e) {
			resultBody.setCode("fail");
		}
		
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
	public ResultBody bidSubmitting(@RequestBody Map<String, Object> params, MultipartFile detailFile, MultipartFile etcFile, CustomUserDetails user) {

		ResultBody resultBody = new ResultBody();
		String userId = user.getUsername();
		int custCode = Integer.parseInt(user.getCustCode());//협력사 번호 
		String biNo = CommonUtils.getString(params.get("biNo"));//입찰번호
		String rebidAtt = "N";//업체 재투찰 여부
		String insModeCode = CommonUtils.getString(params.get("insModeCode"));//입력방식
		String amt = "";//총 견적금액
		LocalDateTime currentDate = LocalDateTime.now();//update 또는 insert 되는 현재시점
		
		//직접입력
		List<Map<String, Object>> itemList = null;//직접입력 품목
		String strItemList = "";//직접입력 품목당 가격을 "‡" 구분자와 같이 넣은 문자열
		
		//파일입력
		int fileId = 0;//t_bi_upload에 insert한 견적내역파일 id
		String fileHashValue = "";//파일입력의 경우 복호화때 필요한 key값
		String fileName = "";//견적내역파일 이름
		String filePath = "";//견적내역파일 경로
		
		String etcFileName = "";//기타파일 이름
		String etcFilePath = "";//기타파일 경로
		
		if (!StringUtils.isEmpty(params.get("biNo"))) {
			biNo = CommonUtils.getString(params.get("biNo"));//입찰번호
		}
		
		if (!StringUtils.isEmpty(params.get("insModeCode"))) {
			insModeCode = CommonUtils.getString(params.get("insModeCode"));//입력방식
		}
		//입력방식 파일등록일 때 총 견적금액
		if (!StringUtils.isEmpty(params.get("amt"))) {
			amt = CommonUtils.getString(params.get("amt"));//총 견적금액
		}
		
		if (!StringUtils.isEmpty(params.get("submitData"))) {
			itemList = (List<Map<String, Object>>) params.get("submitData");//직접입력 품목
			
			for(int i = 0; i < itemList.size(); i++) {
				Map<String,Object> item = itemList.get(i);
				
				int seq = CommonUtils.getInt(item.get("seq"));
				String esmtUc = CommonUtils.getString(item.get("esmtUc"));
				
				if(i > 0) {//구분자
					strItemList += "‡";
				}
				//품목순번 = 가격
				strItemList += (seq + "=" + esmtUc);
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
			e.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("An error occurred while uploading file.");
			resultBody.setData(e.getMessage()); 
			
			return resultBody;
		}
			
		
		
		//암호화
		//여기에 암호화 처리
		//strItemList
		//amt
		//filePath


		try {
			
			//입찰협력업체차수(t_bi_info_mat_cust_temp)에 이미 1차로 투찰했던 이력이 있는지 확인하고 
			//1차로 투찰했던 이력이 있으면 업체재투찰여부(rebid_att)는 "Y"
			TBiInfoMatCustTempID tBiInfoMatCustTempID = new TBiInfoMatCustTempID(); 
			tBiInfoMatCustTempID.setBiNo(biNo);
			tBiInfoMatCustTempID.setCustCode(custCode);
			tBiInfoMatCustTempID.setBiOrder(1);
			
			Optional<TBiInfoMatCustTemp> tBiInfoMatCustTemp = tBiInfoMatCustTempRepository.findById(tBiInfoMatCustTempID);
			
			if(tBiInfoMatCustTemp.isPresent()) {
				rebidAtt = "Y";
			}
			
			
			//파일등록 방식인 경우 견적내역파일을 입찰파일(t_bi_upload)에 반영
			//기타파일의 경우 입찰협력업체(t_bi_info_mat_cust)에 반영
			if(insModeCode.equals("1")) {
				
				//입찰파일테이블(t_bi_upload)에 insert
				TBiUpload tBiUpload = new TBiUpload();
				tBiUpload.setBiNo(biNo);
				tBiUpload.setFileFlag("C");//(K : 세부내역, 0 : 첨부대내용, 1 : 첨부대외용, C : 업체투찰파일)
				tBiUpload.setFCustCode(custCode);
				tBiUpload.setFileNm(fileName);
				tBiUpload.setFilePath(filePath);
				tBiUpload.setCreateDate(currentDate);
				tBiUpload.setUseYn("Y");
				
				entityManager.persist(tBiUpload);
				
				fileId = tBiUpload.getFileId();//t_bi_upload에 insert한 견적내역파일 id
				
			}
			
			
			//입찰협력업체(t_bi_info_mat_cust)에 반영
			TBiInfoMatCustID tBiInfoMatCustId = new TBiInfoMatCustID();
			
			tBiInfoMatCustId.setBiNo(biNo);
			tBiInfoMatCustId.setCustCode(custCode);
			
			Optional<TBiInfoMatCust> optional = tBiInfoMatCustRepository.findById(tBiInfoMatCustId);

			//입찰_협력업체 테이블에 데이터가 들어가 있는지 확인
			if(optional.isPresent()) {//이미 입찰_협력업체 테이블에 데이터가 있는 경우 update(지명경쟁 or 이미투찰함)

				TBiInfoMatCust tBiInfoMatCust = optional.get();
				
				tBiInfoMatCust.setEsmtYn("2");//esmt_yn( 업체투찰 flag  0-업체지정, 1-업체공고확인, 2-업체투찰)
				tBiInfoMatCust.setUpdateUser(userId);
				tBiInfoMatCust.setUpdateDate(currentDate);
				tBiInfoMatCust.setEncQutn(amt);
				tBiInfoMatCust.setRebidAtt(rebidAtt);
				
				//기타파일
				if(!etcFilePath.equals("") &&  etcFilePath != null) {//기타첨부 파일이 있는 경우
					
					tBiInfoMatCust.setEtcBFile(etcFileName);
					tBiInfoMatCust.setEtcBFilePath(etcFilePath);
					
				}
				
				if(insModeCode.equals("1")) {//파일등록 방식
					
					//견적내역파일
					tBiInfoMatCust.setFileId(fileId);
					tBiInfoMatCust.setFileHashValue(fileHashValue);
					
				
				}else {//내역직접 방식
					tBiInfoMatCust.setEncEsmtSpec(strItemList);
				}
				
				
			}else {//입찰_협력업체 테이블에 데이터가 없는 경우 insert

				TBiInfoMatCust tBiInfoMatCust = new TBiInfoMatCust();
				
				tBiInfoMatCust.setBiNo(biNo);
				tBiInfoMatCust.setCustCode(custCode);
				tBiInfoMatCust.setEsmtYn("2");
				tBiInfoMatCust.setCreateUser(userId);
				tBiInfoMatCust.setCreateDate(currentDate);
				tBiInfoMatCust.setBiOrder(1);
				tBiInfoMatCust.setEncQutn(amt);
				tBiInfoMatCust.setRebidAtt(rebidAtt);
				
				//기타파일
				if(!etcFilePath.equals("") &&  etcFilePath != null) {//기타첨부 파일이 있는 경우
					
					tBiInfoMatCust.setEtcBFile(etcFileName);
					tBiInfoMatCust.setEtcBFilePath(etcFilePath);
					
				}
				
				if(insModeCode.equals("1")) {//파일등록 방식
					
					//견적내역파일
					tBiInfoMatCust.setFileId(fileId);
					tBiInfoMatCust.setFileHashValue(fileHashValue);
					
				}else {//내역직접 방식
					tBiInfoMatCust.setEncEsmtSpec(strItemList);
				}
				
				entityManager.persist(tBiInfoMatCust);
				
			}
			
			//로그 남기기
			TBiLog tBiLog = new TBiLog();
			
			tBiLog.setBiNo(biNo);
			tBiLog.setUserId(userId);
			tBiLog.setLogText("[업체]견적제출");
			tBiLog.setCreateDate(currentDate);
			
			entityManager.persist(tBiLog);
			
		}catch(Exception e) {
			e.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("An error occurred while suggesting bid.");
			resultBody.setData(e.getMessage()); 
			
			return resultBody;
		}
			
		return resultBody;
	}
}
