package iljin.framework.ebid.custom.controller;

import iljin.framework.core.dto.ResultBody;
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

    @Autowired
    private TCoCustMasterRepository tCoCustMasterRepository;


    @PostMapping("/approvalList")
    public Page approvalList(@RequestBody Map<String, Object> params) {
        int page = 0;
        int size = 10;
        if (params.get("page") != null) {
            page = (Integer) params.get("page");
        }
        if (params.get("size") != null) {
            size = Integer.parseInt((String) params.get("size"));
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "custName"));
        return tCoCustMasterRepository.findAll(pageable);
    }

    @PostMapping("/approval/{id}")
    public Optional<TCoCustMaster> approvalDetail(@PathVariable String id) {
        return tCoCustMasterRepository.findById(id);
    }

    @PostMapping("/management/{id}")
    public Optional<TCoCustMaster> management(@PathVariable String id) {
        return tCoCustMasterRepository.findById(id);
    }
    @PostMapping("/save")
    public ResultBody save(@RequestBody Map<String, String> params) {
        return custService.save(params);
    }
    @PostMapping("/delete")
    public ResultBody delete(@RequestBody Map<String, String> params) {
        return custService.delete(params);
    }
}
