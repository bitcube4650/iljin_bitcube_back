package iljin.framework.core.security.sso;

import com.saerom.onepass.agent.OpAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@Service
public class Sso {

    private static final Logger logger = LoggerFactory.getLogger(Sso.class);

    private final HashMap configMap = new HashMap();

    @Value("${opagent.app.ptk.use}")
    String appPtkUse;
    @Value("${opagent.sso.server.context}")
    String ssoServerContext;
    @Value("${opagent.sso.server.login.url}")
    String ssoServerLoginUrl;
    @Value("${opagent.sso.server.logout.url}")
    String ssoServerLogoutUrl;
    @Value("${opagent.sso.server.ip}")
    String ssoServerIp;
    @Value("${opagent.sso.server.port}")
    String ssoServerPort;
    @Value("${opagent.cookieUse}")
    String cookieUse;
    @Value("${opagent.validate.request.timeout}")
    String validateRequestTimeout;
    @Value("${opagent.sso.client.login}")
    String ssoClientLogin;
    @Value("${opagent.login.view.emate.apps}")
    String loginViewEmateApps;
    @Value("${opagent.sso.server.url}")
    String ssoServerUrl;
    @Value("${opagent.apps.login.url}")
    String appsLoginUrl;
    @Value("${opagent.apps.logout.url}")
    String appsLogoutUrl;


    public Sso() {
        configMap.put("app.ptk.use", appPtkUse);
        configMap.put("sso.server.context", ssoServerContext);
        configMap.put("sso.server.login.url", ssoServerLoginUrl);
        configMap.put("sso.server.logout.url", ssoServerLogoutUrl);
        configMap.put("sso.server.ip", ssoServerIp);
        configMap.put("sso.server.port", ssoServerPort);
        configMap.put("cookieUse", cookieUse);
        configMap.put("validate.request.timeout", validateRequestTimeout);
        configMap.put("sso.client.login", ssoClientLogin); // 수정 - 로그인 및 로그아웃 페이지를 SSO 서버의 페이지를 보여줄지 아니면 Application 서버의 페이지를 보여줄 지 여부
        configMap.put("login.view.eMateApps", loginViewEmateApps); // 수정 - 해당 시스템에 부여된 SSO시스템구분키(eMateApps 파라미터값)
        configMap.put("sso.server.url", ssoServerUrl);
        configMap.put("apps.login.url", appsLoginUrl); // 수정 - 해당 시스템의 개발서버 로그인 페이지
        configMap.put("apps.logout.url","appsLogoutUrl"); // 수정 - 해당 시스템의 개발서버 로그아웃 페이지
    }


    public String ssoLogin(HttpServletRequest req, HttpServletResponse res) {
        OpAgent onePass = new OpAgent(req, res, configMap);

        try {
            int ssoResult = onePass.op_SsoValidate();

            switch(ssoResult) {

                case OpAgent.OP_ERR:
                    logger.error("SSO 오류 발생: " + OpAgent.OP_ERR);
                    return "-1";
                case OpAgent.OP_ERR_NO_AUTH_TOKEN:
                    logger.error("SSO 오류 발생: 인증 Token 이 없음 " + OpAgent.OP_ERR_NO_AUTH_TOKEN);
                    return "-1";
                case OpAgent.OP_ERR_INVALID_AUTH_TOKEN:
                    logger.error("SSO 오류 발생: 유효하지 않은 인증 " + OpAgent.OP_ERR_INVALID_AUTH_TOKEN);
                    return "-1";
                case OpAgent.OP_ERR_HTTP_COMM_ERR:
                    logger.error("SSO 오류 발생: HTTP 통신 에러 Token " + OpAgent.OP_ERR_HTTP_COMM_ERR);
                    return "-1";
                case OpAgent.OP_ERR_XML_PARSING_FAILED:
                    logger.error("SSO 오류 발생: 서버로 부터 전달받은 XML 파싱 오류 " + OpAgent.OP_ERR_XML_PARSING_FAILED);
                    return "-1";
                case OpAgent.OP_OK:
                    logger.info(
                            "SSO 성공: " +
                                    ", 로그인 ID : " + onePass.op_uId +
                                    ", SSO 비밀번호 : " + onePass.op_passwd +
                                    ", 사번 : " + onePass.op_empNumber +
                                    ", 부서코드 : " + onePass.op_groupId
                    );
                    return onePass.op_empNumber;
                default: return "-1";
            }
        } catch (Exception ex) {
            logger.error("SSO 오류 발생: 예기치 않은 오류 " + ex);
            return "-1";
        }
    }
}
