package iljin.framework.ebid.custom.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.custom.service.CustService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/cust")
@CrossOrigin
@Slf4j
public class CustController {

	@Autowired
	private CustService custService;

	/**
	 * 업체 승인 리스트
	 * @param params
	 * @param user
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping("/approvalList")
	public Page approvalList(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) throws Exception {
		params.put("interrelatedCustCode", user.getCustCode());
		return custService.custList(params);
	}

	/**
	 * 업체 리스트
	 * @param params
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping("/custList")
	public Page custList(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) throws Exception {
		params.put("interrelatedCustCode", user.getCustCode());
		return custService.custList(params);
	}

	/**
	 * 타 계열사 업체 리스트
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping("/otherCustList")
	public Page otherCustList(@RequestBody Map<String, Object> params) throws Exception {
		return custService.otherCustList(params);
	}
	
	/**
	 * 업체 상세 조회
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@PostMapping("/management/{id}")
	public ResultBody management(@PathVariable String id, @AuthenticationPrincipal CustomUserDetails user) {
		ResultBody resultBody = new ResultBody();
		
		Map<String, Object> params = new HashedMap();
		params.put("custCode", id);
		params.put("interrelatedCustCode", user.getCustCode());
		
		try {
			resultBody = custService.custDetail(params);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("업체 상세 내용 조회 중 오류가 발생하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		return resultBody;
	}
	
	/**
	 * 타계열사 정보 상세 조회
	 * @param id
	 * @param user
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@PostMapping("/otherCustManagement/{id}")
	public ResultBody otherCustManagement(@PathVariable String id, @AuthenticationPrincipal CustomUserDetails user) {
		ResultBody resultBody = new ResultBody();
		
		Map<String, Object> params = new HashedMap();
		params.put("custCode", id);
		
		try {
			resultBody = custService.custDetail(params);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("업체 상세 내용 조회 중 오류가 발생하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		return resultBody;
	}

	/**
	 * 업체 자사 정보 조회
	 * @param user
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@PostMapping("/info")
	public ResultBody info(@AuthenticationPrincipal CustomUserDetails user) {
		ResultBody resultBody = new ResultBody();
		
		Map<String, Object> params = new HashedMap();
		params.put("custCode", user.getCustCode());
		
		try {
			resultBody = custService.custDetail(params);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("업체 상세 내용 조회 중 오류가 발생하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		return resultBody;
	}

	/**
	 * 업체 승인 처리
	 * @param params
	 * @return
	 */
	@PostMapping("/approval")
	public ResultBody approval(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) {
		ResultBody resultBody = new ResultBody();
		try {
			custService.approval(params, user);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("업체 승인 중 오류가 발생하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		return resultBody;
	}
	
	/**
	 * 업체 반려 처리
	 * @param params
	 * @param user
	 * @return
	 */
	@PostMapping("/back")
	public ResultBody back(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) {
		ResultBody resultBody = new ResultBody();
		try {
			custService.back(params, user);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("업체 반려 중 오류가 발생하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		
		return resultBody;
	}
	
	/**
	 * 업체 삭제 처리
	 * @param params
	 * @param user
	 * @return
	 */
	@PostMapping("/del")
	public ResultBody del(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) {
		ResultBody resultBody = new ResultBody();
		params.put("updUserId", user.getUsername());
		
		try {
			custService.del(params);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("업체 삭제 중 오류가 발생하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		
		return resultBody;
	}

	/**
	 * 업체 탈퇴
	 * @param params
	 * @param session
	 * @param user
	 * @return
	 */
	@PostMapping("/leave")
	public ResultBody leave(@RequestBody Map<String, Object> params, HttpSession session, @AuthenticationPrincipal CustomUserDetails user) {
		session.invalidate();
		ResultBody resultBody = new ResultBody();
		params.put("userId", user.getUsername());
		
		try {
			custService.del(params);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("탈퇴 중 오류가 발생하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		
		return resultBody;
	}

//	@PostMapping("/idcheck")
//	public ResultBody idcheck(@RequestBody Map<String, Object> params) {
//		return custService.idcheck(params);
//	}

	@PostMapping("/pwdcheck")
	public ResultBody pwdcheck(@RequestBody Map<String, Object> params) {
		return custService.pwdcheck(params);
	}

	/**
	 * 업체 등록(회원가입) 및 수정
	 * @param regnumFile
	 * @param bFile
	 * @param params
	 * @param user
	 * @return
	 */
	@PostMapping("/save")
	public ResultBody save(@RequestPart(value = "regnumFile", required = false) MultipartFile regnumFile,
			@RequestPart(value = "bFile", required = false) MultipartFile bFile,
			@RequestPart("data") Map<String, Object> params,
			@AuthenticationPrincipal CustomUserDetails user) {
		ResultBody resultBody = new ResultBody();

		try {
				custService.save(params, regnumFile, bFile, user);
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
}
