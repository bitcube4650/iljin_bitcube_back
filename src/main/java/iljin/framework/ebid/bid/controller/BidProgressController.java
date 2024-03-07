package iljin.framework.ebid.bid.controller;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.service.BidProgressService;
import iljin.framework.ebid.custom.entity.TCoItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public List<List<?>> progresslistDetail(@RequestBody String param){
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
    public Page custList(@RequestBody Map<String, Object> params){
        return bidProgressService.custList(params);
    }

    @PostMapping("/userList")
    public Page findCoUserInfo(@RequestBody Map<String, Object> params){
        return bidProgressService.findCoUserInfo(params);
    }

    @PostMapping("/updateBid")
    public ResultBody updateBid(@RequestBody Map<String, Object> params) {
        return bidProgressService.updateBid(params);
    }

}
