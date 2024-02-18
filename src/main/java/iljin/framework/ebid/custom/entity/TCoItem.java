package iljin.framework.ebid.custom.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_co_item")
@Data
public class TCoItem {
	@Id
	@Column(name="item_code")
	String itemCode;
	@Column(name="item_grp_cd")
	String itemGrpCd;
	@Column(name="item_name")
	String itemName;
}
