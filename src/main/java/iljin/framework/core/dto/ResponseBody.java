package iljin.framework.core.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResponseBody implements Serializable {
    private String code;
    private String msg;
    private int status;
    private Object data;
    public ResponseBody() {
        this.code = "OK";
        this.status = 200;
        this.msg = "";
    }
}
