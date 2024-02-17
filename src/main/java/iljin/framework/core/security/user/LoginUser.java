package iljin.framework.core.security.user;

import iljin.framework.core.security.role.UserRole;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LoginUser implements Serializable {
    String custType;
    String custCode;
    String custName;
    String userId;
    String userPwd;
    String userName;
    String userAuth;
}
