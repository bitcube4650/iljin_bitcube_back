package iljin.framework.ebid.bid.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data             
@NoArgsConstructor  
@AllArgsConstructor
public class TBiInfoMatCustID implements Serializable{

	private String biNo;
	private Integer custCode;
}
