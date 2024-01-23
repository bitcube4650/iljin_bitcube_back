package iljin.framework.ijeas.sm.code;

import java.io.Serializable;

import lombok.Data;

@Data
public class CodeDetailKey implements Serializable {
	
	private static final long serialVersionUID = -3768347534683106911L;
	
	String groupCd;
	String detailCd;
	String compCd;
	
}
