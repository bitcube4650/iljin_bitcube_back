package iljin.framework.ebid.bid.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BidCompleteSpecDto {
	String name;			//품목명
	String ssize;			//규격
	String unitcode;		//단위
	BigDecimal orderUc;		//단가
	BigDecimal orderQty;	//수량
	String custCode;		//협력사 코드
	BigDecimal esmtUc;		//협력사 견적금액
	
	public BidCompleteSpecDto(String name, String ssize, String unitcode, BigDecimal orderUc, BigDecimal orderQty) {
		this.name = name;
		this.ssize = ssize;
		this.unitcode = unitcode;
		this.orderUc = orderUc;
		this.orderQty = orderQty;
	}
	
	public BidCompleteSpecDto(String custCode, String name, String ssize, String unitcode, BigDecimal orderQty, BigDecimal esmtUc) {
		this.custCode = custCode;
		this.name = name;
		this.ssize = ssize;
		this.unitcode = unitcode;
		this.orderQty = orderQty;
		this.esmtUc = esmtUc;
	}
}
