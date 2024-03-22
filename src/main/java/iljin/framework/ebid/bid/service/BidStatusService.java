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
import iljin.framework.ebid.bid.dto.CurrDto;
import iljin.framework.ebid.bid.dto.EmailDto;
import iljin.framework.ebid.bid.dto.InterUserInfoDto;
import iljin.framework.ebid.bid.dto.InterrelatedCustDto;
import iljin.framework.ebid.bid.dto.ItemDto;
import iljin.framework.ebid.bid.dto.SubmitHistDto;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.PagaUtils;
import iljin.framework.ebid.etc.util.common.file.FileService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import iljin.framework.ebid.etc.util.common.file.FileService;

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
public class BidStatusService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TCoUserRepository tCoUserRepository;
    @Autowired
    Util util;

    @Autowired
    private FileService fileService;

    @Autowired
    private BidProgressService bidProgressService;

    @Value("${file.upload.directory}")
    private String uploadDirectory;

    public Page statuslist(@RequestBody Map<String, Object> params) {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());

        String userAuth = userOptional.get().getUserAuth();
        String interrelatedCode = userOptional.get().getInterrelatedCustCode();
        System.out.println("code111111111111111111111=" + interrelatedCode);
        String userId = principal.getUsername();

        StringBuilder sbCount = new StringBuilder(
                " select count(1) from t_bi_info_mat a where 1=1 ");
        StringBuilder sbList = new StringBuilder(
                "SELECT a.bi_no AS bi_no, a.bi_name AS bi_name, " +
                        "DATE_FORMAT(a.est_start_date, '%Y-%m-%d %H:%i') AS est_start_date, " +
                        "DATE_FORMAT(a.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date, " +
                        "CASE WHEN a.bi_mode = 'A' THEN '지명' ELSE '일반' END AS bi_mode, " +
                        "CASE WHEN a.ins_mode = '1' THEN '파일' ELSE '직접입력' END AS ins_mode, " +
                        "CASE WHEN a.ing_tag = 'A1' AND a.est_close_date < sysdate() THEN '입찰공고(개찰대상)' WHEN a.ing_tag = 'A1' THEN '입찰공고' WHEN a.ing_tag = 'A3' THEN '입찰공고(재)' ELSE '개찰' END AS ing_tag, "
                        +
                        "b.user_name AS cuser, b.user_email AS cuser_email, " +
                        "c.user_name AS gongo_id, c.user_email AS gongo_email, " +
                        "a.interrelated_cust_code AS interrelated_cust_code " +
                        "FROM t_bi_info_mat a LEFT JOIN t_co_user b ON a.create_user = b.user_id LEFT JOIN t_co_user c ON a.gongo_id = c.user_id "
                        +
                        "WHERE 1=1 ");
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("bidNo"))) {
            sbWhere.append(" and a.bi_no = :bidNo ");
        }

        if (!StringUtils.isEmpty(params.get("bidName"))) {
            sbWhere.append(" and a.bi_name like concat('%',:bidName,'%') ");
        }
        // 재입찰 포함, 개찰대상, 업체선정대상 플래그 값 하나라도 체크가 되어있는 경우
        if ((Boolean) params.get("rebidYn") || (Boolean) params.get("openBidYn")
                || (Boolean) (params.get("dateOverYn"))) {

            // 괄호열기
            sbWhere.append(" and ( ");

            // 재입찰 포함, 개찰대상중 하나라도 체크된 경우
            if ((Boolean) params.get("rebidYn") || (Boolean) params.get("dateOverYn")) {

                // 무조건 A1 포함
                sbWhere.append(" a.ing_tag = 'A1' ");

                // 재입찰 체크한 경우
                if ((Boolean) params.get("rebidYn")) {
                    sbWhere.append(" or a.ing_tag = 'A3' ");
                }

                // 업체선정대상 체크한 경우
                if ((Boolean) params.get("rebidYn")) {
                    sbWhere.append(" or a.ing_tag = 'A2' ");
                }

            } else {// 업체선정대상만 체크된 경우

                // 무조건 A2 포함
                sbWhere.append(" a.ing_tag = 'A2' ");

            }

            // 괄호닫기
            sbWhere.append(" ) ");

        } else {// 아무것도 체크하지 않은 경우

            // A1(입찰진행)만 출력
            sbWhere.append(" and a.ing_tag = 'A1' ");
            ;

        }

        // 개찰대상 체크한 경우
        if ((Boolean) params.get("dateOverYn")) {
            sbWhere.append(" and a.est_close_date < sysdate() ");
        }

        if (userAuth.equals("1") || userAuth.equals("2") || userAuth.equals("3")) {
            sbWhere.append(" AND a.interrelated_cust_code = :interrelatedCustCode " +
                    "and (a.create_user = :userid " +
                    "or a.open_att1 = :userid " +
                    "or a.open_att2 = :userid " +
                    "or a.gongo_id = :userid " +
                    "or a.est_bidder = :userid " +
                    "or a.est_opener = :userid)");
        }

        if (userAuth.equals("4")) {
            List<InterUserInfoDto> userInfoList = (List<InterUserInfoDto>) bidProgressService.findInterCustCode(userId);
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
                sbWhere.append(" or a.interrelated_cust_code = :interrelatedCustCode");
            }
            sbWhere.append(")");

            sbWhere.append(
                    " and (a.create_user = :userid " +
                            "or a.open_att1 = :userid " +
                            "or a.open_att2 = :userid " +
                            "or a.gongo_id = :userid " +
                            "or a.est_bidder = :userid " +
                            "or a.est_opener = :userid)");
        }
        sbList.append(sbWhere);
        sbCount.append(sbWhere);

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
        if (userAuth.equals("1") || userAuth.equals("2") || userAuth.equals("3")) {
            queryList.setParameter("interrelatedCustCode", interrelatedCode);
            queryTotal.setParameter("interrelatedCustCode", interrelatedCode);
            queryList.setParameter("userid", userId);
            queryTotal.setParameter("userid", userId);
        }
        if (userAuth.equals("4")) {
            List<InterUserInfoDto> userInfoList = (List<InterUserInfoDto>) bidProgressService.findInterCustCode(userId);
            List<String> custCodes = new ArrayList<>();
            for (InterUserInfoDto userInfo : userInfoList) {
                custCodes.add(userInfo.getInterrelatedCustCode());
            }

            for (int i = 0; i < custCodes.size(); i++) {
                queryList.setParameter("custCode" + i, custCodes.get(i));
                queryTotal.setParameter("custCode" + i, custCodes.get(i));
            }
            queryList.setParameter("interrelatedCustCode", interrelatedCode);
            queryTotal.setParameter("interrelatedCustCode", interrelatedCode);
            queryList.setParameter("userid", userId);
            queryTotal.setParameter("userid", userId);
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
                        "DATE_FORMAT(a.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date, b.user_name AS est_opener, a.est_opener AS est_opener_code, i.user_name AS cuser, a.create_user AS cuser_code, "
                        +
                        "DATE_FORMAT(a.est_open_date, '%Y-%m-%d %H:%i') AS est_open_date, c.user_name AS open_att1, a.open_att1 AS open_att1_code,"
                        +
                        "a.open_att1_sign AS open_att1_sign, d.user_name AS open_att2, a.open_att2 AS open_att2_code, a.open_att2_sign AS open_att2_sign, "
                        +
                        "a.ing_tag AS ing_tag, a.item_code AS item_code, f.item_name AS item_name, e.user_name AS gongo_id, a.gongo_id AS gongo_id_code, i.dept_name AS cuser_dept, a.pay_cond AS pay_cond, a.why_A3 AS why_A3, "
                        +
                        "a.why_A7 AS why_A7, a.bi_open AS bi_open, a.interrelated_cust_code AS interrelated_cust_code, h.interrelated_nm AS interrelated_nm, a.real_amt AS real_amt, a.amt_basis AS amt_basis, a.bd_amt AS bd_amt,"
                        +
                        "a.add_accept AS add_accept, a.mat_dept AS mat_dept, a.mat_proc AS mat_proc, a.mat_cls AS mat_cls, "
                        +
                        "a.mat_factory AS mat_factory, a.mat_factory_line AS mat_factory_line, a.mat_factory_cnt AS mat_factory_cnt "
                        +
                        "FROM t_bi_info_mat a " +
                        "LEFT JOIN t_co_user b ON a.est_opener = b.user_id " +
                        "LEFT JOIN t_co_user c ON a.open_att1 = c.user_id " +
                        "LEFT JOIN t_co_user d ON a.open_att2 = d.user_id " +
                        "LEFT JOIN t_co_user e ON a.gongo_id = e.user_id " +
                        "LEFT JOIN t_co_item f ON a.item_code = f.item_code " +
                        "LEFT JOIN t_co_user g ON a.gongo_id = g.user_id " +
                        "LEFT JOIN t_co_user i ON a.create_user = i.user_id " +
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
                "SELECT a.bi_no AS bi_no, CAST(a.cust_code AS CHAR) AS cust_code, b.cust_name AS cust_name, " +
                        "a.esmt_yn AS esmt_yn, c.file_nm AS file_nm, c.file_path AS file_path, a.etc_b_file AS etc_file, a.etc_b_file_path AS etc_path "
                        +
                        "FROM t_bi_info_mat_cust a " +
                        "LEFT JOIN t_co_cust_master b ON a.cust_code = b.cust_code " +
                        "LEFT JOIN t_bi_upload c ON a.file_id = c.file_id " +
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

    @Transactional
    public ResultBody bidFailure(Map<String, String> params) {
        String biNo = params.get("biNo");

        System.out.println(11111111 + biNo);
        StringBuilder sbList = new StringBuilder(
                "UPDATE t_bi_info_mat set bid_open_date = sysdate()," +
                        "ing_tag = 'A7', why_a7 = :reason " +
                        "WHERE bi_no = :biNo");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biNo", biNo);
        queryList.setParameter("reason", (String) params.get("reason"));
        int rowsUpdated = queryList.executeUpdate();

        if (rowsUpdated > 0) {
            Map<String, String> logParams = new HashMap<>();
            logParams.put("msg", "[본사] 유찰");
            logParams.put("biNo", biNo);
            bidProgressService.updateLog(logParams);
        }

        bidProgressService.updateEmail(params);

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    public Page submitHist(@RequestBody Map<String, Object> params) {
        String biNo = (String) params.get("biNo");
        String custCode = (String) params.get("custCode");

        StringBuilder sbCount = new StringBuilder("");
        StringBuilder sbList = new StringBuilder("");

        StringBuilder ins = new StringBuilder(
                "SELECT ins_mode from t_bi_info_mat where bi_no = :biNo");
        Query insList = entityManager.createNativeQuery(ins.toString());
        insList.setParameter("biNo", biNo);
        String insMode = (String) insList.getSingleResult();

        if (insMode.equals("1")) {
            sbCount.append(
                    "SELECT count(1) from t_bi_info_mat_cust_temp where bi_no = :biNo and cust_code = :custCode");
            sbList.append(
                    "SELECT '1' AS insMode, bi_order, esmt_curr, esmt_amt, DATE_FORMAT(submit_date, '%Y-%m-%d %H:%i') AS submit_date from t_bi_info_mat_cust_temp "
                            +
                            "where bi_no = :biNo and cust_code = :custCode");
        }

        else if (insMode.equals("2")) {
            sbCount.append(
                    "SELECT count(1) from t_bi_detail_mat_cust_temp a, t_bi_info_mat_cust b " +
                            "where a.bi_no = :biNo and a.cust_code = :custCode and (a.bi_no =b.bi_no and a.cust_code = b.cust_code)");
            sbList.append(
                    "SELECT '2' AS insMode, a.bi_order AS bi_order, 'KRW' AS esmt_curr, a.esmt_uc AS esmt_amt, DATE_FORMAT(b.submit_date, '%Y-%m-%d %H:%i') AS submit_date "
                            +
                            "from t_bi_detail_mat_cust_temp a, t_bi_info_mat_cust b " +
                            "where a.bi_no = :biNo and a.cust_code = :custCode and (a.bi_no =b.bi_no and a.cust_code = b.cust_code)");
        }
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        Query queryCountList = entityManager.createNativeQuery(sbCount.toString());
        queryList.setParameter("biNo", biNo);
        queryCountList.setParameter("biNo", biNo);
        queryList.setParameter("custCode", custCode);
        queryCountList.setParameter("custCode", custCode);

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, SubmitHistDto.class);

        BigInteger count = (BigInteger) queryCountList.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    @Transactional
    public ResultBody rebid(@RequestBody Map<String, Object> params) { // rebid 페이지에서 disabled 조건인 칼럼 모두 제외

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();

        StringBuilder sbList = new StringBuilder( // 입찰 업데이트
                "UPDATE t_bi_info_mat SET bi_name = :biName,  " +
                        "bid_join_spec = :bidJoinSpec, special_cond = :specialCond, supply_cond = :supplyCond, " +
                        "spot_date = STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), spot_area = :spotArea, " +
                        "succ_deci_meth = :succDeciMethCode, amt_basis = :amtBasis, bd_amt = :bdAmt, " +
                        "update_user = :userId, update_date = sysdate(), pay_cond = :payCond, ing_tag = 'A3', why_a3 = :whyA3 "
                        +
                        "WHERE bi_no = :biNo");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biName", (String) params.get("biName"));
        queryList.setParameter("bidJoinSpec", (String) params.get("bidJoinSpec"));
        queryList.setParameter("specialCond", (String) params.get("specialCond"));
        queryList.setParameter("supplyCond", (String) params.get("supplyCond"));
        queryList.setParameter("spotDate", (String) params.get("spotDate"));
        queryList.setParameter("spotArea", (String) params.get("spotArea"));
        queryList.setParameter("succDeciMethCode", (String) params.get("succDeciMethCode"));
        queryList.setParameter("amtBasis", (String) params.get("amtBasis"));
        queryList.setParameter("bdAmt", params.get("bdAmt"));
        queryList.setParameter("userId", userId);
        queryList.setParameter("payCond", (String) params.get("payCond"));
        queryList.setParameter("whyA3", (String) params.get("whyA3"));
        queryList.setParameter("biNo", (String) params.get("biNo"));

        queryList.executeUpdate();

        StringBuilder sbList1 = new StringBuilder( // 입찰 hist 업데이트
                "INSERT into t_bi_info_mat_hist (bi_no, bi_name, bi_mode, ins_mode, bid_join_spec, special_cond, supply_cond, spot_date, "
                        +
                        "spot_area, succ_deci_meth, amt_basis, bd_amt, est_start_date, est_close_date, est_opener, est_bidder, "
                        +
                        "open_att1, open_att2, ing_tag, update_user, update_date, item_code, " +
                        "gongo_id, pay_cond, bi_open, mat_dept, mat_proc, mat_cls, mat_factory, mat_factory_line, mat_factory_cnt, why_a3) "
                        +
                        "values (:biNo, :biName, :biModeCode, :insModeCode, :bidJoinSpec, :specialCond, :supplyCond, " +
                        "STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), :spotArea, :succDeciMethCode, :amtBasis, :bdAmt, "
                        +
                        "est_start_date =STR_TO_DATE(:estStartDate, '%Y-%m-%d %H:%i'), est_close_date =STR_TO_DATE(:estCloseDate, '%Y-%m-%d %H:%i'), :estOpenerCode, :estBidderCode, "
                        +
                        ":openAtt1Code, :openAtt2Code, 'A3', :userId, sysdate(), :itemCode, :gongoIdCode, :payCond, 'N', :matDept, :matProc, :matCls, :matFactory, "
                        +
                        ":matFactoryLine, :matFactoryCnt, :whyA3)");

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
        queryList1.setParameter("bdAmt", params.get("bdAmt"));
        queryList1.setParameter("estStartDate", (String) params.get("estStartDate"));
        queryList1.setParameter("estCloseDate", (String) params.get("estCloseDate"));
        queryList1.setParameter("estOpenerCode", (String) params.get("estOpenerCode"));
        queryList1.setParameter("estBidderCode", (String) params.get("estBidderCode"));
        queryList1.setParameter("openAtt1Code", (String) params.get("openAtt1Code"));
        queryList1.setParameter("openAtt2Code", (String) params.get("openAtt2Code"));
        queryList1.setParameter("userId", userId);
        queryList1.setParameter("itemCode", (String) params.get("itemCode"));
        queryList1.setParameter("gongoIdCode", (String) params.get("gongoIdCode"));
        queryList1.setParameter("payCond", (String) params.get("payCond"));
        queryList1.setParameter("matDept", (String) params.get("matDept"));
        queryList1.setParameter("matProc", (String) params.get("matProc"));
        queryList1.setParameter("matCls", (String) params.get("matCls"));
        queryList1.setParameter("matFactory", (String) params.get("matFactory"));
        queryList1.setParameter("matFactoryLine", (String) params.get("matFactoryLine"));
        queryList1.setParameter("matFactoryCnt", (String) params.get("matFactoryCnt"));
        queryList1.setParameter("whyA3", (String) params.get("whyA3"));

        int rowsUpdated = queryList1.executeUpdate();
        if (rowsUpdated > 0) {
            Map<String, String> logParams = new HashMap<>();
            logParams.put("msg", "[본사] 재입찰");
            logParams.put("biNo", (String) params.get("biNo"));
            bidProgressService.updateLog(logParams);

            Map<String, String> mailParams = new HashMap<>();
            mailParams.put("biNo", (String) params.get("biNo"));
            mailParams.put("type", (String) params.get("type"));
            mailParams.put("biName", (String) params.get("biName"));
            mailParams.put("reason", (String) params.get("whyA7"));
            mailParams.put("interNm", (String) params.get("interNm"));
            bidProgressService.updateEmail(mailParams);
        }

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    @Transactional
    public ResultBody rebidCust(@RequestBody List<Map<String, Object>> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String userId = principal.getUsername();
        if (params.size() > 0) {
            String biNo = (String) params.get(0).get("biNo");

            for (Map<String, Object> data : params) {
                StringBuilder sbList = new StringBuilder(
                        "UPDATE t_bi_info_mat_cust rebid_att = 'Y', esmt_yn = '0', esmt_amt = 0, update_user = :userId, update_date = sysdate() "
                                +
                                "where bi_no = :biNo and cust_code = :custCode");
                Query queryList = entityManager.createNativeQuery(sbList.toString());
                queryList.setParameter("userId", userId);
                queryList.setParameter("biNo", biNo);
                queryList.setParameter("custCode", (String) data.get("custCode"));
                queryList.executeUpdate();
            }
        }

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    public List<ItemDto> itemlist(@RequestBody Map<String, Object> params) {
        StringBuilder itemlist = new StringBuilder(
                "SELECT a.bi_no, a.seq, a.order_qty, a.name, a.ssize, a.unitcode, b.esmt_uc, b.cust_code " +
                            "from t_bi_spec_mat a, t_bi_detail_mat_cust b " +
                            "where a.bi_no = :biNo and b.cust_code = :custCode " +
                            "and (a.bi_no = b.bi_no and a.seq = b.seq)");
        Query itemlistQ = entityManager.createNativeQuery(itemlist.toString());
        itemlistQ.setParameter("biNo", (String) params.get("biNo"));
        itemlistQ.setParameter("custCode", (String) params.get("custCode"));
        return new JpaResultMapper().list(itemlistQ, ItemDto.class);
    }
}
