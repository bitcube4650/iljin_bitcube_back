package iljin.framework.ebid.bid.dto;

import lombok.Data;

@Data
public class CurrDto {
    String codeVal;
    String codeName;

    public CurrDto(String codeVal, String codeName){
        this.codeVal = codeVal;
        this.codeName = codeName;
    }
}
