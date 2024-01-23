package iljin.framework.core.security.user;

class UserNotFoundException extends UserException {

    private static final long serialVersionUID = -6641166745915213326L;

    UserNotFoundException() {
        super("사용자 목록이 없습니다.");
    }

    UserNotFoundException(String msg) {
        super(msg);
    }
}
