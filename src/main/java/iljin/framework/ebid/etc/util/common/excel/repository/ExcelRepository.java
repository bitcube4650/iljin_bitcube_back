package iljin.framework.ebid.etc.util.common.excel.repository;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.statistics.dto.BiInfoDetailDto;
import iljin.framework.ebid.etc.statistics.dto.BiInfoDto;
import iljin.framework.ebid.etc.util.common.excel.dto.BidCompleteDto;
import iljin.framework.ebid.etc.util.common.excel.dto.BidDetailListDto;
import iljin.framework.ebid.etc.util.common.excel.entity.FileEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ExcelRepository {

    @PersistenceContext
    private EntityManager entityManager;
    //private final BidCompleteDto bidCompleteDto;

    private final TCoUserRepository tCoUserRepository;
    private TCoCustUserRepository tCoCustUserRepository;

    public List<FileEntity> findAll() {
        return entityManager.createQuery("select f from FileEntity f", FileEntity.class)
                .setMaxResults(10)
                .getResultList();
    }

    //회사별 입찰실적 Cnt 조회
    public int findBiInfoListCnt(@RequestBody Map<String, Object> params) {
        try {
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
            String userAuth = userOptional.get().getUserAuth();// userAuth(1 = 시스템관리자, 4 = 감사사용자)

            StringBuilder sbCount = new StringBuilder("SELECT \r\n"
                    + "    COUNT(1) \r\n"
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
    				+ "                AA.BI_NO,\r\n"
    				+ "                BB.INTERRELATED_NM,\r\n"
    				+ "                AA.BD_AMT,\r\n"
    				+ "                AA.SUCC_AMT,\r\n"
    				+ "                AA.INTERRELATED_CUST_CODE \r\n"
    				+ "            FROM \r\n"
    				+ "                t_bi_info_mat AA \r\n"
    				+ "                inner join t_co_interrelated BB on AA.INTERRELATED_CUST_CODE = BB.INTERRELATED_CUST_CODE  \r\n"
                    + "            WHERE \r\n"
                    + "                AA.ING_TAG = 'A5'\r\n"
                    + "                AND DATE(AA.UPDATE_DATE) BETWEEN :startDay AND :endDay\r\n");

            Object coInter = params.get("coInter");
            if("4".equals(userAuth) || coInter != "") {

                sbCount.append("AND AA.INTERRELATED_CUST_CODE IN(");
                sbCount.append(coInter);
                sbCount.append(")\r\n");
            }
            sbCount.append("GROUP BY \r\n"
                    + "                AA.INTERRELATED_CUST_CODE, AA.bi_no\r\n"
                    + "        ) AS A\r\n"
                    + "    GROUP BY \r\n"
                    + "        A.INTERRELATED_CUST_CODE WITH ROLLUP\r\n"
                    + ") AS B\r\n"
                    + "ORDER BY \r\n"
                    + "    is_rollup, INTERRELATED_NM");

            Query queryTotal = entityManager.createNativeQuery(sbCount.toString());
            queryTotal.setParameter("startDay", params.get("startDay"));
            queryTotal.setParameter("endDay", params.get("endDay"));
            BigInteger count = (BigInteger) queryTotal.getSingleResult();
            return count.intValue(); // 결과를 정수형으로 변환하여 반환

        } catch (Exception e) {
            log.error("findBiInfoList Cnt error : {}", e);
            return 0;
        }
    }

    //회사별 입찰실적 리스트 조회
    public List<BiInfoDto> findBiInfoList(@RequestBody Map<String, Object> params, int offset, int limit) {
        try {
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
    				+ "                AA.BI_NO,\r\n"
    				+ "                BB.INTERRELATED_NM,\r\n"
    				+ "                AA.BD_AMT,\r\n"
    				+ "                AA.SUCC_AMT,\r\n"
    				+ "                AA.INTERRELATED_CUST_CODE \r\n"
    				+ "            FROM \r\n"
    				+ "                t_bi_info_mat AA \r\n"
    				+ "                inner join t_co_interrelated BB on AA.INTERRELATED_CUST_CODE = BB.INTERRELATED_CUST_CODE  \r\n"
                    + "            WHERE \r\n"
                    + "                AA.ING_TAG = 'A5'\r\n"
                    + "                AND DATE(AA.UPDATE_DATE) BETWEEN :startDay AND :endDay\r\n");

            Object coInter = params.get("coInter");
            if("4".equals(userAuth) || coInter != "") {

                sbList.append("AND AA.INTERRELATED_CUST_CODE IN(");
                sbList.append(coInter);
                sbList.append(")\r\n");
            }
            sbList.append("GROUP BY \r\n"
                    + "                AA.INTERRELATED_CUST_CODE, AA.bi_no\r\n"
                    + "        ) AS A\r\n"
                    + "    GROUP BY \r\n"
                    + "        A.INTERRELATED_CUST_CODE WITH ROLLUP\r\n"
                    + ") AS B\r\n"
                    + "ORDER BY \r\n"
                    + "    is_rollup, INTERRELATED_NM");

            Query queryList = entityManager.createNativeQuery(sbList.toString())
                    .setFirstResult(offset)
                    .setMaxResults(limit);
            queryList.setParameter("startDay", params.get("startDay"));
            queryList.setParameter("endDay", params.get("endDay"));
            List<BiInfoDto> resultList = new JpaResultMapper().list(queryList, BiInfoDto.class);
            return resultList;

        } catch (Exception e) {
            log.error("findBiInfoList list error : {}", e);
            return Collections.emptyList();
        }
    }

    //통계 >> 입찰이력 cnt
    public int findComplateBidListCnt(Map<String, Object> params) {
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
                            + "inner join ( "
                            + "	select	tbimc.BI_NO "
                            + " ,       tbimc.ESMT_AMT "
                            + " ,       tccm.CUST_NAME "
                            + " ,       DATE_FORMAT(tbimc.SUBMIT_DATE, '%Y-%m-%d %H:%i') AS SUBMIT_DATE "
                            + "	,		COUNT(1) as CNT "
                            + "	,		tbimc.SUCC_YN "
                            + "	from t_bi_info_mat_cust tbimc "
                            + " inner join t_co_cust_master tccm ON tbimc.CUST_CODE = tccm.CUST_CODE "
                            + "	where tbimc.ESMT_YN = '2' "
                            + "	group by tbimc.BI_NO, tbimc.ESMT_AMT, SUBMIT_DATE, tccm.CUST_NAME "
                            + "	order by tbimc.SUCC_YN "
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


            //쿼리 실행

            Query queryTotal = entityManager.createNativeQuery(sbCount.toString());

            //조건 대입
            queryTotal.setParameter("interrelatedCustCode", userInterrelatedCustCode);
            queryTotal.setParameter("userId", userId);

            queryTotal.setParameter("startDate", params.get("startDate") + " 00:00:00");
            queryTotal.setParameter("endDate", params.get("endDate") + " 23:59:59");

            if (!StringUtils.isEmpty(params.get("biNo"))) {
                queryTotal.setParameter("biNo", params.get("biNo"));
            }
            if (!StringUtils.isEmpty(params.get("biName"))) {
                queryTotal.setParameter("biName", params.get("biName"));
            }
            if (!StringUtils.isEmpty(params.get("matDept"))) {
                queryTotal.setParameter("matDept", params.get("matDept"));
            }
            if (!StringUtils.isEmpty(params.get("matProc"))) {
                queryTotal.setParameter("matProc", params.get("matProc"));
            }
            if (!StringUtils.isEmpty(params.get("matCls"))) {
                queryTotal.setParameter("matCls", params.get("matCls"));
            }

            BigInteger count = (BigInteger) queryTotal.getSingleResult();
            return count.intValue(); // 결과를 정수형으로 변환하여 반환


        }catch(Exception e) {
            log.error("findComplateBidList list error : {}", e);
            return 0;
        }
    }


    //통계 >> 입찰 이력 리스트
    public List<BidCompleteDto> findComplateBidList(Map<String, Object> params, int offset, int limit) {
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
                            + ",		c.CUST_NAME as CUST_NAME2 "
                            + ",		c.ESMT_AMT "
                            + ",		c.SUBMIT_DATE "
                            + "from t_bi_info_mat tbim "
                            + "inner join t_bi_info_mat_cust tbimc "
                            + "	on tbim.BI_NO = tbimc.BI_NO "
                            + "	and tbimc.SUCC_YN = 'Y' "
                            + "inner join t_co_cust_master tccm "
                            + "	on tbimc.CUST_CODE = tccm.CUST_CODE "
                            + "inner join ( "
                            + "	select	tbimc.BI_NO "
                            + " ,       tbimc.ESMT_AMT "
                            + " ,       tccm.CUST_NAME "
                            + " ,       DATE_FORMAT(tbimc.SUBMIT_DATE, '%Y-%m-%d %H:%i') AS SUBMIT_DATE "
                            + "	,		COUNT(1) as CNT "
                            + "	,		tbimc.SUCC_YN "
                            + "	from t_bi_info_mat_cust tbimc "
                            + " inner join t_co_cust_master tccm ON tbimc.CUST_CODE = tccm.CUST_CODE "
                            + "	where tbimc.ESMT_YN = '2' "
                            + "	group by tbimc.BI_NO, tbimc.ESMT_AMT, SUBMIT_DATE, tccm.CUST_NAME "
                            + "	order by tbimc.SUCC_YN "
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
            List<BidCompleteDto> list = new JpaResultMapper().list(queryList, BidCompleteDto.class);

            return list;

        }catch(Exception e) {
            log.error("findComplateBidList list error : {}", e);
            return Collections.emptyList();
        }
    }

    //통계 > 입찰현황 cnt
    public int findBidPresentListCnt(Map<String, Object> params) {

        try {
            //세션 정보 조회
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
            String userId = principal.getUsername();
            String userAuth = userOptional.get().getUserAuth();// userAuth(1 = 시스템관리자, 4 = 감사사용자)

            StringBuilder sbCount = new StringBuilder(
                    "select  COUNT(1)\r\n"
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

            if (userAuth.equals("4")) {
                StringBuilder sbMainAdd = new StringBuilder(
                        "inner join t_co_user_interrelated tcui "
                                + "	on A.INTERRELATED_CUST_CODE = tcui.INTERRELATED_CUST_CODE "
                                + "	and tcui.USER_ID = :userId "
                );

                sbCount.append(sbMainAdd);
            }

            //조건문 쿼리 삽입
            StringBuilder sbWhere = new StringBuilder();
            sbWhere.append(" ");
            // 계열사
            if (!StringUtils.isEmpty(params.get("coInter"))) {
                sbWhere.append(" where A.INTERRELATED_CUST_CODE in ( :interrelatedCustCode ) ");
            }

            sbCount.append(sbWhere);

            sbCount.append("	group by A.INTERRELATED_CUST_CODE\r\n"
                    + "	) AS count\r\n"
                    + "	--	order by A.INTERRELATED_CUST_CODE\r\n");

            //쿼리 실행
            Query queryTotal = entityManager.createNativeQuery(sbCount.toString());

            //조건 대입
            if (userAuth.equals("4")) {
                queryTotal.setParameter("userId", userId);
            }

            queryTotal.setParameter("startDay", params.get("startDay") + " 00:00:00");
            queryTotal.setParameter("endDay", params.get("endDay") + " 23:59:59");

            if (!StringUtils.isEmpty(params.get("coInter"))) {
                queryTotal.setParameter("interrelatedCustCode", params.get("coInter"));
            }
            BigInteger count = (BigInteger) queryTotal.getSingleResult();
            return count.intValue(); // 결과를 정수형으로 변환하여 반환

        } catch (Exception e) {
            log.error("findBidPresentListCnt error : {}", e);
            return 0;
        }
    }

    //통계 > 입찰현황 리스트.
    public List<BiInfoDto> findBidPresentList(Map<String, Object> params, int offset, int limit) {

        try {
            //세션 정보 조회
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
            String userId = principal.getUsername();
            String userAuth = userOptional.get().getUserAuth();// userAuth(1 = 시스템관리자, 4 = 감사사용자)

            StringBuilder sbList = new StringBuilder(
                    "select		IF(INTERRELATED_CUST_CODE IS NULL, '계', (SELECT INTERRELATED_NM FROM T_CO_INTERRELATED AAA WHERE AAA.INTERRELATED_CUST_CODE = BID_LIST.INTERRELATED_CUST_CODE)) AS INTERRELATED_NM\r\n"
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
                            + "	,		D.CUST_CNT as CUST_CNT\r\n"
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
                            + "		and BB.ING_TAG in ('A0', 'A1', 'A5')\r\n"
                            + "	) B\r\n"
                            + "		on A.INTERRELATED_CUST_CODE = B.INTERRELATED_CUST_CODE\r\n"
                            + "	left outer join (\r\n"
                            + "		select AA.INTERRELATED_CUST_CODE\r\n"
                            + "		,		count(1) as REG_CUST_cnt\r\n"
                            + "		from t_co_interrelated AA\r\n"
                            + "		inner join t_co_cust_master BB\r\n"
                            + "			on AA.INTERRELATED_CUST_CODE = BB.INTERRELATED_CUST_CODE\r\n"
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
            Query queryList = entityManager.createNativeQuery(sbList.toString())
                    .setFirstResult(offset)
                    .setMaxResults(limit);

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
            return resultList;

        } catch(Exception e) {
            log.error("findBidPresentList error : {}", e);
            return Collections.emptyList();
        }
    }


    public List<BidDetailListDto> findBidDetailList(Map<String, Object> params) {
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
                            + ",		c.CUST_NAME AS CUST_NAME2 "
                            + ",		c.ESMT_AMT "
                            + ",		c.SUBMIT_DATE "
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
                            + "	,		tccm.CUST_NAME "
                            + "	,		tbimc.ESMT_AMT "
                            + "	,		DATE_FORMAT(tbimc.SUBMIT_DATE, '%Y-%m-%d %H:%i') as SUBMIT_DATE "
                            + "	,		tbimc.SUCC_YN "
                            + "	,		COUNT(1) as CNT "
                            + "	from t_bi_info_mat_cust tbimc "
                            + " inner join t_co_cust_master tccm "
                            + " on tbimc.CUST_CODE = tccm.CUST_CODE "
                            + "	where tbimc.ESMT_YN = '2' "
                            + "	group by tbimc.BI_NO, tccm.CUST_NAME, tbimc.ESMT_AMT, SUBMIT_DATE, tbimc.SUCC_YN "
                            + " ORDER BY tbimc.SUCC_YN "
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
            List<BidDetailListDto> list = new JpaResultMapper().list(queryList, BidDetailListDto.class);
            return list;
        }catch(Exception e) {
            log.error("bidDetailList list error : {}", e);
            return Collections.emptyList();
        }
    }

    public List<BidDetailListDto>
    findBidDetailList2(Map<String, Object> params, int start, int limit) {
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
                            + ",		c.CUST_NAME AS CUST_NAME2 "
                            + ",		c.ESMT_AMT "
                            + ",		c.SUBMIT_DATE "
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
                            + "	,		tccm.CUST_NAME "
                            + "	,		tbimc.ESMT_AMT "
                            + "	,		DATE_FORMAT(tbimc.SUBMIT_DATE, '%Y-%m-%d %H:%i') as SUBMIT_DATE "
                            + "	,		tbimc.SUCC_YN "
                            + "	,		COUNT(1) as CNT "
                            + "	from t_bi_info_mat_cust tbimc "
                            + " inner join t_co_cust_master tccm "
                            + " on tbimc.CUST_CODE = tccm.CUST_CODE "
                            + "	where tbimc.ESMT_YN = '2' "
                            + "	group by tbimc.BI_NO, tccm.CUST_NAME, tbimc.ESMT_AMT, SUBMIT_DATE, tbimc.SUCC_YN "
                            + " ORDER BY tbimc.SUCC_YN "
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
            Query queryList = entityManager.createNativeQuery(sbList.toString())
                    .setFirstResult(start) // 결과의 시작 위치를 10으로 설정
                    .setMaxResults(limit); // 최대 결과 수를 10으로 설정
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
            List<BidDetailListDto> list = new JpaResultMapper().list(queryList, BidDetailListDto.class);
            return list;
        }catch(Exception e) {
            log.error("bidDetailList list error : {}", e);
            return Collections.emptyList();
        }
    }

    public int findBidDetailCnt(Map<String, Object> params) {
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


            if(userAuth.equals("4")) {
                StringBuilder sbMainAdd = new StringBuilder(
                        "inner join t_co_user_interrelated tcui "
                                + "	on tbim.INTERRELATED_CUST_CODE = tcui.INTERRELATED_CUST_CODE "
                                + "	and tcui.USER_ID = :userId "
                );

                sbCount.append(sbMainAdd);
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




            //쿼리 실행
            Query queryTotal = entityManager.createNativeQuery(sbCount.toString());

            //조건 대입
            if(userAuth.equals("4")) {
                queryTotal.setParameter("userId", userId);
            }

            queryTotal.setParameter("startDate", params.get("startDate") + " 00:00:00");
            queryTotal.setParameter("endDate", params.get("endDate") + " 23:59:59");

            if (!StringUtils.isEmpty(params.get("interrelatedCustCode"))) {
                queryTotal.setParameter("interrelatedCustCode", params.get("interrelatedCustCode"));
            }

            BigInteger count = (BigInteger) queryTotal.getSingleResult();
            return count.intValue(); // 결과를 정수형으로 변환하여 반환
        }catch(Exception e) {
            log.error("bidDetailList list error : {}", e);
            return 0;
        }
    }



    public int findBidDetailListCnt2(Map<String, Object> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
        String userId = userOptional.get().getUserId();
        String userAuth = userOptional.get().getUserAuth();

        try {


            StringBuilder sbCount = new StringBuilder(
                    "select count(1) "
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
                            + "	,		tccm.CUST_NAME "
                            + "	,		tbimc.ESMT_AMT "
                            + "	,		DATE_FORMAT(tbimc.SUBMIT_DATE, '%Y-%m-%d %H:%i') as SUBMIT_DATE "
                            + "	,		tbimc.SUCC_YN "
                            + "	,		COUNT(1) as CNT "
                            + "	from t_bi_info_mat_cust tbimc "
                            + " inner join t_co_cust_master tccm "
                            + " on tbimc.CUST_CODE = tccm.CUST_CODE "
                            + "	where tbimc.ESMT_YN = '2' "
                            + "	group by tbimc.BI_NO, tccm.CUST_NAME, tbimc.ESMT_AMT, SUBMIT_DATE, tbimc.SUCC_YN "
                            + " ORDER BY tbimc.SUCC_YN "
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

            sbCount.append("order by tbim.UPDATE_DATE desc ");



            //쿼리 실행
            Query queryList = entityManager.createNativeQuery(sbCount.toString());



            //조건 대입
            if(userAuth.equals("4")) {
                queryList.setParameter("userId", userId);

            }

            queryList.setParameter("startDate", params.get("startDate") + " 00:00:00");
            queryList.setParameter("endDate", params.get("endDate") + " 23:59:59");


            if (!StringUtils.isEmpty(params.get("interrelatedCustCode"))) {
                queryList.setParameter("interrelatedCustCode", params.get("interrelatedCustCode"));
            }
            BigInteger count = (BigInteger) queryList.getSingleResult();
            return count.intValue();
        }catch(Exception e) {
            log.error("bidDetailList list error : {}", e);
            return 0;
        }
    }

    /**
     * 입찰실적 상세 내역
     * @param params
     * @return
     */

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int findBiInfoDetailListCnt(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
        String userAuth = userOptional.get().getUserAuth();


        try {
            StringBuilder sbCount = new StringBuilder(
                    "SELECT COUNT(*) AS TOTAL_COUNT\r\n"
                            + "FROM (\r\n"
                            + "    SELECT  1 \r\n"
                            + "    FROM t_bi_info_mat tbim\r\n"
                            + "        INNER JOIN t_co_interrelated tci ON tbim.INTERRELATED_CUST_CODE = tci.INTERRELATED_CUST_CODE\r\n"
                            + "        INNER JOIN t_co_item tcitem ON tbim.ITEM_CODE = tcitem.ITEM_CODE\r\n"
                            + "        INNER JOIN t_bi_info_mat_cust tbimc ON tbim.BI_NO = tbimc.BI_NO"
                            + "    WHERE\r\n"
                            + "        tbim.ING_TAG = 'A5'\r\n"
                            + "        AND DATE(tbim.UPDATE_DATE) BETWEEN :startDay AND :endDay\r\n"
            );



            Object coInter = params.get("coInter");
            if("4".equals(userAuth) || coInter != ""){
                sbCount.append("AND tbim.INTERRELATED_CUST_CODE IN(" + coInter + ")\r\n");
            }

            String itemCode = params.get("itemCode").toString();
            if(!"".equals(itemCode)){
                sbCount.append("AND tbim.ITEM_CODE = " + itemCode +"\r\n");
            }
            sbCount.append(
                    "GROUP BY\r\n"
                            + " tbim.BI_NO\r\n"
                            + "	) AS cnt"
            );

            //쿼리 실행
            Query queryTotal = entityManager.createNativeQuery(sbCount.toString());


            queryTotal.setParameter("startDay", params.get("startDay"));
            queryTotal.setParameter("endDay", params.get("endDay"));

            BigInteger count = (BigInteger) queryTotal.getSingleResult();
            return count.intValue();

        }catch(Exception e) {
            log.error("bidDetailList cnt error : {}", e);
            return 0;
        }
    }

    /**
     * 입찰실적 상세 내역
     * @param params
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<BiInfoDetailDto> findBiInfoDetailList(Map<String, Object> params, int start, int limit) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
        String userAuth = userOptional.get().getUserAuth();

        try {
            StringBuilder sbList = new StringBuilder(
                    "   SELECT\r\n"
                            + "        tbim.BI_NO AS BI_NO,\r\n"
                            + "        tbim.BI_NAME AS BI_NAME,\r\n"
                            + "        tcitem.ITEM_NAME AS ITEM_NAME,\r\n"
                            + "        IFNULL(tbim.BD_AMT,0) AS BD_AMT,\r\n"
                            + "        IFNULL(tbim.SUCC_AMT,0) AS SUCC_AMT,\r\n"
                            + "        IFNULL(tbim.REAL_AMT, 0) AS REAL_AMT,\r\n"
                            + "        (\r\n"
                            + "            SELECT COUNT(*)\r\n"
                            + "            FROM t_bi_info_mat_cust tbimc3\r\n"
                            + "            WHERE tbimc3.BI_NO = tbim.BI_NO\r\n"
                            + "            GROUP BY BI_NO\r\n"
                            + "        ) AS CUST_CNT,\r\n"
                            + "        (\r\n"
                            + "            SELECT tccm2.CUST_NAME\r\n"
                            + "            FROM t_co_cust_master tccm2\r\n"
                            + "            INNER JOIN t_bi_info_mat_cust tbimc2 ON tccm2.CUST_CODE = tbimc2.CUST_CODE\r\n"
                            + "            WHERE tbimc2.BI_NO = tbimc.BI_NO\r\n"
                            + "            AND tbimc2.SUCC_YN = 'Y'\r\n"
                            + "        ) AS CUST_NAME,\r\n"
                            + "        DATE_FORMAT(tbim.EST_START_DATE, '%Y-%m-%d') AS EST_START_DATE,\r\n"
                            + "        DATE_FORMAT(tbim.EST_CLOSE_DATE, '%Y-%m-%d') AS EST_CLOSE_DATE,\r\n"
                            + "        MAX(tbimc.ESMT_AMT) AS ESMT_AMT_MAX,\r\n"
                            + "        CASE\r\n"
                            + "            WHEN MAX(tbimc.ESMT_AMT) = MIN(tbimc.ESMT_AMT) THEN 0\r\n"
                            + "            ELSE MIN(tbimc.ESMT_AMT)\r\n"
                            + "        END AS ESMT_AMT_MIN,\r\n"
                            + "         (\r\n"
                            + "	        MAX(tbimc.ESMT_AMT) "
                            + "			-	\r\n"
                            + "            CASE\r\n"
                            + "		        WHEN MAX(tbimc.ESMT_AMT) = MIN(tbimc.ESMT_AMT) THEN 0\r\n"
                            + "		        ELSE MIN(tbimc.ESMT_AMT)\r\n"
                            + "       		END \r\n"
                            + "       	) AS ESMT_AMT_DEV,\r\n "
                            + "        IFNULL((\r\n"
                            + "            SELECT tbimc1.BI_ORDER\r\n"
                            + "            FROM t_bi_info_mat_cust tbimc1\r\n"
                            + "            WHERE tbimc1.BI_NO = tbimc.BI_NO\r\n"
                            + "            AND tbimc1.SUCC_YN = 'Y'\r\n"
                            + "        ),0) AS RE_BID_CNT\r\n"
                            + "    FROM t_bi_info_mat tbim\r\n"
                            + "        INNER JOIN t_co_interrelated tci ON tbim.INTERRELATED_CUST_CODE = tci.INTERRELATED_CUST_CODE\r\n"
                            + "        INNER JOIN t_co_item tcitem ON tbim.ITEM_CODE = tcitem.ITEM_CODE\r\n"
                            + "        INNER JOIN t_bi_info_mat_cust tbimc ON tbim.BI_NO = tbimc.BI_NO"
                            + "    WHERE\r\n"
                            + "        tbim.ING_TAG = 'A5'\r\n"
                            + "        AND DATE(tbim.UPDATE_DATE) BETWEEN :startDay AND :endDay\r\n"
            );

            Object coInter = params.get("coInter");
            if("4".equals(userAuth) || coInter != ""){
                sbList.append("AND tbim.INTERRELATED_CUST_CODE IN(" + coInter + ")\r\n");
            }

            String itemCode = params.get("itemCode").toString();
            if(!"".equals(itemCode)){
                sbList.append("AND tbim.ITEM_CODE = " + itemCode +"\r\n");
            }

            sbList.append(
                    "GROUP BY\r\n"
                            + " tbim.BI_NO"
            );

            //쿼리 실행
            Query queryList = entityManager.createNativeQuery(sbList.toString())
                    .setFirstResult(start) // 결과의 시작 위치를 10으로 설정
                    .setMaxResults(limit); // 최대 결과 수를 10으로 설정

            queryList.setParameter("startDay", params.get("startDay"));
            queryList.setParameter("endDay", params.get("endDay"));


            List list = new JpaResultMapper().list(queryList, BiInfoDetailDto.class);

            return list;

        }catch(Exception e) {
            log.error("bidDetailList list error : {}", e);
            return Collections.emptyList();
        }
    }


}
