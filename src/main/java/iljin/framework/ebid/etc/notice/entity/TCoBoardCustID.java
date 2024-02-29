package iljin.framework.ebid.etc.notice.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data             
@NoArgsConstructor  
@AllArgsConstructor
public class TCoBoardCustID implements Serializable{

	private Integer bNo;
	private String interrelatedCustCode;
	


}
