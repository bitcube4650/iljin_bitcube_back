package iljin.framework.ebid.bid.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.bid.service.BidCompleteService;

@RestController
@RequestMapping("/api/v1/bidComplete")
@CrossOrigin
public class BidCompleteController {
	
	@Autowired
	private BidCompleteService bidCompleteSvc;
	
	@PostMapping("/list")
	public ResultBody complateBidList(@RequestBody Map<String, Object> params) {
		return bidCompleteSvc.complateBidList(params);
	}
	
	@PostMapping("/detail")
	public ResultBody complateBidDetail(@RequestBody Map<String, Object> params) {
		return bidCompleteSvc.complateBidDetail(params);
	}
	
	@PostMapping("/fileDown")
	public ByteArrayResource downloadFile(@RequestBody Map<String, Object> params) throws IOException {

		return bidCompleteSvc.fileDown(params);
	}
	
	@PostMapping("/updRealAmt")
	public ResultBody updRealAmt(@RequestBody Map<String, Object> params) throws IOException {

		return bidCompleteSvc.updRealAmt(params);
	}
	
	@PostMapping("/lotteMatCode")
	public ResultBody lotteMatCode(@RequestBody Map<String, Object> params) throws IOException {

		return bidCompleteSvc.lotteMatCode(params);
	}
	
	@PostMapping("/history")
	public ResultBody complateBidhistory(@RequestBody Map<String, Object> params) {
		return bidCompleteSvc.complateBidhistory(params);
	}
	
	@PostMapping("/joinCustList")
	public ResultBody joinCustList(@RequestBody Map<String, Object> params) {
		return bidCompleteSvc.joinCustList(params);
	}

}
