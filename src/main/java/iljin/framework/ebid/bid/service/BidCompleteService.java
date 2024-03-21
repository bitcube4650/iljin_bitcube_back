package iljin.framework.ebid.bid.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.BidCompleteCustDto;
import iljin.framework.ebid.bid.dto.BidCompleteDetailDto;
import iljin.framework.ebid.bid.dto.BidCompleteDto;
import iljin.framework.ebid.bid.dto.BidCompleteSpecDto;
import iljin.framework.ebid.bid.dto.BidProgressFileDto;
import iljin.framework.ebid.custom.entity.TCoCustUser;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.PagaUtils;
import iljin.framework.ebid.etc.util.common.file.FileService;
import lombok.extern.slf4j.Slf4j;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class BidCompleteService {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private TCoUserRepository tCoUserRepository;
	
	@Autowired
	private FileService fileService;
	
	@Autowired
	Util util;
	
	/**
  	 * 그룹사 입찰완료 리스트
  	 * @param params
  	 * @return
  	 */
//	@Transactional
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Page complateBidList(Map<String, Object> params) {
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		String userInterrelatedCustCode = userOptional.get().getInterrelatedCustCode();
		
		try {
			StringBuilder sbCount = new StringBuilder(
				  "select	count(1) "
				+ "from t_bi_info_mat tbim "
			);
			
			StringBuilder sbList = new StringBuilder(
				  "select	tbim.BI_NO "
				+ ", tbim.BI_NAME "
				+ ", DATE_FORMAT(tbim.UPDATE_DATE, '%Y-%m-%d %H:%i') as UPDATE_DATE "
				+ ", tbim.BI_MODE "
				+ ", tbim.ING_TAG "
				+ ", tbim.INS_MODE "
				+ ", tcu.USER_NAME "
				+ ", tcu.USER_EMAIL "
				+ "from t_bi_info_mat tbim "
				+ "left outer join t_co_user tcu "
				+ "	on tbim.CREATE_USER = tcu.USER_ID "
			);
			
			//조건문 쿼리 삽입
			StringBuilder sbWhere = new StringBuilder();
			sbWhere.append("where 1=1 ");
			sbWhere.append(
				  "AND tbim.INTERRELATED_CUST_CODE = :interrelatedCustCode "		//계열사 코드
				+ "AND (tbim.CREATE_USER = :userId "		//담당자
				+ "or tbim.OPEN_ATT1 = :userId "		//입회자1
				+ "or tbim.OPEN_ATT2 = :userId "		//입회자2
				+ "or tbim.EST_OPENER = :userId "		//개찰자
				+ "or tbim.EST_BIDDER = :userId "		//낙찰자
				+ "or tbim.GONGO_ID = :userId) "		//공고자
			);
			
			//입찰완료일
			sbWhere.append("and tbim.UPDATE_DATE BETWEEN :startDate and :endDate ");
			
			//입찰번호
			if (!StringUtils.isEmpty(params.get("biNo"))) {
				sbWhere.append("and tbim.BI_NO = :biNo ");
			}
			//입찰명
			if (!StringUtils.isEmpty(params.get("biName"))) {
				sbWhere.append("and tbim.BI_NAME like concat('%',:biName,'%') ");
			}
			
			//입찰완료상태
			Boolean succBi = (Boolean) params.get("succBi");
			Boolean failBi = (Boolean) params.get("failBi");
			if (succBi && failBi) {
				sbWhere.append("and tbim.ING_TAG IN ('A5', 'A7') ");
			}else if (!succBi && failBi) {
				sbWhere.append("and tbim.ING_TAG IN ('A7') ");
			}else if (succBi && !failBi) {
				sbWhere.append("and tbim.ING_TAG IN ('A5') ");
			}
			
			sbCount.append(sbWhere);
			sbList.append(sbWhere);
			
			sbList.append("order by tbim.UPDATE_DATE desc ");
			
			//쿼리 실행
			Query queryList = entityManager.createNativeQuery(sbList.toString());
			Query queryTotal = entityManager.createNativeQuery(sbCount.toString());
			
			//조건 대입
			queryList.setParameter("interrelatedCustCode", userInterrelatedCustCode);
			queryTotal.setParameter("interrelatedCustCode", userInterrelatedCustCode);
			queryList.setParameter("userId", userId);
			queryTotal.setParameter("userId", userId);
			
			queryList.setParameter("startDate", params.get("startDate") + " 00:00:00");
			queryList.setParameter("endDate", params.get("endDate") + " 23:59:59");
			queryTotal.setParameter("startDate", params.get("startDate") + " 00:00:00");
			queryTotal.setParameter("endDate", params.get("endDate") + " 23:59:59");
			
			if (!StringUtils.isEmpty(params.get("biNo"))) {
				queryList.setParameter("biNo", params.get("biNo"));
				queryTotal.setParameter("biNo", params.get("biNo"));
			}
			
			if (!StringUtils.isEmpty(params.get("biName"))) {
				queryList.setParameter("biName", params.get("biName"));
				queryTotal.setParameter("biName", params.get("biName"));
			}
			
			Pageable pageable = PagaUtils.pageable(params);
			queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
			List list = new JpaResultMapper().list(queryList, BidCompleteDto.class);
			
			BigInteger count = (BigInteger) queryTotal.getSingleResult();
			return new PageImpl(list, pageable, count.intValue());
			
		}catch(Exception e) {
			log.error("bidComplete list error : {}", e);
			return new PageImpl(new ArrayList<>());
		}
	}
	
	/**
  	 * 그룹사 입찰완료 상세
  	 * @param params
  	 * @return
  	 */
	@Transactional
	public ResultBody complateBidDetail(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		BidCompleteDetailDto detailDto = null;
		
		try {
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
				+ ",		tcu.USER_NAME as DAMDANG_NAME "
				+ ",		DATE_FORMAT(tbim.EST_START_DATE, '%Y-%m-%d %H:%i') as EST_START_DATE "
				+ ",		DATE_FORMAT(tbim.EST_CLOSE_DATE, '%Y-%m-%d %H:%i') as EST_CLOSE_DATE "
				+ ",		tcu3.USER_NAME as EST_OPENER "
				+ ",		tcu4.USER_NAME as EST_BIDDER "
				+ ",		tcu.USER_NAME as GONGO_NAME "
				+ ",		tcu1.USER_NAME as OPEN_ATT1 "
				+ ",		tcu2.USER_NAME as OPEN_ATT2 "
				+ ",		tbim.INS_MODE "
				+ ",		tbim.SUPPLY_COND "
				+ ",		tbim.WHY_A3 "
				+ ",		tbim.WHY_A7 "
				+ ",		tbim.ADD_ACCEPT "
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
				+ " and tcc.COL_CODE = 'T_CO_SUCC_METHOD'"
			);
			
			//조건문 쿼리 삽입
			StringBuilder sbMainWhere = new StringBuilder();
			sbMainWhere.append("where tbim.BI_NO = :biNo");
			sbMainData.append(sbMainWhere);
			
			//쿼리 실행
			Query queryMain = entityManager.createNativeQuery(sbMainData.toString());
			
			//조건 대입
			queryMain.setParameter("biNo", params.get("biNo"));
			
			detailDto = new JpaResultMapper().uniqueResult(queryMain, BidCompleteDetailDto.class);
			
			// ************ 데이터 검색 -- 입찰참가업체 ************
			StringBuilder sbCustData = new StringBuilder(
				  "select	tbimc.BI_NO "
				+ ",		cast(tbimc.CUST_CODE as char) as CUST_CODE "
				+ ",		tccm.CUST_NAME "
				+ ",		tccm.PRES_NAME "
				+ ",		tcc.CODE_NAME as ESMT_CURR "
				+ ",		tbimc.ESMT_AMT "
				+ ",		DATE_FORMAT(tbimc.SUBMIT_DATE, '%Y-%m-%d %H:%i') as SUBMIT_DATE "
				+ ",		(select tccu.USER_NAME from t_co_cust_user tccu where tccu.CUST_CODE = tbimc.CUST_CODE AND tccu.USER_TYPE = '1' LIMIT 1) AS DAMDANG_NAME "
				+ ",		case when tbimc.SUCC_YN = 'Y' then DATE_FORMAT(tbimc.UPDATE_DATE, '%Y-%m-%d %H:%i') else '' end as UPDATE_DATE "
				+ ",		tbimc.ESMT_YN "
				+ ",		tbu.FILE_NM "
				+ ",		tbu.FILE_PATH "
				+ ",		tbimc.SUCC_YN "
				+ ",		tbimc.ETC_B_FILE as ETC_FILE "
				+ ",		tbimc.ETC_B_FILE_PATH as ETC_PATH "
				+ "from t_bi_info_mat_cust tbimc "
				+ "inner join t_co_cust_master tccm "
				+ "	on tbimc.CUST_CODE = tccm.CUST_CODE "
				+ "left outer join t_co_code tcc "
				+ "	on tcc.COL_CODE = 'T_CO_RATE' "
				+ "	and tbimc.ESMT_CURR = tcc.CODE_VAL "
				+ "left outer join t_bi_upload tbu "
				+ "	on tbimc.FILE_ID = tbu.FILE_ID "
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
			
			List<BidCompleteCustDto> custData = new JpaResultMapper().list(queryCust, BidCompleteCustDto.class);
			
			//내역방식이 직접등록일 경우
			if(detailDto.getInsMode().equals("2")) {
				for(BidCompleteCustDto custDto : custData) {
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
			log.error("complateBidDetail error : {}", e);
			resultBody.setStatus(999);
			resultBody.setMsg("입찰완료 상세 데이터를 가져오는것을 실패하였습니다.");
		}
		
		return resultBody;
	
	}
	
	/**
	 * 첨부파일 다운로드
	 * @param params
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
	 * @param params
	 * @return
	 */
	@Transactional
	public ResultBody updRealAmt(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		try {
			StringBuilder sbQuery = new StringBuilder(
			  "UPDATE T_BI_INFO_MAT "
			+ "SET REAL_AMT = :realAmt "
			+ "WHERE bi_no = :biNo"
			);

			Query query = entityManager.createNativeQuery(sbQuery.toString());
			query.setParameter("realAmt", params.get("realAmt"));
			query.setParameter("biNo", params.get("biNo"));
			query.executeUpdate();

		}catch(Exception e) {
			log.error("updRealAmt error : {}", e);
			resultBody.setStatus(999);
			resultBody.setMsg("실제계약금액 업데이트를 실패했습니다.");
		}
		return resultBody;
	}
	
}
