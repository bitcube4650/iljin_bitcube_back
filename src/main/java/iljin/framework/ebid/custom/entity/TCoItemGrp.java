package iljin.framework.ebid.custom.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_co_item_grp")
@Data
public class TCoItemGrp {
	@Id
	@Column(name="item_grp_cd")
	String itemGrpCd;
	@Column(name="grp_nm")
	String grpNm;
}
