package iljin.framework.core.security.user;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.AuthToken;
import iljin.framework.core.security.role.Role;
import iljin.framework.core.security.role.RoleRepository;
import iljin.framework.core.security.role.UserRole;
import iljin.framework.core.security.role.UserRoleRepository;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.custom.dto.TCoCustMasterDto;
import iljin.framework.ebid.etc.util.common.mail.service.MailService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.qlrm.mapper.JpaResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.math.BigInteger;
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

    @PersistenceContext
    private EntityManager entityManager;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<AuthToken> login(UserDto userDto, HttpSession session, HttpServletRequest request) {

        try {
            String loginId = userDto.loginId;
            String loginPw = userDto.loginPw;
            String loginToken = userDto.token;

            Optional<UserDto> user = Optional.of(userDto);

            Optional<AuthToken> result =
                    user.map(obj -> {
                        // 1. username, password를 조합하여 UsernamePasswordAuthenticationToken 생성
                        logger.info("1. username, password를 조합하여 UsernamePasswordAuthenticationToken 생성");
                        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginId, loginPw);
                        Authentication authentication = null;
                        if(!StringUtils.isEmpty(loginToken)) {
                            logger.info("Create Granted Authority Rules");
                            // Create Granted Authority Rules
                            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
                            token = new UsernamePasswordAuthenticationToken(loginId, null, grantedAuthorities);
                        } else {
                            logger.info("2. 검증을 위해 UsernamePasswordAuthenticationToken 을 authenticationManager 의 인스턴스로 전달");
                            // form login
                            // 2. 검증을 위해 UsernamePasswordAuthenticationToken 을 authenticationManager 의 인스턴스로 전달
                            authentication = authenticationManager.authenticate(token);// 3. 인증에 성공하면 Authentication 인스턴스 리턴
                            //logger.debug("*Authentication: " + String.valueOf(authentication.isAuthenticated()));
                        }

                        return getAuthToken(session, loginId, obj, token, authentication, false);
                    });

            return result.map(authToken -> new ResponseEntity<>(authToken, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(new AuthToken(
                            null
                            , null
                            , null
                            , null
                            , null
                            ,null
                            , null
                            , false), HttpStatus.UNAUTHORIZED));
        } catch (AuthenticationException e) {

            return new ResponseEntity<>(new AuthToken(
                    null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , false), HttpStatus.UNAUTHORIZED);
        }
    }

    @NotNull
    private AuthToken getAuthToken(final HttpSession session, final String loginId, final UserDto obj, final UsernamePasswordAuthenticationToken token, final Authentication authentication, boolean sso) {
        // 4. Authentication 인스턴스를 SecurityContextHolder의 SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(token);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        StringBuilder sb = new StringBuilder(" SELECT 'inter' AS cust_type\n" +
                "     , a.interrelated_cust_code AS cust_code \n" +
                "     , interrelated_nm AS cust_name\n" +
                "     , user_name \n" +
                "     , user_id \n" +
                "     , user_pwd \n" +
                "     , user_auth\n" +
                "     , 'token' AS token \n" +
                "  FROM t_co_user a\n" +
                "     , t_co_interrelated b\n" +
                " WHERE a.interrelated_cust_code = b.interrelated_cust_code\n" +
                "   AND user_id = :loginId\n" +
                "   AND a.use_yn  = 'Y'\n" +
                "   AND b.use_yn  = 'Y'\n" +
                " UNION ALL\n" +
                "SELECT 'cust' AS cust_type\n" +
                "     , a.cust_code \n" +
                "     , cust_name AS cust_name\n" +
                "     , user_name \n" +
                "     , user_id \n" +
                "     , user_pwd \n" +
                "     , user_type \n" +
                "     , 'token' AS token \n" +
                "  FROM t_co_cust_user   a\n" +
                "     , t_co_cust_master b\n" +
                " WHERE a.cust_code = b.cust_code\n" +
                "   AND user_id = :loginId\n" +
                "   AND a.use_yn  = 'Y'\n" +
                "   AND b.cert_yn = 'Y' ");
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter("loginId", obj.getLoginId());
        UserDto data = new JpaResultMapper().uniqueResult(query, UserDto.class);

        return new AuthToken(data.getCustType(),
                data.getCustCode(),
                data.getCustName(),
                data.getLoginId(),
                data.getUserName(),
                data.getUserAuth(),
                "token",
                sso);
    }

    @Override
    public ResponseEntity<AuthToken> ssoLogin(UserDto userDto, HttpSession session, HttpServletRequest request) {

        try {
            String loginId = userDto.loginId;
            String loginPw = userDto.loginPw;
            String loginToken = userDto.token;

            Optional<UserDto> user = Optional.of(userDto);

            Optional<AuthToken> result =
                    user.map(obj -> {
                        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
                        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginId, null, grantedAuthorities);
                        Authentication authentication = null;
                        return getAuthToken(session, loginId, obj, token, authentication, true);
                    });

            return result.map(authToken -> new ResponseEntity<>(authToken, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(new AuthToken(
                            null
                            , null
                            , null
                            , null
                            , null
                            ,null
                            , null
                            , false), HttpStatus.UNAUTHORIZED));
        } catch (AuthenticationException e) {

            return new ResponseEntity<>(new AuthToken(
                    null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , null
                    , false), HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public void logout(HttpSession session) {
        session.invalidate();
    }

    public ResultBody idSearch(Map<String, String> params) {
        ResultBody resultBody = new ResultBody();
        StringBuilder sbQuery = new StringBuilder(" SELECT user_id \n" +
                "  FROM t_co_cust_user   a\n" +
                "     , t_co_cust_master b\n" +
                " WHERE a.cust_code = b.cust_code\n" +
                "   AND b.regnum  = :regnum\n" +
                "   AND a.user_name = :userName\n" +
                "   AND a.user_email = :userEmail\n" +
                "   AND a.use_yn  = 'Y'\n" +
                "   AND b.cert_yn = 'Y'");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("regnum", params.get("regnum1").toString()+params.get("regnum2").toString()+params.get("regnum3").toString());
        query.setParameter("userName", params.get("userName"));
        query.setParameter("userEmail", params.get("userEmail"));
        Optional<String> userId = query.getResultList().stream().findFirst();

        if (userId.isPresent()) {
            // 로그인 아이디 메일 저장 처리
            mailService.saveMailInfo("[일진그룹 e-bidding] 로그인 아이디", "안녕하십니까\n" +
                    "일진그룹 전자입찰 e-bidding 입니다.\n" +
                    "\n" +
                    "고객님께서 찾으시는 e-bidding 시스템 로그인 아이디는\n" +
                    "<b style='color:red'>" + userId.get() + "</b>\n" +
                    "입니다.\n" +
                    "\n" +
                    "감사합니다.\n", (String) params.get("userEmail"));
        } else {
            resultBody.setCode("notFound");
        }
        return resultBody;
    }

    @Transactional
    public ResultBody pwSearch(Map<String, String> params) {
        ResultBody resultBody = new ResultBody();
        String userPwd = params.get("userId") + "!@#";
        StringBuilder sbQuery = new StringBuilder(" UPDATE t_co_cust_user a\n" +
                "   SET user_pwd = :userPwd\n" +
                "     , pwd_chg_date = null\n" +
                " WHERE user_id = :userId\n" +
                "   AND user_name = :userName\n" +
                "   AND user_email = :userEmail\n" +
                "   AND a.use_yn = 'Y'\n" +
                "   AND EXISTS (SELECT cust_code FROM t_co_cust_master x WHERE x.cust_code = a.cust_code AND x.regnum = :regnum AND x.cert_yn = 'Y')");
        Query query = entityManager.createNativeQuery(sbQuery.toString());
        query.setParameter("regnum", params.get("regnum1").toString()+params.get("regnum2").toString()+params.get("regnum3").toString());
        query.setParameter("userId", params.get("userId"));
        query.setParameter("userPwd", passwordEncoder.encode(userPwd));
        query.setParameter("userName", params.get("userName"));
        query.setParameter("userEmail", params.get("userEmail"));
        int cnt = query.executeUpdate();

        if (cnt > 0) {
            // 로그인 암호 메일 저장 처리
            mailService.saveMailInfo("[일진그룹 e-bidding] 로그인 암호", "안녕하십니까\n" +
                    "일진그룹 전자입찰 e-bidding 입니다.\n" +
                    "\n" +
                    "e-bidding 시스템에 로그인 하기 위해 초기화된 비밀번호는\n" +
                    "<b style='color:red'>" + userPwd + "</b>\n" +
                    "입니다.\n" +
                    "\n" +
                    "감사합니다.\n", (String) params.get("userEmail"));
        } else {
            resultBody.setCode("notFound");
        }
        return resultBody;
    }
    @Override
    @Transactional
    public Map custSave(Map<String, String> params) {
        Map result = new HashMap();
        result.put("code", "ok");
        return result;
    }

    public UserDto findUser(String loginId) {
        StringBuilder sb = new StringBuilder(" SELECT 'inter' AS cust_type\n" +
                "     , interrelated_cust_code AS cust_code \n" +
                "     , (SELECT interrelated_nm FROM t_co_interrelated x WHERE x.interrelated_cust_code = a.interrelated_cust_code) AS cust_name\n" +
                "     , user_name \n" +
                "     , user_id AS loginId\n" +
                "     , user_pwd AS loginPw\n" +
                "     , user_auth\n" +
                "     , 'token' AS token \n" +
                "  FROM t_co_user a\n" +
                " WHERE user_id = :loginId");
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter("loginId", loginId);
        UserDto data = new JpaResultMapper().uniqueResult(query, UserDto.class);
        return data;
    }
    
    //비밀번호 확인
    public boolean checkPassword(String userId, String password) {
        try {
            // 사용자명과 비밀번호를 사용하여 UsernamePasswordAuthenticationToken 생성
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userId, password);
            
            // AuthenticationManager를 사용하여 인증 시도
            Authentication authentication = authenticationManager.authenticate(token);

            // 인증이 성공하면 SecurityContextHolder에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 사용자가 인증되었는지 확인
            return authentication.isAuthenticated();
        } catch (AuthenticationException e) {
            // 인증에 실패한 경우
            return false;
        }
    }
}
