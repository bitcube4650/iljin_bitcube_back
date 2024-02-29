package iljin.framework.ebid.etc.notice.entity;

import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import iljin.framework.ebid.custom.entity.TCoInterrelated;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Entity
@NoArgsConstructor
@IdClass(TCoBoardCustID.class)
@Table(name = "t_co_board_cust")
public class TCoBoardCustCode {

	@Id
    @Column(name = "b_no")
    Integer bNo;

    @Id
    @Column(name = "interrelated_cust_code")
    String interrelatedCustCode;
    
    @ManyToOne
    @JoinColumn(name = "interrelated_cust_code", insertable = false, updatable = false)
    private TCoInterrelated interrelated;


}
