package iljin.framework.ebid.bid.controller;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.service.BidProgressService;
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

import tradesign.crypto.provider.JeTS;  //test import

@RestController
@RequestMapping("/api/v1/bid")
@CrossOrigin

public class BidProgressController {

    @Autowired
    private BidProgressService bidProgressService;

    @PostMapping("/progresslist")
    public Page progresslist(@RequestBody Map<String, Object> params) {
        return bidProgressService.progresslist(params);
    }

    @PostMapping("/progresslistDetail")
    public List<List<?>> progresslistDetail(@RequestBody String param) {
        return bidProgressService.progresslistDetail(param);
    }

    @PostMapping("/openBid")
    public ResultBody save(@RequestBody Map<String, String> params) {
        return bidProgressService.openBid(params);
    }

    @PostMapping("/delete")
    public ResultBody delete(@RequestBody Map<String, String> params) {
        return bidProgressService.delete(params);
    }

    @PostMapping("/custList")
    public Page custList(@RequestBody Map<String, Object> params) {
        return bidProgressService.custList(params);
    }

    @PostMapping("/userList")
    public Page findCoUserInfo(@RequestBody Map<String, Object> params) {
        return bidProgressService.findCoUserInfo(params);
    }

    @PostMapping("/updateBid")
    public ResultBody updateBid(@RequestBody Map<String, Object> params) {
        System.out.println("@@@@@@@@@");
        System.out.println(params);
        return bidProgressService.updateBid(params);
    }

    @PostMapping("/updateBidCust")
    public ResultBody updateBidCust(@RequestBody List<Map<String, Object>> params) {
        return bidProgressService.updateBidCust(params);
    }

    @PostMapping("/updateBidItem")
    public ResultBody updateBidItem(@RequestBody List<Map<String, Object>> params) {
        System.out.println("itemParamOn");
        System.out.println(params);

        return bidProgressService.updateBidItem(params);
    }

    @PostMapping("/updateBidFile")
    public ResultBody updateBidFile(@RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("data") String jsonData) {
        System.out.println("itemParamOn");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> params = null;
        try {
            params = mapper.readValue(jsonData, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bidProgressService.updateBidFile(file, params);
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
    public ResultBody insertBid(@RequestBody Map<String, Object> params) {
        return bidProgressService.insertBid(params);
    }

    @PostMapping("/updateEmail")
    public void updateEmail(@RequestBody Map<String, String> params) {
    }

    @PostMapping("/pastBidList")
    public Page pastBidList(@RequestBody Map<String, Object> params) {
        return bidProgressService.pastBidList(params);
    }

}
