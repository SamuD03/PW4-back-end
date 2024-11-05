package its.incom.webdev.rest;

import its.incom.webdev.rest.model.CreateUserResponse;
import its.incom.webdev.service.AuthenticationService;
import its.incom.webdev.service.UserService;
import its.incom.webdev.service.exception.WrongUsernameOrPasswordException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.*;

import java.sql.SQLException;

@Path("/utente")
public class UserResource {
    private final AuthenticationService authenticationService;

    private final UserService userService;

    public UserResource(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @GET
    @Path("/profile")
    public CreateUserResponse getProfile(@CookieParam("SESSION_ID") @DefaultValue("") String sessionId) throws WrongUsernameOrPasswordException, SQLException {
        if (sessionId.isEmpty()) {
            //eccezione personalizzata notLogged
            throw new WrongUsernameOrPasswordException();
        }
        return authenticationService.getProfile(sessionId);
    }

}
