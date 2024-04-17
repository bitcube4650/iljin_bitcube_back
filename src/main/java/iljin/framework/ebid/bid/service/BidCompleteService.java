package iljin.framework.ebid.bid.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.BidCustDto;
import iljin.framework.ebid.bid.dto.BidCompleteDetailDto;
import iljin.framework.ebid.bid.dto.BidCompleteDto;
import iljin.framework.ebid.bid.dto.BidItemSpecDto;
import iljin.framework.ebid.bid.dto.BidProgressFileDto;
import iljin.framework.ebid.bid.dto.CurrDto;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.math.BigInteger;
import java.util.HashMap;
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResultBody complateBidList(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
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
			Page listPage = new PageImpl(list, pageable, count.intValue());
			resultBody.setData(listPage);
			
		}catch(Exception e) {
			log.error("bidComplete list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰 완료 리스트를 가져오는것을 실패하였습니다.");	
		}
		
		return resultBody;
	}
	
	/**
  	 * 그룹사 입찰완료 상세
  	 * @param params : (String) biNo
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
				+ ",		tbim.REAL_AMT "
				+ ",		tbim.CREATE_USER "
				+ ",		tcu.USER_NAME as DAMDANG_NAME "
				+ ",		DATE_FORMAT(tbim.EST_START_DATE, '%Y-%m-%d %H:%i') as EST_START_DATE "
				+ ",		DATE_FORMAT(tbim.EST_CLOSE_DATE, '%Y-%m-%d %H:%i') as EST_CLOSE_DATE "
				+ ",		DATE_FORMAT(tbim.EST_OPEN_DATE, '%Y-%m-%d %H:%i') as EST_OPEN_DATE "
				+ ",		tcu3.USER_NAME as EST_OPENER "
				+ ",		tcu4.USER_NAME as EST_BIDDER "
				+ ",		tbim.GONGO_ID "
				+ ",		tcu5.USER_NAME as GONGO_NAME "
				+ ",		tcu1.USER_NAME as OPEN_ATT1 "
				+ ",		tcu2.USER_NAME as OPEN_ATT2 "
				+ ",		tbim.INS_MODE "
				+ ",		tbim.SUPPLY_COND "
				+ ",		tbim.WHY_A3 "
				+ ",		tbim.WHY_A7 "
				+ ",		tbim.ADD_ACCEPT "
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
				+ "	on tbim.CREATE_USER = tcu.USER_ID "
				+ "left outer join t_co_user tcu1 "
				+ "	on tbim.OPEN_ATT1 = tcu1.USER_ID "
				+ "left outer join t_co_user tcu2 "
				+ "	on tbim.OPEN_ATT2 = tcu2.USER_ID "
				+ "left outer join t_co_user tcu3 "
				+ "	on tbim.EST_OPENER = tcu3.USER_ID "
				+ "left outer join t_co_user tcu4 "
				+ "	on tbim.EST_BIDDER = tcu4.USER_ID "
				+ "left outer join t_co_user tcu5 "
				+ "	on tbim.GONGO_ID = tcu5.USER_ID "
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
			
			detailDto = new JpaResultMapper().uniqueResult(queryMain, BidCompleteDetailDto.class);
			
			// ************ 데이터 검색 -- 입찰참가업체 ************
			StringBuilder sbCustData = new StringBuilder(
				  "select	tbimc.BI_NO "
				+ ",		tbimc.CUST_CODE "
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
				+ "	and tbu.FILE_FLAG = 'C' "
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
			queryFile.setParameter("biNo", params.get("biNo"));
			
			List<BidProgressFileDto> fileData = new JpaResultMapper().list(queryFile, BidProgressFileDto.class);
			
			detailDto.setFileList(fileData);
			
			resultBody.setData(detailDto);
			
		}catch(Exception e) {
			log.error("complateBidDetail error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰완료 상세 데이터를 가져오는것을 실패하였습니다.");
		}
		
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
	public ResultBody updRealAmt(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		try {
			StringBuilder sbQuery = new StringBuilder(
			  "UPDATE T_BI_INFO_MAT "
			+ "SET REAL_AMT = :realAmt "
			+ "WHERE BI_NO = :biNo"
			);

			Query query = entityManager.createNativeQuery(sbQuery.toString());
			query.setParameter("realAmt", params.get("realAmt"));
			query.setParameter("biNo", params.get("biNo"));
			query.executeUpdate();

		}catch(Exception e) {
			log.error("updRealAmt error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("실제계약금액 업데이트를 실패했습니다.");
		}
		return resultBody;
	}
	
	/**
	 * 입찰 이력 롯데에너지머티리얼즈 코드값
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public ResultBody lotteMatCode(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		try {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			
			StringBuilder sbList = new StringBuilder(
				  "select	tcc.CODE_VAL as codeVal "
				+ ",		tcc.CODE_NAME as codeName "
				+ "from t_co_code tcc "
			);
			
			//조건문 쿼리 삽입
			StringBuilder sbWhere = new StringBuilder();
			sbWhere.append("where tcc.COL_CODE = :colCode ");
			sbWhere.append("and tcc.USEYN = 'Y' ");
			
			sbList.append(sbWhere);
			
			sbList.append("order by tcc.SORT_NO asc");
			
			//쿼리 실행 - mat_dept
			Query queryList1 = entityManager.createNativeQuery(sbList.toString());
			
			//조건 대입
			queryList1.setParameter("colCode", "MAT_DEPT");
			
			List dept = new JpaResultMapper().list(queryList1, CurrDto.class);
			resultMap.put("matDept", dept);
			
			//쿼리 실행 - mat_proc
			Query queryList2 = entityManager.createNativeQuery(sbList.toString());
			
			//조건 대입
			queryList2.setParameter("colCode", "MAT_PROC");
			
			List proc = new JpaResultMapper().list(queryList2, CurrDto.class);
			resultMap.put("matProc", proc);
			
			//쿼리 실행 - mat_proc
			Query queryList3 = entityManager.createNativeQuery(sbList.toString());
			
			//조건 대입
			queryList3.setParameter("colCode", "MAT_CLS");
			
			List cls = new JpaResultMapper().list(queryList3, CurrDto.class);
			resultMap.put("matCls", cls);
			
			resultBody.setData(resultMap);
			
		}catch(Exception e) {
			log.error("lotteMatCode list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("코드값을 가져오는것을 실패하였습니다.");
		}
		
		return resultBody;
		
	}
	
	/**
	 * 입찰 이력 리스트
	 * @param params : (String) biNo, (String) biName, (String) matDept, (String) matProc, (String) matCls, (String) startDate, (String) endDate
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResultBody complateBidhistory(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		String userInterrelatedCustCode = userOptional.get().getInterrelatedCustCode();
		
		try {
			StringBuilder sbCount = new StringBuilder(
				  "select	count(1) "
				+ "from t_bi_info_mat tbim "
				+ "inner join t_bi_info_mat_cust tbimc "
				+ "	on tbim.BI_NO = tbimc.BI_NO "
				+ "	and tbimc.SUCC_YN = 'Y' "
				+ "inner join t_co_cust_master tccm "
				+ "	on tbimc.CUST_CODE = tccm.CUST_CODE "
			);
			
			StringBuilder sbList = new StringBuilder(
				  "select	tbim.BI_NO "
				+ ",		codeMd.CODE_NAME as MAT_DEPT "
				+ ",		codeMp.CODE_NAME as MAT_PROC "
				+ ",		codeMc.CODE_NAME as MAT_CLS "
				+ ",		tbim.MAT_FACTORY "
				+ ",		tbim.MAT_FACTORY_LINE "
				+ ",		tbim.MAT_FACTORY_CNT "
				+ ",		tbim.BI_NAME "
				+ ",		tbim.BD_AMT "
				+ ",		tbim.SUCC_AMT "
				+ ",		tccm.CUST_NAME "
				+ ",		c.CNT as JOIN_CUST_CNT "
				+ ",		DATE_FORMAT(tbim.EST_START_DATE, '%Y-%m-%d %H:%i') as EST_START_DATE "
				+ ",		DATE_FORMAT(tbim.EST_CLOSE_DATE, '%Y-%m-%d %H:%i') as EST_CLOSE_DATE "
				+ ",		tcu.USER_NAME "
				+ "from t_bi_info_mat tbim "
				+ "inner join t_bi_info_mat_cust tbimc "
				+ "	on tbim.BI_NO = tbimc.BI_NO "
				+ "	and tbimc.SUCC_YN = 'Y' "
				+ "inner join t_co_cust_master tccm "
				+ "	on tbimc.CUST_CODE = tccm.CUST_CODE "
				+ "inner join ( "
				+ "	select	tbimc.BI_NO "
				+ "	,		COUNT(1) as CNT "
				+ "	from t_bi_info_mat_cust tbimc "
				+ "	where tbimc.ESMT_YN in('2', '3') "
				+ "	group by tbimc.BI_NO "
				+ ") c "
				+ "	on tbim.BI_NO = c.BI_NO "
				+ "left outer join t_co_user tcu "
				+ "	on tbim.CREATE_USER = tcu.USER_ID "
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
			StringBuilder sbWhere = new StringBuilder();
			sbWhere.append("where tbim.ING_TAG = 'A5' ");
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
			//롯데에너지머티리얼즈 분류군 - 사업부
			if (!StringUtils.isEmpty(params.get("matDept"))) {
				sbWhere.append("and tbim.MAT_DEPT = :matDept ");
			}
			//롯데에너지머티리얼즈 분류군 - 공정
			if (!StringUtils.isEmpty(params.get("matProc"))) {
				sbWhere.append("and tbim.MAT_PROC = :matProc ");
			}
			//롯데에너지머티리얼즈 분류군 - 분류
			if (!StringUtils.isEmpty(params.get("matCls"))) {
				sbWhere.append("and tbim.MAT_CLS = :matCls ");
			}
			
			sbCount.append(sbWhere);
			sbList.append(sbWhere);
			
			sbList.append("order by tbim.EST_CLOSE_DATE desc ");
			
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
			if (!StringUtils.isEmpty(params.get("matDept"))) {
				queryList.setParameter("matDept", params.get("matDept"));
				queryTotal.setParameter("matDept", params.get("matDept"));
			}
			if (!StringUtils.isEmpty(params.get("matProc"))) {
				queryList.setParameter("matProc", params.get("matProc"));
				queryTotal.setParameter("matProc", params.get("matProc"));
			}
			if (!StringUtils.isEmpty(params.get("matCls"))) {
				queryList.setParameter("matCls", params.get("matCls"));
				queryTotal.setParameter("matCls", params.get("matCls"));
			}
			
			Pageable pageable = PagaUtils.pageable(params);
			queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
			List list = new JpaResultMapper().list(queryList, BidCompleteDto.class);
			
			BigInteger count = (BigInteger) queryTotal.getSingleResult();
			Page listPage = new PageImpl(list, pageable, count.intValue());
			resultBody.setData(listPage);
			
		}catch(Exception e) {
			log.error("complateBidhistory list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰 이력 리스트를 가져오는것을 실패하였습니다.");
		}
		
		return resultBody;
		
	}
	
	/**
	 * 투찰 정보 팝업
	 * @param params : (String) biNo
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public ResultBody joinCustList(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		try {
			StringBuilder sbCustList = new StringBuilder(
				  "select	tbim.BI_NO "
				+ ",		tbim.BI_NAME "
				+ ",		tccm.CUST_NAME "
				+ ",		tbimc.ESMT_AMT "
				+ ",		DATE_FORMAT(tbimc.SUBMIT_DATE, '%Y-%m-%d %H:%i') as SUBMIT_DATE "
				+ ",		tbimc.SUCC_YN "
				+ "from t_bi_info_mat tbim "
				+ "inner join t_bi_info_mat_cust tbimc "
				+ "	on tbim.BI_NO = tbimc.BI_NO "
				+ "inner join t_co_cust_master tccm "
				+ "	on tbimc.CUST_CODE = tccm.CUST_CODE "
			);
			
			//조건문 쿼리 삽입
			StringBuilder sbWhere = new StringBuilder();
			sbWhere.append("where tbimc.ESMT_YN = '2' ");
			sbWhere.append("and tbim.BI_NO = :biNo ");
			
			sbCustList.append(sbWhere);
			sbCustList.append("order by field(tbimc.SUCC_YN, 'Y', 'N') ");
			
			//쿼리 실행
			Query queryList = entityManager.createNativeQuery(sbCustList.toString());
			
			//조건 대입
			queryList.setParameter("biNo", params.get("biNo"));
			
			List list = new JpaResultMapper().list(queryList, BidCustDto.class);
			resultBody.setData(list);
			
		}catch(Exception e) {
			log.error("joinCustList list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("투찰 정보를 가져오는것을 실패하였습니다.");
		}
		
		return resultBody;
	}
	
	/**
	 * 협력사 입찰완료 리스트
	 * @param params : (String) biNo, (String) biName, (String) succYn_Y, (String) succYn_N, (String) startDate, (String) endDate 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResultBody complateBidPartnerList(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoCustUser> userOptional = tCoCustUserRepository.findById(principal.getUsername());
		int custCode = userOptional.get().getCustCode();
		
		try {
			StringBuilder sbCount = new StringBuilder(
				  "select	count(1) "
				+ "from t_bi_info_mat tbim "
				+ "inner join t_bi_info_mat_cust tbimc "
				+ "	on tbim.BI_NO = tbimc.BI_NO "
			);
			
			StringBuilder sbList = new StringBuilder(
				  "select	tbim.BI_NO "
				+ ", tbim.BI_NAME "
				+ ", DATE_FORMAT(tbim.BID_OPEN_DATE, '%Y-%m-%d %H:%i') as BID_OPEN_DATE "
				+ ", tbim.BI_MODE "
				+ ", tbimc.SUCC_YN "
				+ ", tbim.INS_MODE "
				+ ", tcu.USER_NAME "
				+ ", tcu.USER_EMAIL "
				+ ", tbimc.CUST_CODE "
				+ "from t_bi_info_mat tbim "
				+ "inner join t_bi_info_mat_cust tbimc "
				+ "	on tbim.BI_NO = tbimc.BI_NO "
				+ "left outer join t_co_user tcu "
				+ "	on tbim.CREATE_USER = tcu.USER_ID "
			);
			
			//조회조건 공통
			StringBuilder sbWhereIf = new StringBuilder("");

			//입찰완료일
			sbWhereIf.append("and tbim.UPDATE_DATE BETWEEN :startDate and :endDate ");
			sbWhereIf.append("and tbimc.CUST_CODE = :custCode ");
			sbWhereIf.append("and tbim.ING_TAG IN ('A5', 'A7') ");
			
			if (!StringUtils.isEmpty(params.get("biNo"))) {
				sbWhereIf.append("and tbim.BI_NO = :biNo ");
			}
			if (!StringUtils.isEmpty(params.get("biName"))) {
				sbWhereIf.append("and tbim.BI_NAME like concat('%',:biName,'%') ");
			}
			
			Boolean succY = (Boolean) params.get("succYn_Y");
			Boolean succN = (Boolean) params.get("succYn_N");
			if (!succY && succN) {	//비선정만
				sbWhereIf.append("and (tbimc.SUCC_YN is null or tbimc.SUCC_YN = 'N') ");
			}else if (succY && !succN) {	//선정(낙찰)만
				sbWhereIf.append("and tbimc.SUCC_YN = 'Y' ");
			}
			
			//조건문 쿼리 삽입
			sbCount.append("where 1=1 " + sbWhereIf);
			sbList.append("where 1=1 " + sbWhereIf);
			
			sbList.append("order by tbim.UPDATE_DATE desc ");
		
			//쿼리 실행
			Query queryList = entityManager.createNativeQuery(sbList.toString());
			Query queryTotal = entityManager.createNativeQuery(sbCount.toString());
			
			queryList.setParameter("startDate", params.get("startDate") + " 00:00:00");
			queryList.setParameter("endDate", params.get("endDate") + " 23:59:59");
			queryTotal.setParameter("startDate", params.get("startDate") + " 00:00:00");
			queryTotal.setParameter("endDate", params.get("endDate") + " 23:59:59");
			
			queryList.setParameter("custCode", custCode);
			queryTotal.setParameter("custCode", custCode);
			
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
			Page listPage = new PageImpl(list, pageable, count.intValue());
			
			resultBody.setData(listPage);
			
		}catch(Exception e) {
			log.error("complateBidPartnerList list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰 완료 리스트를 가져오는것을 실패하였습니다.");	
		}
		
		return resultBody;
	}
	
	/**
  	 * 협력사 입찰완료 상세
  	 * @param params : (String) biNo
  	 * @return
  	 */
	public ResultBody complateBidPartnerDetail(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoCustUser> userOptional = tCoCustUserRepository.findById(principal.getUsername());
		int custCode = userOptional.get().getCustCode();
		
		BidCompleteDetailDto detailDto = null;
		
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
			queryMain.setParameter("biNo", params.get("biNo"));
			
			detailDto = new JpaResultMapper().uniqueResult(queryMain, BidCompleteDetailDto.class);
			
			// ************ 데이터 검색 -- 입찰참가업체 ************
			StringBuilder sbCustData = new StringBuilder(
				  "select	tbimc.BI_NO "
				+ ",		tbimc.CUST_CODE "
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
			sbCustWhere.append("and tbimc.CUST_CODE = :custCode ");
			
			sbCustData.append(sbCustWhere);
			
			//쿼리 실행
			Query queryCust = entityManager.createNativeQuery(sbCustData.toString());
			
			//조건 대입
			queryCust.setParameter("biNo", params.get("biNo"));
			queryCust.setParameter("custCode", custCode);
			
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
			queryFile.setParameter("biNo", params.get("biNo"));
			
			List<BidProgressFileDto> fileData = new JpaResultMapper().list(queryFile, BidProgressFileDto.class);
			
			detailDto.setFileList(fileData);
			
			resultBody.setData(detailDto);
			
		}catch(Exception e) {
			log.error("complateBidPartnerDetail error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰완료 상세 데이터를 가져오는것을 실패하였습니다.");
		}
		
		return resultBody;
	
	}
	
	/**
	 * 협력사 낙찰승인
	 * @param params : (String) esmtYn, (String) biNo
	 * @return
	 */
	@Transactional
	public ResultBody updBiCustFlag(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoCustUser> userOptional = tCoCustUserRepository.findById(principal.getUsername());
		int custCode = userOptional.get().getCustCode();
		String userId = userOptional.get().getUserId();
		
		//상태값 업데이트
		try {
			StringBuilder sbQuery = new StringBuilder(
			  "UPDATE T_BI_INFO_MAT_CUST "
			+ "SET ESMT_YN = :esmtYn "
			+ "WHERE BI_NO = :biNo "
			+ "AND CUST_CODE = :custCode "
			);

			Query query = entityManager.createNativeQuery(sbQuery.toString());
			query.setParameter("esmtYn", params.get("esmtYn"));
			query.setParameter("biNo", params.get("biNo"));
			query.setParameter("custCode", custCode);
			query.executeUpdate();
			
		}catch(Exception e) {
			log.error("updBiCustFlag error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("낙찰승인 저장을 실패하였습니다.");
			
			return resultBody;
		}
		
		//log insert
		try {
			
			StringBuilder sbQuery = new StringBuilder(
			  "INSERT INTO T_BI_LOG (BI_NO, USER_ID, LOG_TEXT, CREATE_DATE) VALUES"
			+ "(:biNo, :userId, :msg, sysdate())"
			);

			Query query = entityManager.createNativeQuery(sbQuery.toString());
			query.setParameter("biNo", params.get("biNo"));
			query.setParameter("userId", userId);
			query.setParameter("msg", "[업체]낙찰확인");
			query.executeUpdate();
			
		}catch(Exception e) {
			log.error("updBiCustFlag insert log error : {}", e);
		}
		
		return resultBody;
	}
}
