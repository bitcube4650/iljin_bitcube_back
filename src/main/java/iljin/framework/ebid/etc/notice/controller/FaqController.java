package iljin.framework.ebid.etc.notice.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.etc.notice.service.FaqService;
import iljin.framework.ebid.etc.notice.service.NoticeService;

@RestController
@RequestMapping("/api/v1/faq")
@CrossOrigin
public class FaqController {

	@Autowired
    private FaqService faqService;
	
	//faq 조회
	@PostMapping("/faqList")
    public Page faqList(@RequestBody Map<String, Object> params) {
		
        return faqService.faqList(params);
    }
	
	//faq 저장
	@PostMapping("/save")
	public ResultBody save(@RequestBody Map<String, Object> params) {
		return faqService.save(params);
	}
}




