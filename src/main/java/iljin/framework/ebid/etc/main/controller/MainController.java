package iljin.framework.ebid.etc.main.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iljin.framework.ebid.etc.main.dto.BidCntDto;
import iljin.framework.ebid.etc.main.dto.PartnerBidCntDto;
import iljin.framework.ebid.etc.main.dto.PartnerCntDto;
import iljin.framework.ebid.etc.main.dto.PartnerCompletedBidCntDto;
import iljin.framework.ebid.etc.main.service.MainService;

@RestController
@RequestMapping("/api/v1/main")
@CrossOrigin
public class MainController {
	
	@Autowired 
	MainService mainService;
	
	//전자입찰 건수 조회
	@PostMapping("/selectBidCnt")
    public BidCntDto selectBidCnt(@RequestBody Map<String, Object> params) throws IOException {

        return mainService.selectBidCnt(params);
    }
	
	//협력사 업채수 조회
	@PostMapping("/selectPartnerCnt")
    public PartnerCntDto selectPartnerCnt(@RequestBody Map<String, Object> params) throws IOException {

        return mainService.selectPartnerCnt(params);
    }
	
	//협력사 전자입찰 건수 조회
	@PostMapping("/selectPartnerBidCnt")
    public PartnerBidCntDto selectPartnerBidCnt(@RequestBody Map<String, Object> params) throws IOException {

        return mainService.selectPartnerBidCnt(params);
    }
	
	//입찰완료 조회
	@PostMapping("/selectCompletedBidCnt")
    public PartnerCompletedBidCntDto selectCompletedBidCnt(@RequestBody Map<String, Object> params) throws IOException {

        return mainService.selectCompletedBidCnt(params);
    }
}
