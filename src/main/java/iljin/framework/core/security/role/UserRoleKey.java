package iljin.framework.core.security.role;

import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;

@Data
public class UserRoleKey implements Serializable {
    @Column(name = "id")
    Long id;
    @Column(name = "comp_cd")
    String compCd;
}
