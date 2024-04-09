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
    private BigInteger ing;				// 입찰진행 전체 건수

    public BidCntDto(BigInteger planning, BigInteger noticing, BigInteger beforeOpening,BigInteger opening,
                     BigInteger completed, BigInteger unsuccessful, BigInteger ing) {
        this.planning = planning;
        this.noticing = noticing;
        this.beforeOpening = beforeOpening;
        this.opening = opening;
        this.completed = completed;
        this.unsuccessful = unsuccessful;
        this.ing = ing;
    }

	public BidCntDto() {
	}
}
