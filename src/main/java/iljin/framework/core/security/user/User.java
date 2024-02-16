package iljin.framework.core.security.user;

import iljin.framework.core.security.role.UserRole;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "t_co_cust_user")
@Data
public class User {

    @Id
    @Column(name = "user_id")
    String loginId;

    @Column(name = "user_pwd")
    @NotEmpty
    @ToString.Exclude
    String loginPw;

    @OneToMany
    @JoinColumn(name = "user_id")
    List<UserRole> roles;

    @Column(name = "user_name")
    String userName;
    @Column(name = "create_user")
    String createUser;
    @Column(name = "create_date", insertable = false, updatable = false)
    LocalDateTime createDate;
    @Column(name = "update_user")
    String updateUser;
    @Column(name = "update_date", insertable = false, updatable = false)
    LocalDateTime updateDate;
}
