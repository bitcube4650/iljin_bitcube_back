package iljin.framework.ebid.custom.controller;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.custom.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/couser")
@CrossOrigin
@Slf4j
public class CoUserController {

	@Autowired
	private UserService userService;

	// 계열사 리스트
	@PostMapping("/interrelatedList")
	public ResultBody interrelatedList() {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = userService.interrelatedList();
		}catch(Exception e) {
			log.error("interrelatedList error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("계열사 가져오기를 실패했습니다.");
		}
		return resultBody;
	}

	/**
	 * 사용자 리스트
	 * @param params interrelatedCustCode
	 * @param params useYn
	 * @param params userName
	 * @param params userId
	 * @return
	 */
	@PostMapping("/userList")
	public ResultBody userList(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = userService.userList(params);
		}catch(Exception e) {
			log.error("userList error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("사용자관리 리스트를 가져오는것을 실패하였습니다.");
		}
		return resultBody;
	}
	/**
	 * 사용자 상세 비밀번호 확인
	 * @param params pwd
	 * @return
	 */
	@PostMapping("/pwdCheck")
	public ResultBody pwdCheck(@RequestBody Map<String, Object> params) {
		return userService.pwdCheck(params);
	}

	/**
	 * 사용자 상세
	 * @param params userId
	 * @return
	 */
    @PostMapping("/userDetail")
	public ResultBody userDetail(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = userService.userDetail(params);
		}catch(Exception e) {
			log.error("userDetail error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("사용자 상세를 가져오는것을 실패하였습니다.");
		}
		return resultBody;
	}

	/**
	 * 사용자 저장/수정
	 * @param params userId
	 * @param params userPwd
	 * @param params userName
	 * @param params interrelatedCustCode
	 * @param params userAuth
	 * @param params openauth
	 * @param params bidauth
	 * @param params userHp
	 * @param params userTel
	 * @param params userEmail
	 * @param params userPosition
	 * @param params deptName
	 * @param params useYn
	 * @param params userInterrelatedList
	 * @return
	 */
    @PostMapping("/userSave")
    public ResultBody userSave(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = userService.userSave(params);
		}catch(Exception e) {
			log.error("userSave error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("사용자 저장에 실패하였습니다.");
		}
		return resultBody;
    }

	/**
	 * 비밀번호 변경
	 * @param params userId
	 * @param params chgPassword
	 * @return
	 */
	@PostMapping("/saveChgPwd")
	public ResultBody saveChgPwd(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = userService.saveChgPwd(params);
		}catch(Exception e) {
			log.error("saveChgPwd error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("비밀번호 변경에 실패하였습니다.");
		}
		return resultBody;
    }


}
