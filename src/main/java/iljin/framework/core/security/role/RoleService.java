package iljin.framework.core.security.role;

import iljin.framework.core.security.user.UserDto;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    Optional<UserRole> getRole(RoleType role);
    List<UserRole> getAll();
    Optional<UserRole> getRoleById(UserRoleDto userRoleDto);
    List<UserRole> getRolesByLoginIdContains(String loginId);
    List<UserRole> getRolesByUser_UserName(String userName);
    void deleteRoleById(UserRoleDto userRoleDto);
    void addRole(UserDto dto);

}