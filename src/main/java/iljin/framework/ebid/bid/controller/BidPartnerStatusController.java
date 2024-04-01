package iljin.framework.ebid.bid.controller;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.dto.CurrDto;
import iljin.framework.ebid.bid.service.BidPartnerStatusService;
import iljin.framework.ebid.bid.service.BidProgressService;
import iljin.framework.ebid.bid.service.BidStatusService;
import iljin.framework.ebid.custom.entity.TCoItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/bidPtStatus")
@CrossOrigin
public class BidPartnerStatusController {
    @Autowired
    private BidPartnerStatusService bidPartnerStatusService;

    @PostMapping("/statuslist")
    public Page statuslist(@RequestBody Map<String, Object> params) {
        return bidPartnerStatusService.statuslist(params);
    }

    //입찰공고확인 처리
    @PostMapping("/checkBid")
    public ResultBody checkBid(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) {
    	return bidPartnerStatusService.checkBid(params, user);
    }

    @PostMapping("/currlist")
    public List<CurrDto> currlist() {
        return bidPartnerStatusService.currlist();
    }
    
    //투찰
    @PostMapping("/bidSubmitting")
    public ResultBody bidSubmitting(
    								@RequestPart("data") String jsonData,
    								@RequestPart(value = "file1", required = false) MultipartFile file1, 
    								@RequestPart(value = "file2", required = false) MultipartFile file2,
    								@AuthenticationPrincipal CustomUserDetails user
    								) {
    	ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> params = null;
		try {
			params = mapper.readValue(jsonData, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
		} 
    	return bidPartnerStatusService.bidSubmitting(params, file1, file2, user);
    }
}
