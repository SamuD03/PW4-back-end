package its.incom.webdev.persistence.exception;

public class UserNotHavePermissionException extends Exception {
    public UserNotHavePermissionException(String message) {
        super(message);
    }
}
