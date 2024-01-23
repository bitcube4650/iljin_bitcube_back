package iljin.framework.ijeas.sm.code;

import java.io.Serializable;

import lombok.Data;

@Data
public class CodeHeaderKey implements Serializable {
	
	private static final long serialVersionUID = -2425465606766097169L;
	
	String groupCd;
	String compCd;
	
}
