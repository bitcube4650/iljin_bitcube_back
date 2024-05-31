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

@RestController
@RequestMapping("/api/v1/bid")
@CrossOrigin

public class BidProgressController {

    @Autowired
    private BidProgressService bidProgressService;

    @PostMapping("/progressList")
    public ResultBody progresslist(@RequestBody Map<String, Object> params) {
        return bidProgressService.progressList(params);
    }

    @PostMapping("/progresslistDetail")
    public ResultBody progresslistDetail(@RequestBody String param, @AuthenticationPrincipal CustomUserDetails user) throws Exception {
        return bidProgressService.progresslistDetail(param, user);
    }

    @PostMapping("/bidNotice")
    public ResultBody bidNotice(@RequestBody Map<String, Object> params) {
    	ResultBody resultBody = new ResultBody();
    	try {
    		bidProgressService.bidNotice(params);
        } catch (Exception e) {
            e.printStackTrace();
            resultBody.setCode("error");
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
            e.printStackTrace();
            resultBody.setCode("error");
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
            e.printStackTrace();
            resultBody.setCode("error");
            }
        return resultBody;
    }

    @PostMapping("/pastBidList")
    public ResultBody pastBidList(@RequestBody Map<String, Object> params) throws Exception {
        return bidProgressService.pastBidList(params);
    }
    @PostMapping("/progressCodeList")
    public ResultBody progressCodeList() throws Exception {
        return bidProgressService.progressCodeList();
    }

}
