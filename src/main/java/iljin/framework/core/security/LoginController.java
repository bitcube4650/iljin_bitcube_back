package iljin.framework.core.security;

import com.rathontech.sso.sp.config.Env;
import iljin.framework.core.security.sso.Sso;
import iljin.framework.core.security.user.UserDto;
import iljin.framework.core.security.user.UserService;
import iljin.framework.ebid.custom.repository.TCoInterrelatedRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class LoginController {

    private final UserService userService;

    private final Sso sso;
    private final TCoInterrelatedRepository tCoInterrelatedRepository;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    public LoginController(UserService userService, Sso sso, TCoInterrelatedRepository tCoInterrelatedRepository) {
        this.userService = userService;
        this.sso = sso;
        this.tCoInterrelatedRepository = tCoInterrelatedRepository;
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

    @PostMapping("/login/idSearch")
    public Map idSearch(@RequestBody Map<String, String> params) {
        return userService.idSearch(params);
    }

    @PostMapping("/login/pwSearch")
    public Map pwSearch(@RequestBody Map<String, String> params) {
        return userService.pwSearch(params);
    }

    @PostMapping("/login/interrelatedList")
    public List interrelatedList() {
        return tCoInterrelatedRepository.findAll();
    }

    @PostMapping("/login/custSave")
    public Map custSave(@RequestBody Map<String, String> params) {
        return userService.custSave(params);
    }

}
