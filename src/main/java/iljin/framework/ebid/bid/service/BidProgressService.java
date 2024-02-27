package iljin.framework.ebid.bid.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.UserDto;
import iljin.framework.core.security.user.UserRepository;
import iljin.framework.core.security.user.UserRepositoryCustom;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.dto.BidProgressDto;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class BidProgressService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TCoUserRepository tCoUserRepository;
    @Autowired
    Util util;

    public Page progresslist(@RequestBody Map<String, Object> params) {
        // String id = util.getLoginId();
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TCoUser user = tCoUserRepository.findById(principal.getUsername()).get();

        System.out.println("111111111111" + user);
        System.out.println(principal.getUsername());

        StringBuilder sbCount = new StringBuilder(" select count(1) from t_bi_info_mat a where 1=1");
        StringBuilder sbList = new StringBuilder(
                "SELECT a.bi_no AS bi_no, a.bi_name AS bi_name, " +
                        "DATE_FORMAT(a.est_start_date, '%Y-%m-%d %H:%i') AS est_start_date, " +
                        "DATE_FORMAT(a.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date, " +
                        "CASE WHEN a.bi_mode = 'A' THEN '지명' ELSE '일반' END AS bi_mode, " +
                        "CASE WHEN a.ins_mode = '1' THEN '파일' ELSE '직접입력' END AS ins_mode, " +
                        "b.user_name AS est_opener, b.user_email AS opener_email, " +
                        "c.user_name AS gongo_id, c.user_email AS gongo_email, " +
                        "a.interrelated_cust_code AS interrelated_cust_code " +
                        "FROM t_bi_info_mat a, t_co_user b, t_co_user c " +
                        "WHERE a.est_opener = b.user_id AND a.gongo_id = c.user_id " +
                        "AND a.ing_tag = 'A0'");
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("bidNo"))) {
            sbWhere.append(" and a.bi_no like concat('%',:bidNo,'%')");
        }

        if (!StringUtils.isEmpty(params.get("bidName"))) {
            sbWhere.append(" and a.bi_name like concat('%',:bidName,'%')");
        }
        sbList.append(sbWhere);
        // sbList.append(" order by bi_no");

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

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, BidProgressDto.class);

        BigInteger count = (BigInteger) queryTotal.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    public List<BidProgressDetailDto> progresslistDetail(String param) {
        StringBuilder sbList = new StringBuilder(
                "SELECT a.bi_no AS bi_no, a.bi_name AS bi_name, " +
                        "CASE WHEN a.bi_mode = 'A' THEN '지명경쟁입찰' ELSE '일반경쟁입찰' END AS bi_mode, " +
                        "CASE WHEN a.ins_mode = '1' THEN '파일등록' ELSE '직접입력' END AS ins_mode, " +
                        "a.bid_join_spec AS bid_join_spec, a.special_cond AS special_cond, a.supply_cond AS supply_cond, "
                        +
                        "DATE_FORMAT(a.spot_date, '%Y-%m-%d %H:%i') AS spot_date, a.spot_area AS spot_area, " +
                        "CASE WHEN a.succ_deci_meth = '1' THEN '최저가' WHEN a.succ_deci_meth = '2' THEN '최고가' " +
                        "WHEN a.succ_deci_meth = '3' THEN '내부적격심사' WHEN a.succ_deci_meth = '4' THEN '최고가&내부적격심사' " +
                        "ELSE '최저가&내부적격심사' END AS succ_deci_meth, DATE_FORMAT(a.est_start_date, '%Y-%m-%d %H:%i') AS est_start_date, "
                        +
                        "DATE_FORMAT(a.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date, b.user_name AS est_opener, "
                        +
                        "DATE_FORMAT(a.est_open_date, '%Y-%m-%d %H:%i') AS est_open_date, c.user_name AS open_att1, " +
                        "a.open_att1_sign AS open_att1_sign, d.user_name AS open_att2, a.open_att2_sign AS open_att2_sign, "
                        +
                        "a.ing_tag AS ing_tag, f.item_code AS item_code, e.user_name AS gongo_id, a.pay_cond AS pay_cond, a.why_A3 AS why_A3, "
                        +
                        "a.why_A7 AS why_A7, a.bi_open AS bi_open, a.interrelated_cust_code AS interrelated_cust_code, a.real_amt AS real_amt, a.amt_basis AS amt_basis, a.bd_amt AS bd_amt"
                        +
                        "a.add_accept AS add_accept, a.mat_dept AS mat_dept, a.mat_proc AS mat_proc, a.mat_cls AS mat_cls, "
                        +
                        "a.mat_factory AS mat_factory, a.mat_factory_line AS mat_factory_line, a.mat_factory_cnt AS mat_factory_cnt "
                        +
                        "FROM t_bi_info_mat a " +
                        "JOIN t_co_user b ON a.est_opener = b.user_id " +
                        "LEFT JOIN t_co_user c ON a.open_att1 = c.user_id " +
                        "LEFT JOIN t_co_user d ON a.open_att2 = d.user_id " +
                        "JOIN t_co_user e ON a.gongo_id = e.user_id "+
                        "LEFT JOIN t_co_item f ON a.item_code = f.item_code "+
                        "WHERE 1=1 ");
        StringBuilder sbWhere = new StringBuilder();
        if (!StringUtils.isEmpty(param)) {
            sbWhere.append(" and a.bi_no = :param");
        }
        sbList.append(sbWhere);

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        queryList.setParameter("param", param);

        return new JpaResultMapper().list(queryList, BidProgressDetailDto.class);
    }

}
