package iljin.framework.core.security.user;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.commons.collections.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.qlrm.mapper.JpaResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.AuthToken;
import iljin.framework.ebid.custom.entity.TCoCustUser;
import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.GeneralDao;
import iljin.framework.ebid.etc.util.common.consts.DB;
import iljin.framework.ebid.etc.util.common.mail.service.MailService;
import iljin.framework.ebid.etc.util.common.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl {
//    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
//    @Autowired
//    private CustomUserDetailsService customUserDetailsService;
//    
//    @Autowired
//    public GeneralDao generalDao;
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    private final AuthenticationManager authenticationManager;
//    private final MailService mailService;
//    private final PasswordEncoder passwordEncoder;
//    private final MessageService messageService;
//    private final TCoCustUserRepository tCoUserCustRepository;
//    private final OracleUserService oracleUserService;
//    private final String profile = System.getProperty("spring.profiles.active");
//
//    @Override
//    public ResponseEntity<AuthToken> login(UserDto userDto, HttpSession session, HttpServletRequest request) {
//        try {
//            String loginId = userDto.loginId;
//            String loginPw = userDto.loginPw;
//            String userAuth = userDto.userAuth;
//            String loginToken = userDto.token;
//            
//            Optional<UserDto> user = Optional.of(userDto);
//
//                // 최초 로그인시 비밀번호 변경 처리
//                if ("tempChange".equals(userAuth)) {
//                    Optional<TCoCustUser> userOptional = tCoUserCustRepository.findById(loginId);
//                    if (userOptional.isPresent()) {
//                        TCoCustUser tCoCustUser = userOptional.get();
//                        String encodedPassword = passwordEncoder.encode(loginPw);
//                        LocalDateTime currentDate = LocalDateTime.now();
//                        tCoCustUser.setUserPwd(encodedPassword);
//                        tCoCustUser.setPwdChgDate(currentDate);
//                        tCoUserCustRepository.save(tCoCustUser);
//                    }
//                }
//
//            //협력사 사용자인데 아이디와 비밀번호가 일치하지만 협력사가 승인되지 않은 경우
//            try {
//            	Map<String, Object> paramMap = new HashMap<String, Object>();
//            	paramMap.put("loginId", loginId);
//            	Optional<Object> certYnOptional = Optional.ofNullable(generalDao.selectGernalObject(DB.QRY_SELECT_LOGIN_USER_INFO, paramMap));
//                String certYn = null;
//                String userPwd = null;
//
//                if (certYnOptional.isPresent()) {//협력사인 경우
//                	Map<String, Object> map = (Map<String, Object>) certYnOptional.get();
//                	certYn = CommonUtils.getString(map.get("certYn"));
//                	userPwd = CommonUtils.getString(map.get("userPwd"));
//                    
//                    if(certYn != null && userPwd != null) {
//                    	// db 비밀번호
//                		String dbPassword = userPwd;
//
//                		// 비밀번호 체크
//                		boolean pwdCheck = ((BCryptPasswordEncoder) passwordEncoder).matches(loginPw, dbPassword);
//                		
//                		if(pwdCheck ) {//비밀번호 일치
//                            if(certYn.equals("N")) {//아이디 비밀번호는 일치하지만 아직 승인이 안된 협력사인 경우
//                            	//403에러 발생
//                            	return new ResponseEntity<>(new AuthToken(
//                                        null, null, null, null, null, null, null, false), HttpStatus.FORBIDDEN);
//                            }
//                		}
//                    }
//                    
//                }
//            }catch(NoResultException e) {//결과가 없는 경우로 잘못된 아이디 이거나 계열사인 경우
//            	
//            }catch(Exception e) {
//            	
//            }
//            
//            Optional<AuthToken> result =
//                    user.map(obj -> {
//                        // 1. username, password를 조합하여 UsernamePasswordAuthenticationToken 생성
//                        logger.info("1. username, password를 조합하여 UsernamePasswordAuthenticationToken 생성");
//                        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginId, loginPw);
//                        Authentication authentication = null;
//                        if(!StringUtils.isEmpty(loginToken)) {
//                            logger.info("Create Granted Authority Rules");
//                            // Create Granted Authority Rules
//                            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
//                            token = new UsernamePasswordAuthenticationToken(loginId, null, grantedAuthorities);
//                        } else {
//                            logger.info("2. 검증을 위해 UsernamePasswordAuthenticationToken 을 authenticationManager 의 인스턴스로 전달");
//                            // form login
//                            // 2. 검증을 위해 UsernamePasswordAuthenticationToken 을 authenticationManager 의 인스턴스로 전달
//                            authentication = authenticationManager.authenticate(token);// 3. 인증에 성공하면 Authentication 인스턴스 리턴
//                        }
//                        
//                        try {
//							return getAuthToken(session, loginId, obj, token, authentication, false);
//						} catch (Exception e) {
//							return null;
//						}
//                    });
//            
//             return result.map(authToken -> new ResponseEntity<>(authToken, HttpStatus.OK))
//                    .orElseGet(() -> new ResponseEntity<>(new AuthToken(
//                            null
//                            , null
//                            , null
//                            , null
//                            , null
//                            ,null
//                            , null
//                            , false), HttpStatus.UNAUTHORIZED));
//            
//        } catch (AuthenticationException e) {
//
//            return new ResponseEntity<>(new AuthToken(
//                    null
//                    , null
//                    , null
//                    , null
//                    , null
//                    , null
//                    , null
//                    , false), HttpStatus.UNAUTHORIZED);
//        }
//    }
//
//    @NotNull
//    private AuthToken getAuthTokenForSso(final HttpSession session, final String loginId, final UserDto obj, final UsernamePasswordAuthenticationToken token, final Authentication authentication, boolean sso) {
//        // 4. Authentication 인스턴스를 SecurityContextHolder의 SecurityContext에 설정
//        SecurityContextHolder.getContext().setAuthentication(token);
//        log.info("tokentokentokentokentokentokentokentokentoken={}", token);
//        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
//
//        StringBuilder sb = new StringBuilder(" SELECT 'inter' AS cust_type\n" +
//                "     , a.interrelated_cust_code AS cust_code \n" +
//                "     , interrelated_nm AS cust_name\n" +
//                "     , user_name \n" +
//                "     , user_id \n" +
//                "     , user_pwd \n" +
//                "     , user_auth\n" +
//                "     , 'token' AS token \n" +
//                "  FROM t_co_user a\n" +
//                "     , t_co_interrelated b\n" +
//                " WHERE a.interrelated_cust_code = b.interrelated_cust_code\n" +
//                "   AND user_id = :loginId\n" +
//                "   AND a.use_yn  = 'Y'\n" +
//                "   AND b.use_yn  = 'Y'");
//        Query query = entityManager.createNativeQuery(sb.toString());
//        query.setParameter("loginId", loginId);
//        UserDto data = new JpaResultMapper().uniqueResult(query, UserDto.class);
//
//        return new AuthToken(data.getCustType(),
//                data.getCustCode(),
//                data.getCustName(),
//                data.getLoginId(),
//                data.getUserName(),
//                data.getUserAuth(),
//                "token",
//                sso);
//    }
//
//    @NotNull
//    private AuthToken getAuthToken(final HttpSession session, final String loginId, final UserDto obj, final UsernamePasswordAuthenticationToken token, final Authentication authentication, boolean sso) throws Exception {
//        // 4. Authentication 인스턴스를 SecurityContextHolder의 SecurityContext에 설정
//        SecurityContextHolder.getContext().setAuthentication(token);
//        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
//    	Map<String, Object> paramMap = new HashMap<String, Object>();
//        paramMap.put("loginId", loginId);
//        UserDto data = (UserDto) generalDao.selectGernalObject(DB.QRY_SELECT_LOGIN_USER_TOKEN_INFO, paramMap);
//        
//        return new AuthToken(data.getCustType(),
//                data.getCustCode(),
//                data.getCustName(),
//                data.getLoginId(),
//                data.getUserName(),
//                data.getUserAuth(),
//                "token",
//                sso);
//    }
//
//    @Override
//    public ResponseEntity<AuthToken> ssoLogin(UserDto userDto, HttpSession session, HttpServletRequest request) {
//        try {
//            String loginId = userDto.loginId;
//            String loginPw = userDto.loginPw;
//            String loginToken = userDto.token;
//
//            Optional<UserDto> user = Optional.of(userDto);
//
//            Optional<AuthToken> result =
//                    user.map(obj -> {
//                        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(loginId);
//                        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, null,  AuthorityUtils.createAuthorityList("ADMIN"));
//                        return getAuthTokenForSso(session, loginId, obj, token, null, true);
//                    });
//
//            return result.map(authToken -> new ResponseEntity<>(authToken, HttpStatus.OK))
//                    .orElseGet(() -> new ResponseEntity<>(new AuthToken(
//                            null
//                            , null
//                            , null
//                            , null
//                            , null
//                            ,null
//                            , null
//                            , false), HttpStatus.UNAUTHORIZED));
//        } catch (AuthenticationException e) {
//
//            return new ResponseEntity<>(new AuthToken(
//                    null
//                    , null
//                    , null
//                    , null
//                    , null
//                    , null
//                    , null
//                    , false), HttpStatus.UNAUTHORIZED);
//        } catch(NoResultException e) {//결과가 없는 경우 > 계열사인데 전자입찰에는 계정이 생성안된 경우
//        	return new ResponseEntity<>(new AuthToken(
//                    null
//                    , null
//                    , null
//                    , null
//                    , null
//                    , null
//                    , null
//                    , false), HttpStatus.UNAUTHORIZED);
//        }
//    }
//
//    @Override
//    public void logout(HttpSession session) {
//        session.invalidate();
////        session.removeAttribute(Env.DEFAULT_SESSION_USERID);
//    }
//
//    public ResultBody idSearch(Map<String, String> params) {
//        ResultBody resultBody = new ResultBody();
//        try {
//	        Map<String, Object> paramMap = new HashMap<String, Object> ();
//	        paramMap.put("regnum", CommonUtils.getString(params.get("regnum1"))+CommonUtils.getString(params.get("regnum2"))+CommonUtils.getString(params.get("regnum3")));
//	        paramMap.put("userName", params.get("userName"));
//	        paramMap.put("userEmail", params.get("userEmail"));
//        	Map<String, Object> userMap = (Map<String, Object>) generalDao.selectGernalObject(DB.QRY_SELECT_LOGIN_USER_SEARCH, paramMap);
//        	if(!MapUtils.isEmpty(userMap)) {
//        		mailService.saveMailInfo("[일진그룹 e-bidding] 로그인 아이디", "고객님께서 찾으시는 e-bidding 시스템 로그인 아이디는\n" +
//                      "<b style='color:red'>" + CommonUtils.getString(userMap.get("userId")) + "</b>\n" +
//                      "입니다.\n" +
//                      "\n" +
//                      "감사합니다.\n", (String) params.get("userEmail"));
//        		
//        		messageService.send("일진그룹", CommonUtils.getString(userMap.get("userHp")) , CommonUtils.getString(userMap.get("userName")) ,
//        				"[일진그룹 전자입찰시스템] 찾고자 하는 아이디는 " + CommonUtils.getString(userMap.get("userId")) + " 입니다.");
//        	} else {
//        		resultBody.setCode("notFound");
//        	}
//        } catch(Exception e) {
//        	log.error("idSearch send message error : {}", e);
//        }
//        return resultBody;
//    }
//    
//	@Transactional
//	public ResultBody pwSearch(Map<String, String> params) {
//		ResultBody resultBody = new ResultBody();
//		try {
//			String userPwd = UUID.randomUUID().toString().substring(0, 6);
//			Map<String, Object> paramMap = new HashMap<String, Object>();
//			paramMap.put("regnum", CommonUtils.getString(params.get("regnum1"))+CommonUtils.getString(params.get("regnum2"))+CommonUtils.getString(params.get("regnum3")));
//			paramMap.put("userId", params.get("userId"));
//			paramMap.put("userPwd", passwordEncoder.encode(userPwd));
//			paramMap.put("userName", params.get("userName"));
//			paramMap.put("userEmail", params.get("userEmail"));
//			
//			int cnt = generalDao.updateGernal(DB.QRY_UPDATE_LOGIN_USER_SEARCH_PWD, paramMap);
//			if (cnt > 0) {
//				// 로그인 암호 메일 저장 처리
//				mailService.saveMailInfo("[일진그룹 e-bidding] 로그인 암호", "e-bidding 시스템에 로그인 하기 위해 초기화된 비밀번호는\n" +
//						"<b style='color:red'>" + userPwd + "</b>\n" +
//						"입니다.\n" +
//						"\n" +
//						"감사합니다.\n", CommonUtils.getString(params.get("userEmail")));
//				
//				String userHp = CommonUtils.getString(generalDao.selectGernalObject(DB.QRY_SELECT_LOGIN_USER_SEARCH_NEW_PWD, paramMap));
//				if (!"".equals(userHp)) {
//					messageService.send("일진그룹", userHp, params.get("userName"), "[일진그룹 전자입찰시스템] 초기화 된 비밀번호는 " + userPwd + " 입니다.");
//				}
//				
//			} else {
//				resultBody.setCode("notFound");
//			}
//		} catch(Exception e) {
//			log.error("pwSearch send message error : {}", e);
//		}
//		return resultBody;
//	}
//
//    @Override
//    @Transactional
//    public Map custSave(Map<String, String> params) {
//        Map result = new HashMap();
//        result.put("code", "ok");
//        return result;
//    }
//
//    public UserDto findUser(String loginId) {
//        StringBuilder sb = new StringBuilder(" SELECT 'inter' AS cust_type\n" +
//                "     , interrelated_cust_code AS cust_code \n" +
//                "     , (SELECT interrelated_nm FROM t_co_interrelated x WHERE x.interrelated_cust_code = a.interrelated_cust_code) AS cust_name\n" +
//                "     , user_name \n" +
//                "     , user_id AS loginId\n" +
//                "     , user_pwd AS loginPw\n" +
//                "     , user_auth\n" +
//                "     , 'token' AS token \n" +
//                "  FROM t_co_user a\n" +
//                " WHERE user_id = :loginId");
//        Query query = entityManager.createNativeQuery(sb.toString());
//        query.setParameter("loginId", loginId);
//        UserDto data = new JpaResultMapper().uniqueResult(query, UserDto.class);
//        return data;
//    }
//    
//    //비밀번호 확인
//    public boolean checkPassword(String userId, String password) {
//        try {
//            // 사용자명과 비밀번호를 사용하여 UsernamePasswordAuthenticationToken 생성
//            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userId, password);
//            
//            // AuthenticationManager를 사용하여 인증 시도
//            Authentication authentication = authenticationManager.authenticate(token);
//
//            // 인증이 성공하면 SecurityContextHolder에 인증 정보 설정
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            // 사용자가 인증되었는지 확인
//            return authentication.isAuthenticated();
//        } catch (AuthenticationException e) {
//            // 인증에 실패한 경우
//            return false;
//        }
//    }
}
