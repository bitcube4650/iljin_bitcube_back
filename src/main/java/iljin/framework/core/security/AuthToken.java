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
    private String custType;
    private String custCode;
    private String custName;
    private String userId;
    private String userName;
    private String userAuth;
    private String token;
}
