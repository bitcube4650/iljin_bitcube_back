package iljin.framework.ebid.custom.entity;

import iljin.framework.core.security.user.User;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_co_user")
@Data
public class TCoUser {
	@Id
	@Column(name="user_id")
	String userId;
	@Column(name="user_name")
	String userName;
}
