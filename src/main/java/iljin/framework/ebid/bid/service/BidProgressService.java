package iljin.framework.ebid.bid.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.core.security.user.UserDto;
import iljin.framework.core.security.user.UserRepository;
import iljin.framework.core.security.user.UserRepositoryCustom;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.BidPastDto;
import iljin.framework.ebid.bid.dto.BidProgressCustDto;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.dto.BidProgressDto;
import iljin.framework.ebid.bid.dto.BidProgressFileDto;
import iljin.framework.ebid.bid.dto.BidProgressListDetailDto;
import iljin.framework.ebid.bid.dto.BidProgressTableDto;
import iljin.framework.ebid.bid.dto.CoUserInfoDto;
import iljin.framework.ebid.bid.dto.SendDto;
import iljin.framework.ebid.bid.dto.InterUserInfoDto;
import iljin.framework.ebid.bid.dto.InterrelatedCustDto;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
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
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private FileService fileService;

    @Value("${file.upload.directory}")
    private String uploadDirectory;

    public Page custList(@RequestBody Map<String, Object> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());

        String interrelatedCode = userOptional.get().getInterrelatedCustCode();

        StringBuilder sbCount = new StringBuilder(
                " select count(1) FROM t_co_cust_ir a, t_co_cust_master b WHERE a.interrelated_cust_code = :interrelatedCode and a.cust_code = b.cust_code and b.cert_yn='Y'");
        StringBuilder sbList = new StringBuilder(
                "SELECT CAST(b.cust_code AS CHAR) AS cust_code, b.cust_name AS cust_name, b.pres_name AS pres_name," +
                        "CONCAT('(', b.zipcode, ')', b.addr, ' ', b.addr_detail) AS combined_addr, " +
                        "a.interrelated_cust_code AS interrelated_cust_code " +
                        "FROM t_co_cust_ir a, t_co_cust_master b WHERE a.interrelated_cust_code = :interrelatedCode and a.cust_code = b.cust_code and b.cert_yn='Y'");

        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("custName"))) {
            sbWhere.append(" and b.cust_name like concat('%',:custName,'%') ");
        }

        if (!StringUtils.isEmpty(params.get("chairman"))) {
            sbWhere.append(" and b.pres_name like concat('%',:chairman,'%') ");
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

    public Page pastBidList(@RequestBody Map<String, Object> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());

        String interrelatedCode = userOptional.get().getInterrelatedCustCode();
        String userId = principal.getUsername();

        StringBuilder sbCount = new StringBuilder(
                "SELECT count(1) " +
                        "FROM t_bi_info_mat a " +
                        "WHERE a.interrelated_cust_code = :interrelatedCode " +
                        "and (a.create_user = :userId " +
                        "or a.open_att1 = :userId " +
                        "or a.open_att2 = :userId " +
                        "or a.gongo_id = :userId " +
                        "or a.est_bidder = :userId " +
                        "or a.est_opener = :userId)");

        StringBuilder sbList = new StringBuilder(
                "SELECT a.bi_no AS bi_no, a.bi_name AS bi_name, " +
                        "CASE WHEN a.bi_mode = 'A' THEN '지명경쟁입찰' ELSE '일반경쟁입찰' END AS bi_mode, " +
                        "CASE WHEN a.ins_mode = '1' THEN '파일등록' ELSE '직접입력' END AS ins_mode, " +
                        "DATE_FORMAT(a.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date, " +
                        "CASE WHEN a.ing_tag = 'A0' THEN '입찰계획' WHEN a.ing_tag = 'A1' THEN '입찰진행' " +
                        "WHEN a.ing_tag = 'A2' THEN '개찰' WHEN ing_tag = 'A3' THEN '재입찰' " +
                        "WHEN a.ing_tag = 'A5' THEN '입찰완료' ELSE '유찰' END AS ing_tag " +
                        "FROM t_bi_info_mat a " +
                        "WHERE a.interrelated_cust_code = :interrelatedCode " +
                        "and (a.create_user = :userId " +
                        "or a.open_att1 = :userId " +
                        "or a.open_att2 = :userId " +
                        "or a.gongo_id = :userId " +
                        "or a.est_bidder = :userId " +
                        "or a.est_opener = :userId)");

        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("biNo"))) {
            sbWhere.append(" and a.bi_no like concat('%',:biNo,'%') ");
        }

        if (!StringUtils.isEmpty(params.get("biName"))) {
            sbWhere.append(" and a.bi_name like concat('%',:biName,'%') ");
        }

        sbList.append(sbWhere);
        sbCount.append(sbWhere);
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        Query queryCountList = entityManager.createNativeQuery(sbCount.toString());

        queryList.setParameter("interrelatedCode", interrelatedCode);
        queryCountList.setParameter("interrelatedCode", interrelatedCode);
        queryList.setParameter("userId", userId);
        queryCountList.setParameter("userId", userId);

        if (!StringUtils.isEmpty(params.get("biNo"))) {
            queryList.setParameter("biNo", params.get("biNo"));
            queryCountList.setParameter("biNo", params.get("biNo"));
        }

        if (!StringUtils.isEmpty(params.get("biName"))) {
            queryList.setParameter("biName", params.get("biName"));
            queryCountList.setParameter("biName", params.get("biName"));
        }

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, BidPastDto.class);

        BigInteger count = (BigInteger) queryCountList.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    public Page progresslist(@RequestBody Map<String, Object> params) {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());

        String userAuth = userOptional.get().getUserAuth();
        String interrelatedCode = userOptional.get().getInterrelatedCustCode();

        String userId = principal.getUsername();

        StringBuilder sbCount = new StringBuilder(
                " select count(1) from t_bi_info_mat a where 1=1 AND a.ing_tag = 'A0' ");
        StringBuilder sbList = new StringBuilder(
                "SELECT a.bi_no AS bi_no, a.bi_name AS bi_name, " +
                        "DATE_FORMAT(a.est_start_date, '%Y-%m-%d %H:%i') AS est_start_date, " +
                        "DATE_FORMAT(a.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date, " +
                        "CASE WHEN a.bi_mode = 'A' THEN '지명' ELSE '일반' END AS bi_mode, " +
                        "CASE WHEN a.ins_mode = '1' THEN '파일' ELSE '직접입력' END AS ins_mode, a.ing_tag AS ing_tag, " +
                        "b.user_name AS cuser, b.user_email AS cuser_email, " +
                        "c.user_name AS gongo_id, c.user_email AS gongo_email, " +
                        "a.interrelated_cust_code AS interrelated_cust_code " +
                        "FROM t_bi_info_mat a LEFT JOIN t_co_user b ON a.create_user = b.user_id LEFT JOIN t_co_user c ON a.gongo_id = c.user_id "
                        +
                        "WHERE a.ing_tag = 'A0'");
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("bidNo"))) {
            sbWhere.append(" and a.bi_no = :bidNo ");
        }

        if (!StringUtils.isEmpty(params.get("bidName"))) {
            sbWhere.append(" and a.bi_name like concat('%',:bidName,'%') ");
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
            sbWhere.append(
                    "and (a.create_user = :userid " +
                            "or a.open_att1 = :userid " +
                            "or a.open_att2 = :userid " +
                            "or a.gongo_id = :userid " +
                            "or a.est_bidder = :userid " +
                            "or a.est_opener = :userid)");

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
            sbWhere.append(" or a.interrelated_cust_code = :interrelatedCustCode");
            sbWhere.append(")");
        }
        sbList.append(sbWhere);
        sbList.append(" order by a.create_date desc");
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
            List<InterUserInfoDto> userInfoList = (List<InterUserInfoDto>) findInterCustCode(userId);
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

    public List<List<?>> progresslistDetail(String param, CustomUserDetails user) {
    	int custCode = Integer.parseInt(user.getCustCode());//협력사 번호 
    	
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
                        "IFNULL(DATE_FORMAT(a.est_open_date, '%Y-%m-%d %H:%i'),'') AS est_open_date, c.user_name AS open_att1, a.open_att1 AS open_att1_code, a.est_bidder AS est_bidder_code, i.user_name AS est_bidder, "
                        +
                        "a.open_att1_sign AS open_att1_sign, d.user_name AS open_att2, a.open_att2 AS open_att2_code, a.open_att2_sign AS open_att2_sign, "
                        +
                        "a.ing_tag AS ing_tag, a.item_code AS item_code, f.item_name AS item_name, e.user_name AS gongo_id, a.gongo_id AS gongo_id_code, i.dept_name AS cuser_dept, a.pay_cond AS pay_cond, IFNULL(a.why_A3,'') AS why_A3, "
                        +
                        "IFNULL(a.why_A7,'') AS why_A7, a.bi_open AS bi_open, a.interrelated_cust_code AS interrelated_cust_code, h.interrelated_nm AS interrelated_nm, IFNULL(a.real_amt,0) AS real_amt, a.amt_basis AS amt_basis, a.bd_amt AS bd_amt,"
                        +
                        "IFNULL(a.add_accept,'') AS add_accept, a.mat_dept AS mat_dept, a.mat_proc AS mat_proc, a.mat_cls AS mat_cls, "
                        +
                        "a.mat_factory AS mat_factory, a.mat_factory_line AS mat_factory_line, a.mat_factory_cnt AS mat_factory_cnt, j.user_name as create_user_name "
                        +
                        "FROM t_bi_info_mat a " +
                        "LEFT JOIN t_co_user b ON a.est_opener = b.user_id " +
                        "LEFT JOIN t_co_user c ON a.open_att1 = c.user_id " +
                        "LEFT JOIN t_co_user d ON a.open_att2 = d.user_id " +
                        "LEFT JOIN t_co_user e ON a.gongo_id = e.user_id " +
                        "LEFT JOIN t_co_item f ON a.item_code = f.item_code " +
                        "LEFT JOIN t_co_user g ON a.gongo_id = g.user_id " +
                        "JOIN t_co_interrelated h ON a.interrelated_cust_code = h.interrelated_cust_code " +
                        "LEFT JOIN t_co_user i ON a.create_user = i.user_id " +
                        "LEFT JOIN t_co_user j ON a.est_bidder = i.user_id " +
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
                        "WHERE a.use_yn = 'Y' "
        		);

        StringBuilder sbCustList = new StringBuilder(
                "SELECT a.bi_no AS bi_no, CAST(a.cust_code AS CHAR) AS cust_code, b.cust_name AS cust_name, d.code_name AS esmt_curr, a.esmt_amt AS esmt_amt, e.user_name AS user_name, "
                        +
                        "a.esmt_yn AS esmt_yn, c.file_nm AS file_nm, c.file_path AS file_path, a.etc_b_file AS etc_file, a.etc_b_file_path AS etc_path, a.succ_yn AS succ_yn, "
                        +
                        "DATE_FORMAT(a.submit_date, '%Y-%m-%d %H:%i') AS submit_date " +
                        "FROM t_bi_info_mat_cust a " +
                        "LEFT JOIN t_co_cust_master b ON a.cust_code = b.cust_code " +
                        "LEFT JOIN t_bi_upload c ON a.file_id = c.file_id " +
                        "left join t_co_code d on a.esmt_curr = d.code_val " +
                        "left join t_co_cust_user e on a.cust_code = e.cust_code AND e.user_type = '1' " +
                        "WHERE 1=1 ");

        StringBuilder sbWhere = new StringBuilder();
        sbWhere.append(" and a.bi_no = :param ");
        sbList.append(sbWhere);
        sbList.append("GROUP BY a.BI_NO");
        
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
        

        List<BidProgressListDetailDto> resultList = new JpaResultMapper().list(queryList, BidProgressListDetailDto.class);
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
                        "WHERE use_yn = 'Y' ");
        StringBuilder sbCount = new StringBuilder("SELECT count(1) FROM t_co_user a WHERE use_yn = 'Y' ");
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
            case "biddingUser":
                sbWhere.append("AND a.bidauth = '1' AND a.interrelated_cust_code = :interrelatedCD ");

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
    public ResultBody bidNotice(Map<String, String> params) {
        String biNo = params.get("biNo");

        StringBuilder sbList = new StringBuilder(
                "UPDATE t_bi_info_mat set bid_open_date = sysdate()," +
                        "ing_tag = 'A1' " +
                        "WHERE bi_no = :biNo");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biNo", biNo);
        int rowsUpdated = queryList.executeUpdate();
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();

        if (rowsUpdated > 0) {
            Map<String, String> logParams = new HashMap<>();
            logParams.put("msg", "[본사]입찰공고");
            logParams.put("biNo", biNo);
            logParams.put("userId", userId);
            updateLog(logParams);
        }
           
        StringBuilder sbMail = new StringBuilder();

        if( "A".equals(CommonUtils.getString(params.get("biModeCode"))) ) {
                //지명경쟁입찰
                sbMail.append(
        				"select 	tcu.user_email\r\n"
        				+ "				,		tccu.USER_EMAIL as from_email \r\n"
        				+ "				from t_co_user tcu \r\n"
        				+ "				inner join t_co_interrelated tci\r\n"
        				+ "				on tcu.INTERRELATED_CUST_CODE = tci.INTERRELATED_CUST_CODE\r\n"
        				+ "				inner join t_co_cust_ir tcci \r\n"
        				+ "				on tci.INTERRELATED_CUST_CODE = tcci.INTERRELATED_CUST_CODE \r\n"
        				+ "				inner join t_co_cust_master tccm \r\n"
        				+ "				on tcci.CUST_CODE = tccm.CUST_CODE 		\r\n"
        				+ "				left outer join t_co_cust_user tccu \r\n"
        				+ "				on tccm.CUST_CODE = tccu.CUST_CODE \r\n"
        				+ "				where tcu.USER_ID = :userId\r\n"
        				+ "				and tccu.USER_EMAIL is not null\r\n"
        				+ "				and tccm.CUST_CODE in(" +  params.get("custCode") + ")"
        		);
        		
            }else {
            	// 일반경쟁입찰
            	sbMail.append("select tcu.user_email\r\n"
                		+ ",		tccu.USER_EMAIL as from_email \r\n"
                		+ "from t_co_user tcu \r\n"
                		+ "inner join t_co_interrelated tci\r\n"
                		+ "on tcu.INTERRELATED_CUST_CODE = tci.INTERRELATED_CUST_CODE\r\n"
                		+ "inner join t_co_cust_ir tcci \r\n"
                		+ "on tci.INTERRELATED_CUST_CODE = tcci.INTERRELATED_CUST_CODE \r\n"
                		+ "inner join t_co_cust_master tccm \r\n"
                		+ "on tcci.CUST_CODE = tccm.CUST_CODE 		\r\n"
                		+ "left outer join t_co_cust_user tccu \r\n"
                		+ "on tccm.CUST_CODE = tccu.CUST_CODE \r\n"
                		+ "where tcu.USER_ID = :userId\r\n"
                		+ "and tccu.USER_EMAIL is not null\r\n"
                		+ "group by tccu.USER_EMAIL "
        		);
            }
    		
    		//쿼리 실행
    		Query queryMail = entityManager.createNativeQuery(sbMail.toString());
    		//조건 대입
    		queryMail.setParameter("userId", userId);
    		List<SendDto> sendList = new JpaResultMapper().list(queryMail, SendDto.class);
			
			if(sendList.size() > 0) {
				Map<String, Object> emailParam = new HashMap<String, Object>();

				emailParam.put("type", "notice");
				emailParam.put("biName", params.get("biName"));
				emailParam.put("interNm", params.get("interNm"));
				emailParam.put("reason", "");
				emailParam.put("sendList", sendList);
				emailParam.put("biNo", biNo);
				this.updateEmail(emailParam);
			}

		
        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    @Transactional
    public ResultBody updateBid(Map<String, Object> params,  
    		MultipartFile insFile, 
    		MultipartFile innerFile, 
    		MultipartFile outerFile) throws Exception {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();
        Map<String, Object> bidContent = (Map<String, Object>) params.get("bidContent");
        String biNo = (String) bidContent.get("biNo");
        String bdAmtStr = (String) bidContent.get("bdAmt");
        
        BigDecimal bdAmt = null;
        if (!bdAmtStr.isEmpty()) {
            bdAmt = new BigDecimal(bdAmtStr);
        }

        StringBuilder sbList = new StringBuilder( // 입찰 업데이트
                "UPDATE t_bi_info_mat SET bi_name = :biName, bi_mode = :biModeCode, ins_mode = :insModeCode, " +
                        "bid_join_spec = :bidJoinSpec, special_cond = :specialCond, supply_cond = :supplyCond, " +
                        "spot_date = STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), spot_area = :spotArea, " +
                        "succ_deci_meth = :succDeciMethCode, amt_basis = :amtBasis, bd_amt = :bdAmt, " +
                        "est_start_date = STR_TO_DATE(:estStartDate, '%Y-%m-%d %H:%i'), " +
                        "est_close_date = STR_TO_DATE(:estCloseDate, '%Y-%m-%d %H:%i'), est_opener = :estOpenerCode, est_bidder = :estBidderCode, "
                        +
                        "open_att1 = :openAtt1Code, open_att2 = :openAtt2Code, update_user = :userId, update_date = sysdate(), "
                        +
                        "item_code = :itemCode, gongo_id = :gongoIdCode, pay_cond = :payCond, mat_dept = :matDept, mat_proc = :matProc, "
                        +
                        "mat_cls = :matCls, mat_factory = :matFactory, mat_factory_line = :matFactoryLine, mat_factory_cnt = :matFactoryCnt "
                        +
                        "WHERE bi_no = :biNo");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biName", (String) bidContent.get("biName"));
        queryList.setParameter("biModeCode", (String) bidContent.get("biModeCode"));
        queryList.setParameter("insModeCode", (String) bidContent.get("insModeCode"));
        queryList.setParameter("bidJoinSpec", (String) bidContent.get("bidJoinSpec"));
        queryList.setParameter("specialCond", (String) bidContent.get("specialCond"));
        queryList.setParameter("supplyCond", (String) bidContent.get("supplyCond"));
        queryList.setParameter("spotDate", (String) bidContent.get("spotDate"));
        queryList.setParameter("spotArea", (String) bidContent.get("spotArea"));
        queryList.setParameter("succDeciMethCode", (String) bidContent.get("succDeciMethCode"));
        queryList.setParameter("amtBasis", (String) bidContent.get("amtBasis"));
        queryList.setParameter("bdAmt", bdAmt);
        queryList.setParameter("estStartDate", (String) bidContent.get("estStartDate"));
        queryList.setParameter("estCloseDate", (String) bidContent.get("estCloseDate"));
        queryList.setParameter("estOpenerCode", (String) bidContent.get("estOpenerCode"));
        queryList.setParameter("estBidderCode", (String) bidContent.get("estBidderCode"));
        queryList.setParameter("openAtt1Code", (String) bidContent.get("openAtt1Code"));
        queryList.setParameter("openAtt2Code", (String) bidContent.get("openAtt2Code"));
        queryList.setParameter("userId", userId);
        queryList.setParameter("itemCode", (String) bidContent.get("itemCode"));
        queryList.setParameter("gongoIdCode", (String) bidContent.get("gongoIdCode"));
        queryList.setParameter("payCond", (String) bidContent.get("payCond"));
        queryList.setParameter("matDept", (String) bidContent.get("matDept"));
        queryList.setParameter("matProc", (String) bidContent.get("matProc"));
        queryList.setParameter("matCls", (String) bidContent.get("matCls"));
        queryList.setParameter("matFactory", (String) bidContent.get("matFactory"));
        queryList.setParameter("matFactoryLine", (String) bidContent.get("matFactoryLine"));
        queryList.setParameter("matFactoryCnt", (String) bidContent.get("matFactoryCnt"));
        queryList.setParameter("biNo", biNo);

        queryList.executeUpdate();

        StringBuilder sbList1 = new StringBuilder( // 입찰 hist 업데이트
                "INSERT into t_bi_info_mat_hist (bi_no, bi_name, bi_mode, ins_mode, bid_join_spec, special_cond, supply_cond, spot_date, "
                        +
                        "spot_area, succ_deci_meth, amt_basis, bd_amt, est_start_date, est_close_date, est_opener, est_bidder, "
                        +
                        "open_att1, open_att2, ing_tag, update_user, update_date, item_code, " +
                        "gongo_id, pay_cond, bi_open, mat_dept, mat_proc, mat_cls, mat_factory, mat_factory_line, mat_factory_cnt) "
                        +
                        "values (:biNo, :biName, :biModeCode, :insModeCode, :bidJoinSpec, :specialCond, :supplyCond, " +
                        "STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), :spotArea, :succDeciMethCode, :amtBasis, :bdAmt, "
                        +
                        "est_start_date =STR_TO_DATE(:estStartDate, '%Y-%m-%d %H:%i'), est_close_date =STR_TO_DATE(:estCloseDate, '%Y-%m-%d %H:%i'), :estOpenerCode, :estBidderCode, "
                        +
                        ":openAtt1Code, :openAtt2Code, 'A0', :userId, sysdate(), :itemCode, :gongoIdCode, :payCond, 'N', :matDept, :matProc, :matCls, :matFactory, "
                        +
                        ":matFactoryLine, :matFactoryCnt)");

        Query queryList1 = entityManager.createNativeQuery(sbList1.toString());
        queryList1.setParameter("biNo", biNo);
        queryList1.setParameter("biName", (String) bidContent.get("biName"));
        queryList1.setParameter("biModeCode", (String) bidContent.get("biModeCode"));
        queryList1.setParameter("insModeCode", (String) bidContent.get("insModeCode"));
        queryList1.setParameter("bidJoinSpec", (String) bidContent.get("bidJoinSpec"));
        queryList1.setParameter("specialCond", (String) bidContent.get("specialCond"));
        queryList1.setParameter("supplyCond", (String) bidContent.get("supplyCond"));
        queryList1.setParameter("spotDate", (String) bidContent.get("spotDate"));
        queryList1.setParameter("spotArea", (String) bidContent.get("spotArea"));
        queryList1.setParameter("succDeciMethCode", (String) bidContent.get("succDeciMethCode"));
        queryList1.setParameter("amtBasis", (String) bidContent.get("amtBasis"));
        queryList1.setParameter("bdAmt", bdAmt);
        queryList1.setParameter("estStartDate", (String) bidContent.get("estStartDate"));
        queryList1.setParameter("estCloseDate", (String) bidContent.get("estCloseDate"));
        queryList1.setParameter("estOpenerCode", (String) bidContent.get("estOpenerCode"));
        queryList1.setParameter("estBidderCode", (String) bidContent.get("estBidderCode"));
        queryList1.setParameter("openAtt1Code", (String) bidContent.get("openAtt1Code"));
        queryList1.setParameter("openAtt2Code", (String) bidContent.get("openAtt2Code"));
        queryList1.setParameter("userId", userId);
        queryList1.setParameter("itemCode", (String) bidContent.get("itemCode"));
        queryList1.setParameter("gongoIdCode", (String) bidContent.get("gongoIdCode"));
        queryList1.setParameter("payCond", (String) bidContent.get("payCond"));
        queryList1.setParameter("matDept", (String) bidContent.get("matDept"));
        queryList1.setParameter("matProc", (String) bidContent.get("matProc"));
        queryList1.setParameter("matCls", (String) bidContent.get("matCls"));
        queryList1.setParameter("matFactory", (String) bidContent.get("matFactory"));
        queryList1.setParameter("matFactoryLine", (String) bidContent.get("matFactoryLine"));
        queryList1.setParameter("matFactoryCnt", (String) bidContent.get("matFactoryCnt"));

        queryList1.executeUpdate();

        // 지명경쟁 협력사 등록

        StringBuilder init = new StringBuilder("DELETE from t_bi_info_mat_cust where bi_no = :biNo");

        Query initQuery = entityManager.createNativeQuery(init.toString());
        initQuery.setParameter("biNo", biNo);
        initQuery.executeUpdate();
        
        StringBuilder sbMail = new StringBuilder();
        Map<String, Object> emailMap = new HashMap<String, Object>();
        if( "A".equals(CommonUtils.getString(bidContent.get("biModeCode"))) ) {
            List<Map<String, Object >> custContent = (List<Map<String, Object>>) params.get("custContent");

                for (Map<String, Object> data : custContent) {
                    StringBuilder sbList2 = new StringBuilder(
                            "INSERT into t_bi_info_mat_cust (bi_no, cust_code, rebid_att, esmt_yn, esmt_amt, succ_yn, create_user, create_date) "
                                    +
                                    "values (:biNo, :custCode, 'N', '0', 0, 'N', :userId, sysdate())");
                    Query queryList2 = entityManager.createNativeQuery(sbList2.toString());
                    queryList2.setParameter("biNo", (String) data.get("biNo"));
                    queryList2.setParameter("custCode", (String) data.get("custCode"));
                    queryList2.setParameter("userId", userId);
                    queryList2.executeUpdate();
                }

            // 지명경쟁 협력사의 모든 대상자 이메일 insert

                // 지명경쟁 협력사의 모든 대상자 이메일 insert
                
                sbMail.append(
        				"select 	tcu.user_email\r\n"
        				+ "				,		tccu.USER_EMAIL as from_email \r\n"
        				+ "				from t_co_user tcu \r\n"
        				+ "				inner join t_co_interrelated tci\r\n"
        				+ "				on tcu.INTERRELATED_CUST_CODE = tci.INTERRELATED_CUST_CODE\r\n"
        				+ "				inner join t_co_cust_ir tcci \r\n"
        				+ "				on tci.INTERRELATED_CUST_CODE = tcci.INTERRELATED_CUST_CODE \r\n"
        				+ "				inner join t_co_cust_master tccm \r\n"
        				+ "				on tcci.CUST_CODE = tccm.CUST_CODE 		\r\n"
        				+ "				left outer join t_co_cust_user tccu \r\n"
        				+ "				on tccm.CUST_CODE = tccu.CUST_CODE \r\n"
        				+ "				where tcu.USER_ID = :userId\r\n"
        				+ "				and tccu.USER_EMAIL is not null\r\n"
        				+ "				and tccm.CUST_CODE in(" +  bidContent.get("custCode") + ")"
        		);
        		
            }else {
            	// 일반경쟁입찰
            	sbMail.append("select tcu.user_email\r\n"
                		+ ",		tccu.USER_EMAIL as from_email \r\n"
                		+ "from t_co_user tcu \r\n"
                		+ "inner join t_co_interrelated tci\r\n"
                		+ "on tcu.INTERRELATED_CUST_CODE = tci.INTERRELATED_CUST_CODE\r\n"
                		+ "inner join t_co_cust_ir tcci \r\n"
                		+ "on tci.INTERRELATED_CUST_CODE = tcci.INTERRELATED_CUST_CODE \r\n"
                		+ "inner join t_co_cust_master tccm \r\n"
                		+ "on tcci.CUST_CODE = tccm.CUST_CODE 		\r\n"
                		+ "left outer join t_co_cust_user tccu \r\n"
                		+ "on tccm.CUST_CODE = tccu.CUST_CODE \r\n"
                		+ "where tcu.USER_ID = :userId\r\n"
                		+ "and tccu.USER_EMAIL is not null\r\n"
                		+ "group by tccu.USER_EMAIL "
        		);
            }
    		
    		//쿼리 실행
    		Query queryMail = entityManager.createNativeQuery(sbMail.toString());
    		//조건 대입
    		queryMail.setParameter("userId", userId);
    		List<SendDto> sendList = new JpaResultMapper().list(queryMail, SendDto.class);
            
			if(sendList.size() > 0) {
	            // 계열사명 가져오기
	            StringBuilder sbInterNm = new StringBuilder(
	            		"select tci.INTERRELATED_NM "
	            		+ "from t_co_interrelated tci "
	            		+ "inner join t_co_user tcu "
	            		+ "	on tci.INTERRELATED_CUST_CODE = tcu.INTERRELATED_CUST_CODE "
	            		+ "where tcu.USER_ID = :userId");
	            
	            Query queryInterNm = entityManager.createNativeQuery(sbInterNm.toString());
	    		queryInterNm.setParameter("userId", userId);
	    		List<String> interNm = new JpaResultMapper().list(queryInterNm, String.class);
	    		
	            emailMap.put("type", "insert");
	            emailMap.put("biName", (String) bidContent.get("biName"));
	            emailMap.put("interNm", interNm.get(0)); // 계열사명
	            emailMap.put("reason", "");	// 입찰계획은 사유 없음
	            emailMap.put("sendList", sendList);	// 수신자 리스트
	            emailMap.put("biNo", biNo);	// 수신자 리스트
	            
	            this.updateEmail(emailMap);				
			}

        
        Map<String,Object> changeFileCheck = (Map<String, Object>) bidContent.get("changeFileCheck");
        // 첨부파일 대내용
    	String innerFileCheck = changeFileCheck.get("innerFileCheck").toString();
        if(!"Y".equals(innerFileCheck)) {
        	
            StringBuilder fileInit = new StringBuilder("DELETE from t_bi_upload where bi_no = :biNo and file_flag = '0'");
            Query fileInitQuery = entityManager.createNativeQuery(fileInit.toString());
            fileInitQuery.setParameter("biNo", biNo);
            fileInitQuery.executeUpdate();
            if("C".equals(innerFileCheck)) {
            	this.saveFileBid(innerFile, biNo, "0", "0");
        	}
        	
        } 
        // 첨부파일 대외용
    	String outerFileCheck = changeFileCheck.get("outerFileCheck").toString();
        if( !"Y".equals(outerFileCheck)) {
            StringBuilder fileInit = new StringBuilder("DELETE from t_bi_upload where bi_no = :biNo and file_flag = '1'");
            Query fileInitQuery = entityManager.createNativeQuery(fileInit.toString());
            fileInitQuery.setParameter("biNo", biNo);
            fileInitQuery.executeUpdate();
        	if("C".equals(outerFileCheck)) {
            	this.saveFileBid(outerFile, biNo, "0", "1");
        	}
        	

        }
        

        // 내역방식 - 파일등록
        if( "1".equals(CommonUtils.getString(bidContent.get("insModeCode")))){
            // 파일직접입력 - fileData / 대내용 - fileData2 / 대외용 - fileData3
        	// MultipartFile file, String biNo, String fCustCode, String fileFlag
        	
            // 파일직접입력 
        	
            StringBuilder init4 = new StringBuilder("DELETE from t_bi_spec_mat where bi_no = :biNo");

            Query initQuery4 = entityManager.createNativeQuery(init4.toString());
            initQuery4.setParameter("biNo", biNo);
            initQuery4.executeUpdate();
            
        	String insFileCheck = changeFileCheck.get("insFileCheck").toString();
        	
        	// Y는 기존에 있는 파일 그대로 저장되는 거라 따로 수정할 필요 없어서 Y 아닌 경우만 처리
        	if(!"Y".equals(insFileCheck)){
                StringBuilder fileInit = new StringBuilder("DELETE from t_bi_upload where bi_no = :biNo and file_flag = 'K'");
                Query fileInitQuery = entityManager.createNativeQuery(fileInit.toString());
                fileInitQuery.setParameter("biNo", biNo);
                fileInitQuery.executeUpdate();
                
	        	if("C".equals(insFileCheck)) {
	            	this.saveFileBid(insFile, biNo, "0", "K");
	        	}
        	}

        } 
        // 내역방식 - 내역직접등록
        else if( "2".equals(CommonUtils.getString(bidContent.get("insModeCode"))) ) {

            StringBuilder fileInit = new StringBuilder("DELETE from t_bi_upload where bi_no = :biNo and file_flag = 'K'");
            Query fileInitQuery = entityManager.createNativeQuery(fileInit.toString());
            fileInitQuery.setParameter("biNo", biNo);
            fileInitQuery.executeUpdate();
            
            int orderUc = 0;
            int orderQty = 0;
          
            StringBuilder init4 = new StringBuilder("DELETE from t_bi_spec_mat where bi_no = :biNo");

            Query initQuery4 = entityManager.createNativeQuery(init4.toString());
            initQuery4.setParameter("biNo", biNo);
            initQuery4.executeUpdate();
            List<Map<String, Object>> tableContent = (List<Map<String, Object>>)params.get("tableContent");
            for (Map<String, Object> data : tableContent) {
            	// orderUc 값이 null이거나 비어 있는 경우 0으로 초기화
                orderUc = !StringUtils.isEmpty(data.get("orderUc")) ? Integer.parseInt(data.get("orderUc").toString()) : 0;
                // orderQty 값이 null이거나 비어 있는 경우 0으로 초기화
                orderQty = !StringUtils.isEmpty(data.get("orderQty")) ? Integer.parseInt(data.get("orderQty").toString()) : 0;
                
                StringBuilder sbList4 = new StringBuilder(
                        "INSERT into t_bi_spec_mat (bi_no, seq, name, ssize, unitcode, order_uc, create_user, create_date, order_qty) "
                                +
                                "values (:biNo, :seq, :name, :ssize, :unitcode, :orderUc, :userId, sysdate(), :orderQty)");
                Query queryList4 = entityManager.createNativeQuery(sbList4.toString());
                queryList4.setParameter("biNo", biNo);
                queryList4.setParameter("seq", data.get("seq"));
                queryList4.setParameter("name", (String) data.get("name"));
                queryList4.setParameter("ssize", (String) data.get("ssize"));
                queryList4.setParameter("unitcode", (String) data.get("unitcode"));
                queryList4.setParameter("orderUc", orderUc);
                queryList4.setParameter("userId", userId);
                queryList4.setParameter("orderQty", orderQty);
                queryList4.executeUpdate();
            }
        }
        

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    public String newBiNo() {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());

        String interrelatedCode = userOptional.get().getInterrelatedCustCode();
        String biNoHeader = "";

        switch (interrelatedCode) {
            case "01":
                biNoHeader = "E";
                break;
            case "02":
                biNoHeader = "C";
                break;
            case "03":
                biNoHeader = "D";
                break;
            case "04":
                biNoHeader = "A";
                break;
            case "05":
                biNoHeader = "M";
                break;
            case "06":
                biNoHeader = "S";
                break;
            case "07":
                biNoHeader = "J";
                break;
            case "08":
                biNoHeader = "P";
                break;
            case "09":
                biNoHeader = "G";
                break;
            case "10":
                biNoHeader = "L";
                break;
            case "11":
                biNoHeader = "Z";
                break;
            case "12":
                biNoHeader = "T";
                break;
            case "13":
                biNoHeader = "K";
                break;
            case "14":
                biNoHeader = "N";
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
    
    /**
     * @param bidContent	입찰정보
     * @param custContent	지명경쟁 협력사 리스트
     * @param updateEmail	지명경쟁 협력사의 모든 대상자 이메일
     * @param tableContent	내역직접등록
     * @param insFile		파일직접등록
     * @param innerFile		대내용
     * @param outerFile		대외용
     * @return
     * @throws Exception 
     */
    @Transactional
    public ResultBody insertBid(Map<String, Object> params,  
    		MultipartFile insFile, 
    		MultipartFile innerFile, 
    		MultipartFile outerFile) throws Exception {
    	// 세션 가져오기
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String userId = principal.getUsername();
        
        Map<String, Object> bidContent = (Map<String, Object>) params.get("bidContent");
        
        // 입찰번호 생성
        String biNo = this.newBiNo();
        // 예산금액...?
        String bdAmtStr = (String) bidContent.get("bdAmt");
        
        BigDecimal bdAmt = null;
        if (!bdAmtStr.isEmpty()) {
            bdAmt = new BigDecimal(bdAmtStr);
        }
        
        StringBuilder sbList = new StringBuilder( // 입찰 t_bi_info_mat insert
                		"INSERT into t_bi_info_mat "
                		+ " (bi_no, bi_name, bi_mode, ins_mode, bid_join_spec,"
                		+ " special_cond, supply_cond, spot_date, spot_area, succ_deci_meth,"
                		+ " bid_open_date, amt_basis, bd_amt, est_start_date, est_close_date,"
                		+ " est_opener, est_bidder, open_att1, open_att2, ing_tag,"
                		+ " create_user, create_date, update_user, update_date, item_code,"
                		+ " gongo_id, pay_cond, bi_open, interrelated_cust_code, mat_dept, "
                		+ " mat_proc, mat_cls, mat_factory, mat_factory_line, mat_factory_cnt,"
                		+ " open_att1_sign, open_att2_sign) "
                        + "values "
                        + "(:biNo, :biName, :biModeCode, :insModeCode, :bidJoinSpec,"
                        + " :specialCond, :supplyCond, STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), :spotArea, :succDeciMethCode,"
                        + " sysdate(), :amtBasis, :bdAmt, STR_TO_DATE(:estStartDate, '%Y-%m-%d %H:%i'), STR_TO_DATE(:estCloseDate, '%Y-%m-%d %H:%i'), "
                        + " :estOpenerCode, :estBidderCode, :openAtt1Code, :openAtt2Code, 'A0',"
                        + " :userId, sysdate(), :userId, sysdate(), :itemCode,"
                        + " :gongoIdCode, :payCond, 'N', :interrelatedCustCode, :matDept,"
                        + " :matProc, :matCls, :matFactory, :matFactoryLine, :matFactoryCnt,"
                        + " 'N', 'N')");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("biNo", biNo);									// 입찰번호
        queryList.setParameter("biName", (String) bidContent.get("biName"));								// 입찰명
        queryList.setParameter("biModeCode", (String) bidContent.get("biModeCode"));						// 입찰방식 A/B
        queryList.setParameter("insModeCode", (String) bidContent.get("insModeCode"));						// 내역방식 파일등록/내역직접등록
        queryList.setParameter("bidJoinSpec", (String) bidContent.get("bidJoinSpec"));						// 입찰참가자격 
        queryList.setParameter("specialCond", (String) bidContent.get("specialCond"));						// 특수조건
        queryList.setParameter("supplyCond", (String) bidContent.get("supplyCond"));						// 납품조건
        queryList.setParameter("spotDate", (String) bidContent.get("spotDate"));							// 현장설명일자
        queryList.setParameter("spotArea", (String) bidContent.get("spotArea"));							// 현장설명장소
        queryList.setParameter("succDeciMethCode", (String) bidContent.get("succDeciMethCode"));			// 낙찰자결정방법
        queryList.setParameter("amtBasis", (String) bidContent.get("amtBasis"));							// 금액기준
        queryList.setParameter("bdAmt", bdAmt);		 													// 예산금액
        queryList.setParameter("estStartDate", (String) bidContent.get("estStartDate"));					// 제출시작일시
        queryList.setParameter("estCloseDate", (String) bidContent.get("estCloseDate"));					// 제출마감일시
        queryList.setParameter("estOpenerCode", (String) bidContent.get("estOpenerCode"));					// 개찰자
        queryList.setParameter("estBidderCode", (String) bidContent.get("estBidderCode"));					// 낙찰자
        queryList.setParameter("openAtt1Code", (String) bidContent.get("openAtt1Code"));					// 입회자1
        queryList.setParameter("openAtt2Code", (String) bidContent.get("openAtt2Code"));					// 입회자21
        queryList.setParameter("userId", userId);														// 
        queryList.setParameter("itemCode", (String) bidContent.get("itemCode"));							// 품목
        queryList.setParameter("gongoIdCode", (String) bidContent.get("gongoIdCode"));						// 입찰공고자
        queryList.setParameter("payCond", (String) bidContent.get("payCond"));								// 결제조건
        queryList.setParameter("interrelatedCustCode", (String) bidContent.get("interrelatedCustCode"));	// 입찰계열사 코드
        queryList.setParameter("matDept", (String) bidContent.get("matDept"));								// 사업부
        queryList.setParameter("matProc", (String) bidContent.get("matProc"));								// 공정
        queryList.setParameter("matCls", (String) bidContent.get("matCls"));								// 분류
        queryList.setParameter("matFactory", (String) bidContent.get("matFactory"));						// 공장동
        queryList.setParameter("matFactoryLine", (String) bidContent.get("matFactoryLine"));				// 라인
        queryList.setParameter("matFactoryCnt", (String) bidContent.get("matFactoryCnt"));					// 호기

        queryList.executeUpdate();

        StringBuilder sbList1 = new StringBuilder( // 입찰 Hist insert
                "INSERT into t_bi_info_mat_hist (bi_no, bi_name, bi_mode, ins_mode, bid_join_spec, special_cond, supply_cond, spot_date, "
                        +
                        "spot_area, succ_deci_meth, bid_open_date, amt_basis, bd_amt, est_start_date, est_close_date, est_opener, est_bidder, open_att1, "
                        +
                        "open_att2, ing_tag, create_user, create_date, item_code, gongo_id, pay_cond, bi_open, interrelated_cust_code, mat_dept, "
                        +
                        "mat_proc, mat_cls, mat_factory, mat_factory_line, mat_factory_cnt) values (:biNo, :biName, :biModeCode, :insModeCode, :bidJoinSpec, "
                        +
                        ":specialCond, :supplyCond, STR_TO_DATE(:spotDate, '%Y-%m-%d %H:%i'), :spotArea, :succDeciMethCode, sysdate(), "
                        +
                        ":amtBasis, :bdAmt, STR_TO_DATE(:estStartDate, '%Y-%m-%d %H:%i'), STR_TO_DATE(:estCloseDate, '%Y-%m-%d %H:%i'), "
                        +
                        ":estOpenerCode, :estBidderCode, :openAtt1Code, :openAtt2Code, 'A0', :userId, sysdate(), :itemCode, :gongoIdCode, :payCond, 'N', "
                        +
                        ":interrelatedCustCode, :matDept, :matProc, :matCls, :matFactory, :matFactoryLine, :matFactoryCnt)");

        Query queryList1 = entityManager.createNativeQuery(sbList1.toString());
        queryList1.setParameter("biNo", biNo);
        queryList1.setParameter("biName", (String) bidContent.get("biName"));
        queryList1.setParameter("biModeCode", (String) bidContent.get("biModeCode"));
        queryList1.setParameter("insModeCode", (String) bidContent.get("insModeCode"));
        queryList1.setParameter("bidJoinSpec", (String) bidContent.get("bidJoinSpec"));
        queryList1.setParameter("specialCond", (String) bidContent.get("specialCond"));
        queryList1.setParameter("supplyCond", (String) bidContent.get("supplyCond"));
        queryList1.setParameter("spotDate", (String) bidContent.get("spotDate"));
        queryList1.setParameter("spotArea", (String) bidContent.get("spotArea"));
        queryList1.setParameter("succDeciMethCode", (String) bidContent.get("succDeciMethCode"));
        queryList1.setParameter("amtBasis", (String) bidContent.get("amtBasis"));
        queryList1.setParameter("bdAmt", bdAmt);
        queryList1.setParameter("estStartDate", (String) bidContent.get("estStartDate"));
        queryList1.setParameter("estCloseDate", (String) bidContent.get("estCloseDate"));
        queryList1.setParameter("estOpenerCode", (String) bidContent.get("estOpenerCode"));
        queryList1.setParameter("estBidderCode", (String) bidContent.get("estBidderCode"));
        queryList1.setParameter("openAtt1Code", (String) bidContent.get("openAtt1Code"));
        queryList1.setParameter("openAtt2Code", (String) bidContent.get("openAtt2Code"));
        queryList1.setParameter("userId", userId);
        queryList1.setParameter("itemCode", (String) bidContent.get("itemCode"));
        queryList1.setParameter("gongoIdCode", (String) bidContent.get("gongoIdCode"));
        queryList1.setParameter("payCond", (String) bidContent.get("payCond"));
        queryList1.setParameter("interrelatedCustCode", (String) bidContent.get("interrelatedCustCode"));
        queryList1.setParameter("matDept", (String) bidContent.get("matDept"));
        queryList1.setParameter("matProc", (String) bidContent.get("matProc"));
        queryList1.setParameter("matCls", (String) bidContent.get("matCls"));
        queryList1.setParameter("matFactory", (String) bidContent.get("matFactory"));
        queryList1.setParameter("matFactoryLine", (String) bidContent.get("matFactoryLine"));
        queryList1.setParameter("matFactoryCnt", (String) bidContent.get("matFactoryCnt"));

        queryList1.executeUpdate();
        
        // 지명경쟁 협력사 등록
        StringBuilder sbMail = new StringBuilder();
        Map<String, Object> emailMap = new HashMap<String, Object>();
        if( "A".equals(CommonUtils.getString(bidContent.get("biModeCode"))) ) {

            List<Map<String, Object >> custContent = (List<Map<String, Object>>) params.get("custContent");
            //if(params.get(0).containsKey("insertYn") ) {
                for (Map<String, Object> data : custContent) {
                    StringBuilder sbList2 = new StringBuilder(
                            "INSERT into t_bi_info_mat_cust (bi_no, cust_code, rebid_att, esmt_yn, esmt_amt, succ_yn, create_user, create_date) "
                                    +
                                    "values (:biNo, :custCode, 'N', '0', 0, 'N', :userId, sysdate())");
                    Query queryList2 = entityManager.createNativeQuery(sbList2.toString());
                    queryList2.setParameter("biNo", (String) data.get("biNo"));
                    queryList2.setParameter("custCode", (String) data.get("custCode"));
                    queryList2.setParameter("userId", userId);
                    queryList2.executeUpdate();
                }
            //}
                
            // 지명경쟁 협력사의 모든 대상자 이메일 insert
            
            sbMail.append(
    				"select 	tcu.user_email\r\n"
    				+ "				,		tccu.USER_EMAIL as from_email \r\n"
    				+ "				from t_co_user tcu \r\n"
    				+ "				inner join t_co_interrelated tci\r\n"
    				+ "				on tcu.INTERRELATED_CUST_CODE = tci.INTERRELATED_CUST_CODE\r\n"
    				+ "				inner join t_co_cust_ir tcci \r\n"
    				+ "				on tci.INTERRELATED_CUST_CODE = tcci.INTERRELATED_CUST_CODE \r\n"
    				+ "				inner join t_co_cust_master tccm \r\n"
    				+ "				on tcci.CUST_CODE = tccm.CUST_CODE 		\r\n"
    				+ "				left outer join t_co_cust_user tccu \r\n"
    				+ "				on tccm.CUST_CODE = tccu.CUST_CODE \r\n"
    				+ "				where tcu.USER_ID = :userId\r\n"
    				+ "				and tccu.USER_EMAIL is not null\r\n"
    				+ "				and tccm.CUST_CODE in(" +  bidContent.get("custCode") + ")"
    		);
    		
        }else {
        	// 일반경쟁입찰
        	sbMail.append("select tcu.user_email\r\n"
            		+ ",		tccu.USER_EMAIL as from_email \r\n"
            		+ "from t_co_user tcu \r\n"
            		+ "inner join t_co_interrelated tci\r\n"
            		+ "on tcu.INTERRELATED_CUST_CODE = tci.INTERRELATED_CUST_CODE\r\n"
            		+ "inner join t_co_cust_ir tcci \r\n"
            		+ "on tci.INTERRELATED_CUST_CODE = tcci.INTERRELATED_CUST_CODE \r\n"
            		+ "inner join t_co_cust_master tccm \r\n"
            		+ "on tcci.CUST_CODE = tccm.CUST_CODE 		\r\n"
            		+ "left outer join t_co_cust_user tccu \r\n"
            		+ "on tccm.CUST_CODE = tccu.CUST_CODE \r\n"
            		+ "where tcu.USER_ID = :userId\r\n"
            		+ "and tccu.USER_EMAIL is not null\r\n"
            		+ "group by tccu.USER_EMAIL "
    		);
        }
        
		//쿼리 실행
		Query queryMail = entityManager.createNativeQuery(sbMail.toString());
		//조건 대입
		queryMail.setParameter("userId", userId);
		List<SendDto> sendList = new JpaResultMapper().list(queryMail, SendDto.class);
        
		if(sendList.size() > 0) {
	        // 계열사명 가져오기
	        StringBuilder sbInterNm = new StringBuilder(
	        		"select tci.INTERRELATED_NM "
	        		+ "from t_co_interrelated tci "
	        		+ "inner join t_co_user tcu "
	        		+ "	on tci.INTERRELATED_CUST_CODE = tcu.INTERRELATED_CUST_CODE "
	        		+ "where tcu.USER_ID = :userId");
	        
	        Query queryInterNm = entityManager.createNativeQuery(sbInterNm.toString());
			queryInterNm.setParameter("userId", userId);
			List<String> interNm = new JpaResultMapper().list(queryInterNm, String.class);
			
	        emailMap.put("type", "insert");
	        emailMap.put("biName", (String) bidContent.get("biName"));
	        emailMap.put("interNm", interNm.get(0)); // 계열사명
	        emailMap.put("reason", "");	// 입찰계획은 사유 없음
	        emailMap.put("sendList", sendList);	// 수신자 리스트
	        emailMap.put("biNo", biNo);	// 수신자 리스트
	        
	        this.updateEmail(emailMap);
		}


        // 첨부파일 대내용
        if( innerFile != null ) {
        	this.saveFileBid(innerFile, biNo, "0", "0");
        } 
        // 첨부파일 대외용
        if( outerFile != null ) {
        	this.saveFileBid(outerFile, biNo, "0", "1");
        }
        
        // 내역방식 - 파일등록
        if( "1".equals(CommonUtils.getString(bidContent.get("insModeCode")))){
            // 파일직접입력 - fileData / 대내용 - fileData2 / 대외용 - fileData3
        	// MultipartFile file, String biNo, String fCustCode, String fileFlag
        	
            // 파일직접입력 
            this.saveFileBid(insFile, biNo, "0", "K");
        } 
        // 내역방식 - 내역직접등록
        else if( "2".equals(CommonUtils.getString(bidContent.get("insModeCode"))) ) {
        	//this.updateBidItem();
            int orderUc = 0;
            int orderQty = 0;
          
            StringBuilder init4 = new StringBuilder("DELETE from t_bi_spec_mat where bi_no = :biNo");

            Query initQuery4 = entityManager.createNativeQuery(init4.toString());
            initQuery4.setParameter("biNo", biNo);
            initQuery4.executeUpdate();
            List<Map<String, Object>> tableContent = (List<Map<String, Object>>)params.get("tableContent");
            for (Map<String, Object> data : tableContent) {
            	// orderUc 값이 null이거나 비어 있는 경우 0으로 초기화
                orderUc = !StringUtils.isEmpty(data.get("orderUc")) ? Integer.parseInt(data.get("orderUc").toString()) : 0;
                // orderQty 값이 null이거나 비어 있는 경우 0으로 초기화
                orderQty = !StringUtils.isEmpty(data.get("orderQty")) ? Integer.parseInt(data.get("orderQty").toString()) : 0;
                
                StringBuilder sbList4 = new StringBuilder(
                        "INSERT into t_bi_spec_mat (bi_no, seq, name, ssize, unitcode, order_uc, create_user, create_date, order_qty) "
                                +
                                "values (:biNo, :seq, :name, :ssize, :unitcode, :orderUc, :userId, sysdate(), :orderQty)");
                Query queryList4 = entityManager.createNativeQuery(sbList4.toString());
                queryList4.setParameter("biNo", biNo);
                queryList4.setParameter("seq", data.get("seq"));
                queryList4.setParameter("name", (String) data.get("name"));
                queryList4.setParameter("ssize", (String) data.get("ssize"));
                queryList4.setParameter("unitcode", (String) data.get("unitcode"));
                queryList4.setParameter("orderUc", orderUc);
                queryList4.setParameter("userId", userId);
                queryList4.setParameter("orderQty", orderQty);
                queryList4.executeUpdate();
            }
        }
        
        ResultBody resultBody = new ResultBody();
        return resultBody;
    }
    private void saveFileBid( MultipartFile file, String biNo, String fCustCode, String fileFlag ) throws Exception {
        String filePath = null;
        String fileNm = null;
        if (file != null) {
            // 첨부파일 등록
            filePath = fileService.uploadEncryptedFile(file);

            // 원래 파일명
            fileNm = file.getOriginalFilename();

            StringBuilder sbList = new StringBuilder(
                    "INSERT into t_bi_upload (bi_no, file_flag, f_cust_code, file_nm, file_path, create_date, use_yn) "
                            +
                            "values (:biNo, :fileFlag, :fCustCode, :fileNm, :filePath, sysdate(), 'Y') ");
            Query queryList = entityManager.createNativeQuery(sbList.toString());
            queryList.setParameter("biNo", biNo);
            queryList.setParameter("fileFlag", fileFlag);
            queryList.setParameter("fCustCode", fCustCode);
            queryList.setParameter("fileNm", fileNm);
            queryList.setParameter("filePath", filePath);
            queryList.executeUpdate();
        }
    }

    @Transactional
    public ResultBody updateBidCust(@RequestBody List<Map<String, Object>> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String userId = principal.getUsername();
        //if (params.size() > 0) {
            String biNo = (String) params.get(0).get("biNo");

            StringBuilder init = new StringBuilder(
                    "DELETE from t_bi_info_mat_cust where bi_no = :biNo");

            Query initQuery = entityManager.createNativeQuery(init.toString());
            initQuery.setParameter("biNo", biNo);
            initQuery.executeUpdate();

            if(params.get(0).containsKey("insertYn") ) {
                for (Map<String, Object> data : params) {
                    StringBuilder sbList = new StringBuilder(
                            "INSERT into t_bi_info_mat_cust (bi_no, cust_code, rebid_att, esmt_yn, esmt_amt, succ_yn, create_user, create_date) "
                                    +
                                    "values (:biNo, :custCode, 'N', '0', 0, 'N', :userId, sysdate())");
                    Query queryList = entityManager.createNativeQuery(sbList.toString());
                    queryList.setParameter("biNo", (String) data.get("biNo"));
                    queryList.setParameter("custCode", (String) data.get("custCode"));
                    queryList.setParameter("userId", userId);
                    queryList.executeUpdate();
                }
            }
            
        //}

        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    @Transactional
    public ResultBody delete(Map<String, String> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();
        String biNo = params.get("biNo");
        ResultBody resultBody = new ResultBody();
        // 입찰서내용 삭제가 아닌 ING_TAG 를 D 로 업데이트 처리
//        StringBuilder sbList = new StringBuilder(
//                "DELETE FROM t_bi_info_mat " +
//                        "WHERE bi_no = :biNo");
        StringBuilder sbList = new StringBuilder(
                "UPDATE t_bi_info_mat "
                + "SET	ING_TAG = 'D' "
                + ",	UPDATE_USER = :userId "
                + ",	UPDATE_DATE = SYSDATE() "
                + "WHERE bi_no = :biNo");
        
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("userId", userId);
        queryList.setParameter("biNo", biNo);
        queryList.executeUpdate();
        
        // 발송대상 가져오기
        
        
        StringBuilder sbMail = new StringBuilder();

        if( "A".equals(CommonUtils.getString(params.get("biModeCode"))) ) {
                //지명경쟁입찰
                sbMail.append(
        				"select 	tcu.user_email\r\n"
        				+ "				,		tccu.USER_EMAIL as from_email \r\n"
        				+ "				from t_co_user tcu \r\n"
        				+ "				inner join t_co_interrelated tci\r\n"
        				+ "				on tcu.INTERRELATED_CUST_CODE = tci.INTERRELATED_CUST_CODE\r\n"
        				+ "				inner join t_co_cust_ir tcci \r\n"
        				+ "				on tci.INTERRELATED_CUST_CODE = tcci.INTERRELATED_CUST_CODE \r\n"
        				+ "				inner join t_co_cust_master tccm \r\n"
        				+ "				on tcci.CUST_CODE = tccm.CUST_CODE 		\r\n"
        				+ "				left outer join t_co_cust_user tccu \r\n"
        				+ "				on tccm.CUST_CODE = tccu.CUST_CODE \r\n"
        				+ "				where tcu.USER_ID = :userId\r\n"
        				+ "				and tccu.USER_EMAIL is not null\r\n"
        				+ "				and tccm.CUST_CODE in(" +  params.get("custCode") + ")"
        		);
        		
            }else {
            	// 일반경쟁입찰
            	sbMail.append("select tcu.user_email\r\n"
                		+ ",		tccu.USER_EMAIL as from_email \r\n"
                		+ "from t_co_user tcu \r\n"
                		+ "inner join t_co_interrelated tci\r\n"
                		+ "on tcu.INTERRELATED_CUST_CODE = tci.INTERRELATED_CUST_CODE\r\n"
                		+ "inner join t_co_cust_ir tcci \r\n"
                		+ "on tci.INTERRELATED_CUST_CODE = tcci.INTERRELATED_CUST_CODE \r\n"
                		+ "inner join t_co_cust_master tccm \r\n"
                		+ "on tcci.CUST_CODE = tccm.CUST_CODE 		\r\n"
                		+ "left outer join t_co_cust_user tccu \r\n"
                		+ "on tccm.CUST_CODE = tccu.CUST_CODE \r\n"
                		+ "where tcu.USER_ID = :userId\r\n"
                		+ "and tccu.USER_EMAIL is not null\r\n"
                		+ "group by tccu.USER_EMAIL "
        		);
            }
    		
    		//쿼리 실행
    		Query queryMail = entityManager.createNativeQuery(sbMail.toString());
    		//조건 대입
    		queryMail.setParameter("userId", userId);
        
		List<SendDto> sendList = new JpaResultMapper().list(queryMail, SendDto.class);
		if(sendList.size() > 0) {
	        Map<String, Object> emailMap = new HashMap<String, Object>();
	        emailMap.put("type", "del");
	        emailMap.put("biName", (String) params.get("biName"));
	        emailMap.put("reason", (String) params.get("reason")); // 삭제사유
	        emailMap.put("interNm", params.get("interNm"));
	        emailMap.put("sendList", sendList);	// 수신자 리스트
	        emailMap.put("biNo", biNo);	// 수신자 리스트
	        
	        this.updateEmail(emailMap);
		}

        return resultBody;
    }

    @Transactional
    public ResultBody updateBidItem(@RequestBody List<Map<String, Object>> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String userId = principal.getUsername();

        String biNo = (String) params.get(0).get("biNo");
        int orderUc = 0;
        int orderQty = 0;
      
        StringBuilder init = new StringBuilder(
                "DELETE from t_bi_spec_mat where bi_no = :biNo");

        Query initQuery = entityManager.createNativeQuery(init.toString());
        initQuery.setParameter("biNo", biNo);
        initQuery.executeUpdate();

        for (Map<String, Object> data : params) {
        	// orderUc 값이 null이거나 비어 있는 경우 0으로 초기화
            orderUc = !StringUtils.isEmpty(data.get("orderUc")) ? Integer.parseInt(data.get("orderUc").toString()) : 0;
            // orderQty 값이 null이거나 비어 있는 경우 0으로 초기화
            orderQty = !StringUtils.isEmpty(data.get("orderQty")) ? Integer.parseInt(data.get("orderQty").toString()) : 0;
            
            StringBuilder sbList = new StringBuilder(
                    "INSERT into t_bi_spec_mat (bi_no, seq, name, ssize, unitcode, order_uc, create_user, create_date, order_qty) "
                            +
                            "values (:biNo, :seq, :name, :ssize, :unitcode, :orderUc, :userId, sysdate(), :orderQty)");
            Query queryList = entityManager.createNativeQuery(sbList.toString());
            queryList.setParameter("biNo", (String) data.get("biNo"));
            queryList.setParameter("seq", data.get("seq"));
            queryList.setParameter("name", (String) data.get("name"));
            queryList.setParameter("ssize", (String) data.get("ssize"));
            queryList.setParameter("unitcode", (String) data.get("unitcode"));
            queryList.setParameter("orderUc", orderUc);
            queryList.setParameter("userId", userId);
            queryList.setParameter("orderQty", orderQty);
            queryList.executeUpdate();
        }
        ResultBody resultBody = new ResultBody();
        return resultBody;
    }

    @Transactional
    public ResultBody updateBidFile(MultipartFile file, Map<String, Object> params) {
        try {
            String biNo = (String) params.get("biNo");
            String fCustCode = (String) params.get("fCustCode");
            String fileFlag = (String) params.get("fileFlag");
            String filePath = null;
            String fileNm = null;

            StringBuilder init = new StringBuilder(
                    "DELETE from t_bi_upload where bi_no = :biNo and f_cust_code = :fCustCode and file_flag = :fileFlag");

            Query initQuery = entityManager.createNativeQuery(init.toString());
            initQuery.setParameter("biNo", biNo);
            initQuery.setParameter("fCustCode", fCustCode);
            initQuery.setParameter("fileFlag", fileFlag);
            initQuery.executeUpdate();

            if (file != null) {
                // 첨부파일 등록
                filePath = fileService.uploadEncryptedFile(file);

                // 원래 파일명
                fileNm = file.getOriginalFilename();

                StringBuilder sbList = new StringBuilder(
                        "INSERT into t_bi_upload (bi_no, file_flag, f_cust_code, file_nm, file_path, create_date, use_yn) "
                                +
                                "values (:biNo, :fileFlag, :fCustCode, :fileNm, :filePath, sysdate(), 'Y') ");
                Query queryList = entityManager.createNativeQuery(sbList.toString());
                queryList.setParameter("biNo", biNo);
                queryList.setParameter("fileFlag", fileFlag);
                queryList.setParameter("fCustCode", (String) params.get("fCustCode"));
                queryList.setParameter("fileNm", fileNm);
                queryList.setParameter("filePath", filePath);
                queryList.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        ResultBody resultBody = new ResultBody();
        return resultBody;
    }
    
	/**
	 * 메일전송
	 * @param params : (String) type, (String) biName, (String) reason, (String) interNm, (List<SendDto>) sendList
	 */
	public void updateEmail(Map<String, Object> params) {
		List<SendDto> sendList = (List<SendDto>) params.get("sendList");		//수신인/발송인 메일 리스트

		//메일 내용 셋팅
		Map<String, String> emailContent = this.emailContent(params);
		
		if (sendList != null) {
			for (SendDto recvInfo : sendList) {
				StringBuilder sbList = new StringBuilder(
						"INSERT INTO t_email (title, conts, send_flag, create_date, receives, from_mail, bi_no) VALUES " +
								"(:title, :content, 'N', CURRENT_TIMESTAMP, :userEmail, :fromMail, :biNo)");

				Query queryList = entityManager.createNativeQuery(sbList.toString());
				queryList.setParameter("title", emailContent.get("title"));
				queryList.setParameter("content", emailContent.get("content"));
				queryList.setParameter("userEmail", recvInfo.getUserEmail());
				queryList.setParameter("fromMail", recvInfo.getFromEmail());
				queryList.setParameter("biNo", params.get("biNo"));
				queryList.executeUpdate();
			}
		}
	}
		
	//메일 제목 및 내용 셋팅
	public Map<String, String> emailContent(Map<String, Object> params){
		Map<String, String> result = new HashMap<String, String>();
		
		String type = CommonUtils.getString(params.get("type"));			// del : 입찰삭제 , notice : 입찰공고, insert: 입찰등록, fail: 유찰, rebid:재입찰,succ:낙찰
		String biName = CommonUtils.getString(params.get("biName"));		//입찰명
		String reason = CommonUtils.getString(params.get("reason"));		//사유
		String interNm = CommonUtils.getString(params.get("interNm"));		//계열사명
		
		String title = "";
		String content = "";
		
		//입찰 계획 삭제
		if(type.equals("del")) {
			title = "[일진그룹 e-bidding] 입찰 계획 삭제(" + biName + ")";
			content = "입찰명 [" + biName + "] 입찰계획을\n삭제하였습니다.\n아래 삭제사유를 확인해 주십시오.\n\n"+
						"-삭제사유\n" + reason;
			
		//입찰 공고
		}else if(type.equals("notice")) {
			title = "[일진그룹 e-bidding] 입찰 공고(" + biName + ")";
			content = "[" + interNm + "]에서 입찰공고 하였습니다.\n입찰명은 [" + biName + "] 입니다.\n"
					+ "자세한 사항은 e-bidding 시스템에 로그인하여 확인해 주십시오.\n\n";
			
		//입찰 계획 등록
		}else if(type.equals("insert")) {
			title = "[일진그룹 e-bidding] 계획 등록(" + biName + ")";
			content = "[" + interNm + "]에서 입찰계획을 등록하였습니다.\n입찰명은 [" + biName + "] 입니다.\n"
					+ "자세한 사항은 e-bidding 시스템에 로그인하여 확인해 주십시오.\n\n";
			
		//입찰 유찰처리
		}else if(type.equals("fail")) {
			title = "[일진그룹 e-bidding] 유찰 처리(" + biName + ")";
			content = "입찰명 [" + biName + "]를 유찰처리 하였습니다.\n" +
					"아래 유찰사유를 확인해 주십시오.\n\n"+
					"-유찰사유\n" + reason;
			
		//재입찰
		}else if(type.equals("rebid")) {
			title = "[일진그룹 e-bidding] 재입찰(" + biName + ")";
			content = "입찰명 [" + biName + "]이 재입찰되었습니다.\n" +
					"아래 재입찰사유를 확인해 주시고 e-bidding 시스템에 로그인하여 다시 한번 투찰해 주십시오\n\n"+
					"-재입찰사유\n" + reason;
			
		//낙찰
		}else if(type.equals("succ")) {
			title = "[일진그룹 e-bidding] 낙찰(" + biName + ")";
			content = "입찰명 [" + biName + "]에 업체선정 되었습니다.\n" +
					"자세한 사항은 e-bidding 시스템에  로그인하여 입찰내용 확인 및 낙찰확인을 하시기 바랍니다.\n(낙찰확인은 계약과 관련없는 내부절차 입니다.)\n\n"+
					"-추가합의사항\n" + reason;
		}
		
		result.put("title", title);
		result.put("content", content);
		return result;
	}

    /**
     * 입찰 로그
     * @param params
     */
    @Transactional
    public void updateLog(Map<String, String> params) {
        String msg = params.get("msg");
        String biNo = params.get("biNo");
        String userId = params.get("userId");

        StringBuilder sbList = new StringBuilder(
                "INSERT INTO t_bi_log (bi_no, user_id, log_text, create_date) VALUES " +
                        "(:biNo, :userId, :msg, sysdate())");

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("msg", msg);
        queryList.setParameter("biNo", biNo);
        queryList.setParameter("userId", userId);
        queryList.executeUpdate();
    }

    // 첨부파일 다운로드
    public ByteArrayResource downloadFile(Map<String, Object> params) {

        String filePath = (String) params.get("fileId");
        ByteArrayResource fileResource = null;

        try {
            fileResource = fileService.downloadDecryptedFile(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileResource;
    }

}