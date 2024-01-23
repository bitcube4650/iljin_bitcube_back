package iljin.framework.core.security.user;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UserDto implements Serializable {
    private static final long serialVersionUID = -1255150713846954145L;
    Long id;
    String loginCompCd;
    String loginCompNm;
    String loginDeptCd;
    String loginDeptNm;
    String loginJobCd;
    String loginJobNm;
    String loginDutCd;
    String loginDutNm;
    String loginId;
    String loginPw;
    String userName;
    String attribute1;
    String attribute2;
    String attribute3;
    String attribute4;
    String attribute5;
    boolean enableFlag;
    List<String> roles;
    String role;

    public UserDto(Long id,
                   String loginId,
                   String loginPw,
                   String userName,
                   boolean enableFlag,
                   String role,
                   List<String> roles) {
        this.id = id;
        this.loginId = loginId;
        this.loginPw = loginPw;
        this.userName = userName;
        this.enableFlag = enableFlag;
        this.roles = roles;
        this.role = role;
    }

    public UserDto() {
    }

    /* UserServiceImpl.getAuthToken */
    public UserDto(String loginCompCd, String loginCompNm, String loginDeptCd, String loginDeptNm, String loginJobCd, String loginJobNm, String loginDutCd, String loginDutNm, String loginId, String userName, boolean enableFlag, String attribute1, String attribute2, String attribute3, String attribute4, String attribute5) {
        this.loginCompCd = loginCompCd;
        this.loginCompNm = loginCompNm;
        this.loginDeptCd = loginDeptCd;
        this.loginDeptNm = loginDeptNm;
        this.loginJobCd = loginJobCd;
        this.loginJobNm = loginJobNm;
        this.loginDutCd = loginDutCd;
        this.loginDutNm = loginDutNm;
        this.loginId = loginId;
        this.userName = userName;
        this.enableFlag = enableFlag;
        this.attribute1 = attribute1;
        this.attribute2 = attribute2;
        this.attribute3 = attribute3;
        this.attribute4 = attribute4;
        this.attribute5 = attribute5;
    }

    /*
    * added on 26.08.2019
    * Login history
    * */
    //log_id
    Long logId;
    String connectId;
    String connectIp;
    //W:web, M:Mobile
    String connectMthd;
    //error msg
    String connectError;
    String connectUrl;
    LocalDateTime creationDate;

    /* 27.08.2019 */
    String compCd;
    String deptCd;

    String token;
}
