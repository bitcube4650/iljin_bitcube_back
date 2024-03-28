package iljin.framework.ebid.etc.statistics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.statistics.dto.BiInfoDto;
import iljin.framework.ebid.etc.statistics.dto.CoInterDto;


@Service
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
	
	//계열사 목록 조회
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
	

}
