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
    String userAuth;
    String interrelatedCustCode;

    public CoUserInfoDto(String userId, String userAuth, String interrelatedCustCode){
        this.userId = userId;
        this.userAuth = userAuth;
        this.interrelatedCustCode = interrelatedCustCode;
    }

}


