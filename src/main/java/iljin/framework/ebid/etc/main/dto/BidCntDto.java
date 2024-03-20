package iljin.framework.ebid.etc.main.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class BidCntDto {

	private BigInteger planning;
    private BigInteger noticing;
    private BigInteger beforeOpening;
    private BigInteger opening;
    private BigInteger completed;
    private BigInteger unsuccessful;

    public BidCntDto(BigInteger planning, BigInteger noticing, BigInteger beforeOpening,BigInteger opening,
                     BigInteger completed, BigInteger unsuccessful) {
        this.planning = planning;
        this.noticing = noticing;
        this.beforeOpening = beforeOpening;
        this.opening = opening;
        this.completed = completed;
        this.unsuccessful = unsuccessful;
    }

	public BidCntDto() {
	}
}
