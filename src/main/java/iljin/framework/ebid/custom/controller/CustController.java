package iljin.framework.ebid.custom.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;

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
import iljin.framework.ebid.custom.dto.TCoCustMasterDto;
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

	@SuppressWarnings("rawtypes")
	@PostMapping("/custList")
	public Page custList(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) throws Exception {
		params.put("interrelatedCustCode", user.getCustCode());
		return custService.custList(params);
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/otherCustList")
	public Page otherCustList(@RequestBody Map<String, Object> params) {
		return custService.otherCustList(params);
	}
	
	/**
	 * 승인 대상 업체 상세 조회
	 * @param id
	 * @return
	 */
	@PostMapping("/approval/{id}")
	public ResultBody approvalDetail(@PathVariable String id) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = custService.custDetail(id);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("업체 상세 내용 조회 중 오류가 발생하였습니다.");
			log.error(e.getMessage());
		}
		return resultBody;
	}
	
	/**
	 * 업체 상세 조회
	 * @param id
	 * @return
	 */
	@PostMapping("/management/{id}")
	public TCoCustMasterDto management(@PathVariable String id) {
		return custService.custDetailForInter(id);
	}

	@PostMapping("/info")
	public TCoCustMasterDto info(@AuthenticationPrincipal CustomUserDetails user) {
		return custService.custDetailForCust(user.getCustCode());
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
			log.error(e.getMessage());
		}
		return resultBody;
	}

	@PostMapping("/back")
	public ResultBody back(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) {
		ResultBody resultBody = new ResultBody();
		try {
			custService.back(params, user);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("업체 반려 중 오류가 발생하였습니다.");
			log.error(e.getMessage());
		}
		
		return resultBody;
	}

	@PostMapping("/del")
	public ResultBody del(@RequestBody Map<String, Object> params) {
		return custService.del(params);
	}

	@PostMapping("/leave")
	public ResultBody leave(@RequestBody Map<String, Object> params, HttpSession session) {
		session.invalidate();
		return custService.del(params);
	}

//	@PostMapping("/idcheck")
//	public ResultBody idcheck(@RequestBody Map<String, Object> params) {
//		return custService.idcheck(params);
//	}

	@PostMapping("/pwdcheck")
	public ResultBody pwdcheck(@RequestBody Map<String, Object> params) {
		return custService.pwdcheck(params);
	}

	@PostMapping("/save")
	public ResultBody save(@RequestPart(value = "regnumFile", required = false) MultipartFile regnumFile,
			@RequestPart(value = "bFile", required = false) MultipartFile bFile,
			@RequestPart("data") Map<String, Object> params) {
		if (params.get("custCode") == null) {
			return custService.insert(params, regnumFile, bFile);
		} else {
			return custService.update(params, regnumFile, bFile);
		}
	}
}
