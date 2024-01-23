package iljin.framework.core.security.user;

import java.util.Optional;

public interface UserRepositoryCustom {
    Optional<UserDto> findByLoginId(String loginId);
}
