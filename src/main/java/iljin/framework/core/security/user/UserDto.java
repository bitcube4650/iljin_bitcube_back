package iljin.framework.core.security.user;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDto implements Serializable {
    private static final long serialVersionUID = -1255150713846954145L;
    String loginId;
    String loginPw;
    String custType;
    String custCode;
    String custName;
    String userName;
    String userAuth;
    String userHp;
    String token;

    public UserDto() {
    }

    /* UserServiceImpl.getAuthToken */
    public UserDto(String custType, String custCode, String custName, String userName, String loginId, String loginPw, String userAuth, String token) {
        this.custType = custType;
        this.custCode = custCode;
        this.custName = custName;
        this.loginId = loginId;
        this.loginPw = loginPw;
        this.userName = userName;
        this.userAuth = userAuth;
        this.token = token;
    }
    public UserDto(String userId, String userHp, String userName) {
        this.loginId = userId;
        this.userHp = userHp;
        this.userName = userName;
    }
}
