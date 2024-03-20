package iljin.framework.ebid.etc.main.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class PartnerCompletedBidCntDto {

	private BigInteger posted;
    private BigInteger submitted;
    private BigInteger awarded;
    
    public PartnerCompletedBidCntDto(BigInteger posted, BigInteger submitted, BigInteger awarded) {
    	this.posted = posted;
    	this.submitted = submitted;
    	this.awarded = awarded;
	}
	
	public PartnerCompletedBidCntDto() {
	}
}
