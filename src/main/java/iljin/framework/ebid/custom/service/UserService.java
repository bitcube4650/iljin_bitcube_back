package iljin.framework.ebid.custom.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.util.Pair;
import iljin.framework.ebid.custom.dto.TCoUserDto;
import iljin.framework.ebid.custom.entity.TCoItem;
import iljin.framework.ebid.custom.entity.TCoItemGrp;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoItemGrpRepository;
import iljin.framework.ebid.custom.repository.TCoItemRepository;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.*;

@Service
@Slf4j
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;

    public List interrelatedList() {
        StringBuilder sb = new StringBuilder(" select interrelated_cust_code, interrelated_nm from t_co_interrelated where use_yn = 'Y' order by interrelated_nm");
        Query query = entityManager.createNativeQuery(sb.toString());
        return new JpaResultMapper().list(query, Pair.class);
    }

    public Page userList(Map<String, Object> params) {
        StringBuilder sbCount = new StringBuilder(" select count(1) from t_co_user x where 1=1");
        StringBuilder sbList = new StringBuilder(" select user_name, user_id, user_position, dept_name, user_tel, user_hp, user_auth, use_yn, (select interrelated_nm from t_co_interrelated x where x.interrelated_cust_code = a.interrelated_cust_code) as interrelated_cust_nm from t_co_user a where 1=1");
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("interrelatedCustCode"))) {
            sbWhere.append(" and interrelated_cust_code = '"+params.get("interrelatedCustCode")+"'");
        }
        if (!StringUtils.isEmpty(params.get("useYn"))) {
            sbWhere.append(" and use_yn = :useYn");
        }
        if (!StringUtils.isEmpty(params.get("userName"))) {
            sbWhere.append(" and user_name like concat('%',:userName,'%')");
        }
        if (!StringUtils.isEmpty(params.get("userId"))) {
            sbWhere.append(" and user_id like concat('%',:userId,'%')");
        }
        sbList.append(sbWhere);
        sbList.append(" order by create_date desc");
        Query queryList = entityManager.createNativeQuery(sbList.toString());
        sbCount.append(sbWhere);
        Query queryTotal = entityManager.createNativeQuery(sbCount.toString());

        if (!StringUtils.isEmpty(params.get("interrelatedCustCode"))) {
            queryList.setParameter("interrelatedCustCode", params.get("interrelatedCustCode"));
            queryTotal.setParameter("interrelatedCustCode", params.get("interrelatedCustCode"));
        }
        if (!StringUtils.isEmpty(params.get("useYn"))) {
            queryList.setParameter("useYn", params.get("useYn"));
            queryTotal.setParameter("useYn", params.get("useYn"));
        }
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
        List list = new JpaResultMapper().list(queryList, TCoUserDto.class);

        BigInteger count = (BigInteger) queryTotal.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    public TCoUserDto detail(String id) {
        StringBuilder sb = new StringBuilder(" select user_id, user_name, user_position, dept_name, user_tel, user_hp, user_auth, use_yn, interrelated_cust_code, ifnull(openauth, '') as openauth, user_email from t_co_user where user_id = :userId");
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter("userId", id);
        TCoUserDto data = new JpaResultMapper().uniqueResult(query, TCoUserDto.class);
        if ("4".equals(data.getUserAuth())) { // 감사사용자의 경우 감사 계열사 조회
            data.setUserInterrelated(this.interrelatedListByUser(id));
        }
        return data;
    }

    public List interrelatedListByUser(String id) {
        StringBuilder sb = new StringBuilder(" select a.interrelated_cust_code, interrelated_nm from t_co_interrelated a, t_co_user_interrealated b where a.use_yn = 'Y' and a.interrelated_cust_code = b.interrelated_cust_code and b.user_id = :userId order by interrelated_nm");
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter("userId", id);
        return new JpaResultMapper().list(query, Pair.class);
    }
    @Transactional
    public ResultBody save(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
        StringBuilder sbQuery = null;
        if ((boolean) params.get("isCreate")) {
            sbQuery = new StringBuilder(
            " insert into t_co_user (user_id, user_pwd, user_name, interrelated_cust_code, user_auth, openauth, user_hp, user_tel, user_email, user_position, dept_name, use_yn, create_user, create_date, update_user, update_date) " +
            " values (:userId, :userPwd, :userName, :interrelatedCustCode, :userAuth, :openauth, :userHp, :userTel, :userEmail, :userPosition, :deptName, :useYn, :updateUser, now(), :updateUser, now())");
        } else {
            sbQuery = new StringBuilder(
            " update t_co_user set user_name = :userName, interrelated_cust_code = :interrelatedCustCode, user_auth = :userAuth, openauth = :openauth, user_hp = :userHp, user_tel = :userTel, user_email = :userEmail, user_position = :userPosition, dept_name = :deptName, use_yn = :useYn, update_user = :updateUser, update_date = now() where user_id = :userId");
        }
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("userId", params.get("userId"));
        if ((boolean) params.get("isCreate")) {
            query.setParameter("userPwd", params.get("userPwd"));
        }
        query.setParameter("userName", params.get("userName"));
        query.setParameter("interrelatedCustCode", params.get("interrelatedCustCode"));
        query.setParameter("userAuth", params.get("userAuth"));
        query.setParameter("openauth", params.get("openauth"));
        query.setParameter("userHp", params.get("userHp"));
        query.setParameter("userTel", params.get("userTel"));
        query.setParameter("userEmail", params.get("userEmail"));
        query.setParameter("userPosition", params.get("userPosition"));
        query.setParameter("deptName", params.get("deptName"));
        query.setParameter("useYn", params.get("useYn"));
        String createUser = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        query.setParameter("updateUser", createUser);
        query.executeUpdate();

        // 고유 키가 없기에 매번 지워야 한다.
        sbQuery = new StringBuilder(" delete from t_co_user_interrelated where user_id = :userId");
        query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("userId", params.get("userId"));
        query.executeUpdate();
        // 감사사용자의 경우 감사계열사 정보를 저장 처리
        if ("4".equals(params.get("userAuth"))) {
            List<Map> list = (List) params.get("userInterrelatedList");
            for (Map<String, Object> data : list) {
                if (data.get("check") != null) {
                    sbQuery = new StringBuilder(" insert into t_co_user_interrelated (interrelated_cust_code, user_id) values (:interrelatedCustCode, :userId)");
                    query = entityManager.createNativeQuery(sbQuery.toString());
                    query.setParameter("interrelatedCustCode", data.get("key"));
                    query.setParameter("userId", params.get("userId"));
                    query.executeUpdate();
                }
            }
        }
        return resultBody;
    }
    public ResultBody idcheck(Map<String, Object> params) {
        ResultBody resultBody = new ResultBody();
        StringBuilder sb = new StringBuilder(" SELECT (SELECT COUNT(1) FROM t_co_user WHERE user_id = :userId) + (SELECT COUNT(1) FROM t_co_cust_user WHERE user_id = :userId)");
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter("userId", params.get("userId"));
        BigInteger cnt = (BigInteger) query.getSingleResult();
        if (cnt.longValue() > 0) {
            resultBody.setCode("DUP"); // 아이디중복됨
        }
        return resultBody;
    }
}
