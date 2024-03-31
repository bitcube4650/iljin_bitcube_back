package iljin.framework.core.security.user;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        CustomUserDetails user = findUser(username);
        if(user == null) {
            throw new UsernameNotFoundException(username);
        }
        return user;
    }
    public CustomUserDetails findUser(String username) {
        StringBuilder sb = new StringBuilder(" SELECT 'inter' AS cust_type\n" +
                "     , a.interrelated_cust_code AS cust_code \n" +
                "     , interrelated_nm AS cust_name\n" +
                "     , user_name \n" +
                "     , user_id \n" +
                "     , user_pwd \n" +
                "     , user_auth\n" +
                "  FROM t_co_user a\n" +
                "     , t_co_interrelated b\n" +
                " WHERE a.interrelated_cust_code = b.interrelated_cust_code\n" +
                "   AND user_id = :username\n" +
                "   AND a.use_yn  = 'Y'\n" +
                "   AND b.use_yn  = 'Y'\n" +
                " UNION ALL\n" +
                "SELECT 'cust' AS cust_type\n" +
                "     , a.cust_code \n" +
                "     , cust_name \n" +
                "     , user_name \n" +
                "     , user_id \n" +
                "     , user_pwd \n" +
                "     , user_type \n" +
                "  FROM t_co_cust_user   a\n" +
                "     , t_co_cust_master b\n" +
                " WHERE a.cust_code = b.cust_code\n" +
                "   AND user_id = :username\n" +
                "   AND a.use_yn  = 'Y'\n" +
                "   AND b.cert_yn = 'Y' ");
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter("username", username);
        return new JpaResultMapper().uniqueResult(query, CustomUserDetails.class);
    }

}