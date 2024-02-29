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

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.etc.notice.entity.TCoBoardCustCode;
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
	
	//공지사항 조회수 + 1
	@PostMapping("/updateClickNum")
	public ResultBody updateClickNum(@RequestBody Map<String, Object> params) {

		return noticeService.updateClickNum(params);
	}
	
	//공지사항 공개되는 계열사 리스트 조회
	@PostMapping("/selectGroupList")
	public List<TCoBoardCustCode> selectGroupList(@RequestBody Map<String, Object> params) {

		return noticeService.selectGroupList(params);
	}
	
	//공지사항 삭제
	@PostMapping("/deleteNotice")
	public ResultBody deleteNotice(@RequestBody Map<String, Object> params) {

		return noticeService.deleteNotice(params);
	}
	
	//공지사항 수정
	@PostMapping("/updateNotice")
	public ResultBody updateNotice(@RequestBody Map<String, Object> params) {

		return noticeService.updateNotice(params);
	}
	
	//공지사항 등록
	@PostMapping("/insertNotice")
	public ResultBody insertNotice(@RequestBody Map<String, Object> params) {

		return noticeService.insertNotice(params);
	}
}
