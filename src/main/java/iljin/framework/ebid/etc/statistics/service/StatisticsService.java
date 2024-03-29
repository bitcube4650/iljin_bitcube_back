package iljin.framework.ebid.etc.statistics.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.bid.dto.BidCompleteDto;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.statistics.dto.BiInfoDto;
import iljin.framework.ebid.etc.statistics.dto.CoInterDto;
import iljin.framework.ebid.etc.util.PagaUtils;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class StatisticsService {
	
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private TCoUserRepository tCoUserRepository;
	
	//계열사 목록 조회
	public List<List<?>> selectCoInterList() {
  
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = principal.getUsername();
		String userAuth = userOptional.get().getUserAuth();// userAuth(1 = 시스템관리자, 4 = 감사사용자)
		
		
		StringBuilder sbList = null;
		if("1".equals(userAuth)) {
			 sbList = new StringBuilder("SELECT INTERRELATED_CUST_CODE\r\n"
			 		+ ",INTERRELATED_NM  FROM t_co_interrelated\r\n"
			 		+ "ORDER BY INTERRELATED_NM ");
		}else if("4".equals(userAuth)) {
			 sbList = new StringBuilder("SELECT	tci.INTERRELATED_CUST_CODE  \r\n"
			 		+ "	,		tci.INTERRELATED_NM \r\n"
			 		+ "	FROM t_co_interrelated tci \r\n"
			 		+ "	INNER JOIN t_co_user_interrelated tcui \r\n"
			 		+ "	ON tci.INTERRELATED_CUST_CODE = tcui.INTERRELATED_CUST_CODE \r\n"
			 		+ "	INNER join t_co_user tcu \r\n"
			 		+ "	ON tcui.USER_ID = tcu.USER_ID \r\n"
			 		+ "	WHERE tcu.USER_ID = :userId \r\n"
			 		+ "	ORDER BY INTERRELATED_NM ");
		}
		
        Query queryList = entityManager.createNativeQuery(sbList.toString());

        if("4".equals(userAuth)) {
            queryList.setParameter("userId", userId);
        }

        List<CoInterDto> resultList = new JpaResultMapper().list(queryList, CoInterDto.class);

        List<List<?>> combinedResults = new ArrayList<>();
        combinedResults.add(resultList);

        return combinedResults;
	}
	

	/**
	 * 계열사리스트 v2
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public ResultBody interrelatedCustCodeList(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		
		try {
			
			StringBuilder sbList = new StringBuilder(
				  "select	tcui.INTERRELATED_CUST_CODE "
				+ ",		tci.INTERRELATED_NM "
				+ "from t_co_user_interrelated tcui "
				+ "inner join t_co_interrelated tci "
				+ "	on tcui.INTERRELATED_CUST_CODE = tci.INTERRELATED_CUST_CODE "
			);
			
			//조건문 쿼리 삽입
			StringBuilder sbWhere = new StringBuilder();
			sbWhere.append("where tcui.USER_ID = :userId ");
			
			sbList.append(sbWhere);
			
			//쿼리 실행 - mat_dept
			Query queryList1 = entityManager.createNativeQuery(sbList.toString());
			
			//조건 대입
			queryList1.setParameter("userId", userId);
			
			List dept = new JpaResultMapper().list(queryList1, CoInterDto.class);
			
			resultBody.setData(dept);
			
		}catch(Exception e) {
			log.error("interrelatedCustCodeList list error : {}", e);
			resultBody.setCode("999");
			resultBody.setMsg("계열사 리스트를 가져오는것을 실패하였습니다.");
		}
		
		return resultBody;
		
	}
	
	/**
	 * 입찰상세내역 리스트
	 * @param params : (String) biNo, (String) startDate, (String) endDate, (String) interrelatedCustCode
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResultBody bidDetailList(Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = userOptional.get().getUserId();
		String userAuth = userOptional.get().getUserAuth();
		
		try {
			StringBuilder sbCount = new StringBuilder(
				  "select	count(1) "
				+ "from t_bi_info_mat tbim "
				+ "inner join t_bi_info_mat_cust tbimc "
				+ "	on tbim.BI_NO = tbimc.BI_NO "
				+ "	and tbimc.SUCC_YN = 'Y' "
			);
			
			StringBuilder sbList = new StringBuilder(
				  "select	tbim.BI_NO "
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
				+ "left outer join t_co_user tcu "
				+ "	on tbim.CREATE_USER = tcu.USER_ID "
				+ "inner join ( "
				+ "	select	tbimc.BI_NO "
				+ "	,		COUNT(1) as CNT "
				+ "	from t_bi_info_mat_cust tbimc "
				+ "	where tbimc.ESMT_YN = '2' "
				+ "	group by tbimc.BI_NO "
				+ ") c "
				+ "	on tbim.BI_NO = c.BI_NO "
			);
			
			if(userAuth.equals("4")) {
				StringBuilder sbMainAdd = new StringBuilder(
				  "inner join t_co_user_interrelated tcui "
				+ "	on tbim.INTERRELATED_CUST_CODE = tcui.INTERRELATED_CUST_CODE "
				+ "	and tcui.USER_ID = :userId "
				);
				
				sbCount.append(sbMainAdd);
				sbList.append(sbMainAdd);
			}
			
			//조건문 쿼리 삽입
			StringBuilder sbWhere = new StringBuilder();
			sbWhere.append("where tbim.ING_TAG = 'A5' ");
			
			//입찰완료일
			sbWhere.append("and tbim.UPDATE_DATE BETWEEN :startDate and :endDate ");

			//계열사
			if (!StringUtils.isEmpty(params.get("interrelatedCustCode"))) {
				sbWhere.append("and tbim.INTERRELATED_CUST_CODE = :interrelatedCustCode ");
			}
			
			sbCount.append(sbWhere);
			sbList.append(sbWhere);
			
			sbList.append("order by tbim.UPDATE_DATE desc ");
			
			//쿼리 실행
			Query queryList = entityManager.createNativeQuery(sbList.toString());
			Query queryTotal = entityManager.createNativeQuery(sbCount.toString());

			//조건 대입
			if(userAuth.equals("4")) {
				queryList.setParameter("userId", userId);
				queryTotal.setParameter("userId", userId);
			}
			
			queryList.setParameter("startDate", params.get("startDate") + " 00:00:00");
			queryList.setParameter("endDate", params.get("endDate") + " 23:59:59");
			queryTotal.setParameter("startDate", params.get("startDate") + " 00:00:00");
			queryTotal.setParameter("endDate", params.get("endDate") + " 23:59:59");
			
			if (!StringUtils.isEmpty(params.get("interrelatedCustCode"))) {
				queryList.setParameter("interrelatedCustCode", params.get("interrelatedCustCode"));
				queryTotal.setParameter("interrelatedCustCode", params.get("interrelatedCustCode"));
			}
			
			Pageable pageable = PagaUtils.pageable(params);
			queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
			List list = new JpaResultMapper().list(queryList, BidCompleteDto.class);
			
			BigInteger count = (BigInteger) queryTotal.getSingleResult();
			Page listPage = new PageImpl(list, pageable, count.intValue());
			resultBody.setData(listPage);
			
		}catch(Exception e) {
			log.error("bidDetailList list error : {}", e);
			resultBody.setCode("999");
			resultBody.setMsg("입찰상세내역 리스트를 가져오는것을 실패하였습니다.");	
		}
		
		return resultBody;
	}
	
	//회사별 입찰실적 리스트 조회
	public List<List<?>> selectBiInfoList(@RequestBody Map<String, Object> params) {

		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userAuth = userOptional.get().getUserAuth();// userAuth(1 = 시스템관리자, 4 = 감사사용자)
		
		StringBuilder sbList = new StringBuilder("SELECT \r\n"
				+ "    IF(is_rollup = 1, '계', INTERRELATED_NM) AS INTERRELATED_NM,\r\n"
				+ "    CNT,\r\n"
				+ "    BD_AMT,\r\n"
				+ "    SUCC_AMT,\r\n"
				+ "    M_AMT,\r\n"
				+ "    INTERRELATED_CUST_CODE \r\n"
				+ "FROM (\r\n"
				+ "    SELECT \r\n"
				+ "        IFNULL(A.INTERRELATED_NM, 'Gye') AS INTERRELATED_NM,\r\n"
				+ "        COUNT(BI_NO) AS CNT,\r\n"
				+ "        SUM(A.BD_AMT) as BD_AMT,\r\n"
				+ "        SUM(A.SUCC_AMT) as SUCC_AMT,\r\n"
				+ "        SUM(A.BD_AMT) - SUM(A.SUCC_AMT) as M_AMT,\r\n"
				+ "        A.INTERRELATED_CUST_CODE,\r\n"
				+ "        CASE \r\n"
				+ "            WHEN A.INTERRELATED_CUST_CODE IS NULL THEN 1\r\n"
				+ "            ELSE 0\r\n"
				+ "        END AS is_rollup\r\n"
				+ "    FROM \r\n"
				+ "        (\r\n"
				+ "            SELECT \r\n"
				+ "                tbim.BI_NO,\r\n"
				+ "                tci.INTERRELATED_NM,\r\n"
				+ "                tbim.BD_AMT,\r\n"
				+ "                tbim.SUCC_AMT,\r\n"
				+ "                tcci.INTERRELATED_CUST_CODE \r\n"
				+ "            FROM \r\n"
				+ "                t_bi_detail_mat_cust tbdmc \r\n"
				+ "                INNER JOIN t_bi_spec_mat tbsm ON tbdmc.BI_NO = tbsm.BI_NO \r\n"
				+ "                INNER JOIN t_bi_info_mat tbim ON tbsm.BI_NO = tbim.BI_NO \r\n"
				+ "                INNER JOIN t_co_cust_master tccm ON tbdmc.CUST_CODE = tccm.CUST_CODE\r\n"
				+ "                INNER JOIN t_co_cust_ir tcci ON tbdmc.CUST_CODE = tcci.CUST_CODE \r\n"
				+ "                INNER JOIN t_co_interrelated tci ON tcci.INTERRELATED_CUST_CODE = tci.INTERRELATED_CUST_CODE \r\n"
				+ "            WHERE \r\n"
				+ "                tbim.ING_TAG = 'A5'\r\n"
				+ "                AND DATE(tbim.UPDATE_DATE) BETWEEN :startDay AND :endDay\r\n");
		
			Object coInter = params.get("coInter");
			if("4".equals(userAuth) || coInter != "") {
				
			 sbList.append("AND tcci.INTERRELATED_CUST_CODE IN(");
			 sbList.append(coInter);
			 sbList.append(")\r\n");
		}
			 sbList.append("GROUP BY \r\n"
			 		+ "                tci.INTERRELATED_CUST_CODE, tbim.bi_no\r\n"
			 		+ "        ) AS A\r\n"
			 		+ "    GROUP BY \r\n"
			 		+ "        A.INTERRELATED_CUST_CODE WITH ROLLUP\r\n"
			 		+ ") AS B\r\n"
			 		+ "ORDER BY \r\n"
			 		+ "    is_rollup, INTERRELATED_NM");
		
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("startDay", params.get("startDay"));
        queryList.setParameter("endDay", params.get("endDay"));
        List<BiInfoDto> resultList = new JpaResultMapper().list(queryList, BiInfoDto.class);
        List<List<?>> combinedResults = new ArrayList<>();
        combinedResults.add(resultList);

        return combinedResults;
	}

	/**
	 * 입찰현황 리스트 조회
	 * 
	 * @param params
	 * @return
	 */
	public List<List<?>> bidPresentList(Map<String, Object> params) {
        List<List<?>> combinedResults = new ArrayList<>();
		
		//세션 정보 조회
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
		String userId = principal.getUsername();
		String userAuth = userOptional.get().getUserAuth();// userAuth(1 = 시스템관리자, 4 = 감사사용자)
		
		StringBuilder sbList = new StringBuilder(
			"select  max(BID_LIST.INTERRELATED_NM) as INTERRELATED_NM\r\n"
			+ ",		sum(BID_LIST.PLAN_CNT) as PLAN_CNT\r\n"
			+ ",		sum(BID_LIST.PLAN_AMT) as PLAN_AMT\r\n"
			+ ",		sum(BID_LIST.ING_CNT) as ING_CNT\r\n"
			+ ",		sum(BID_LIST.ING_AMT) as ING_AMT\r\n"
			+ ",		sum(BID_LIST.SUCC_CNT) as SUCC_CNT\r\n"
			+ ",		sum(BID_LIST.SUCC_AMT) as SUCC_AMT\r\n"
			+ ",		ifnull(round(sum(BID_LIST.cust_cnt)/sum(BID_LIST.SUCC_CNT), 1), 0) as cust_cnt\r\n"
			+ ",		sum(BID_LIST.REG_CUST_CNT) as REG_CUST_CNT\r\n"
			+ "from ("
			+ "	select	max(A.INTERRELATED_NM ) as INTERRELATED_NM \r\n"
			+ "	,		A.INTERRELATED_CUST_CODE\r\n"
			+ "	,		sum(B.PLAN_CNT) as PLAN_CNT\r\n"
			+ "	,		ifnull(sum(B.PLAN_AMT), 0) as PLAN_AMT\r\n"
			+ "	,		sum(B.ING_CNT) as ING_CNT\r\n"
			+ "	,		ifnull(sum(B.ING_AMT), 0) as ING_AMT\r\n"
			+ "	,		sum(B.SUCC_CNT) as SUCC_CNT\r\n"
			+ "	,		ifnull(sum(B.SUCC_AMT), 0) as SUCC_AMT \r\n"
			+ "	,		ROUND(ifnull(D.CUST_CNT / sum(B.SUCC_CNT), 0), 1) as CUST_CNT\r\n"
			+ "	,		ifnull(max(C.REG_CUST_cnt), 0) as REG_CUST_CNT\r\n"
			+ "	from t_co_interrelated A \r\n"
			+ "	INNER JOIN (\r\n"
			+ "		select	BB.BI_NO\r\n"
			+ "		,		BB.INTERRELATED_CUST_CODE\r\n"
			+ "		,		CASE WHEN BB.ING_TAG = 'A0' THEN 1 ELSE 0 END AS PLAN_CNT\r\n"
			+ "		,		CASE WHEN BB.ING_TAG = 'A0' THEN BB.BD_AMT ELSE 0 END AS PLAN_AMT\r\n"
			+ "		,		CASE WHEN BB.ING_TAG = 'A1' THEN 1 ELSE 0 END AS ING_CNT\r\n"
			+ "		,		CASE WHEN BB.ING_TAG = 'A1' THEN BB.BD_AMT ELSE 0 END AS ING_AMT\r\n"
			+ "		,		CASE WHEN BB.ING_TAG = 'A5' THEN 1 ELSE 0 END AS SUCC_CNT\r\n"
			+ "		,		CASE WHEN BB.ING_TAG = 'A5' THEN BB.SUCC_AMT ELSE 0 END AS SUCC_AMT\r\n"
			+ "		from T_BI_INFO_MAT BB			-- 입찰서내용\r\n"
			+ "		where DATE(BB.UPDATE_DATE) BETWEEN :startDay AND :endDay\r\n"
			+ "	) B\r\n"
			+ "		on A.INTERRELATED_CUST_CODE = B.INTERRELATED_CUST_CODE\r\n"
			+ "	left outer join (\r\n"
			+ "		select AA.INTERRELATED_CUST_CODE\r\n"
			+ "		,		count(1) as REG_CUST_cnt\r\n"
			+ "		from t_co_interrelated AA\r\n"
			+ "		inner join t_co_cust_master BB\r\n"
			+ "			on AA.INTERRELATED_CUST_CODE = BB.INTERRELATED_CUST_CODE\r\n"
			+ "		where DATE(BB.CREATE_DATE) BETWEEN :startDay AND :endDay\r\n"
			+ "		group by AA.INTERRELATED_CUST_CODE\r\n"
			+ "	) C\r\n"
			+ "		on a.INTERRELATED_CUST_CODE = c.INTERRELATED_CUST_CODE\r\n "
			+ "	left outer join (\r\n"
			+ "		select	INTERRELATED_CUST_CODE\r\n"
			+ "		,		COUNT(CD.BI_NO) as CUST_CNT\r\n"
			+ "		from T_BI_INFO_MAT CC\r\n"
			+ "		inner join t_bi_info_mat_cust CD\r\n"
			+ "			on CC.BI_NO = CD.BI_NO\r\n"
			+ "		where CC.ING_TAG = 'A5'\r\n"
			+ "		and DATE(CC.UPDATE_DATE) BETWEEN :startDay AND :endDay\r\n"
			+ "		group by CC.INTERRELATED_CUST_CODE\r\n"
			+ "	) D\r\n"
			+ "		on D.INTERRELATED_CUST_CODE = A.INTERRELATED_CUST_CODE\r\n"
			//+ "where 1=1\r\n"
		);
		
			if(userAuth.equals("4")) {
				StringBuilder sbMainAdd = new StringBuilder(
				  "inner join t_co_user_interrelated tcui "
				+ "	on A.INTERRELATED_CUST_CODE = tcui.INTERRELATED_CUST_CODE "
				+ "	and tcui.USER_ID = :userId "
				);
				
				sbList.append(sbMainAdd);
			}
		
		//조건문 쿼리 삽입
		StringBuilder sbWhere = new StringBuilder();
		sbWhere.append(" ");
		// 계열사
		if (!StringUtils.isEmpty(params.get("coInter"))) {
			sbWhere.append(" where A.INTERRELATED_CUST_CODE in ( :interrelatedCustCode ) ");
		}

		sbList.append(sbWhere);
		
		sbList.append("	group by A.INTERRELATED_CUST_CODE\r\n"
				+ " ) BID_LIST\r\n"
				+ " group by BID_LIST.INTERRELATED_CUST_CODE with rollup\r\n"
				+ "	--	order by A.INTERRELATED_CUST_CODE\r\n");
		
		//쿼리 실행
		Query queryList = entityManager.createNativeQuery(sbList.toString());

		//조건 대입
		if(userAuth.equals("4")) {
			queryList.setParameter("userId", userId);
		}
		
		queryList.setParameter("startDay", params.get("startDay") + " 00:00:00");
		queryList.setParameter("endDay", params.get("endDay") + " 23:59:59");
		
		if (!StringUtils.isEmpty(params.get("coInter"))) {
			queryList.setParameter("interrelatedCustCode", params.get("coInter"));
		}
		
		List<BiInfoDto> resultList = new JpaResultMapper().list(queryList, BiInfoDto.class);
        combinedResults.add(resultList);

        return combinedResults;
	}
}
