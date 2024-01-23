package iljin.framework.core.security.role;

import java.io.Serializable;

import lombok.Data;

@Data
public class RoleKey implements Serializable {

    private static final long serialVersionUID = 3281511803969286152L;

    private String compCd;

    private String roleCd;

}
