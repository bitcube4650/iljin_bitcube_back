package iljin.framework.core.security.role;

import iljin.framework.core.security.user.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping(name = "/api/v1/role")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping(value = "/")
    public List<UserRole> getAll() {
        return null;
    }

    @PostMapping(value = "/getRole")
    public Optional<UserRole> getRoleById(@RequestBody UserRoleDto userRoleDto) {
        return roleService.getRoleById(userRoleDto);
    }

    @GetMapping(value = "/role/{role}")
    public Optional<UserRole> getRole(@PathVariable RoleType role) {
        return roleService.getRole(role);
    }

    @GetMapping(value = "/login-id/{loginId}")
    public List<UserRole> getRolesByLoginIdContains(@PathVariable String loginId){
        return roleService.getRolesByLoginIdContains(loginId);
    }

    @GetMapping(value = "/user-name/{userName}")
    public List<UserRole> getRolesByUser_UserName(@PathVariable String userName) {
        return roleService.getRolesByUser_UserName(userName);
    }

    @PostMapping(value = "/deleteRole")
    public void updateRoleById(@RequestBody UserRoleDto userRoleDto) {
        roleService.deleteRoleById(userRoleDto);
    }

    @PostMapping(value = "/")
    public void addRole(@RequestBody UserDto dto) {
        roleService.addRole(dto);
    }
}
