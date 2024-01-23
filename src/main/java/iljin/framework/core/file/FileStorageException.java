package iljin.framework.core.file;

public class FileStorageException extends RuntimeException {

    private static final long serialVersionUID = 775349986243617575L;

    public FileStorageException(String msg) {
        super(msg);
    }

    public FileStorageException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
