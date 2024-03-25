package iljin.framework.ebid.custom.controller;

import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.custom.dto.TCoCustUserDto;
import iljin.framework.ebid.custom.dto.TCoUserDto;
import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
import iljin.framework.ebid.custom.service.CustUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/custuser")
@CrossOrigin
public class CustUserController {

    @Autowired
    private CustUserService custUserService;


    @PostMapping("/userList")
    public Page userList(@RequestBody Map<String, Object> params) {
        return custUserService.userList(params);
    }
    @PostMapping("/userListForCust")
    public Page userListForCust(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) {
        params.put("custCode", user.getCustCode());
        return custUserService.userList(params);
    }
    @PostMapping("/{id}")
    public TCoCustUserDto detail(@PathVariable String id, @AuthenticationPrincipal CustomUserDetails user) {
        return custUserService.detail(user.getCustCode(), id);
    }
}
