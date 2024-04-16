package iljin.framework.ebid.bid.dto;

import lombok.Data;

@Data
public class BidCodeDto {
	    String colCode;
	    String codeVal;
	    String codeName;


	    public BidCodeDto(String colCode, String codeVal, String codeName){
	        this.colCode = colCode;
	        this.codeVal = codeVal;
	        this.codeName = codeName;
	    }
}
