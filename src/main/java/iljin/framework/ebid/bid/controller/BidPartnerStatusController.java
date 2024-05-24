package iljin.framework.ebid.bid.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.bid.service.BidPartnerStatusService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/bidPtStatus")
@CrossOrigin
@Slf4j
public class BidPartnerStatusController {
    @Autowired
    private BidPartnerStatusService bidPartnerStatusService;

	/**
	 * 협력사 입찰진행
	 * @param params
	 * @return
	 */
	@PostMapping("/statuslist")
	public ResultBody statuslist(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidPartnerStatusService.statuslist(params);
		}catch(Exception e) {
			log.error("bidPartnerStatusService statuslist list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰 진행 리스트를 가져오는것을 실패하였습니다.");	
		}
		return resultBody;
	}
	
	/**
	 * 협력사 입찰공고확인 처리
	 * @param params
	 * @return
	 */
	@PostMapping("/checkBid")
	public ResultBody checkBid(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidPartnerStatusService.checkBid(params); 
		}catch(Exception e) {
			log.error("rebid error : {}", e);
		}
		return resultBody;
	}
	
	/**
	 * 입찰진행 상세
	 * @return
	 */
	@PostMapping("bidStatusDetail")
	public ResultBody bidStatusDetail(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidPartnerStatusService.bidStatusDetail(params);
		}catch(Exception e) {
			log.error("bidStatusDetail error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰진행 상세 데이터를 가져오는것을 실패하였습니다.");
		}
		
		return resultBody;
	}
	
	/**
	 * 견적금액 단위 코드
	 * @return
	 */
	@PostMapping("/currList")
	public ResultBody currList() {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidPartnerStatusService.currList();
		}catch(Exception e) {
			log.error("currList error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("견적금액 단위 리스트를 가져오는것을 실패하였습니다.");	
		}
		return resultBody;
	}
	
	/**
	 * 투찰
	 * @param jsonData
	 * @param file1
	 * @param file2
	 * @param user
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	@PostMapping("/bidSubmitting")
	public ResultBody bidSubmitting(
			@RequestPart("data") String jsonData,
			@RequestPart(value = "detailFile", required = false) MultipartFile detailFile, 
			@RequestPart(value = "etcFile", required = false) MultipartFile etcFile,
			@AuthenticationPrincipal CustomUserDetails user
		) {
		
		ResultBody resultBody = new ResultBody();
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> params = null;
		try {
			params = mapper.readValue(jsonData, Map.class);
			resultBody = bidPartnerStatusService.bidSubmitting(params, detailFile, etcFile, user);
		} catch (Exception e) {
			log.error("bidSubmitting error : {}", e);
			resultBody.setCode("ERROR");
			resultBody.setStatus(999);
		} 
		return resultBody;
	}
}
