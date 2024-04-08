package iljin.framework.ebid.etc.main.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.UserService;
import iljin.framework.ebid.etc.main.dto.BidCntDto;
import iljin.framework.ebid.etc.main.dto.PartnerBidCntDto;
import iljin.framework.ebid.etc.main.dto.PartnerCntDto;
import iljin.framework.ebid.etc.main.dto.PartnerCompletedBidCntDto;
import iljin.framework.ebid.etc.main.service.MainService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/main")
@CrossOrigin
@Slf4j
public class MainController {
	
	@Autowired 
	private MainService mainService;
	
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
	
	//비밀번호 확인
	@PostMapping("/checkPwd")
	public boolean checkPwd(@RequestBody Map<String, Object> params) {
		
		return mainService.checkPwd(params);
		
	}
	
	//비밀번호 변경
	@PostMapping("/changePwd")
	public boolean changePwd(@RequestBody Map<String, Object> params) {
		
		return mainService.changePwd(params);
				
	}
	
	//유저 정보 조회
	@PostMapping("/selectUserInfo")
	public ResultBody selectUserInfo(@RequestBody Map<String, Object> params) {
		
		return mainService.selectUserInfo(params);
				
	}
	
	//유저 정보 변경
	@PostMapping("/saveUserInfo")
	public ResultBody saveUserInfo(@RequestBody Map<String, Object> params) {
		
		return mainService.saveUserInfo(params);
				
	}
	
	//계열사 정보 조회
	@PostMapping("/selectCompInfo")
	public ResultBody selectCompInfo(@RequestBody Map<String, Object> params) {
		
		return mainService.selectCompInfo(params);
				
	}

	//비밀번호 변경 권장 플래그
	@PostMapping("/chkPwChangeEncourage")
	public ResultBody chkPwChangeEncourage(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		
		try {
			resultBody = mainService.chkPwChangeEncourage(params);
		}catch(Exception e) {
			log.error("chkPwChangeEncourage error : {}", e);
			resultBody.setCode("fail");
		}
		
		return resultBody;
				
	}
	
}
