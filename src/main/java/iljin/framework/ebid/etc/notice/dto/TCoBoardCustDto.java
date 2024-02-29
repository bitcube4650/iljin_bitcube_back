package iljin.framework.ebid.etc.notice.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class TCoBoardCustDto {

	private Integer bNo;
	private String interrelatedCustCode;
	private String interrelatedNm;
	
	public TCoBoardCustDto() {}
	
	/**
	 * 목록 (/api/v1/notice/selectGroupList)
	 */
	public TCoBoardCustDto(
            Integer bNo, 
            String interrelatedCustCode, 
            String interrelatedNm
    ) {
        this.bNo = bNo;
        this.interrelatedCustCode = interrelatedCustCode;
        this.interrelatedNm = interrelatedNm;
    }
}
