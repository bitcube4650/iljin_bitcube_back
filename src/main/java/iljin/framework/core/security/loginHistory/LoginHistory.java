package iljin.framework.core.security.loginHistory;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tb_user_login_history")
public class LoginHistory {
    @Id
    @Column(name = "log_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long logId;

    @Column(name = "connect_id")
    String connectId;

    @Column(name = "connect_ip")
    String connectIp;

    @Column(name = "connect_mthd")
    String connectMthd;

    @Column(name = "connect_error")
    String connectError;

    @Column(name = "connect_url")
    String connectUrl;

    @Column(name = "creation_date")
    LocalDateTime creationDate;
}
