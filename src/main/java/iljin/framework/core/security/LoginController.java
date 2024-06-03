package iljin.framework.core.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rathontech.sso.sp.config.Env;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.sso.Sso;
import iljin.framework.core.security.user.UserDto;
import iljin.framework.core.security.user.UserService;
import iljin.framework.ebid.custom.repository.TCoInterrelatedRepository;
import iljin.framework.ebid.custom.repository.TCoItemGrpRepository;
import iljin.framework.ebid.custom.service.CustService;
import iljin.framework.ebid.custom.service.ItemService;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.PagaUtils;
import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin
@Slf4j
public class LoginController {

	private final UserService userService;

	private final Sso sso;
	private final TCoInterrelatedRepository tCoInterrelatedRepository;
	private final TCoItemGrpRepository tCoItemGrpRepository;
	private final CustService custService;
	private final ItemService itemService;

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	public LoginController(UserService userService, Sso sso, TCoInterrelatedRepository tCoInterrelatedRepository, TCoItemGrpRepository tCoItemGrpRepository, CustService custService, ItemService itemService) {
		this.userService = userService;
		this.sso = sso;
		this.tCoInterrelatedRepository = tCoInterrelatedRepository;
		this.tCoItemGrpRepository = tCoItemGrpRepository;
		this.custService = custService;
		this.itemService = itemService;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthToken> login(@RequestBody UserDto userDto, HttpSession session, HttpServletRequest request) {
		return userService.login(userDto, session, request);
	}

	@PostMapping("/login/sso")
	public ResponseEntity<AuthToken> ssoLogin(HttpSession session, HttpServletRequest req) {
		String loginId = req.getSession().getAttribute(Env.DEFAULT_SESSION_USERID).toString();
		UserDto userDto = new UserDto();
		userDto.setLoginId(loginId);
		return userService.ssoLogin(userDto, session, req);
	}

	@GetMapping("/logout")
	public ResponseEntity<String> logout(HttpSession session) {
		userService.logout(session);
		return new ResponseEntity<> ("로그아웃 되었습니다.", HttpStatus.OK);
	}

	@PostMapping("/login/idSearch")
	public ResultBody idSearch(@RequestBody Map<String, String> params) {
		ResultBody resultBody = new ResultBody();
		try {
			Map<String, Object> paramMap = new HashMap<String, Object> ();
			paramMap.put("regnum", CommonUtils.getString(params.get("regnum1"))+CommonUtils.getString(params.get("regnum2"))+CommonUtils.getString(params.get("regnum3")));
			paramMap.put("userName", params.get("userName"));
			paramMap.put("userEmail", params.get("userEmail"));
			
			resultBody = userService.idSearch(paramMap);
		} catch (Exception e) {
			log.error("login idSearch error : {}", e);
		}
		return resultBody;
	}

	@PostMapping("/login/pwSearch")
	public ResultBody pwSearch(@RequestBody Map<String, String> params) {
		ResultBody resultBody = new ResultBody();
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("regnum", CommonUtils.getString(params.get("regnum1"))+CommonUtils.getString(params.get("regnum2"))+CommonUtils.getString(params.get("regnum3")));
			paramMap.put("userId", params.get("userId"));
			paramMap.put("userName", params.get("userName"));
			paramMap.put("userEmail", params.get("userEmail"));
			
			resultBody = userService.pwSearch(paramMap);
		} catch (Exception e) {
			log.error("login pwSearch error : {}", e);
		}
		return resultBody;
	}

	@PostMapping("/login/interrelatedList")
	public List interrelatedList() {
		List<Object> list = new ArrayList<Object>();
		try {
			list = userService.interrelatedList();
		} catch (Exception e) {
			log.error("login interrelatedList error : {}", e);
		}
		return list;
	}

	@PostMapping("/login/custSave")
	public ResultBody custSave(@RequestPart(value = "regnumFile", required = false) MultipartFile regnumFile, @RequestPart(value = "bFile", required = false) MultipartFile bFile, @RequestPart("data") Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			custService.save(params, regnumFile, bFile, null);
		} catch (IOException e) {
			resultBody.setCode("UPLOAD");
			resultBody.setStatus(500);
			resultBody.setMsg("파일 업로드시 오류가 발생했습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("업체 저장 중 오류가 발생하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		
		return resultBody;
	}

	@PostMapping("/login/itemGrpList")
	public List itemGrpList() {
		List<Object> list = new ArrayList<Object>();
		try {
			list = userService.itemGrpList();
		} catch (Exception e) {
			log.error("login itemGrpList error : {}", e);
		}
		return list;
	}

	@PostMapping("/login/itemList")
	public Page itemList(@RequestBody Map<String, Object> params) {
		Pageable pageable = PagaUtils.pageable(params);
		Page page = new PageImpl<>(new ArrayList<Object>(), pageable, 0);
		try {
//			page = itemService.itemList(params);
		} catch (Exception e) {
			log.error("login itemList error : {}", e);
		}
		return page;
	}
	
	@PostMapping("/login/idcheck")
	public ResultBody idcheck(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = custService.idcheck(params);
		} catch (Exception e) {
			log.error("login idcheck error : {}", e);
		}
		return resultBody;
	}
}
