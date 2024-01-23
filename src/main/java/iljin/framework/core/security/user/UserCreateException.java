package iljin.framework.core.security.user;

class UserCreateException extends UserException {

    private static final long serialVersionUID = 2530141110754453084L;

    UserCreateException() {
        super("사용자 생성이 실패했습니다.");
    }

    UserCreateException(String msg) {
        super(msg);
    }
}
