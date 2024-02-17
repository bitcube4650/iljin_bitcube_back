package iljin.framework.ebid.custom.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import iljin.framework.ijeas.sm.code.CodeDetail;
import iljin.framework.ijeas.sm.code.CodeHeaderKey;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "t_co_interrelated")
@Data
public class TCoInterrelated {
	@Id
	@Column(name="interrelated_cust_code")
	String interrelatedCustCode;
	@Column(name="interrelated_nm")
	String interrelatedNm;
}
