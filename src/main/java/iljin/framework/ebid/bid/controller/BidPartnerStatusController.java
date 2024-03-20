package iljin.framework.ebid.bid.controller;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.service.BidPartnerStatusService;
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
@RequestMapping("/api/v1/bidPtStatus")
@CrossOrigin
public class BidPartnerStatusController {
    @Autowired
    private BidPartnerStatusService bidPartnerStatusService;

    @PostMapping("/statuslist")
    public Page statuslist(@RequestBody Map<String, Object> params) {
        return bidPartnerStatusService.statuslist(params);
    }
}
