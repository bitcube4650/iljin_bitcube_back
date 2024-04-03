package iljin.framework.ebid.bid.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import java.math.BigDecimal;

@Data
public class CoUserInfoDto {
    String userId;
    String userName;
    String deptName;
    String userAuth;
    String interrelatedCustCode;
    String openauth;
    String bidauth;

    public CoUserInfoDto(String userId, String userName, String deptName, String userAuth, 
                         String interrelatedCustCode, String openauth){
        this.userId = userId;
        this.userName = userName;
        this.deptName = deptName;
        this.userAuth = userAuth;
        this.interrelatedCustCode = interrelatedCustCode;
        this.openauth = openauth;
    }

	public CoUserInfoDto(String openauth, String bidauth){
		this.bidauth = bidauth;
		this.openauth = openauth;
	}
}


