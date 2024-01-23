package iljin.framework.core.security.role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "a_role")
@Data
@IdClass(RoleKey.class)
public class Role {

    @Id
    @Column(name = "comp_cd")
    private String compCd;

    @Id
    @Column(name = "role_cd")
    private String roleCd;

    @Column(name = "role_nm")
    private String roleNm;

    @Column(name = "role_select_cd")
    private String roleSelectCd;

    @Column(name = "role_dc")
    @JsonIgnore
    private String roleDc;

}
