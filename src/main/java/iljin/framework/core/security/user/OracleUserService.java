package iljin.framework.core.security.user;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class OracleUserService {

    @Value("${oracle.datasource.url}")
    private String url;
    @Value("${oracle.datasource.username}")
    private String username;
    @Value("${oracle.datasource.password}")
    private String password;
    @Value("${oracle.datasource.driver-class-name}")
    private String drivername;
    @Value("${asis-url}")
    private String asisUrl;

    /**
     * 기존 오라클 DB에서 협력사 사용자 정보 조회
     *
     * @param userId
     * @param userPwd
     */
    public boolean check(String userId, String userPwd) {
//        Map<String, String> persistenceMap = new HashMap();
//        persistenceMap.put("javax.persistence.jdbc.url", url);
//        persistenceMap.put("javax.persistence.jdbc.user", username);
//        persistenceMap.put("javax.persistence.jdbc.password", password);
//        persistenceMap.put("javax.persistence.jdbc.driver", drivername);
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory("oracle", persistenceMap);
//        EntityManager em = emf.createEntityManager();
//        EntityTransaction tx = em.getTransaction();
//        tx.begin();
//        boolean ret = false;
//        try {
////            Query query = em.createNativeQuery("SELECT COUNT(1) FROM t_co_cust_user WHERE user_id = :userId AND user_pwd = :userPwd");
////            query.setParameter("userPwd", userPwd);
//            Query query = em.createNativeQuery("SELECT COUNT(1) FROM t_co_cust_user WHERE user_id = :userId AND user_pwd IS NULL");
//            query.setParameter("userId", userId);
//            BigInteger count = (BigInteger) query.getSingleResult();
//            if (count.intValue() > 0) ret = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        tx.commit();
//        em.close();
//        emf.close();
//        return ret;
        boolean isOK = false;
        try {
            HttpResponse<String> response = Unirest.post(asisUrl)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .field("id", userId)
                    .field("pwd", userPwd).asString();
            String body = response.getBody();
            log.info("=========> body:" + body);
            isOK = body.startsWith("<head><title>");
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
        return isOK;
    }
}
