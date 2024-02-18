package iljin.framework.ebid.custom.controller;

import iljin.framework.core.dto.ResponseBody;
import iljin.framework.ebid.custom.entity.TCoCustMaster;
import iljin.framework.ebid.custom.repository.TCoCustMasterRepository;
import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
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
@RequestMapping("/api/v1/custuser")
@CrossOrigin
public class CustUserController {

    @Autowired
    private TCoCustUserRepository tCoCustUserRepository;


    @PostMapping("/userList")
    public Page userList(@RequestBody Map<String, Object> params) {
        int page = 0;
        int size = 5;
        if (params.get("page") != null) {
            page = (Integer) params.get("page");
        }
        if (params.get("size") != null) {
            size = Integer.parseInt((String) params.get("size"));
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "userId"));
        return tCoCustUserRepository.findAllByCustCode((String) params.get("custCode"), pageable);
    }
}
