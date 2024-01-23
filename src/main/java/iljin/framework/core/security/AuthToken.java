package iljin.framework.core.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuthToken {
    private String userName;
    private String loginId;
    private String loginCompCd;
    private String loginCompNm;
    private String loginDeptCd;
    private String loginDeptNm;
    private String loginJobCd;
    private String loginJobNm;
    private String loginDutCd;
    private String loginDutNm;
    private String attribute2;
    private String token;
    private Collection authorities;
}
