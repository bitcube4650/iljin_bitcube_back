package iljin.framework.core.security.role;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class RoleDto {
    Long id;
    Long userId;
    String role;
    String roleType;
    Long createdBy;
}
