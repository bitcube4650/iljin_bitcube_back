package iljin.framework.ebid.custom.controller;

import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
import iljin.framework.ebid.custom.service.CustUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
}
