package iljin.framework.ebid.etc.statistics.dto;


import lombok.Data;

@Data
public class CoInterDto {

	private String interrelatedCustCode;
	private String interrelatedNm;

	
    public CoInterDto() {}
    
	/**
	 * 통계 계열사 리스트 (/api/v1/statistics/coInterList)
	 */

    public CoInterDto(
            String interrelatedCustCode,
            String interrelatedNm
    ) {
        this.interrelatedCustCode = interrelatedCustCode;
        this.interrelatedNm = interrelatedNm;
    }
}
