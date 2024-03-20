package iljin.framework.ebid.custom.controller;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.custom.dto.TCoCustMasterDto;
import iljin.framework.ebid.custom.entity.TCoCustMaster;
import iljin.framework.ebid.custom.repository.TCoCustMasterRepository;
import iljin.framework.ebid.custom.service.CustService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cust")
@CrossOrigin
public class CustController {
    @Autowired
    private CustService custService;

    @PostMapping("/approvalList")
    public Page approvalList(@RequestBody Map<String, Object> params) {
        params.put("certYn", "N");
        return custService.custList(params);
    }

    @PostMapping("/custList")
    public Page custList(@RequestBody Map<String, Object> params) {
        return custService.custList(params);
    }
    @PostMapping("/otherCustList")
    public Page otherCustList(@RequestBody Map<String, Object> params) {
        return custService.otherCustList(params);
    }

    @PostMapping("/approval/{id}")
    public TCoCustMasterDto approvalDetail(@PathVariable String id) {
        return custService.custDetail(id);
    }

    @PostMapping("/management/{id}")
    public TCoCustMasterDto management(@PathVariable String id) {
        return custService.custDetail(id);
    }

    @PostMapping("/approval")
    public ResultBody approval(@RequestBody Map<String, Object> params) {
        return custService.approval(params);
    }

    @PostMapping("/del")
    public ResultBody back(@RequestBody Map<String, Object> params) {
        return custService.del(params);
    }

    @PostMapping("/idcheck")
    public ResultBody idcheck(@RequestBody Map<String, Object> params) {
        return custService.approval(params);
    }
    @PostMapping("/save")
    public ResultBody save(@RequestBody Map<String, Object> params) {
        if (params.get("custCode") == null) {
            return custService.insert(params);
        } else {
            return custService.update(params);
        }
    }
}
