package iljin.framework.core.security;

import com.rathontech.sso.sp.config.Env;
import iljin.framework.core.security.sso.Sso;
import iljin.framework.core.security.user.UserDto;
import iljin.framework.core.security.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
@CrossOrigin
public class LoginController {

    private final
    UserService userService;

    private final Sso sso;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    public LoginController(UserService userService, Sso sso) {
        this.userService = userService;
        this.sso = sso;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthToken> login(@RequestBody UserDto userDto, HttpSession session, HttpServletRequest request) {
        return userService.login(userDto, session, request);
    }

    @PostMapping("/login/sso")
    public ResponseEntity<AuthToken> ssoLogin(@RequestBody UserDto userDto, HttpSession session, HttpServletRequest req) {
        String loginId = req.getSession().getAttribute(Env.DEFAULT_SESSION_USERID).toString();
        userDto.setLoginId(loginId);
        return userService.ssoLogin(userDto, session, req);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        userService.logout(session);
        return new ResponseEntity<> ("로그아웃 되었습니다.", HttpStatus.OK);
    }

    @GetMapping("/logout/mobile")
    public ResponseEntity<String> logoutMobile(HttpSession session){
        userService.logoutMobile(session);
        return new ResponseEntity<>("모바일 로그아웃 완료.",HttpStatus.OK);
    }

    /*@RequestMapping("/ssoLogin")
    public ModelAndView ssoLoginPage(HttpServletRequest request
            , HttpServletResponse response
            , RedirectAttributes redirectAttributes
            , @CookieValue(value="onepassid", required = false) Cookie onePassId) throws IOException {
        HttpSession session = request.getSession(true);
        String sessionUserId = (String) session.getAttribute("userId");
        logger.info("Controller - /ssoLogin. session User Id : " + sessionUserId);

        ModelAndView view  = new ModelAndView("index");
        if(sessionUserId != null) {
            view  = new ModelAndView("redirect:/login");
        } else {
            String wingsOnePassId = onePassId.getValue();

            String ssoFlag = "0";

            logger.info("wingsOnePassId : " + wingsOnePassId);
            if(sessionUserId == null && wingsOnePassId.length() > 0) {
                ssoFlag = "1";
                logger.info("ssoFlag is true.");
            }

            if(ssoFlag.equals("1")) {
                logger.info("sessionUserId : " + sessionUserId);
                logger.info("sso onePassId : " + onePassId.getValue());

                HashMap configMap = new HashMap();
                //Application 기본 정보 설정 - 수정 불필요
                configMap.put("app.ptk.use", "Y");
                configMap.put("sso.server.context", "");
                configMap.put("sso.server.login.url", "login.jsp");
                configMap.put("sso.server.logout.url", "logout.do");
                configMap.put("sso.server.ip", "197.200.11");
                configMap.put("sso.server.port", "");
                configMap.put("cookieUse", "Y");
                configMap.put("validate.request.timeout", "5000");
                configMap.put("sso.client.login", "N"); // 수정 - 로그인 및 로그아웃 페이지를 SSO 서버의 페이지를 보여줄지 아니면 Application 서버의 페이지를 보여줄 지 여부
                configMap.put("login.view.eMateApps", "APP_145"); // 수정 - 해당 시스템에 부여된 SSO시스템구분키(eMateApps 파라미터값)(일진전기 APP_126)
                configMap.put("sso.server.url", "http://wingsso1.iljin.co.kr/");
                configMap.put("apps.login.url", "http://197.200.1.81:9081/login"); // TODO: 수정 - 해당 시스템의 로그인 페이지
                configMap.put("apps.logout.url", "http://197.200.1.81:9081/logout"); // TODO: 수정 - 해당 시스템의 로그아웃 페이지

                *//*
                 * Onepass Policy Server 로 부터 사용자 인증정보를 검증한다.
                 *//*
                OpAgent onepass = new OpAgent(request, response, configMap);
                try {
                    int ssoResult = onepass.op_SsoValidate();
                    if (ssoResult == OpAgent.OP_ERR) {
                        String opErrUrl = onepass.getOpErrUrl();
                        logger.info("오류 발생<BR>");
                        response.sendRedirect(opErrUrl + "?message=OP_ERR");
                    } else if (ssoResult == OpAgent.OP_ERR_NO_AUTH_TOKEN) {
                        String loginURL = onepass.getLoginURL();
                        logger.info("인증 Token 이 없음<BR>");
                        response.sendRedirect(loginURL);
                    } else if (ssoResult == OpAgent.OP_ERR_INVALID_AUTH_TOKEN) {
                        logger.info("유효하지 않은 인증 Token<BR>");
                        String opErrUrl = onepass.getOpErrUrl();
                        response.sendRedirect(opErrUrl + "?message=OP_ERR_INVALID_AUTH_TOKEN");
                    } else if (ssoResult == OpAgent.OP_ERR_HTTP_COMM_ERR) {
                        logger.info("HTTP 통신 에러 Token<BR>");
                        String opErrUrl = onepass.getOpErrUrl();
                        response.sendRedirect(opErrUrl + "?message=OP_ERR_HTTP_COMM_ERR");
                    } else if (ssoResult == OpAgent.OP_ERR_XML_PARSING_FAILED) {
                        logger.info("서버로 부터 전달받은 XML 파싱 오류<BR>");
                        String logoutURL = onepass.getLogoutURL();
                        response.sendRedirect(logoutURL);
                    } else if (ssoResult == OpAgent.OP_OK) {
                        *//**************************************************************************************
                         ********************                 [ 코딩 필요 ]                ********************
                         ********************       사용자 인증 성공 시 생성되는 값        ********************
                         ********************    onepass.op_uId         : SSO 로그인 ID    ********************
                         ********************    onepass.op_passwd      : SSO 비밀번호     ********************
                         ********************    onepass.op_empNumber   : 사번             ********************
                         ********************    onepass.op_groupId     : 조직코드         ********************
                         ***************************************************************************************//*

//       out.println("사용자 인증 성공<BR>");
//       out.println("SSO 로그인 ID : " + onepass.op_uId + "<BR>");
//       out.println("SSO 비밀번호 : " + onepass.op_passwd + "<BR>");
//       out.println("사번 : " + onepass.op_empNumber + "<BR>");
//       out.println("부서코드 : " + onepass.op_groupId + "<BR>");

                        *//**
                         * ====== 이 부분에 자체 로그인 처리 작성 ======
                         **//*
                        logger.info("SSO Succeed!!!");
                        logger.info(onepass.op_empNumber);

                        view = new ModelAndView("comn/system/ssoLoginPage");
                        view.addObject("username", onepass.op_empNumber);
                        view.addObject("password", "ssoLoginPassword");
                    }
                } catch (Exception ex) {
                    *//*************************************************************************************
                     * 사용자 인증 시 오류가 발생했을 경우 Onepass Policy Server 의 에러 페이지로 이동한다.
                     *************************************************************************************//*
                    String opErrUrl = onepass.getOpErrUrl();
                    response.sendRedirect(opErrUrl + "?message=" + ex.toString());
                }
            }
        }

        return view;
    }*/

}
