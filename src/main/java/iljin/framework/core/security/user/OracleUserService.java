package iljin.framework.core.security.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Service
public class OracleUserService {

    @Value("${oracle.datasource.url}")
    private String url;
    @Value("${oracle.datasource.username}")
    private String username;
    @Value("${oracle.datasource.password}")
    private String password;
    @Value("${oracle.datasource.driver-class-name}")
    private String drivername;

    /**
     * 기존 오라클 DB에서 협력사 사용자 정보 조회
     *
     * @param userId
     * @param userPwd
     */
    public boolean check(String userId, String userPwd) {
        Map<String, String> persistenceMap = new HashMap();
        persistenceMap.put("javax.persistence.jdbc.url", url);
        persistenceMap.put("javax.persistence.jdbc.user", username);
        persistenceMap.put("javax.persistence.jdbc.password", password);
        persistenceMap.put("javax.persistence.jdbc.driver", drivername);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("oracle", persistenceMap);
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        boolean ret = false;
        try {
//            Query query = em.createNativeQuery("SELECT COUNT(1) FROM t_co_cust_user WHERE user_id = :userId AND user_pwd = :userPwd");
//            query.setParameter("userPwd", userPwd);
            Query query = em.createNativeQuery("SELECT COUNT(1) FROM t_co_cust_user WHERE user_id = :userId AND user_pwd IS NULL");
            query.setParameter("userId", userId);
            BigInteger count = (BigInteger) query.getSingleResult();
            if (count.intValue() > 0) ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        tx.commit();
        em.close();
        emf.close();
        return ret;
    }
}
