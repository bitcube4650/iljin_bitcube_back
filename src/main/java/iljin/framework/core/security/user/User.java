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
@Table(name = "A_USER")
@Data
public class User {

    @Id
    @Column(name ="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "session_p_id")
    String sessionPId;

    @Column(name = "login_id")
    String loginId;

    @Column(name = "login_pw")
    @NotEmpty
    @ToString.Exclude
    String loginPw;

    @Column(name = "comp_cd")
    String compCd;

    @Column(name = "dept_cd")
    String deptCd;

    @Column(name = "dept_id")
    String deptId;

    @Column(name = "employee_no")
    String employeeNo;

    @OneToMany
    @JoinColumn(name = "user_id")
    List<UserRole> roles;

    @Column(name = "user_name")
    String userName;

    @Column(name = "enable_flag")
    boolean enableFlag;

    @Column(name = "attribute_1")
    String attribute1;

    @Column(name = "attribute_2")
    String attribute2;

    @Column(name = "attribute_3")
    String attribute3;

    @Column(name = "attribute_4")
    String attribute4;

    @Column(name = "attribute_5")
    String attribute5;

    @Column(name = "created_by")
    Long createdBy;

    @Column(name = "creation_date", insertable = false, updatable = false)
    LocalDateTime creationDate;

    @Column(name = "modified_by")
    Long modifiedBy;

    @Column(name = "modified_date", insertable = false, updatable = false)
    LocalDateTime modifiedDate;

    public User() {
        /* JPA */
    }
}
