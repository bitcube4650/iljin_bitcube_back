package iljin.framework.core.security.user;

class UserException extends RuntimeException {

    private static final long serialVersionUID = -306314042961146191L;

    UserException() {
        super();
    }

    UserException(String msg) {
        super(msg);
    }
}
