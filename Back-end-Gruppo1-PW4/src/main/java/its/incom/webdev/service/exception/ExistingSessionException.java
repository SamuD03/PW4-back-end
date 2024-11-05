package its.incom.webdev.service.exception;

public class ExistingSessionException extends Exception{
    public ExistingSessionException(String errorMessage){
        super(errorMessage);
    }
}
