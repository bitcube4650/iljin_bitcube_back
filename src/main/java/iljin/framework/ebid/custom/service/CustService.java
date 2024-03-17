package iljin.framework.ebid.custom.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.custom.dto.TCoCustMasterDto;
import iljin.framework.ebid.custom.dto.TCoUserDto;
import iljin.framework.ebid.etc.util.PagaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qlrm.mapper.JpaResultMapper;
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
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustService {

    @PersistenceContext
    private EntityManager entityManager;

    public Page custList(Map<String, Object> params) {
        StringBuilder sbCount = new StringBuilder(" select count(1) from t_co_cust_master x where 1 = 1");
        StringBuilder sbList = new StringBuilder(" SELECT cust_code \n" +
                "     , cust_name \n" +
                "     , (SELECT item_name FROM t_co_item x WHERE x.item_code = a.cust_type1) AS cust_type1\n" +
                "     , CONCAT(SUBSTR(regnum, 1, 3), '-', SUBSTR(regnum, 4, 2), '-', SUBSTR(regnum, 6, 5)) AS regnum\n" +
                "     , pres_name \n" +
                "     , (SELECT user_name FROM t_co_cust_user x WHERE x.cust_code = a.cust_code AND x.user_type = '1' LIMIT 1) AS user_name\n" +
                "     , DATE_FORMAT(create_date, '%Y-%m-%d %H:%i') AS create_date \n" +
                "  FROM t_co_cust_master a\n" +
                " WHERE 1 = 1");
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("certYn"))) {
            sbWhere.append(" AND cert_yn = :certYn");
        }
        if (!StringUtils.isEmpty(params.get("custName"))) {
            sbWhere.append(" AND cust_name like concat('%',:custName,'%')");
        }
        if (!StringUtils.isEmpty(params.get("custTypeCode1"))) {
            sbWhere.append(" AND cust_type1 = :custTypeCode1");
        }
        sbList.append(sbWhere);
        sbList.append(" order by create_date desc");
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        sbCount.append(sbWhere);
        Query queryTotal = entityManager.createNativeQuery(sbCount.toString());

        if (!StringUtils.isEmpty(params.get("certYn"))) {
            queryList.setParameter("certYn", params.get("certYn"));
            queryTotal.setParameter("certYn", params.get("certYn"));
        }
        if (!StringUtils.isEmpty(params.get("custName"))) {
            queryList.setParameter("custName", params.get("custName"));
            queryTotal.setParameter("custName", params.get("custName"));
        }
        if (!StringUtils.isEmpty(params.get("custTypeCode1"))) {
            queryList.setParameter("custTypeCode1", params.get("custTypeCode1"));
            queryTotal.setParameter("custTypeCode1", params.get("custTypeCode1"));
        }

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, TCoCustMasterDto.class);

        BigInteger count = (BigInteger) queryTotal.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    public TCoCustMasterDto custDetail(String id) {
        StringBuilder sb = new StringBuilder(" SELECT a.cust_code \n" +
                "     , cust_name \n" +
                "     , (SELECT interrelated_nm FROM t_co_interrelated x WHERE x.interrelated_cust_code = a.interrelated_cust_code) AS interrelated_nm\n" +
                "     , (SELECT item_name FROM t_co_item x WHERE x.item_code = a.cust_type1) AS cust_type1\n" +
                "     , (SELECT item_name FROM t_co_item x WHERE x.item_code = a.cust_type2) AS cust_type2\n" +
                "     , CONCAT(SUBSTR(regnum, 1, 3), '-', SUBSTR(regnum, 4, 2), '-', SUBSTR(regnum, 6, 5)) AS regnum\n" +
                "     , pres_name \n" +
                "     , CONCAT(SUBSTR(pres_jumin_no, 1, 6), '-', SUBSTR(pres_jumin_no, 7, 7)) AS pres_jumin_no\n" +
                "     , capital\n" +
                "     , found_year \n" +
                "     , tel\n" +
                "     , fax\n" +
                "     , zipcode \n" +
                "     , addr \n" +
                "     , addr_detail\n" +
                "     , b.user_name \n" +
                "     , b.user_email \n" +
                "     , b.user_id \n" +
                "     , b.user_hp \n" +
                "     , b.user_tel \n" +
                "     , b.user_buseo \n" +
                "     , b.user_position \n" +
                "  FROM t_co_cust_master a\n" +
                "     , t_co_cust_user   b\n" +
                " WHERE a.cust_code = b.cust_code\n" +
                "   AND b.user_type = '1'" +
                "   AND a.cust_code   = :custCode" +
                " LIMIT 1");
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter("custCode", id);
        TCoCustMasterDto data = new JpaResultMapper().uniqueResult(query, TCoCustMasterDto.class);
        return data;
    }

    @Transactional
    public ResultBody save(Map<String, String> params) {
        ResultBody resultBody = new ResultBody();
        return resultBody;
    }
    @Transactional
    public ResultBody approval(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
        StringBuilder sbQuery = new StringBuilder(" UPDATE  t_co_cust_master SET cert_yn = 'Y', update_user = :userId, update_date = now() WHERE cust_code = :custCode LIMIT 1");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("custCode", params.get("custCode"));
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        query.setParameter("userId", principal.getUsername());
        query.executeUpdate();
        return resultBody;
    }
    @Transactional
    public ResultBody del(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
        StringBuilder sbQuery = new StringBuilder(" UPDATE  t_co_cust_master SET cert_yn = 'D', etc = :etc, update_user = :userId, update_date = now() WHERE cust_code = :custCode LIMIT 1");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("etc", params.get("etc"));
        query.setParameter("custCode", params.get("custCode"));
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        query.setParameter("userId", principal.getUsername());
        query.executeUpdate();
        return resultBody;
    }
}
