package iljin.framework.ebid.etc.main.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class PartnerBidCntDto {

	private BigInteger noticing;
	private BigInteger submitted;
	private BigInteger confirmation;
	private BigInteger awarded;
	private BigInteger unsuccessful;
	private BigInteger ing;
	
	public PartnerBidCntDto(BigInteger noticing, BigInteger submitted, BigInteger confirmation, 
				     BigInteger awarded,BigInteger unsuccessful, BigInteger ing) {
		this.noticing = noticing;
		this.submitted = submitted;
		this.confirmation = confirmation;
		this.awarded = awarded;
		this.unsuccessful = unsuccessful;
		this.ing = ing;
	}
	
	public PartnerBidCntDto() {
	}
}
