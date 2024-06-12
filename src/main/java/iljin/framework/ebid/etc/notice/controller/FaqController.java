package iljin.framework.ebid.etc.notice.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.etc.notice.service.FaqService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/faq")
@CrossOrigin
@Slf4j
public class FaqController {

	@Autowired
	private FaqService faqService;

	/**
	 * FAQ 조회
	 * @param params
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping("/faqList")
	public ResultBody faqList(@RequestBody Map<String, Object> params) {

		return faqService.faqList(params);
	}

	/**
	 * FAQ 등록 및 수정
	 * @param params
	 * @param user
	 * @return
	 */
	@PostMapping("/save")
	public ResultBody save(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) {
		ResultBody resultBody = new ResultBody();
		
		try {
			faqService.save(params, user);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("FAQ 저장시 오류가 발생하였습니다.");
			log.error(e.getMessage());
		}
		
		return resultBody;
	}

	/**
	 * FAQ 삭제
	 * @param params
	 * @return
	 */
	@PostMapping("/delete")
	public ResultBody delete(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();

		try {
			faqService.delete(params);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("FAQ 등록 및 수정시 오류가 발생하였습니다.");
			log.error(e.getMessage());
		}
		
		return resultBody;
	}
}
