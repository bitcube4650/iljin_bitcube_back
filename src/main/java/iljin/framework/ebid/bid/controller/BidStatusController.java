package iljin.framework.ebid.bid.controller;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.dto.ItemDto;
import iljin.framework.ebid.bid.service.BidProgressService;
import iljin.framework.ebid.bid.service.BidStatusService;
import iljin.framework.ebid.custom.entity.TCoItem;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/bidstatus")
@CrossOrigin
@Slf4j
public class BidStatusController {
    @Autowired
    private BidStatusService bidStatusService;

	/**
	 * 입찰진행 리스트
	 * @param params
	 * @return
	 */
	@PostMapping("/statuslist")
	public ResultBody statuslist(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidStatusService.statuslist(params); 
		}catch(Exception e) {
			log.error("statuslist list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰 진행 리스트를 가져오는것을 실패하였습니다.");	
		}
		return resultBody;
	}

	/**
	 * 입찰(개찰) 상세
	 * @param param
	 * @return
	 */
	@PostMapping("/statusDetail")
	public ResultBody progresslistDetail(@RequestBody Map<String, Object> param) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidStatusService.statusDetail(param); 
		}catch(Exception e) {
			log.error("statusDetail error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰진행 상세 데이터를 가져오는것을 실패하였습니다.");
		}
		return resultBody;
	}
	
	/**
	 * 유찰처리
	 * @param params
	 * @return
	 */
	@PostMapping("/bidFailure")
	public ResultBody bidFailure(@RequestBody Map<String, String> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidStatusService.bidFailure(params); 
		}catch(Exception e) {
			log.error("bidFailure error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("유찰 처리중 오류가 발생했습니다.");
		}
		return resultBody;
	}

	/**
	 * 재입찰처리
	 * @param params
	 * @return
	 */
	@PostMapping("/rebid")
	public ResultBody rebid(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidStatusService.rebid(params); 
		}catch(Exception e) {
			log.error("rebid error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("재입찰 처리 중 오류가 발생했습니다.");	
		}
		return resultBody;
	}

	/**
	 * 제출이력
	 * @param params
	 * @return
	 */
	@PostMapping("/submitHist")
	public ResultBody submitHist(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidStatusService.submitHist(params); 
		}catch(Exception e) {
			log.error("bidSucc error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("제출 이력을 가져오는 도중 오류가 발생했습니다.");	
		}
		return resultBody;
	}


	/**
	 * 낙찰
	 * @param params
	 * @return
	 */
	@PostMapping("/bidSucc")
	public ResultBody bidSucc(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidStatusService.bidSucc(params); 
		}catch(Exception e) {
			log.error("bidSucc error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("낙찰 처리중 오류가 발생했습니다.");	
		}
		return resultBody;
	}
	
	/**
	 * 입회자 서명
	 * @param params
	 * @return
	 */
	@PostMapping("/attSign")
	public ResultBody attSign(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidStatusService.attSign(params); 
		}catch(Exception e) {
			log.error("attSign error : {}" , e);
			resultBody.setCode("fail");
		}
		return resultBody;
	}
	
	/**
	 * 개찰하기
	 * @param params
	 * @return
	 */
	@PostMapping("/bidOpening")
	public ResultBody bidOpening(@RequestBody Map<String, String> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidStatusService.bidOpening(params); 
		}catch(Exception e) {
			log.error("bidOpening error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("개찰 처리중 오류가 발생했습니다.");	
		}
		return resultBody;
	}
	
}
