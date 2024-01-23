package iljin.framework.core.security.role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import iljin.framework.core.security.user.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "A_USER_ROLE")
@IdClass(UserRoleKey.class)
@Data
public class UserRole {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Id
    @Column(name = "comp_cd")
    String compCd;

    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
    @JsonIgnore
    User user;

//    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    String role;

    @Column(name = "created_by")
    Long createdBy;

    @Column(name = "creation_date", insertable = false, updatable = false)
    LocalDateTime creationDate;

    @Column(name = "modified_by")
    Long modifiedBy;

    @Column(name = "modified_date", insertable = false, updatable = false)
    LocalDateTime modifiedDate;

    public UserRole() {}
}
