package iljin.framework.ebid.custom.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.core.util.Pair;
import iljin.framework.ebid.custom.dto.TCoCustUserDto;
import iljin.framework.ebid.custom.dto.TCoUserDto;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.PagaUtils;
import lombok.extern.slf4j.Slf4j;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CustUserService {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page userList(Map<String, Object> params) {
        StringBuilder sbCount = new StringBuilder(" SELECT COUNT(1) FROM t_co_cust_user a WHERE cust_code = :custCode");
        StringBuilder sbList = new StringBuilder(" SELECT user_name, user_id, user_buseo, user_position, user_email, user_tel, user_hp, user_type, use_yn FROM t_co_cust_user a WHERE cust_code = :custCode");
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("userName"))) {
            sbWhere.append(" AND user_name like concat('%',:userName,'%')");
        }
        if (!StringUtils.isEmpty(params.get("userId"))) {
            sbWhere.append(" AND user_id like concat('%',:userId,'%')");
        }
        if (!StringUtils.isEmpty(params.get("useYn"))) {
            sbWhere.append(" AND use_yn = :useYn");
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
        if (!StringUtils.isEmpty(params.get("useYn"))) {
            queryList.setParameter("useYn", params.get("useYn"));
            queryTotal.setParameter("useYn", params.get("useYn"));
        }

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, TCoCustUserDto.class);

        BigInteger count = (BigInteger) queryTotal.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    public TCoCustUserDto detail(String custCode, String id) {
        StringBuilder sb = new StringBuilder(" select user_name, user_id, user_buseo, user_position, user_email, user_tel, user_hp, user_type, use_yn from t_co_cust_user where cust_code = :custCode AND user_id = :userId");
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter("custCode", custCode);
        query.setParameter("userId", id);
        TCoCustUserDto data = new JpaResultMapper().uniqueResult(query, TCoCustUserDto.class);
        return data;
    }

    @Transactional
    public ResultBody save(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
        StringBuilder sbQuery = null;
        String userPwd = (String) params.get("userPwd");
        // 저장
        if ((boolean) params.get("isCreate")) {
            sbQuery = new StringBuilder(
                    " insert into t_co_cust_user (user_id, user_pwd, user_name, cust_code, user_type, user_hp, user_tel, user_email, user_position, user_buseo, use_yn, create_user, create_date, update_user, update_date) " +
                            " values (:userId, :userPwd, :userName, :custCode, :userType, :userHp, :userTel, :userEmail, :userPosition, :userBuseo, :useYn, :updateUser, now(), :updateUser, now())");
        }
        // 수정
        else {
            sbQuery = new StringBuilder(
                    " update t_co_cust_user "
                            + "set user_name = :userName"
                            + (userPwd != null ? ", user_pwd = :userPwd" : "")
                            + ", user_hp = :userHp"
                            + ", user_tel = :userTel"
                            + ", user_email = :userEmail"
                            + ", user_position = :userPosition"
                            + ", user_buseo = :userBuseo"
                            + ", update_user = :updateUser"
                            + ", update_date = now() "
                            + "where user_id = :userId");
        }
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        query.setParameter("userId", params.get("userId"));
        if ((boolean) params.get("isCreate")) {
            query.setParameter("userType", "2"); // 신규만 적용
            query.setParameter("custCode", user.getCustCode());
            query.setParameter("useYn", "Y");
            // 비밀번호 암호화
            query.setParameter("userPwd", passwordEncoder.encode(userPwd));
        } else if (userPwd != null) {
            query.setParameter("userPwd", passwordEncoder.encode(userPwd));
        }
        query.setParameter("userName", params.get("userName"));
        query.setParameter("userHp", params.get("userHp"));
        query.setParameter("userTel", params.get("userTel"));
        query.setParameter("userEmail", params.get("userEmail"));
        query.setParameter("userPosition", params.get("userPosition"));
        query.setParameter("userBuseo", params.get("userBuseo"));
        query.setParameter("updateUser", user.getUsername());
        query.executeUpdate();

        return resultBody;
    }

    @Transactional
    public ResultBody del(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
        StringBuilder sbQuery = new StringBuilder(" UPDATE t_co_cust_user SET use_yn = 'N', update_user = :updateUser, update_date = now() WHERE user_id = :userId");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("userId", params.get("userId"));
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        query.setParameter("updateUser", user.getUsername());
        query.executeUpdate();
        return resultBody;
    }
}
