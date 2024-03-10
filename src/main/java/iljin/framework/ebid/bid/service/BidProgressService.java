package iljin.framework.ebid.bid.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.UserDto;
import iljin.framework.core.security.user.UserRepository;
import iljin.framework.core.security.user.UserRepositoryCustom;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.BidProgressCustDto;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.dto.BidProgressDto;
import iljin.framework.ebid.bid.dto.BidProgressFileDto;
import iljin.framework.ebid.bid.dto.BidProgressTableDto;
import iljin.framework.ebid.bid.dto.CoUserInfoDto;
import iljin.framework.ebid.bid.dto.EmailDto;
import iljin.framework.ebid.bid.dto.InterUserInfoDto;
import iljin.framework.ebid.bid.dto.InterrelatedCustDto;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.PagaUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class BidProgressService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TCoUserRepository tCoUserRepository;
    @Autowired
    Util util;

    public Page custList(@RequestBody Map<String, Object> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());

        String interrelatedCode = userOptional.get().getInterrelatedCustCode();

        
        StringBuilder sbCount = new StringBuilder(
                " select count(1) FROM t_co_cust_master a WHERE a.interrelated_cust_code = :interrelatedCode");
        StringBuilder sbList = new StringBuilder(
                "SELECT CAST(a.cust_code AS CHAR) AS cust_code, a.cust_name AS cust_name, a.pres_name AS pres_name," +
                        "CONCAT('(', a.zipcode, ')', a.addr, ' ', a.addr_detail) AS combined_addr, " +
                        "a.interrelated_cust_code AS interrelated_cust_code " +
                        "FROM t_co_cust_master a WHERE a.interrelated_cust_code = :interrelatedCode");

        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("custName"))) {
            sbWhere.append(" and a.cust_name like concat('%',:custName,'%')");
        }

        if (!StringUtils.isEmpty(params.get("chairman"))) {
            sbWhere.append(" and a.pres_name like concat('%',:chairman,'%')");
        }
        sbList.append(sbWhere);
        sbCount.append(sbWhere);
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        Query queryCountList = entityManager.createNativeQuery(sbCount.toString());
        queryList.setParameter("interrelatedCode", interrelatedCode);
        if (!StringUtils.isEmpty(params.get("custName"))) {
            queryList.setParameter("custName", params.get("custName"));
            queryCountList.setParameter("custName", params.get("custName"));
        }
        if (!StringUtils.isEmpty(params.get("chairman"))) {
            queryList.setParameter("chairman", params.get("chairman"));
            queryCountList.setParameter("chairman", params.get("chairman"));
        }
        queryList.setParameter("interrelatedCode", interrelatedCode);
        queryCountList.setParameter("interrelatedCode", interrelatedCode);

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, InterrelatedCustDto.class);

        BigInteger count = (BigInteger) queryCountList.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    public Page progresslist(@RequestBody Map<String, Object> params) {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());

        String userAuth = userOptional.get().getUserAuth();
        String interrelatedCode = userOptional.get().getInterrelatedCustCode();
        System.out.println("code111111111111111111111="+interrelatedCode);
        String userId = principal.getUsername();

        StringBuilder sbCount = new StringBuilder(
                " select count(1) from t_bi_info_mat a where 1=1 AND a.ing_tag = 'A0' ");
        StringBuilder sbList = new StringBuilder(
                "SELECT a.bi_no AS bi_no, a.bi_name AS bi_name, " +
                        "DATE_FORMAT(a.est_start_date, '%Y-%m-%d %H:%i') AS est_start_date, " +
                        "DATE_FORMAT(a.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date, " +
                        "CASE WHEN a.bi_mode = 'A' THEN '지명' ELSE '일반' END AS bi_mode, " +
                        "CASE WHEN a.ins_mode = '1' THEN '파일' ELSE '직접입력' END AS ins_mode, " +
                        "b.user_name AS est_opener, b.user_email AS opener_email, " +
                        "c.user_name AS gongo_id, c.user_email AS gongo_email, " +
                        "a.interrelated_cust_code AS interrelated_cust_code " +
                        "FROM t_bi_info_mat a LEFT JOIN t_co_user b ON a.est_opener = b.user_id LEFT JOIN t_co_user c ON a.gongo_id = c.user_id " +
                        "WHERE a.ing_tag = 'A0'");
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("bidNo"))) {
            sbWhere.append(" and a.bi_no like concat('%',:bidNo,'%') ");
        }

        if (!StringUtils.isEmpty(params.get("bidName"))) {
            sbWhere.append(" and a.bi_name like concat('%',:bidName,'%') ");
        }

        if (userAuth.equals("1")) {
            sbWhere.append(" AND a.interrelated_cust_code = :interrelatedCustCode ");
        }

        if (userAuth.equals("2") || userAuth.equals("3")) {
            sbWhere.append(" AND a.interrelated_cust_code = :interrelatedCustCode " +
                    "and a.create_user = :userid " +
                    "or a.open_att1 = :userid " +
                    "or a.open_att2 = :userid " +
                    "or a.gongo_id = :userid");
        }

        if (userAuth.equals("4")) {
            List<InterUserInfoDto> userInfoList = (List<InterUserInfoDto>) findInterCustCode(userId);
            List<String> custCodes = new ArrayList<>();
            for (InterUserInfoDto userInfo : userInfoList) {
                custCodes.add(userInfo.getInterrelatedCustCode());
            }

            sbWhere.append(" and (");
            for (int i = 0; i < custCodes.size(); i++) {
                if (i > 0) {
                    sbWhere.append(" or ");
                }
                sbWhere.append("a.interrelated_cust_code = :custCode").append(i);
            }
            sbWhere.append(")");
        }
        sbList.append(sbWhere);

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        sbCount.append(sbWhere);
        Query queryTotal = entityManager.createNativeQuery(sbCount.toString());

        if (!StringUtils.isEmpty(params.get("bidNo"))) {
            queryList.setParameter("bidNo", params.get("bidNo"));
            queryTotal.setParameter("bidNo", params.get("bidNo"));
        }
        if (!StringUtils.isEmpty(params.get("bidName"))) {
            queryList.setParameter("bidName", params.get("bidName"));
            queryTotal.setParameter("bidName", params.get("bidName"));
        }
        if (userAuth.equals("1")) {
            queryList.setParameter("interrelatedCustCode", interrelatedCode);
            queryTotal.setParameter("interrelatedCustCode", interrelatedCode);
        }
        if (userAuth.equals("2") || userAuth.equals("3")) {
            queryList.setParameter("interrelatedCustCode", interrelatedCode);
            queryTotal.setParameter("interrelatedCustCode", interrelatedCode);
            queryList.setParameter("userid", userId);
            queryTotal.setParameter("userid", userId);
            queryList.setParameter("userid", userId);
            queryTotal.setParameter("userid", userId);
            queryList.setParameter("userid", userId);
            queryTotal.setParameter("userid", userId);
        }
        if (userAuth.equals("4")) {
            List<InterUserInfoDto> userInfoList = (List<InterUserInfoDto>) findInterCustCode(userId);
            List<String> custCodes = new ArrayList<>();
            for (InterUserInfoDto userInfo : userInfoList) {
                custCodes.add(userInfo.getInterrelatedCustCode());
            }

            sbWhere.append(" and (");
            for (int i = 0; i < custCodes.size(); i++) {
                queryList.setParameter("custCode" + i, custCodes.get(i));
                queryTotal.setParameter("custCode" + i, custCodes.get(i));
            }
        }


        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, BidProgressDto.class);

        BigInteger count = (BigInteger) queryTotal.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    public List<List<?>> progresslistDetail(String param) {
        StringBuilder sbList = new StringBuilder(
                "SELECT a.bi_no AS bi_no, a.bi_name AS bi_name, " +
                        "CASE WHEN a.bi_mode = 'A' THEN '지명경쟁입찰' ELSE '일반경쟁입찰' END AS bi_mode, a.bi_mode AS bi_mode_code, "
                        +
                        "CASE WHEN a.ins_mode = '1' THEN '파일등록' ELSE '직접입력' END AS ins_mode, a.ins_mode AS ins_mode_code, "
                        +
                        "a.bid_join_spec AS bid_join_spec, a.special_cond AS special_cond, a.supply_cond AS supply_cond, "
                        +
                        "DATE_FORMAT(a.spot_date, '%Y-%m-%d %H:%i') AS spot_date, a.spot_area AS spot_area, " +
                        "CASE WHEN a.succ_deci_meth = '1' THEN '최저가' WHEN a.succ_deci_meth = '2' THEN '최고가' " +
                        "WHEN a.succ_deci_meth = '3' THEN '내부적격심사' WHEN a.succ_deci_meth = '4' THEN '최고가&내부적격심사' " +
                        "ELSE '최저가&내부적격심사' END AS succ_deci_meth, a.succ_deci_meth AS succ_deci_meth_code, DATE_FORMAT(a.est_start_date, '%Y-%m-%d %H:%i') AS est_start_date, "
                        +
                        "DATE_FORMAT(a.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date, b.user_name AS est_opener, a.est_opener AS est_opener_code,"
                        +
                        "DATE_FORMAT(a.est_open_date, '%Y-%m-%d %H:%i') AS est_open_date, c.user_name AS open_att1, a.open_att1 AS open_att1_code,"
                        +
                        "a.open_att1_sign AS open_att1_sign, d.user_name AS open_att2, a.open_att2 AS open_att2_code, a.open_att2_sign AS open_att2_sign, "
                        +
                        "a.ing_tag AS ing_tag, a.item_code AS item_code, f.item_name AS item_name, e.user_name AS gongo_id, a.gongo_id AS gongo_id_code, g.dept_name AS gongo_dept, a.pay_cond AS pay_cond, a.why_A3 AS why_A3, "
                        +
                        "a.why_A7 AS why_A7, a.bi_open AS bi_open, a.interrelated_cust_code AS interrelated_cust_code, h.interrelated_nm AS interrelated_nm, a.real_amt AS real_amt, a.amt_basis AS amt_basis, a.bd_amt AS bd_amt,"
                        +
                        "a.add_accept AS add_accept, a.mat_dept AS mat_dept, a.mat_proc AS mat_proc, a.mat_cls AS mat_cls, "
                        +
                        "a.mat_factory AS mat_factory, a.mat_factory_line AS mat_factory_line, a.mat_factory_cnt AS mat_factory_cnt "
                        +
                        "FROM t_bi_info_mat a " +
                        "JOIN t_co_user b ON a.est_opener = b.user_id " +
                        "LEFT JOIN t_co_user c ON a.open_att1 = c.user_id " +
                        "LEFT JOIN t_co_user d ON a.open_att2 = d.user_id " +
                        "JOIN t_co_user e ON a.gongo_id = e.user_id " +
                        "LEFT JOIN t_co_item f ON a.item_code = f.item_code " +
                        "LEFT JOIN t_co_user g ON a.gongo_id = g.user_id " +
                        "JOIN t_co_interrelated h ON a.interrelated_cust_code = h.interrelated_cust_code " +
                        "WHERE 1=1 ");

        StringBuilder sbTableList = new StringBuilder(
                "SELECT a.bi_no AS bi_no, a.seq AS seq, a.name AS name, a.ssize AS ssize, " +
                        "a.order_qty AS order_qty, a.unitcode AS unitcode, ifnull(a.order_uc, 0) AS order_uc " +
                        "FROM t_bi_spec_mat a " +
                        "WHERE 1=1 ");

        StringBuilder sbFileList = new StringBuilder(
                "SELECT a.bi_no AS bi_no, a.file_flag AS file_flag, " +
                        "CASE WHEN a.file_flag = 'K' THEN '세부내역' WHEN a.file_flag = '0' THEN '대내용' WHEN a.file_flag = '1' THEN '대외용' END AS file_flag_ko, "
                        +
                        "a.file_nm AS file_NM, a.file_path AS file_path " +
                        "FROM t_bi_upload a " +
                        "WHERE a.use_yn = 'Y' ");

        StringBuilder sbCustList = new StringBuilder(
                "SELECT a.bi_no AS bi_no, CAST(a.cust_code AS CHAR) AS cust_code, b.cust_name AS cust_name " +
                        "FROM t_bi_info_mat_cust a " +
                        "LEFT JOIN t_co_cust_master b ON a.cust_code = b.cust_code " +
                        "WHERE 1=1 ");

        StringBuilder sbWhere = new StringBuilder();
        sbWhere.append(" and a.bi_no = :param");
        sbList.append(sbWhere);
        sbTableList.append(sbWhere);
        sbFileList.append(sbWhere);
        sbCustList.append(sbWhere);

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        Query queryTableList = entityManager.createNativeQuery(sbTableList.toString());
        Query queryFileList = entityManager.createNativeQuery(sbFileList.toString());
        Query queryCustList = entityManager.createNativeQuery(sbCustList.toString());
        queryList.setParameter("param", param);
        queryTableList.setParameter("param", param);
        queryFileList.setParameter("param", param);
        queryCustList.setParameter("param", param);

        List<BidProgressDetailDto> resultList = new JpaResultMapper().list(queryList, BidProgressDetailDto.class);
        List<BidProgressTableDto> tableList = new JpaResultMapper().list(queryTableList, BidProgressTableDto.class);
        List<BidProgressFileDto> fileList = new JpaResultMapper().list(queryFileList, BidProgressFileDto.class);
        List<BidProgressCustDto> custList = new JpaResultMapper().list(queryCustList, BidProgressCustDto.class);
        List<List<?>> combinedResults = new ArrayList<>();
        combinedResults.add(resultList);
        combinedResults.add(tableList);
        combinedResults.add(fileList);
        combinedResults.add(custList);

        return combinedResults;
    }

    public Page findCoUserInfo(@RequestBody Map<String, Object> params) {
        String searchType = (String) params.get("type");
        Query queryList;
        Query queryTotal;
        StringBuilder sbList = new StringBuilder(
                "SELECT a.user_id AS user_id, a.user_name AS user_name, a.dept_name AS dept_name, a.user_auth AS user_auth, a.interrelated_cust_code AS interrelated_cust_code, a.openauth AS open_auth "
                        +
                        "FROM t_co_user a " +
                        "WHERE 1=1 ");
        StringBuilder sbCount = new StringBuilder("SELECT count(1) FROM t_co_user a WHERE 1=1 ");
        StringBuilder sbWhere = new StringBuilder();

        switch (searchType) {
            case "openBidUser":
                sbWhere.append("AND a.openauth = '1' AND a.interrelated_cust_code = :interrelatedCD ");

                if (!StringUtils.isEmpty(params.get("userName"))) {
                    sbWhere.append("AND a.user_name like concat('%',:userName,'%')");
                }
                if (!StringUtils.isEmpty(params.get("deptName"))) {
                    sbWhere.append("AND a.dept_name like concat('%',:deptName,'%')");
                }
                sbList.append(sbWhere);
                sbCount.append(sbWhere);
                queryList = entityManager.createNativeQuery(sbList.toString());
                queryTotal = entityManager.createNativeQuery(sbCount.toString());
                queryList.setParameter("interrelatedCD", params.get("interrelatedCD"));
                queryTotal.setParameter("interrelatedCD", params.get("interrelatedCD"));

                if (!StringUtils.isEmpty(params.get("userName"))) {
                    queryList.setParameter("userName", params.get("userName"));
                    queryTotal.setParameter("userName", params.get("userName"));
                }
                if (!StringUtils.isEmpty(params.get("deptName"))) {
                    queryList.setParameter("deptName", params.get("deptName"));
                    queryTotal.setParameter("deptName", params.get("deptName"));
                }
                break;
            case "normalUser":
                sbWhere.append("AND a.interrelated_cust_code = :interrelatedCD ");

                if (!StringUtils.isEmpty(params.get("userName"))) {
                    sbWhere.append("AND a.user_name like concat('%',:userName,'%')");
                }
                if (!StringUtils.isEmpty(params.get("deptName"))) {
                    sbWhere.append("AND a.dept_name like concat('%',:deptName,'%')");
                }
                sbList.append(sbWhere);
                sbCount.append(sbWhere);
                queryList = entityManager.createNativeQuery(sbList.toString());
                queryTotal = entityManager.createNativeQuery(sbCount.toString());
                queryList.setParameter("interrelatedCD", params.get("interrelatedCD"));
                queryTotal.setParameter("interrelatedCD", params.get("interrelatedCD"));

                if (!StringUtils.isEmpty(params.get("userName"))) {
                    queryList.setParameter("userName", params.get("userName"));
                    queryTotal.setParameter("userName", params.get("userName"));
                }
                if (!StringUtils.isEmpty(params.get("deptName"))) {
                    queryList.setParameter("deptName", params.get("deptName"));
                    queryTotal.setParameter("deptName", params.get("deptName"));
                }
                break;
            default:
                UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                        .getPrincipal();
                String userId = principal.getUsername();
                sbWhere.append("AND a.user_id = :userId");
                sbList.append(sbWhere);
                sbCount.append(sbWhere);
                queryList = entityManager.createNativeQuery(sbList.toString());
                queryTotal = entityManager.createNativeQuery(sbCount.toString());
                queryList.setParameter("userId", userId);
                queryTotal.setParameter("userId", userId);
                break;
        }
        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, CoUserInfoDto.class);

        BigInteger count = (BigInteger) queryTotal.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    public List<?> findInterCustCode(String param) {
        StringBuilder sbList = new StringBuilder(
                "SELECT a.user_id AS user_id, a.interrelated_cust_code AS interrelated_cust_code " +
                        "FROM t_co_user_interrelated a " +
                        "WHERE a.user_id = :param");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("param", param);
        return new JpaResultMapper().list(queryList, InterUserInfoDto.class);
    }

    @Transactional
    public ResultBody openBid(Map<String, String> params) {
        String biNo = params.get("biNo");

        System.out.println(11111111 + biNo);
        StringBuilder sbList = new StringBuilder(
                "UPDATE t_bi_info_mat set bid_open_date = sysdate()," +
                        "ing_tag = 'A1' " +
                        "WHERE bi_no = :biNo");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biNo", biNo);
        int rowsUpdated = queryList.executeUpdate();

        if (rowsUpdated > 0) {
            Map<String, String> logParams = new HashMap<>();
            logParams.put("msg", "[본사]입찰공고");
            logParams.put("biNo", biNo);
            updateLog(logParams);
        }

        updateEmail(params);

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    @Transactional
    public ResultBody updateBid(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = (Map<String, Object>) params.get("result");

        String bdAmtString = (String) result.get("bdAmt");
        Integer bdAmt = Integer.parseInt(bdAmtString);

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();

        StringBuilder sbList = new StringBuilder( // 입찰 업데이트
                "UPDATE t_bi_info_mat SET bi_name = :biName, bi_mode = :biModeCode, ins_mode = :insModeCode, " +
                        "bid_join_spec = :bidJoinSpec, special_cond = :specialCond, supply_cond = :supplyCond, " +
                        "spot_date = STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), spot_area = :spotArea, " +
                        "succ_deci_meth = :succDeciMethCode, amt_basis = :amtBasis, bd_amt = :bdAmt, " +
                        "est_start_date = STR_TO_DATE(:estStartDate, '%Y-%m-%d %H:%i'), " +
                        "est_close_date = STR_TO_DATE(:estCloseDate, '%Y-%m-%d %H:%i'), est_opener = :estOpenerCode, " +
                        "open_att1 = :openAtt1Code, open_att2 = :openAtt2Code, update_user = :userId, update_date = sysdate(), "
                        +
                        "item_code = :itemCode, gongo_id = :gongoIdCode, pay_cond = :payCond, mat_dept = :matDept, mat_proc = :matProc, "
                        +
                        "mat_cls = :matCls, mat_factory = :matFactory, mat_factory_line = :matFactoryLine, mat_factory_cnt = :matFactoryCnt "
                        +
                        "WHERE bi_no = :biNo");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biName", (String) result.get("biName"));
        queryList.setParameter("biModeCode", (String) result.get("biModeCode"));
        queryList.setParameter("insModeCode", (String) result.get("insModeCode"));
        queryList.setParameter("bidJoinSpec", (String) result.get("bidJoinSpec"));
        queryList.setParameter("specialCond", (String) result.get("specialCond"));
        queryList.setParameter("supplyCond", (String) result.get("supplyCond"));
        queryList.setParameter("spotDate", (String) result.get("spotDate"));
        queryList.setParameter("spotArea", (String) result.get("spotArea"));
        queryList.setParameter("succDeciMethCode", (String) result.get("succDeciMethCode"));
        queryList.setParameter("amtBasis", (String) result.get("amtBasis"));
        queryList.setParameter("bdAmt", bdAmt);
        queryList.setParameter("estStartDate", (String) result.get("estStartDate"));
        queryList.setParameter("estCloseDate", (String) result.get("estCloseDate"));
        queryList.setParameter("estOpenerCode", (String) result.get("estOpenerCode"));
        queryList.setParameter("openAtt1Code", (String) result.get("openAtt1Code"));
        queryList.setParameter("openAtt2Code", (String) result.get("openAtt2Code"));
        queryList.setParameter("userId", userId);
        queryList.setParameter("itemCode", (String) result.get("itemCode"));
        queryList.setParameter("gongoIdCode", (String) result.get("gongoIdCode"));
        queryList.setParameter("payCond", (String) result.get("payCond"));
        queryList.setParameter("matDept", (String) result.get("matDept"));
        queryList.setParameter("matProc", (String) result.get("matProc"));
        queryList.setParameter("matCls", (String) result.get("matCls"));
        queryList.setParameter("matFactory", (String) result.get("matFactory"));
        queryList.setParameter("matFactoryLine", (String) result.get("matFactoryLine"));
        queryList.setParameter("matFactoryCnt", (String) result.get("matFactoryCnt"));
        queryList.setParameter("biNo", (String) result.get("biNo"));

        queryList.executeUpdate();

        StringBuilder sbList1 = new StringBuilder( // 입찰 hist 업데이트
                "INSERT into t_bi_info_mat_hist (bi_no, bi_name, bi_mode, ins_mode, bid_join_spec, special_cond, supply_cond, spot_date, "
                        +
                        "spot_area, succ_deci_meth, amt_basis, bd_amt, est_start_date, est_close_date, est_opener, "
                        +
                        "open_att1, open_att2, ing_tag, update_user, update_date, item_code, " +
                        "gongo_id, pay_cond, bi_open, mat_dept, mat_proc, mat_cls, mat_factory, mat_factory_line, mat_factory_cnt) "
                        +
                        "values (:biNo, :biName, :biModeCode, :insModeCode, :bidJoinSpec, :specialCond, :supplyCond, " +
                        "STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), :spotArea, :succDeciMethCode, :amtBasis, :bdAmt, "
                        +
                        "est_start_date =STR_TO_DATE(:estStartDate, '%Y-%m-%d %H:%i'), est_close_date =STR_TO_DATE(:estCloseDate, '%Y-%m-%d %H:%i'), :estOpenerCode,"
                        +
                        ":openAtt1Code, :openAtt2Code, 'A0', :userId, sysdate(), :itemCode, :gongoIdCode, :payCond, 'N', :matDept, :matProc, :matCls, :matFactory, "
                        +
                        ":matFactoryLine, :matFactoryCnt)");

        Query queryList1 = entityManager.createNativeQuery(sbList1.toString());
        queryList1.setParameter("biNo", (String) result.get("biNo"));
        queryList1.setParameter("biName", (String) result.get("biName"));
        queryList1.setParameter("biModeCode", (String) result.get("biModeCode"));
        queryList1.setParameter("insModeCode", (String) result.get("insModeCode"));
        queryList1.setParameter("bidJoinSpec", (String) result.get("bidJoinSpec"));
        queryList1.setParameter("specialCond", (String) result.get("specialCond"));
        queryList1.setParameter("supplyCond", (String) result.get("supplyCond"));
        queryList1.setParameter("spotDate", (String) result.get("spotDate"));
        queryList1.setParameter("spotArea", (String) result.get("spotArea"));
        queryList1.setParameter("succDeciMethCode", (String) result.get("succDeciMethCode"));
        queryList1.setParameter("amtBasis", (String) result.get("amtBasis"));
        queryList1.setParameter("bdAmt", bdAmt);
        queryList1.setParameter("estStartDate", (String) result.get("estStartDate"));
        queryList1.setParameter("estCloseDate", (String) result.get("estCloseDate"));
        queryList1.setParameter("estOpenerCode", (String) result.get("estOpenerCode"));
        queryList1.setParameter("openAtt1Code", (String) result.get("openAtt1Code"));
        queryList1.setParameter("openAtt2Code", (String) result.get("openAtt2Code"));
        queryList1.setParameter("userId", userId);
        queryList1.setParameter("itemCode", (String) result.get("itemCode"));
        queryList1.setParameter("gongoIdCode", (String) result.get("gongoIdCode"));
        queryList1.setParameter("payCond", (String) result.get("payCond"));
        queryList1.setParameter("matDept", (String) result.get("matDept"));
        queryList1.setParameter("matProc", (String) result.get("matProc"));
        queryList1.setParameter("matCls", (String) result.get("matCls"));
        queryList1.setParameter("matFactory", (String) result.get("matFactory"));
        queryList1.setParameter("matFactoryLine", (String) result.get("matFactoryLine"));
        queryList1.setParameter("matFactoryCnt", (String) result.get("matFactoryCnt"));

        queryList1.executeUpdate();

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    public String newBiNo(){

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());

        String interrelatedCode = userOptional.get().getInterrelatedCustCode();
        String biNoHeader = "";

        switch(interrelatedCode){
            case "01": biNoHeader = "E"; break;
            case "02": biNoHeader = "C"; break;
            case "03": biNoHeader = "D"; break;
            case "04": biNoHeader = "A"; break;
            case "05": biNoHeader = "M"; break;
            case "06": biNoHeader = "S"; break;
            case "07": biNoHeader = "J"; break;
            case "08": biNoHeader = "P"; break;
            case "09": biNoHeader = "G"; break;
            case "10": biNoHeader = "L"; break;
            case "11": biNoHeader = "Z"; break;
            case "12": biNoHeader = "T"; break;
            case "13": biNoHeader = "K"; break;
            case "14": biNoHeader = "N";
        }

        LocalDate currentDate = LocalDate.now();
        String biNoYear = currentDate.format(DateTimeFormatter.ofPattern("yyyy"));
        String biNoMonth = currentDate.format(DateTimeFormatter.ofPattern("MM"));

        String combinedBiNo = biNoHeader + biNoYear + biNoMonth;

        StringBuilder binoList = new StringBuilder( // 입찰번호 seq 조회
        "SELECT CONCAT(MAX(CAST(SUBSTRING(bi_no, LENGTH(bi_no) - 2) AS UNSIGNED) + 1)) FROM t_bi_info_mat WHERE bi_no LIKE concat(:combinedBiNo,'%')");
        Query biNoQ = entityManager.createNativeQuery(binoList.toString());
        biNoQ.setParameter("combinedBiNo", combinedBiNo);
        String seq = (String) biNoQ.getSingleResult();

        if (seq == null) {
            seq = "001";
        } else {
            // 3자리로 포맷팅
            seq = String.format("%03d", Integer.parseInt(seq));
        }
        String biNo = combinedBiNo + seq;
        return biNo;
    }

    @Transactional
    public ResultBody insertBid(@RequestBody Map<String, Object> params) {
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println(Integer.parseInt((String) params.get("bdAmt")));

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String userId = principal.getUsername();

        String bdAmtString = (String) params.get("bdAmt");
        Integer bdAmt = Integer.parseInt(bdAmtString);

        StringBuilder sbList = new StringBuilder( // 입찰 insert
                "INSERT into t_bi_info_mat (bi_no, bi_name, bi_mode, ins_mode, bid_join_spec, special_cond, supply_cond, spot_date, " +
                        "spot_area, succ_deci_meth, bid_open_date, amt_basis, bd_amt, est_start_date, est_close_date, est_opener, open_att1, " +
                        "open_att2, ing_tag, create_user, create_date, item_code, gongo_id, pay_cond, bi_open, interrelated_cust_code, mat_dept, " + 
                        "mat_proc, mat_cls, mat_factory, mat_factory_line, mat_factory_cnt) values (:biNo, :biName, :biModeCode, :insModeCode, :bidJoinSpec, " +
                        ":specialCond, :supplyCond, STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), :spotArea, :succDeciMethCode, sysdate(), " +
                        ":amtBasis, :bdAmt, STR_TO_DATE(:estStartDate, '%Y-%m-%d %H:%i'), STR_TO_DATE(:estCloseDate, '%Y-%m-%d %H:%i'), " +
                        ":estOpenerCode, :openAtt1Code, :openAtt2Code, 'A0', :userId, sysdate(), :itemCode, :gongoIdCode, :payCond, 'N', " +
                        ":interrelatedCustCode, :matDept, :matProc, :matCls, :matFactory, :matFactoryLine, :matFactoryCnt)" );

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biNo", (String) params.get("biNo"));
        queryList.setParameter("biName", (String) params.get("biName"));
        queryList.setParameter("biModeCode", (String) params.get("biModeCode"));
        queryList.setParameter("insModeCode", (String) params.get("insModeCode"));
        queryList.setParameter("bidJoinSpec", (String) params.get("bidJoinSpec"));
        queryList.setParameter("specialCond", (String) params.get("specialCond"));
        queryList.setParameter("supplyCond", (String) params.get("supplyCond"));
        queryList.setParameter("spotDate", (String) params.get("spotDate"));
        queryList.setParameter("spotArea", (String) params.get("spotArea"));
        queryList.setParameter("succDeciMethCode", (String) params.get("succDeciMethCode"));
        queryList.setParameter("amtBasis", (String) params.get("amtBasis"));
        queryList.setParameter("bdAmt", bdAmt);
        queryList.setParameter("estStartDate", (String) params.get("estStartDate"));
        queryList.setParameter("estCloseDate", (String) params.get("estCloseDate"));
        queryList.setParameter("estOpenerCode", (String) params.get("estOpenerCode"));
        queryList.setParameter("openAtt1Code", (String) params.get("openAtt1Code"));
        queryList.setParameter("openAtt2Code", (String) params.get("openAtt2Code"));
        queryList.setParameter("userId", userId);
        queryList.setParameter("itemCode", (String) params.get("itemCode"));
        queryList.setParameter("gongoIdCode", (String) params.get("gongoIdCode"));
        queryList.setParameter("payCond", (String) params.get("payCond"));
        queryList.setParameter("interrelatedCustCode", (String) params.get("interrelatedCustCode"));
        queryList.setParameter("matDept", (String) params.get("matDept"));
        queryList.setParameter("matProc", (String) params.get("matProc"));
        queryList.setParameter("matCls", (String) params.get("matCls"));
        queryList.setParameter("matFactory", (String) params.get("matFactory"));
        queryList.setParameter("matFactoryLine", (String) params.get("matFactoryLine"));
        queryList.setParameter("matFactoryCnt", (String) params.get("matFactoryCnt"));
        
        queryList.executeUpdate();

        StringBuilder sbList1 = new StringBuilder( // 입찰 Hist insert
                "INSERT into t_bi_info_mat_hist (bi_no, bi_name, bi_mode, ins_mode, bid_join_spec, special_cond, supply_cond, spot_date, " +
                        "spot_area, succ_deci_meth, bid_open_date, amt_basis, bd_amt, est_start_date, est_close_date, est_opener, open_att1, " +
                        "open_att2, ing_tag, create_user, create_date, item_code, gongo_id, pay_cond, bi_open, interrelated_cust_code, mat_dept, " + 
                        "mat_proc, mat_cls, mat_factory, mat_factory_line, mat_factory_cnt) values (:biNo, :biName, :biModeCode, :insModeCode, :bidJoinSpec, " +
                        ":specialCond, :supplyCond, STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), :spotArea, :succDeciMethCode, sysdate(), " +
                        ":amtBasis, :bdAmt, STR_TO_DATE(:estStartDate, '%Y-%m-%d %H:%i'), STR_TO_DATE(:estCloseDate, '%Y-%m-%d %H:%i'), " +
                        ":estOpenerCode, :openAtt1Code, :openAtt2Code, 'A0', :userId, sysdate(), :itemCode, :gongoIdCode, :payCond, 'N', " +
                        ":interrelatedCustCode, :matDept, :matProc, :matCls, :matFactory, :matFactoryLine, :matFactoryCnt)" );

        Query queryList1 = entityManager.createNativeQuery(sbList1.toString());
        queryList1.setParameter("biNo", (String) params.get("biNo"));
        queryList1.setParameter("biName", (String) params.get("biName"));
        queryList1.setParameter("biModeCode", (String) params.get("biModeCode"));
        queryList1.setParameter("insModeCode", (String) params.get("insModeCode"));
        queryList1.setParameter("bidJoinSpec", (String) params.get("bidJoinSpec"));
        queryList1.setParameter("specialCond", (String) params.get("specialCond"));
        queryList1.setParameter("supplyCond", (String) params.get("supplyCond"));
        queryList1.setParameter("spotDate", (String) params.get("spotDate"));
        queryList1.setParameter("spotArea", (String) params.get("spotArea"));
        queryList1.setParameter("succDeciMethCode", (String) params.get("succDeciMethCode"));
        queryList1.setParameter("amtBasis", (String) params.get("amtBasis"));
        queryList1.setParameter("bdAmt", bdAmt);
        queryList1.setParameter("estStartDate", (String) params.get("estStartDate"));
        queryList1.setParameter("estCloseDate", (String) params.get("estCloseDate"));
        queryList1.setParameter("estOpenerCode", (String) params.get("estOpenerCode"));
        queryList1.setParameter("openAtt1Code", (String) params.get("openAtt1Code"));
        queryList1.setParameter("openAtt2Code", (String) params.get("openAtt2Code"));
        queryList1.setParameter("userId", userId);
        queryList1.setParameter("itemCode", (String) params.get("itemCode"));
        queryList1.setParameter("gongoIdCode", (String) params.get("gongoIdCode"));
        queryList1.setParameter("payCond", (String) params.get("payCond"));
        queryList1.setParameter("interrelatedCustCode", (String) params.get("interrelatedCustCode"));
        queryList1.setParameter("matDept", (String) params.get("matDept"));
        queryList1.setParameter("matProc", (String) params.get("matProc"));
        queryList1.setParameter("matCls", (String) params.get("matCls"));
        queryList1.setParameter("matFactory", (String) params.get("matFactory"));
        queryList1.setParameter("matFactoryLine", (String) params.get("matFactoryLine"));
        queryList1.setParameter("matFactoryCnt", (String) params.get("matFactoryCnt"));
        
        queryList1.executeUpdate();

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    @Transactional
    public ResultBody updateBidCust(@RequestBody List<Map<String, Object>> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                        .getPrincipal();
        String userId = principal.getUsername();
        

        String biNo = (String) params.get(0).get("biNo");
        String type = (String) params.get(0).get("type");
    
        StringBuilder init = new StringBuilder(
            "DELETE from t_bi_info_mat_cust where bi_no = :biNo"
        );

        Query initQuery = entityManager.createNativeQuery(init.toString());
        initQuery.setParameter("biNo", biNo);
        initQuery.executeUpdate();

        for (Map<String, Object> data : params) {
            StringBuilder sbList = new StringBuilder(
                    "INSERT into t_bi_info_mat_cust (bi_no, cust_code, rebid_att, esmt_yn, esmt_amt, succ_yn, create_user, create_date) "+
                            "values (:biNo, :custCode, 'N', '0', 0, 'N', :userId, sysdate())");
            Query queryList = entityManager.createNativeQuery(sbList.toString());
            queryList.setParameter("biNo", (String) data.get("biNo"));
            queryList.setParameter("custCode", (String) data.get("custCode"));
            queryList.setParameter("userId", userId);
            queryList.executeUpdate();
        }

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    @Transactional
    public ResultBody delete(Map<String, String> params) {
        String biNo = params.get("biNo");

        ResultBody resultBody = new ResultBody();
        StringBuilder sbList = new StringBuilder(
                "DELETE FROM t_bi_info_mat " +
                        "WHERE bi_no = :biNo");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biNo", biNo);
        queryList.executeUpdate();

        updateEmail(params);
        return resultBody;
    }

    @Transactional
    public ResultBody updateBidItem(@RequestBody List<Map<String, Object>> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                        .getPrincipal();
        String userId = principal.getUsername();

        String biNo = (String) params.get(0).get("biNo");
    
        StringBuilder init = new StringBuilder(
            "DELETE from t_bi_spec_mat where bi_no = :biNo"
        );

        Query initQuery = entityManager.createNativeQuery(init.toString());
        initQuery.setParameter("biNo", biNo);
        initQuery.executeUpdate();

        for (Map<String, Object> data : params) {
            StringBuilder sbList = new StringBuilder(
                    "INSERT into t_bi_spec_mat (bi_no, seq, name, ssize, unitcode, order_uc, create_user, create_date, order_qty) "+
                            "values (:biNo, :seq, :name, :ssize, :unitcode, :orderUc, :userId, sysdate(), :orderQty)");
            Query queryList = entityManager.createNativeQuery(sbList.toString());
            queryList.setParameter("biNo", (String) data.get("biNo"));
            queryList.setParameter("seq", (Integer) data.get("seq"));
            queryList.setParameter("name", (String) data.get("name"));
            queryList.setParameter("ssize", (String) data.get("ssize"));
            queryList.setParameter("unitcode", (String) data.get("unitcode"));
            queryList.setParameter("orderUc", Integer.parseInt((String) data.get("orderUc")));
            queryList.setParameter("userId", userId);
            queryList.setParameter("orderQty", Integer.parseInt((String) data.get("orderQty")));
            queryList.executeUpdate();
        }
        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    @Transactional
    public void updateEmail(Map<String, String> params) {
        String biNo = params.get("biNo");
        String type = params.get("type"); // del : 입찰삭제 , open : 입찰공고, insert: 입찰등록, 
        String biName = params.get("biName");
        String reason = params.get("reason");
        String interNm = params.get("interNm");

        String titleType = "";

        String contentHeader = "안녕하십니까\n일진그룹 전자입찰 e-bidding입니다.\n\n";
        String contentBody = "";
        String contentFooter = "감사합니다.";
        String contentReason = "";

        StringBuilder userList = new StringBuilder(
                "SELECT b.user_email from (SELECT bi_no, cust_code from t_bi_info_mat_cust where bi_no =:biNo) a, t_co_cust_user b where b.cust_code = a.cust_code ");

        Query query = entityManager.createNativeQuery(userList.toString());
        query.setParameter("biNo", biNo);
        List<EmailDto> receiverList = new JpaResultMapper().list(query, EmailDto.class);

        if(interNm==null){
            StringBuilder interCoList = new StringBuilder(
                "SELECT interrelated_cust_code from t_co_interrelated where interrelated_cust_code = :interCd");
            Query cdQ = entityManager.createNativeQuery(interCoList.toString());
            cdQ.setParameter("interCd", params.get("interCd"));
            interNm = (String) cdQ.getSingleResult();
        }

        switch (type) {
            case "del":
                titleType = "계획 삭제";
                contentBody = "입찰명 [<b>" + biName + "</b>] 입찰계획을\n삭제하였습니다.\n아래 삭제사유를 확인해 주십시오.\n\n";
                contentReason = "-삭제사유\n" + reason;
                break;
            case "open":
                titleType = "입찰 공고";
                contentBody = "[" + interNm + "]에서 입찰공고 하였습니다.\n입찰명은 [" + biName
                        + "] 입니다.\n자세한 사항은 e-bidding 시스템에 로그인하여 확인해 주십시오.\n\n";
            case "insert":
                titleType = "계획 등록";
                contentBody = "[" + interNm + "]에서 입찰계획을 등록하였습니다.\n입찰명은 [" + biName
                        + "] 입니다.\n자세한 사항은 e-bidding 시스템에 로그인하여 확인해 주십시오.\n\n";
                break;              
        }
        String title = "[일진그룹 e-bidding] 입찰" + titleType + "(" + biName + ")";
        String content = contentHeader + contentBody + contentFooter + contentReason;
        if (receiverList != null && !receiverList.isEmpty()) {
            for (EmailDto emailDto : receiverList) {
                String userEmail = emailDto.getUserEmail();
                StringBuilder sbList = new StringBuilder(
                        "INSERT INTO t_email (title, conts, send_flag, create_date, receives) VALUES " +
                                "(:title, :content, 'N', sysdate(), :userEmail)");

                Query queryList = entityManager.createNativeQuery(sbList.toString());
                queryList.setParameter("title", title);
                queryList.setParameter("content", content);
                queryList.setParameter("userEmail", userEmail);
                queryList.executeUpdate();
            }
        }

    }

    @Transactional
    public void updateLog(Map<String, String> params) {
        String msg = params.get("msg");
        String biNo = params.get("biNo");

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();

        StringBuilder sbList = new StringBuilder(
                "INSERT INTO t_bi_log (bi_no, user_id, log_text, create_date) VALUES " +
                        "(:biNo, :userId, :msg, sysdate())");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("msg", msg);
        queryList.setParameter("biNo", biNo);
        queryList.setParameter("userId", userId);
        queryList.executeUpdate();
    }

}
