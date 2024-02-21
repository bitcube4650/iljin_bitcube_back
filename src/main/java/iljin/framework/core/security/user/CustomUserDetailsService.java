package iljin.framework.core.security.user;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        CustomUserDetails user = new CustomUserDetails();
        user.setName(username);
        if(username == null) {
            throw new UsernameNotFoundException(username);
        }
        System.out.println(username+"CustomUserDetailsService 들어왔다!!!!!!!!!!!!!!!!");
        return user;
    }

}