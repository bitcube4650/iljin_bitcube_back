package iljin.framework.ebid.custom.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TCoCustUserDto {
	String userId;
	String userName;
	String userBuseo;
	String userPosition;
	String userEmail;
	String userTel;
	String userHp;
	String userType;
	String useYn;

	/**
	 * 목록 (/api/v1/cocustuser/userList)
	 */
	public TCoCustUserDto(String userName, String userId, String userBuseo, String userPosition, String userEmail, String userTel, String userHp, String userType, String useYn) {
		this.userName = userName;
		this.userId = userId;
		this.userBuseo = userBuseo;
		this.userPosition = userPosition;
		this.userEmail = userEmail;
		this.userTel = userTel;
		this.userHp = userHp;
		this.userType = userType;
		this.useYn = useYn;
	}
}
