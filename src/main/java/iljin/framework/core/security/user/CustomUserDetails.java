package iljin.framework.core.security.user;
import java.util.ArrayList;
import java.util.Collection;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
@Data
public class CustomUserDetails implements UserDetails {

    private String username;
    private String custType;
    private String custCode;
    private String custName;
    private String password;
    private String userNm;
    private String userAuth;

    /* UserServiceImpl.getAuthToken */
    public CustomUserDetails(String custType, String custCode, String custName, String userNm, String loginId, String loginPw, String userAuth) {
        this.custType = custType;
        this.custCode = custCode;
        this.custName = custName;
        this.userNm = userNm;
        this.username = loginId;
        this.password = loginPw;
        this.userAuth = userAuth;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<GrantedAuthority> auth = new ArrayList<GrantedAuthority>();
        auth.add(new SimpleGrantedAuthority("ADMIN"));
        return auth;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}