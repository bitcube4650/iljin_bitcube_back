package iljin.framework.ebid.custom.dto;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class TCoItemDto {
	String itemCode;
	String itemName;
	String grpNm;
	String useYn;
	String createUser;
	String createDate;

	public TCoItemDto(String itemCode, String itemName, String grpNm, String useYn, String createUser, String createDate) {
		this.itemCode = itemCode;
		this.itemName = itemName;
		this.grpNm = grpNm;
		this.useYn = useYn;
		this.useYn = useYn;
		this.createUser = createUser;
		this.createDate = createDate;
	}
}
