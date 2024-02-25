package iljin.framework.ebid.etc.notice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iljin.framework.ebid.custom.service.ItemService;
import iljin.framework.ebid.etc.notice.service.NoticeService;

@RestController
@RequestMapping("/api/v1/notice")
@CrossOrigin
public class NoticeContoller {

	@Autowired
    private NoticeService noticeService;
	
	//공지사항 조회
	@PostMapping("/noticeList")
    public Page noticeList(@RequestBody Map<String, Object> params) {
        return noticeService.noticeList(params);
    }
	
}
