package iljin.framework.ebid.etc.main.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class BidCntDto {

	private BigInteger planning;
    private BigInteger progress;
    private BigInteger opening;
    private BigInteger beforeOpening;
    private BigInteger beforeReopening;
    private BigInteger rebid;
    private BigInteger completed;
    private BigInteger unsuccessful;

    public BidCntDto(BigInteger planning, BigInteger progress, BigInteger opening, BigInteger beforeOpening,BigInteger beforeReopening,
                     BigInteger rebid, BigInteger completed, BigInteger unsuccessful) {
        this.planning = planning;
        this.progress = progress;
        this.opening = opening;
        this.beforeOpening = beforeOpening;
        this.beforeReopening = beforeReopening;
        this.rebid = rebid;
        this.completed = completed;
        this.unsuccessful = unsuccessful;
    }

	public BidCntDto() {
	}
}
