package iljin.framework.ebid.etc.notice.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.etc.notice.entity.TCoBoardCustCode;
import iljin.framework.ebid.etc.notice.service.NoticeService;
import iljin.framework.ebid.etc.util.common.file.FileService;

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
	public ResultBody updateNotice(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("data") String jsonData) {

		ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> params = null;
		try {
			params = mapper.readValue(jsonData, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
		} 
        
		return noticeService.updateNotice(file, params);
	}
	
	//공지사항 등록
	@PostMapping("/insertNotice")
	public ResultBody insertNotice(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("data") String jsonData) {

		ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> params = null;
		try {
			params = mapper.readValue(jsonData, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return noticeService.insertNotice(file, params);
	}
	
	//첨부파일 다운로드
	@PostMapping("/downloadFile")
    public ByteArrayResource downloadFile(@RequestBody Map<String, Object> params) throws IOException {

        return noticeService.downloadFile(params);
    }
	
}
