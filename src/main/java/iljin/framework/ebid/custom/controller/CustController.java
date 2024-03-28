package iljin.framework.ebid.custom.controller;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.core.security.user.UserService;
import iljin.framework.ebid.custom.dto.TCoCustMasterDto;
import iljin.framework.ebid.custom.entity.TCoCustMaster;
import iljin.framework.ebid.custom.repository.TCoCustMasterRepository;
import iljin.framework.ebid.custom.service.CustService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cust")
@CrossOrigin
@Slf4j
public class CustController {

    @Autowired
    private CustService custService;
    @PostMapping("/approvalList")
    public Page approvalList(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) {
        params.put("interrelatedCustCode", user.getCustCode());
        params.put("certYn", "N");
        return custService.custList(params);
    }
    @PostMapping("/custList")
    public Page custList(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) {
        params.put("interrelatedCustCode", user.getCustCode());
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
        return custService.custDetailForInter(id);
    }
    @PostMapping("/info")
    public TCoCustMasterDto info(@AuthenticationPrincipal CustomUserDetails user) {
        return custService.custDetailForCust(user.getCustCode());
    }
    @PostMapping("/approval")
    public ResultBody approval(@RequestBody Map<String, Object> params) {
        return custService.approval(params);
    }
    @PostMapping("/back")
    public ResultBody back(@RequestBody Map<String, Object> params) {
        return custService.back(params);
    }
    @PostMapping("/del")
    public ResultBody del(@RequestBody Map<String, Object> params) {
        return custService.del(params);
    }
    @PostMapping("/leave")
    public ResultBody leave(@RequestBody Map<String, Object> params, HttpSession session) {
        session.invalidate();
        return custService.del(params);
    }
    @PostMapping("/pwdcheck")
    public ResultBody pwdcheck(@RequestBody Map<String, Object> params) {
        return custService.pwdcheck(params);
    }
    @PostMapping("/save")
    public ResultBody save(@RequestPart(value = "regnumFile", required = false) MultipartFile regnumFile, @RequestPart(value = "bFile", required = false) MultipartFile bFile, @RequestPart("data") Map<String, Object> params) {
        if (params.get("custCode") == null) {
            return custService.insert(params, regnumFile, bFile);
        } else {
            return custService.update(params, regnumFile, bFile);
        }
    }
}
