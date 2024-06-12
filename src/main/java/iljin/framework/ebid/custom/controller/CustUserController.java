package iljin.framework.ebid.custom.controller;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.custom.service.CustUserService;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/custuser")
@CrossOrigin
@Slf4j
public class CustUserController {

	@Autowired
	private CustUserService custUserService;

	/**
	 * 업체 사용자 리스트
	 * 
	 * @param params
	 * @param user
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping("/userList")
	public ResultBody userList(@RequestBody Map<String, Object> params) throws Exception {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = custUserService.userList(params);
		}catch(Exception e) {
			log.error("userList error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("사용자관리 리스트를 가져오는것을 실패하였습니다.");
		}
		return resultBody;
	}

	/**
	 * 업체 사용자 리스트
	 * 
	 * @param params
	 * @param user
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping("/userListForCust")
	public ResultBody userListForCust(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) throws Exception {
		ResultBody resultBody = new ResultBody();
		try {
			if (params.get("custCode") == null) {
				params.put("custCode", Integer.parseInt(user.getCustCode()));
			}
			resultBody = custUserService.userList(params);
		}catch(Exception e) {
			log.error("userList error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("사용자관리 리스트를 가져오는것을 실패하였습니다.");
		}
		return resultBody;
	}
	
	/**
	 * 업체 사용자 상세
	 * @param id
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@PostMapping("/{id}")
	public ResultBody detail(@PathVariable String id, @AuthenticationPrincipal CustomUserDetails user) throws Exception {
		ResultBody resultBody = new ResultBody();
		
		Map<String, Object> params = new HashedMap();
		params.put("userId", id);
		
		resultBody.setData(custUserService.detail(params));
		
		return resultBody;
	}
	
	/**
	 * 업체 사용자 등록 및 수정
	 * @param params
	 * @param user
	 * @return
	 */
	@PostMapping("/save")
	public ResultBody save(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) {
		ResultBody resultBody = new ResultBody();
		try {
			custUserService.save(params, user);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("업체 상세 내용 조회 중 오류가 발생하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		
		return resultBody;
	}

	/**
	 * 업체 사용자 삭제
	 * @param params
	 * @param user
	 * @return
	 */
	@PostMapping("/del")
	public ResultBody del(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) {
		ResultBody resultBody = new ResultBody();
		try {
			custUserService.del(params, user);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("업체 상세 내용 조회 중 오류가 발생하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		
		return resultBody;
	}
}
