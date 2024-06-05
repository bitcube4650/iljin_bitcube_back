package iljin.framework.ebid.bid.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.custom.entity.TCoCustUser;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.common.consts.DB;
import iljin.framework.ebid.etc.util.common.file.FileService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BidCompleteService {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private GeneralDao generalDao;
	
	@Autowired
	private TCoUserRepository tCoUserRepository;
	
	@Autowired
	private TCoCustUserRepository tCoCustUserRepository;
	
	@Autowired
	private FileService fileService;
	
	@Autowired
	Util util;
	
	/**
  	 * 그룹사 입찰완료 리스트
  	 * @param params : (String) biNo, (String) biName, (Boolean) succBi, (Boolean) failBi, (String) startDate, (String) endDate
  	 * @return
  	 */
	@SuppressWarnings({ "rawtypes" })
	public ResultBody complateBidList(Map<String, Object> params) throws Exception{
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		String userInterrelatedCustCode = userOptional.get().getInterrelatedCustCode();
		String userAuth = userOptional.get().getUserAuth();
		
		params.put("userId", userId);
		params.put("interrelatedCustCode", userInterrelatedCustCode);
		params.put("userAuth", userAuth);
		
		Page listPage = generalDao.selectGernalListPage(DB.QRY_SELECT_COMPLETE_EBID_LIST, params);
		resultBody.setData(listPage);
			
		return resultBody;
	}
	
	/**
  	 * 그룹사 입찰완료 상세
  	 * @param params : (String) biNo
  	 * @return
  	 */
	@SuppressWarnings({ "unchecked" })
	public ResultBody complateBidDetail(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		Map<String, Object> detailMap = new HashMap<String, Object>();

		// ************ 데이터 검색 -- 입찰참가업체, 세부내역, 첨부파일 제외 ************
		detailMap = (Map<String, Object>) generalDao.selectGernalObject(DB.QRY_SELECT_COMPLETE_EBID_DETAIL, params);
		
		// ************ 데이터 검색 -- 입찰참가업체 ************
		
		List<Object> custData = generalDao.selectGernalList(DB.QRY_SELECT_COMPLETE_EBID_JOIN_CUST_LIST, params);
		
		//내역방식이 직접등록일 경우
		if(CommonUtils.getString(detailMap.get("insMode")).equals("2")) {
			for(Object obj : custData) {
				
				Map<String, Object> custDto = (Map<String, Object>) obj;
				
				Map<String, Object> innerParams = new HashMap<String, Object>();
				innerParams.put("biNo", params.get("biNo"));
				innerParams.put("custCode", custDto.get("custCode"));
				
				List<Object> specDto = generalDao.selectGernalList(DB.QRY_SELECT_COMPLETE_EBID_JOIN_CUST_SPEC, innerParams);
				custDto.put("bid_Spec", specDto);
			}
		}
		
		detailMap.put("cust_List", custData);
		
		// ************ 데이터 검색 -- 세부내역 ************
		if(CommonUtils.getString(detailMap.get("insMode")).equals("1")) {		//내역방식이 파일등록일 경우
			ArrayList<String> fileFlagArr = new ArrayList<String>();
			fileFlagArr.add("K");
			
			Map<String, Object> innerParams = new HashMap<String, Object>();
			innerParams.put("biNo", params.get("biNo"));
			innerParams.put("fileFlag", fileFlagArr);
			
			List<Object> specfile = generalDao.selectGernalList(DB.QRY_SELECT_COMPLETE_EBID_DETAIL_FILE, innerParams);
			detailMap.put("spec_File", specfile);
			
		}else if(CommonUtils.getString(detailMap.get("insMode")).equals("2")) {		//내역방식이 직접입력일 경우
			List<Object> specInput = generalDao.selectGernalList(DB.QRY_SELECT_COMPLETE_EBID_DETAIL_SPEC, params);
			detailMap.put("spec_Input", specInput);
		}
		
		// ************ 데이터 검색 -- 첨부파일 ************
		ArrayList<String> fileFlagArr = new ArrayList<String>();
		fileFlagArr.add("0");
		fileFlagArr.add("1");
		
		Map<String, Object> innerParams = new HashMap<String, Object>();
		innerParams.put("biNo", params.get("biNo"));
		innerParams.put("fileFlag", fileFlagArr);
		
		List<Object> fileData = generalDao.selectGernalList(DB.QRY_SELECT_COMPLETE_EBID_DETAIL_FILE, innerParams);
		detailMap.put("file_List", fileData);
		
		resultBody.setData(detailMap);
		
		return resultBody;
	
	}
	
	/**
	 * 첨부파일 다운로드
	 * @param params : (String) fileId - file경로
	 * @return
	 */
	public ByteArrayResource fileDown(Map<String, Object> params) {
		
		String filePath = (String) params.get("fileId");
		ByteArrayResource fileResource = null;
		
		try {
			fileResource = fileService.downloadFile(filePath);
		} catch (Exception e) {
			log.error("fileDown error : {}", e);
		}
		
		return fileResource;
	}
	
	/**
	 * 실제계약금액 업데이트
	 * @param params : (String) biNo, (String) realAmt
	 * @return
	 */
	@Transactional
	public ResultBody updRealAmt(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		generalDao.updateGernal(DB.QRY_UPDATE_COMPLETE_EBID_REAL_AMT, params);
		return resultBody;
	}
	
	/**
	 * 낙찰 이력 롯데에너지머티리얼즈 코드값
	 * @param params
	 * @return
	 */
	public ResultBody lotteMatCode(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		params.put("colCode", "MAT_DEPT");
		resultMap.put("matDept", generalDao.selectGernalList(DB.QRY_SELECT_COMPLETE_EBID_LOTTE_MAT_CODE, params));
		
		params.put("colCode", "MAT_PROC");
		resultMap.put("matProc", generalDao.selectGernalList(DB.QRY_SELECT_COMPLETE_EBID_LOTTE_MAT_CODE, params));
		
		params.put("colCode", "MAT_CLS");
		resultMap.put("matCls", generalDao.selectGernalList(DB.QRY_SELECT_COMPLETE_EBID_LOTTE_MAT_CODE, params));
		
		resultBody.setData(resultMap);
		
		return resultBody;
		
	}
	
	/**
	 * 낙찰 이력 리스트
	 * @param params : (String) biNo, (String) biName, (String) matDept, (String) matProc, (String) matCls, (String) startDate, (String) endDate
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public ResultBody complateBidhistory(Map<String, Object> params) throws Exception{
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		String userInterrelatedCustCode = userOptional.get().getInterrelatedCustCode();
		String userAuth = userOptional.get().getUserAuth();
		
		params.put("userId", userId);
		params.put("interrelatedCustCode", userInterrelatedCustCode);
		params.put("userAuth", userAuth);
		
		Page listPage = generalDao.selectGernalListPage(DB.QRY_SELECT_COMPLETE_EBID_HISTORY_LIST, params);
		resultBody.setData(listPage);
			
		return resultBody;
		
	}
	
	/**
	 * 투찰 정보 팝업
	 * @param params : (String) biNo
	 * @return
	 */
	public ResultBody joinCustList(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		List<Object> list = generalDao.selectGernalList(DB.QRY_SELECT_EBID_HISTORY_JOIN_CUST_LIST, params);
		resultBody.setData(list);
		
		return resultBody;
	}
	
	/**
	 * 협력사 입찰완료 리스트
	 * @param params : (String) biNo, (String) biName, (String) succYn_Y, (String) succYn_N, (String) startDate, (String) endDate 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public ResultBody complateBidPartnerList(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoCustUser> userOptional = tCoCustUserRepository.findById(principal.getUsername());
		int custCode = userOptional.get().getCustCode();
		
		params.put("custCode", custCode);
		
		Page listPage = generalDao.selectGernalListPage(DB.QRY_SELECT_PARTNER_COMPLETE_EBID_LIST, params);
		resultBody.setData(listPage);
		
		return resultBody;
	}
	
	/**
  	 * 협력사 입찰완료 상세
  	 * @param params : (String) biNo
  	 * @return
  	 */
	@SuppressWarnings({ "unchecked" })
	public ResultBody complateBidPartnerDetail(Map<String, Object> params) throws Exception{
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoCustUser> userOptional = tCoCustUserRepository.findById(principal.getUsername());
		int custCode = userOptional.get().getCustCode();
		
		params.put("custCode", custCode);
		
		Map<String, Object> detailMap = new HashMap<String, Object>();
		
		// ************ 데이터 검색 -- 입찰참가업체, 세부내역, 첨부파일 제외 ************
		detailMap = (Map<String, Object>) generalDao.selectGernalObject(DB.QRY_SELECT_PARTNER_COMPLETE_EBID_DETAIL, params);
		
		// ************ 데이터 검색 -- 입찰참가업체 ************
		List<Object> custData = generalDao.selectGernalList(DB.QRY_SELECT_PARTNER_COMPLETE_EBID_CUST_DETAIL, params);
		
		//내역방식이 직접등록일 경우
		if(CommonUtils.getString(detailMap.get("insMode")).equals("2")) {
			for(Object obj : custData) {
				Map<String, Object> custDto = (Map<String, Object>) obj;
				Map<String, Object> innerParams = new HashMap<String, Object>();
				innerParams.put("biNo", params.get("biNo"));
				innerParams.put("custCode", custDto.get("custCode"));
				
				List<Object> specDto = generalDao.selectGernalList(DB.QRY_SELECT_COMPLETE_EBID_JOIN_CUST_SPEC, innerParams);
				custDto.put("bid_Spec",specDto);
			}
		}
		
		detailMap.put("cust_List", custData);
		
		// ************ 데이터 검색 -- 세부내역 ************
		if(CommonUtils.getString(detailMap.get("insMode")).equals("1")) {		//내역방식이 파일등록일 경우
			ArrayList<String> fileFlagArr = new ArrayList<String>();
			fileFlagArr.add("K");
			
			Map<String, Object> innerParams = new HashMap<String, Object>();
			innerParams.put("biNo", params.get("biNo"));
			innerParams.put("fileFlag", fileFlagArr);
			
			List<Object> specfile = generalDao.selectGernalList(DB.QRY_SELECT_COMPLETE_EBID_DETAIL_FILE, innerParams);
			detailMap.put("spec_File", specfile);
			
		}else if(CommonUtils.getString(detailMap.get("insMode")).equals("2")) {		//내역방식이 직접입력일 경우
			List<Object> specInput = generalDao.selectGernalList(DB.QRY_SELECT_COMPLETE_EBID_DETAIL_SPEC, params);
			detailMap.put("spec_Input", specInput);
		}
		
		// ************ 데이터 검색 -- 첨부파일 ************
		ArrayList<String> fileFlagArr = new ArrayList<String>();
		fileFlagArr.add("1");
		
		Map<String, Object> innerParams = new HashMap<String, Object>();
		innerParams.put("biNo", params.get("biNo"));
		innerParams.put("fileFlag", fileFlagArr);
		
		List<Object> fileData = generalDao.selectGernalList(DB.QRY_SELECT_COMPLETE_EBID_DETAIL_FILE, innerParams);
		detailMap.put("file_List", fileData);
		
		resultBody.setData(detailMap);
		
		return resultBody;
	
	}
	
	/**
	 * 협력사 낙찰승인
	 * @param params : (String) esmtYn, (String) biNo
	 * @return
	 */
	@Transactional
	public ResultBody updBiCustFlag(Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoCustUser> userOptional = tCoCustUserRepository.findById(principal.getUsername());
		int custCode = userOptional.get().getCustCode();
		String userId = userOptional.get().getUserId();
		
		params.put("custCode", custCode);
		params.put("userId", userId);
		
		//상태값 업데이트
		generalDao.updateGernal(DB.QRY_UPDATE_SUCC_EBID_CONFIRM, params);
			
		//log insert
		try {
			
			Map<String, Object> logParams = new HashMap<String, Object>();
			logParams.put("msg", "[업체]낙찰확인");
			logParams.put("biNo", params.get("biNo"));
			logParams.put("userId", userId);
			generalDao.insertGernal(DB.QRY_BID_STATUS_INSERT_T_BI_LOG, logParams);
			
		}catch(Exception e) {
			log.error("updBiCustFlag insert log error : {}", e);
		}
		
		return resultBody;
	}
}
