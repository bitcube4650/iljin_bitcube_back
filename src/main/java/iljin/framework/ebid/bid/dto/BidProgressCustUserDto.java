package iljin.framework.ebid.bid.dto;

import lombok.Data;

@Data
public class BidProgressCustUserDto {
    String custCode;
    String userId;
    String userName;


    public BidProgressCustUserDto( String custCode, String userId, String userName){
        this.custCode = custCode;
        this.userId = userId;
        this.userName = userName;

    }
}
