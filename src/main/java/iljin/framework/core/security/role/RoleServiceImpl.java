package iljin.framework.core.security.role;

import iljin.framework.core.security.user.UserDto;
import iljin.framework.core.security.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService{

    private final UserRoleRepository roleRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoleServiceImpl(UserRoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UserRole> getRole(RoleType role) {
        return roleRepository.findByRole(role);
    }

    @Override
    public List<UserRole> getAll() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<UserRole> getRoleById(UserRoleDto userRoleDto) {
        UserRoleKey userRoleKey = new UserRoleKey();
        userRoleKey.setId(userRoleDto.getId());
        userRoleKey.setCompCd(userRoleDto.getCompCd());
        return roleRepository.findRoleById(userRoleKey);
    }

    @Override
    public List<UserRole> getRolesByLoginIdContains(String loginId) {
        return roleRepository.findRolesByUser_LoginId(loginId);
    }

    @Override
    public List<UserRole> getRolesByUser_UserName(String userName) {
        return roleRepository.findRolesByUser_UserName(userName);
    }

    @Override
    public void deleteRoleById(UserRoleDto userRoleDto) {
        UserRoleKey userRoleKey = new UserRoleKey();
        userRoleKey.setId(userRoleDto.getId());
        userRoleKey.setCompCd(userRoleDto.getCompCd());
        roleRepository.deleteById(userRoleKey);
    }

    @Override
    public void addRole(UserDto dto) {
        List<UserRole> newRoles = new ArrayList<>();

        userRepository.findById(dto.getId()).ifPresent(
                c -> c.setRoles(newRoles)
        );
    }
}
