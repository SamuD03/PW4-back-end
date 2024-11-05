package its.incom.webdev.service.exception;

import java.sql.SQLException;

public class SessionCreationException extends Exception{
    public SessionCreationException(SQLException e) {
        super(e);
    }
}
