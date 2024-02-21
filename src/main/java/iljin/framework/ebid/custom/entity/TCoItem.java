package iljin.framework.ebid.custom.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import iljin.framework.core.security.user.User;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_co_item")
@Data
public class TCoItem {
	@Id
	@Column(name="item_code")
	String itemCode;
	@Column(name="item_name")
	String itemName;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="item_grp_cd", referencedColumnName="item_grp_cd")
	TCoItemGrp itemGrp;
	@Column(name="use_yn")
	String useYn;
	@ManyToOne
	@JoinColumn(name="create_user", referencedColumnName="user_id")
	TCoUser createUser;
	@CreationTimestamp
	@JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm", timezone="Asia/Seoul")
	@Column(name="create_date")
	LocalDateTime createDate;

}
