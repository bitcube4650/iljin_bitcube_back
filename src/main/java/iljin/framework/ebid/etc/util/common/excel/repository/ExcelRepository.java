package iljin.framework.ebid.etc.util.common.excel.repository;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.bid.dto.BidCompleteCustDto;
import iljin.framework.ebid.bid.dto.BidCompleteDto;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.common.excel.dto.BidCompleteExcelDto;
import iljin.framework.ebid.etc.util.common.excel.entity.FileEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

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

    public List<BidCompleteDto> findComplateBidList(Map<String, Object> params) {
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
                            + "	where tbimc.ESMT_YN = '2' "
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

            //List list = queryList.getResultList(); // 페이지네이션 없이 모든 데이터를 조회합니다.

          //  Pageable pageable = PagaUtils.pageable(params);
         //   queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
            List<BidCompleteDto> list = new JpaResultMapper().list(queryList, BidCompleteDto.class);

            return list;

         //   BigInteger count = (BigInteger) queryTotal.getSingleResult();
        //    Page listPage = new PageImpl(list, pageable, count.intValue());
         //   resultBody.setData(listPage);

        }catch(Exception e) {
            log.error("complateBidhistory list error : {}", e);
            return Collections.emptyList();
         //   resultBody.setCode("999");
        //    resultBody.setMsg("입찰 이력 리스트를 가져오는것을 실패하였습니다.");
        }

    }

    public List<BidCompleteCustDto> joinCustList(Map<String, Object> params) {
      //  ResultBody resultBody = new ResultBody();

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

            List<BidCompleteCustDto> list = new JpaResultMapper().list(queryList, BidCompleteCustDto.class);
         //   resultBody.setData(list);
            return list;
        }catch(Exception e) {
            log.error("joinCustList list error : {}", e);
            return Collections.emptyList();
        //    resultBody.setCode("999");
       //     resultBody.setMsg("투찰 정보를 가져오는것을 실패하였습니다.");
        }
    }



    public List<BidCompleteExcelDto> findComplateBidListV2(Map<String, Object> params) {
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

           // queryList.setMaxResults(10);

            //List list = queryList.getResultList(); // 페이지네이션 없이 모든 데이터를 조회합니다.

            //  Pageable pageable = PagaUtils.pageable(params);
            //   queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
            List<BidCompleteExcelDto> list = new JpaResultMapper().list(queryList, BidCompleteExcelDto.class);

            return list;

            //   BigInteger count = (BigInteger) queryTotal.getSingleResult();
            //    Page listPage = new PageImpl(list, pageable, count.intValue());
            //   resultBody.setData(listPage);

        }catch(Exception e) {
            log.error("complateBidhistory list error : {}", e);
            return Collections.emptyList();
            //   resultBody.setCode("999");
            //    resultBody.setMsg("입찰 이력 리스트를 가져오는것을 실패하였습니다.");
        }

    }


}
