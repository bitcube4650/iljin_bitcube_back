package iljin.framework.core.file;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public
class FileNotFoundException extends RuntimeException{

    private static final long serialVersionUID = 6738738808772199984L;

    public FileNotFoundException(String msg) {
        super(msg);
    }

    public FileNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
