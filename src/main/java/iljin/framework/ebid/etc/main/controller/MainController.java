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
import org.springframework.web.bind.annotation.GetMapping;
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
	public ResultBody selectBidCnt(@RequestBody Map<String, Object> params) throws IOException {
		ResultBody resultBody = new ResultBody();
		try { 
			resultBody = mainService.selectBidCnt(params);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("전자입찰 건수 조회에 실패하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		return resultBody;
	}
	
	//협력사 업채수 조회
	@PostMapping("/selectPartnerCnt")
	public ResultBody selectPartnerCnt(@RequestBody Map<String, Object> params) throws IOException {
		ResultBody resultBody = new ResultBody();
		try { 
			resultBody = mainService.selectPartnerCnt(params);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("협력사 업채수 조회에 실패하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		return resultBody;
	}
	
	//협력사 전자입찰 건수 조회
	@PostMapping("/selectPartnerBidCnt")
	public ResultBody selectPartnerBidCnt(@RequestBody Map<String, Object> params) throws IOException {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = mainService.selectPartnerBidCnt(params);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("전자입찰 건수 조회에 실패하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		return resultBody;
	}
	
	//입찰완료 조회
	@PostMapping("/selectCompletedBidCnt")
	public ResultBody selectCompletedBidCnt(@RequestBody Map<String, Object> params) throws IOException {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = mainService.selectCompletedBidCnt(params);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("입찰완료 조회에 실패하였습니다.");
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		return resultBody;
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
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = mainService.selectUserInfo(params);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("An error occurred while selecting the user info.");
			resultBody.setData(e.getMessage());
			
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		return resultBody;
	}
	
	//유저 정보 변경
	@PostMapping("/saveUserInfo")
	public ResultBody saveUserInfo(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = mainService.saveUserInfo(params);
		} catch (Exception e) {
			resultBody.setCode("ERROR");
			resultBody.setStatus(500);
			resultBody.setMsg("An error occurred while updating the user info.");
			resultBody.setData(e.getMessage());
			
			log.error("{} Error : {}", this.getClass(), e.getMessage());
		}
		return resultBody;
	}
	
	//계열사 정보 조회 (사용하지 않음)
	@Deprecated
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
	

	// 초기 계열사 사용자 비밀번호 변경 처리 (호출 메소드를 못찾음 확인 필요)
	@PostMapping("/chgPwdFirst")
	public void chgPwdFirst() {
		log.info("-----------------------chgPwdFirst start----------------------");
//		try {
			mainService.chgPwdFirst();
//		}catch(Exception e) {
//		}
		log.info("-----------------------chgPwdFirst end----------------------");
	}
}
