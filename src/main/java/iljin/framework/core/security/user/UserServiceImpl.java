package iljin.framework.core.security.user;

import com.rathontech.sso.sp.agent.web.WebAgent;
import com.rathontech.sso.sp.config.Env;
import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.AuthToken;
import iljin.framework.core.security.role.Role;
import iljin.framework.core.security.role.RoleRepository;
import iljin.framework.core.security.role.UserRole;
import iljin.framework.core.security.role.UserRoleRepository;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.custom.dto.TCoCustMasterDto;
import iljin.framework.ebid.custom.entity.TCoCustUser;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
import iljin.framework.ebid.etc.util.common.mail.service.MailService;
import iljin.framework.ebid.etc.util.common.message.MessageService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @PersistenceContext
    private EntityManager entityManager;

    private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final MessageService messageService;
    private final TCoCustUserRepository tCoUserCustRepository;
    private final OracleUserService oracleUserService;
    private final String profile = System.getProperty("spring.profiles.active");

    @Override
    public ResponseEntity<AuthToken> login(UserDto userDto, HttpSession session, HttpServletRequest request) {

        try {
            String loginId = userDto.loginId;
            String loginPw = userDto.loginPw;
            String userAuth = userDto.userAuth;
            String loginToken = userDto.token;

            Optional<UserDto> user = Optional.of(userDto);

            // 실서버만 적용 2024-12-31까지만
//            if ("production".equals(profile)) {
//            if ("dev".equals(profile)) {
                // 최초 로그인시 비밀번호 변경 처리
                if ("tempChange".equals(userAuth)) {
                    Optional<TCoCustUser> userOptional = tCoUserCustRepository.findById(loginId);
                    if (userOptional.isPresent()) {
                        TCoCustUser tCoCustUser = userOptional.get();
                        String encodedPassword = passwordEncoder.encode(loginPw);
                        LocalDateTime currentDate = LocalDateTime.now();
                        tCoCustUser.setUserPwd(encodedPassword);
                        tCoCustUser.setPwdChgDate(currentDate);
                        tCoUserCustRepository.save(tCoCustUser);
                    }
                } else {
                    // 협력사 사용자만 해당
                    StringBuilder sbQuery = new StringBuilder(" SELECT user_id \n" +
                            "  FROM t_co_cust_user   a\n" +
                            "     , t_co_cust_master b\n" +
                            " WHERE a.cust_code = b.cust_code\n" +
                            "   AND user_id = :loginId\n" +
                            "   AND user_pwd IS NULL\n" +
                            "   AND a.use_yn  = 'Y'\n" +
                            "   AND b.cert_yn = 'Y'");

                    Query query = entityManager.createNativeQuery(sbQuery.toString());
                    query.setParameter("loginId", loginId);
                    Optional<String> userId = query.getResultList().stream().findFirst();

                    if (userId.isPresent()) {
                        // 오라클 DB에서 사용자 정보 체크 맞으면
                        if (oracleUserService.check(loginId, loginPw)) {
                            return new ResponseEntity<>(new AuthToken(
                                    null
                                    , null
                                    , null
                                    , null
                                    , null
                                    , null
                                    , null
                                    , false), HttpStatus.LOCKED);
                        }
                    }
                }
//            }

            //협력사 사용자인데 아이디와 비밀번호가 일치하지만 협력사가 승인되지 않은 경우
            StringBuilder sbQuery2 = new StringBuilder(
								            		   " SELECT b.cert_yn ,"
								            		        + " a.user_pwd "
								            		 + " FROM t_co_cust_user a "
								            		 + "    , t_co_cust_master b "
								            		 + " WHERE a.cust_code = b.cust_code "
								            		 + " AND a.user_id = :loginId "
								            		  );    
            Query query2 = entityManager.createNativeQuery(sbQuery2.toString());
            query2.setParameter("loginId", loginId);
            try {
            	Optional<Object> certYnOptional = Optional.ofNullable(query2.getSingleResult());
                String certYn = null;
                String userPwd = null;

                if (certYnOptional.isPresent()) {//협력사인 경우
                	Object row = certYnOptional.get();
                    if (row instanceof Object[]) {
                        Object[] values = (Object[]) row;
                        if (values.length > 0) {
                            certYn = (String) values[0]; // 첫 번째 열은 'cert_yn' 값
                        }
                        if (values.length > 1) {
                            userPwd = (String) values[1]; // 두 번째 열은 'user_pwd' 값
                        }
                    }
                    if(certYn != null && userPwd != null) {
                    	// db 비밀번호
                		String dbPassword = userPwd;

                		// 비밀번호 체크
                		boolean pwdCheck = ((BCryptPasswordEncoder) passwordEncoder).matches(loginPw, dbPassword);
                		
                		if(pwdCheck ) {//비밀번호 일치
                            if(certYn.equals("N")) {//아이디 비밀번호는 일치하지만 아직 승인이 안된 협력사인 경우
                            	//403에러 발생
                            	return new ResponseEntity<>(new AuthToken(
                                        null, null, null, null, null, null, null, false), HttpStatus.FORBIDDEN);
                            }
                		}
                    }
                    
                }
            }catch(NoResultException e) {//결과가 없는 경우로 잘못된 아이디 이거나 계열사인 경우
            	
            }
            
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
    private AuthToken getAuthTokenForSso(final HttpSession session, final String loginId, final UserDto obj, final UsernamePasswordAuthenticationToken token, final Authentication authentication, boolean sso) {
        // 4. Authentication 인스턴스를 SecurityContextHolder의 SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(token);
        log.info("tokentokentokentokentokentokentokentokentoken={}", token);
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
                "   AND b.use_yn  = 'Y'");
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter("loginId", loginId);
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
        query.setParameter("loginId", loginId);
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
                        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(loginId);
                        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, null,  AuthorityUtils.createAuthorityList("ADMIN"));
                        return getAuthTokenForSso(session, loginId, obj, token, null, true);
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
//        session.removeAttribute(Env.DEFAULT_SESSION_USERID);
    }

    public ResultBody idSearch(Map<String, String> params) {
        ResultBody resultBody = new ResultBody();
        StringBuilder sbQuery = new StringBuilder(" SELECT user_id, user_hp, user_name \n" +
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
        List<UserDto> list = new JpaResultMapper().list(query, UserDto.class);

        if (!list.isEmpty()) {
            UserDto user = list.get(0);
            // 로그인 아이디 메일 저장 처리
            mailService.saveMailInfo("[일진그룹 e-bidding] 로그인 아이디", "고객님께서 찾으시는 e-bidding 시스템 로그인 아이디는\n" +
                    "<b style='color:red'>" + user.getLoginId() + "</b>\n" +
                    "입니다.\n" +
                    "\n" +
                    "감사합니다.\n", (String) params.get("userEmail"));

            try {
            	 messageService.send("일진그룹", user.getUserHp(), user.getUserName(), "[일진그룹 전자입찰시스템] 찾고자 하는 아이디는 " + user.getLoginId() + " 입니다.");
            }catch(Exception e) {
				log.error("idSearch send message error : {}", e);
			}
           
            
        } else {
            resultBody.setCode("notFound");
        }
        return resultBody;
    }

    @Transactional
    public ResultBody pwSearch(Map<String, String> params) {
        ResultBody resultBody = new ResultBody();
        String userPwd = UUID.randomUUID().toString().substring(0, 6);
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
            mailService.saveMailInfo("[일진그룹 e-bidding] 로그인 암호", "e-bidding 시스템에 로그인 하기 위해 초기화된 비밀번호는\n" +
                    "<b style='color:red'>" + userPwd + "</b>\n" +
                    "입니다.\n" +
                    "\n" +
                    "감사합니다.\n", (String) params.get("userEmail"));

            sbQuery = new StringBuilder(" SELECT user_hp \n" +
                    "  FROM t_co_cust_user   a\n" +
                    "     , t_co_cust_master b\n" +
                    " WHERE a.cust_code = b.cust_code\n" +
                    "   AND b.regnum  = :regnum\n" +
                    "   AND a.user_name = :userName\n" +
                    "   AND a.user_email = :userEmail\n" +
                    "   AND a.use_yn  = 'Y'\n" +
                    "   AND b.cert_yn = 'Y'");

            query = entityManager.createNativeQuery(sbQuery.toString());
            query.setParameter("regnum", params.get("regnum1").toString()+params.get("regnum2").toString()+params.get("regnum3").toString());
            query.setParameter("userName", params.get("userName"));
            query.setParameter("userEmail", params.get("userEmail"));
            Optional<String> userHp = query.getResultList().stream().findFirst();

            if (userHp.isPresent()) {
            	
                try {
                	 messageService.send("일진그룹", userHp.get(), params.get("userName"), "[일진그룹 전자입찰시스템] 초기화 된 비밀번호는 " + userPwd + " 입니다.");
               }catch(Exception e) {
   					log.error("pwSearch send message error : {}", e);
   				}
  
            }
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
