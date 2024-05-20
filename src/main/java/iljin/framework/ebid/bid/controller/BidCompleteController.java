package iljin.framework.ebid.bid.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.bid.service.BidCompleteService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/bidComplete")
@CrossOrigin
@Slf4j
public class BidCompleteController {
	
	@Autowired
	private BidCompleteService bidCompleteSvc;
	
	/**
	 * 입찰완료 리스트
	 * @param params
	 * @return
	 */
	@PostMapping("/list")
	public ResultBody complateBidList(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidCompleteSvc.complateBidList(params); 
		}catch(Exception e) {
			log.error("bidComplete list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰 완료 리스트를 가져오는것을 실패하였습니다.");	
		}
		return resultBody;
	}
	
	/**
	 * 입찰완료 상세
	 * @param params
	 * @return
	 */
	@PostMapping("/detail")
	public ResultBody complateBidDetail(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidCompleteSvc.complateBidDetail(params); 
		}catch(Exception e) {
			log.error("complateBidDetail error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰완료 상세 데이터를 가져오는것을 실패하였습니다.");
		}
		return resultBody;
	}
	
	/**
	 * 암호화 안된 파일 다운로드
	 * @param params
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/fileDown")
	public ByteArrayResource downloadFile(@RequestBody Map<String, Object> params) throws IOException {
		ByteArrayResource result = null;
		try {
			
			result = bidCompleteSvc.fileDown(params); 
		}catch(Exception e) {
			log.error("downloadFile error : {}", e);
		}
		return result;

	}
	
	/**
	 * 실제계약금액 업데이트
	 * @param params
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/updRealAmt")
	public ResultBody updRealAmt(@RequestBody Map<String, Object> params) throws IOException {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidCompleteSvc.updRealAmt(params); 
		}catch(Exception e) {
			log.error("updRealAmt error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("실제계약금액 업데이트를 실패했습니다.");
		}
		return resultBody;
	}
	
	/**
	 * 롯데에너지머티리얼즈 코드값
	 * @param params
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/lotteMatCode")
	public ResultBody lotteMatCode(@RequestBody Map<String, Object> params) throws IOException {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidCompleteSvc.lotteMatCode(params); 
		}catch(Exception e) {
			log.error("lotteMatCode list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("코드값을 가져오는것을 실패하였습니다.");
		}
		return resultBody;
	}
	
	/**
	 * 낙찰이력 리스트
	 * @param params
	 * @return
	 */
	@PostMapping("/history")
	public ResultBody complateBidhistory(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidCompleteSvc.complateBidhistory(params); 
		}catch(Exception e) {
			log.error("complateBidhistory list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("낙찰 이력 리스트를 가져오는것을 실패하였습니다.");
		}
		return resultBody;
	}
	
	/**
	 * 낙찰이력 내 투찰업체 팝업 리스트
	 * @param params
	 * @return
	 */
	@PostMapping("/joinCustList")
	public ResultBody joinCustList(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidCompleteSvc.joinCustList(params); 
		}catch(Exception e) {
			log.error("joinCustList list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("투찰 정보를 가져오는것을 실패하였습니다.");
		}
		return resultBody;
	}
	
	/**
	 * 협력사 입찰완료 리스트
	 * @param params
	 * @return
	 */
	@PostMapping("/partnerList")
	public ResultBody complateBidPartnerList(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidCompleteSvc.complateBidPartnerList(params); 
		}catch(Exception e) {
			log.error("complateBidPartnerList list error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰 완료 리스트를 가져오는것을 실패하였습니다.");	
		}
		return resultBody;
	}
	
	/**
	 * 협력사 입찰완료 상세
	 * @param params
	 * @return
	 */
	@PostMapping("/partnerDetail")
	public ResultBody complateBidPartnerDetail(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidCompleteSvc.complateBidPartnerDetail(params); 
		}catch(Exception e) {
			log.error("complateBidPartnerDetail error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰완료 상세 데이터를 가져오는것을 실패하였습니다.");
		}
		return resultBody;
	}
	
	/**
	 * 협력사 낙찰확인 업데이트
	 * @param params
	 * @return
	 */
	@PostMapping("/updBiCustFlag")
	public ResultBody updBiCustFlag(@RequestBody Map<String, Object> params) {
		ResultBody resultBody = new ResultBody();
		try {
			resultBody = bidCompleteSvc.updBiCustFlag(params); 
		}catch(Exception e) {
			log.error("updBiCustFlag error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("낙찰승인 저장을 실패하였습니다.");
		}
		return resultBody;
	}
}
