package iljin.framework.ebid.custom.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TCoUserDto {
	String userId;
	String userPwd;
	String userName;
	String userAuth;
	String userEmail;
	String userHp;
	String userPosition;
	String userTel;
	String createUser;
	LocalDateTime createDate;
	String updateUser;
	LocalDateTime updateDate;
	String pwdEditYn;
	LocalDateTime pwdEditDate;
	String pwdEditDateStr;
	String deptName;
	String openauth;
	String bidauth;
	String useYn;
	String interrelatedCustNm;
	String interrelatedCustCode;
	List userInterrelated; // 감사사용자의 경우 해당됨

	/**
	 * 목록 (/api/v1/couser/userList)
	 */
	public TCoUserDto(String userName, String userId, String userPosition, String deptName, String userTel, String userHp, String userAuth, String useYn, String interrelatedCustNm) {
		this.userName = userName;
		this.userId = userId;
		this.userPosition = userPosition;
		this.deptName = deptName;
		this.userTel = userTel;
		this.userHp = userHp;
		this.userAuth = userAuth;
		this.useYn = useYn;
		this.interrelatedCustNm = interrelatedCustNm;
	}
	/**
	 * 상세 (/api/v1/couser/detail)
	 */
	public TCoUserDto(String userId, String userName, String userPosition, String deptName, String userTel, String userHp, String userAuth, String useYn, String interrelatedCustCode, String openauth, String bidauth, String userEmail, String pwdEditDateStr, String interrelatedCustNm) {
		this.userId = userId;
		this.userName = userName;
		this.userPosition = userPosition;
		this.deptName = deptName;
		this.userTel = userTel;
		this.userHp = userHp;
		this.userAuth = userAuth;
		this.useYn = useYn;
		this.interrelatedCustCode = interrelatedCustCode;
		this.openauth = openauth;
		this.bidauth = bidauth;
		this.userEmail = userEmail;
		this.pwdEditDateStr = pwdEditDateStr;
		this.interrelatedCustNm = interrelatedCustNm;
	}

	/**
	 * ??
	 */
	public TCoUserDto(String userId, String userName, String userPosition, String deptName, String userTel, String userHp, String userAuth, String useYn, String interrelatedCustCode, String openauth, String userEmail, String userType) {
		this.userId = userId;
		this.userName = userName;
		this.userPosition = userPosition;
		this.deptName = deptName;
		this.userTel = userTel;
		this.userHp = userHp;
		this.userAuth = userAuth;
		this.useYn = useYn;
		this.interrelatedCustCode = interrelatedCustCode;
		this.openauth = openauth;
		this.userEmail = userEmail;
	}
}
