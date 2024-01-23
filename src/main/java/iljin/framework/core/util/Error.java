package iljin.framework.core.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Error {

    private int code;
    private String message;

    public Error(int code, String message) {
        this.code = code;
        this.message = message;
    }

}