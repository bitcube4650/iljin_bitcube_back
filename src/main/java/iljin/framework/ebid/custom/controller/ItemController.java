package iljin.framework.ebid.custom.controller;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.CustomUserDetails;
import iljin.framework.ebid.custom.entity.TCoItem;
import iljin.framework.ebid.custom.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/item")
@CrossOrigin
public class ItemController {

    @Autowired
    private ItemService itemService;

    //품목 그룹 조회
    @PostMapping("/itemGrpList")
    public ResultBody itemGrpList() throws Exception {
        return itemService.itemGrpList();
    }

    //품목 목록 조회
    @PostMapping("/itemList")
    public ResultBody itemList(@RequestBody Map<String, Object> params) throws Exception {
        return itemService.itemList(params);
    }

    //품목 상세 조회
    @PostMapping("/{id}")
    public ResultBody findById(@PathVariable String id) throws Exception {
        return itemService.findById(id);
    }
    // 품목 수정
    @PostMapping("/saveUpdate")
    public ResultBody saveUpdate(@RequestBody Map<String, Object> params) {
        return itemService.saveUpdate(params);
    }
    // 품목 저장
    @PostMapping("/save")
    public ResultBody save(@RequestBody Map<String, Object> params, @AuthenticationPrincipal CustomUserDetails user) {
        return itemService.save(params, user);
    }

}
