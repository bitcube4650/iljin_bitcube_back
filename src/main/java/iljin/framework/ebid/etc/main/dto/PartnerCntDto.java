package iljin.framework.ebid.etc.main.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class PartnerCntDto {
	
	private BigInteger request;
    private BigInteger approval;
    private BigInteger deletion;

    public PartnerCntDto(BigInteger request, BigInteger approval, BigInteger deletion) {
        this.request = request;
        this.approval = approval;
        this.deletion = deletion;
    }

	public PartnerCntDto() {
	}

}
