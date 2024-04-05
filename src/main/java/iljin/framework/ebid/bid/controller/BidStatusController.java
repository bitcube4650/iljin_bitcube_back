package iljin.framework.ebid.bid.controller;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.dto.ItemDto;
import iljin.framework.ebid.bid.service.BidProgressService;
import iljin.framework.ebid.bid.service.BidStatusService;
import iljin.framework.ebid.custom.entity.TCoItem;
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
		return bidStatusService.statuslist(params);
	}

	/**
	 * 입찰(개찰) 상세
	 * @param param
	 * @return
	 */
	@PostMapping("/statusDetail")
	public ResultBody progresslistDetail(@RequestBody Map<String, Object> param) {
		return bidStatusService.statusDetail(param);
	}
	
	/**
	 * 유찰처리
	 * @param params
	 * @return
	 */
	@PostMapping("/bidFailure")
	public ResultBody bidFailure(@RequestBody Map<String, String> params) {
		return bidStatusService.bidFailure(params);
	}

	/**
	 * 재입찰처리
	 * @param params
	 * @return
	 */
    @PostMapping("/rebid")
    public ResultBody rebid(@RequestBody Map<String, Object> params) {
        return bidStatusService.rebid(params);
    }

    
    @PostMapping("/submitHist")
    public Page submitHist(@RequestBody Map<String, Object> params) {
        return bidStatusService.submitHist(params);
    }

    @PostMapping("/rebidCust")
    public ResultBody rebidCust(@RequestBody List<Map<String, Object>> params) {
        return bidStatusService.rebidCust(params);
    }

    @PostMapping("/itemlist")
    public List<ItemDto> itemlist(@RequestBody Map<String, Object> params) {
        return bidStatusService.itemlist(params);
    }

    /**
     * 낙찰
     * @param params
     * @return
     */
    @PostMapping("/bidSucc")
    public ResultBody bidSucc(@RequestBody Map<String, Object> params) {
        return bidStatusService.bidSucc(params);
    }

    @PostMapping("/attSign")
    public ResultBody attSign(@RequestBody Map<String, Object> params) {
    	return bidStatusService.attSign(params);
    }
    
    /**
     * 개찰하기
     * @param params
     * @return
     */
    @PostMapping("/bidOpening")
    public ResultBody bidOpening(@RequestBody Map<String, String> params) {
        return bidStatusService.bidOpening(params);
    }
    
    
}
