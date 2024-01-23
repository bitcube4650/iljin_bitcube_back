package iljin.framework.core.security.user;

import iljin.framework.core.security.AuthToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDto> getUsers();
    Optional<User> getUser(Long id);
    Optional<User> getUserByLoginId(String loginId);
    List<User> getSearchUser(String loginId);
    @Modifying
    @Transactional
    ResponseEntity<Object> addUser(UserDto dto);
    @Modifying
    @Transactional
    ResponseEntity<String> deleteUser(String loginId);
    @Modifying
    @Transactional
    ResponseEntity<User> updateUser(Long id, UserDto dto);
    ResponseEntity<AuthToken> login(UserDto userDto, HttpSession session, HttpServletRequest request);
    ResponseEntity<AuthToken> ssoLogin(UserDto userDto, HttpSession session, HttpServletRequest request);
    void logout(HttpSession session);
    void logoutMobile(HttpSession session);
}
