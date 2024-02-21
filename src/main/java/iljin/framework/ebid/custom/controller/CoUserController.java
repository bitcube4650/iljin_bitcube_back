package iljin.framework.ebid.custom.controller;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.ebid.custom.dto.TCoUserDto;
import iljin.framework.ebid.custom.entity.TCoItem;
import iljin.framework.ebid.custom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/couser")
@CrossOrigin
public class CoUserController {

    @Autowired
    private UserService userService;

    @PostMapping("/interrelatedList")
    public List interrelatedList() {
        return userService.interrelatedList();
    }

    @PostMapping("/userList")
    public Page userList(@RequestBody Map<String, Object> params) {
        return userService.userList(params);
    }

    @PostMapping("/{id}")
    public TCoUserDto detail(@PathVariable String id) {
        return userService.detail(id);
    }

    @PostMapping("/save")
    public ResultBody save(@RequestBody Map<String, Object> params) {
        return userService.save(params);
    }
}
