package iljin.framework.ebid.custom.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.util.Pair;
import iljin.framework.ebid.custom.dto.TCoCustUserDto;
import iljin.framework.ebid.custom.dto.TCoUserDto;
import iljin.framework.ebid.etc.util.PagaUtils;
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
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CustUserService {

    @PersistenceContext
    private EntityManager entityManager;

    public Page userList(Map<String, Object> params) {
        StringBuilder sbCount = new StringBuilder(" SELECT COUNT(1) FROM t_co_cust_user a WHERE cust_code = :custCode");
        StringBuilder sbList = new StringBuilder(" SELECT user_name, user_id, user_buseo, user_position, user_email, user_tel, user_hp, user_type FROM t_co_cust_user a WHERE cust_code = :custCode");
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("userName"))) {
            sbWhere.append(" AND user_name like concat('%',:userName,'%')");
        }
        if (!StringUtils.isEmpty(params.get("userId"))) {
            sbWhere.append(" AND user_id like concat('%',:userId,'%')");
        }
        sbList.append(sbWhere);
        sbList.append(" ORDER BY create_date DESC");
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        sbCount.append(sbWhere);
        Query queryTotal = entityManager.createNativeQuery(sbCount.toString());

        queryList.setParameter("custCode", params.get("custCode"));
        queryTotal.setParameter("custCode", params.get("custCode"));

        if (!StringUtils.isEmpty(params.get("userName"))) {
            queryList.setParameter("userName", params.get("userName"));
            queryTotal.setParameter("userName", params.get("userName"));
        }
        if (!StringUtils.isEmpty(params.get("userId"))) {
            queryList.setParameter("userId", params.get("userId"));
            queryTotal.setParameter("userId", params.get("userId"));
        }

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, TCoCustUserDto.class);

        BigInteger count = (BigInteger) queryTotal.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }
}
