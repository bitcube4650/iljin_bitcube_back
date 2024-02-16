package iljin.framework.core.security.user;

import iljin.framework.core.security.AuthToken;
import iljin.framework.core.security.loginHistory.LoginHistory;
import iljin.framework.core.security.loginHistory.LoginHistoryRepository;
import iljin.framework.core.security.role.Role;
import iljin.framework.core.security.role.RoleRepository;
import iljin.framework.core.security.role.UserRole;
import iljin.framework.core.security.role.UserRoleRepository;
import iljin.framework.core.util.Util;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserRepositoryCustom userRepositoryCustom;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final Util util;

    @Value("${address.frontend}")
    private String frontAddress;
    @Value("${address.backend}")
    private String backAddress;

    @Override
    public List<UserDto> getUsers() {
        List<User> users = userRepository.findAll();
        return null;
    }

    @Override
    public Optional<User> getUser(Long id) {
//        return userRepository.findById(id);
        return null;
    }

    @Override
    public List<User> getSearchUser(String loginId) {
        return userRepository.findAllByLoginIdContains(loginId);
    }

    @Override
    public ResponseEntity<Object> addUser(UserDto dto) {
        try {
            dto.setLoginPw(passwordEncoder.encode(dto.getLoginPw()));
            User newUser = new User();
            newUser.loginId = dto.loginId;
            newUser.loginPw = dto.loginPw;
            userRepository.save(newUser);


            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (DataIntegrityViolationException ex) {
            throw new UserCreateException();
        }
    }

    @Override
    public ResponseEntity<String> deleteUser(String loginId) {
        userRepository.deleteByLoginId(loginId);
        return new ResponseEntity<>("사용자가 삭제되었습니다", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<User> updateUser(String id, UserDto dto) {
        Optional<User> userData = userRepository.findByLoginId(id);

        if (userData.isPresent()) {
            User modifiedUser = userData.get();

            // 비밀번호가 가려진 거 아닌 경우에만 변경.
            if (!dto.loginPw.equals("******")) {
                modifiedUser.loginPw = passwordEncoder.encode(dto.getLoginPw());
            }

            modifiedUser.userName = dto.userName;

            List<UserRole> modifiedRoles = new ArrayList<>();

            // TODO 복수 권한 부여는 차후 필요시 구현 예정
            Optional<UserRole> modifiedRole = userRoleRepository.findRoleByUser_LoginId(dto.loginId);
            if (!modifiedRole.isPresent()) {
                modifiedRole = Optional.of(new UserRole());
            }
            modifiedRole.ifPresent(c -> {
//                c.setRole(RoleType.valueOf(dto.role));
                c.setRole(dto.role);
                userRoleRepository.save(c);
                modifiedRoles.add(c);
            });
            userRepository.save(modifiedUser);
            return new ResponseEntity<>(userRepository.save(modifiedUser), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Optional<User> getUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId);
    }

    @Override
    public ResponseEntity<AuthToken> login(UserDto userDto, HttpSession session, HttpServletRequest request) {

        try {
            String loginId = userDto.loginId;
            String loginPw = userDto.loginPw;
            String loginToken = userDto.token;

            Optional<User> user = userRepository.findByLoginId(loginId);

            Optional<AuthToken> result =
                    user.map(obj -> {
                        // 1. username, password를 조합하여 UsernamePasswordAuthenticationToken 생성
                        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginId, loginPw);
                        Authentication authentication = null;
                        if(!StringUtils.isEmpty(loginToken)) {
                            // Create Granted Authority Rules
                            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
                            token = new UsernamePasswordAuthenticationToken(loginId, null, grantedAuthorities);
                        } else {
                            // form login
                            // 2. 검증을 위해 UsernamePasswordAuthenticationToken 을 authenticationManager 의 인스턴스로 전달
                            authentication = authenticationManager.authenticate(token);// 3. 인증에 성공하면 Authentication 인스턴스 리턴
                            //logger.debug("*Authentication: " + String.valueOf(authentication.isAuthenticated()));
                        }

                        /*
                         * added on 26.08.2019
                         * Login history
                         * */
                        String clientIp = this.getClientIp(request);

                        /* login success */
                        LoginHistory loginHistory = new LoginHistory();
                        loginHistory.setConnectId(loginId);
                        loginHistory.setConnectIp(clientIp);
                        loginHistory.setConnectMthd(userDto.getConnectMthd());
                        loginHistory.setConnectError("");
                        loginHistory.setConnectUrl(request.getRequestURI());
                        loginHistory.setCreationDate(LocalDateTime.now());

                        loginHistoryRepository.save(loginHistory);

                        if (!StringUtils.isEmpty(userDto.getAttribute1())) {
                            /* from mobile */
                            userRepository.findByLoginId(loginId).ifPresent(c -> {

                                // save Mobile token (String value)
                                userRepository.save(c);
                            });
                        }
                        return getAuthToken(session, loginId, obj, token, authentication);
                    });

            return result.map(authToken -> new ResponseEntity<>(authToken, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(new AuthToken(
                            null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            ,null
                            , null), HttpStatus.UNAUTHORIZED));
        } catch (AuthenticationException e) {
            LoginHistory loginHistory = new LoginHistory();
            loginHistory.setConnectId(userDto.loginId);
            loginHistory.setConnectIp(this.getClientIp(request));
            loginHistory.setConnectMthd(userDto.connectMthd);
            loginHistory.setConnectError(e.getMessage());
            loginHistory.setConnectUrl(request.getRequestURI());
            loginHistory.setCreationDate(LocalDateTime.now());

            loginHistoryRepository.save(loginHistory);

            return new ResponseEntity<>(new AuthToken(
                    null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null), HttpStatus.UNAUTHORIZED);
        }
    }

    @NotNull
    private AuthToken getAuthToken(final HttpSession session, final String loginId, final User obj, final UsernamePasswordAuthenticationToken token, final Authentication authentication) {
        // 4. Authentication 인스턴스를 SecurityContextHolder의 SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(token);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        Optional<UserDto> userDto = userRepositoryCustom.findByLoginId(loginId);

//        Collection authList = authentication.getAuthorities();
        List<Role> authList = roleRepository.findByLoginId(loginId);

        return new AuthToken(obj.userName,
                loginId,
                userDto.get().getLoginCompCd(),
                userDto.get().getLoginCompNm(),
                userDto.get().getLoginDeptCd(),
                userDto.get().getLoginDeptNm(),
                userDto.get().getLoginJobCd(),
                userDto.get().getLoginJobNm(),
                userDto.get().getLoginDutCd(),
                userDto.get().getLoginDutNm(),
                userDto.get().getAttribute2(),
                session.getId(),
                authList);
    }

    @Override
    public ResponseEntity<AuthToken> ssoLogin(UserDto userDto, HttpSession session, HttpServletRequest request) {
        try {
            String loginId = userDto.loginId;

            Optional<User> user = userRepository.findByLoginId(loginId);

            List<UserRole> roles = userRoleRepository.findRolesByUser_LoginId(loginId);
            List<String> r = roles.stream().map(x -> x.getRole()).collect(Collectors.toList());

            List<GrantedAuthority> grantedAuthorities = AuthorityUtils.createAuthorityList(r.get(0));

            Optional<AuthToken> result =
                    user.map(obj -> {
                        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginId, null, grantedAuthorities);

                        return getAuthToken(session, loginId, obj, token, token);
                    });


            return result.map(authToken -> new ResponseEntity<>(authToken, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(new AuthToken(
                            null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null
                            , null), HttpStatus.UNAUTHORIZED));
        } catch (AuthenticationException e) {
            LoginHistory loginHistory = new LoginHistory();
            loginHistory.setConnectId(userDto.loginId);
            loginHistory.setConnectIp(this.getClientIp(request));
            loginHistory.setConnectMthd(userDto.connectMthd);
            loginHistory.setConnectError(e.getMessage());
            loginHistory.setConnectUrl(request.getRequestURI());
            loginHistory.setCreationDate(LocalDateTime.now());

            loginHistoryRepository.save(loginHistory);

            return new ResponseEntity<>(new AuthToken(
                    null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null), HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @Override
    public void logoutMobile(HttpSession session) {
        User mobileUser = util.getLoginUser();
        userRepository.save(mobileUser);
        session.invalidate();
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-RealIP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
