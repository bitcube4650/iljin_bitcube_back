package iljin.framework.ebid.bid.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.bid.service.BidProgressService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/bid")
@CrossOrigin
@Slf4j
public class BidProgressController {

    @Autowired
    private BidProgressService bidProgressService;

    @PostMapping("/progressList")
    public ResultBody progresslist(@RequestBody Map<String, Object> params) {
        return bidProgressService.progressList(params);
    }

    @PostMapping("/progresslistDetail")
    public ResultBody progresslistDetail(@RequestBody Map<String,Object> params){
		ResultBody resultBody = new ResultBody();
        try {
        	resultBody =  bidProgressService.progresslistDetail(params);
		} catch (Exception e) {
			log.error("progresslistDetail error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰 상세 조회를 실패하였습니다.");
		}
        return resultBody;
    }

    @PostMapping("/bidNotice")
    public ResultBody bidNotice(@RequestBody Map<String, Object> params) {
    	ResultBody resultBody = new ResultBody();
    	try {
    		bidProgressService.bidNotice(params);
        } catch (Exception e) {
			log.error("bidNotice error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰 공고를 실패하였습니다.");
            }
        return resultBody;
    }

    @PostMapping("/delete")
    public ResultBody delete(@RequestBody Map<String, Object> params) throws Exception {
        return bidProgressService.delete(params);
    }

    @PostMapping("/custList")
    public ResultBody custList(@RequestBody Map<String, Object> params) throws Exception {
        return bidProgressService.custList(params);
    }

    @PostMapping("/userList")
    public ResultBody selectUserList(@RequestBody Map<String, Object> params) throws Exception {
        return bidProgressService.userList(params);
    }

   
    @PostMapping("/updateBid")
    public ResultBody updateBid(
    		@RequestPart("bidContent") String bidContent,
    		@RequestPart(value = "insFile", required = false) MultipartFile insFile,
    		@RequestPart(value = "innerFiles", required = false) List<MultipartFile> innerFiles,
    		@RequestPart(value = "outerFiles", required = false) List<MultipartFile> outerFiles,
			@AuthenticationPrincipal CustomUserDetails user) {
    	ResultBody resultBody = new ResultBody();
		ObjectMapper mapper = new ObjectMapper();
	       
    	try {
    		Map<String, Object> params = mapper.readValue(bidContent, Map.class);
    		resultBody = bidProgressService.updateBid(params, insFile, innerFiles, outerFiles);
        } catch (Exception e) {
			log.error("updateBid  error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰 공고 수정을 실패하였습니다.");
            }
        return resultBody;
    }

    @PostMapping("/downloadFile")
    public ByteArrayResource downloadFile(@RequestBody Map<String, Object> params) throws IOException {

        return bidProgressService.downloadFile(params);
    }

    @PostMapping("/newBiNo")
    public String newBiNo() {
        return bidProgressService.newBiNo();
    }

    @PostMapping("/insertBid")
    public ResultBody insertBid(
//    		@RequestPart("bidContent") Map<String, Object> bidContent,
//    		@RequestPart("custContent") List<Map<String, Object>> custContent,
//    		@RequestPart("tableContent") List<Map<String, Object>> tableContent,
//    		@RequestPart("updateEmail") Map<String, Object> updateEmail,
    		@RequestPart("bidContent") String bidContent,
    		@RequestPart(value = "insFile", required = false) MultipartFile insFile,
    		@RequestPart(value = "innerFiles", required = false) List<MultipartFile> innerFiles,
    		@RequestPart(value = "outerFiles", required = false) List<MultipartFile> outerFiles,
			@AuthenticationPrincipal CustomUserDetails user) {
    	ResultBody resultBody = new ResultBody();
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> params = null;
    	try {
			params = mapper.readValue(bidContent, Map.class);
    		resultBody = bidProgressService.insertBid(params, insFile, innerFiles, outerFiles);
        } catch (Exception e) {
			log.error("insertBid  error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰 공고 등록을 실패하였습니다.");
            }
        return resultBody;
    }

    @PostMapping("/pastBidList")
    public ResultBody pastBidList(@RequestBody Map<String, Object> params){
		ResultBody resultBody = new ResultBody();
        try {
        	resultBody = bidProgressService.pastBidList(params);
		} catch (Exception e) {
			log.error("progressCodeList error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("과거입찰 조회를 실패하였습니다.");
		}
		return resultBody;
    }
    @PostMapping("/progressCodeList")
    public ResultBody progressCodeList(){
		ResultBody resultBody = new ResultBody();
        try {
        	resultBody = bidProgressService.progressCodeList();
		} catch (Exception e) {
			log.error("progressCodeList error : {}", e);
			resultBody.setCode("fail");
			resultBody.setMsg("입찰분류 코드 조회를 실패하였습니다.");
		}
		return resultBody;
    }

}
