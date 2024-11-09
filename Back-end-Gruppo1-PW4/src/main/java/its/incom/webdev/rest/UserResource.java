package its.incom.webdev.rest;

import its.incom.webdev.rest.model.CreateUserResponse;
import its.incom.webdev.service.AuthenticationService;
import its.incom.webdev.service.UserService;
import its.incom.webdev.service.exception.WrongUsernameOrPasswordException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;

@Path("/user")
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
    @PUT
    @Path("/notifications")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateNotificationPreference(
            @CookieParam("SESSION_ID") @DefaultValue("") String sessionId,
            NotificationRequest notificationRequest) {

        if (sessionId.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Session not found. Please log in.").build();
        }

        boolean notification = notificationRequest.isNotification();
        try {
            boolean updated = userService.updateNotificationPreference(sessionId, notification);
            if (updated) {
                return Response.ok("Notification preference updated successfully.").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Cannot enable notifications without a valid email.").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error updating preference: " + e.getMessage()).build();
        }
    }

    public static class NotificationRequest {
        private boolean notification;

        public boolean isNotification() {
            return notification;
        }

        public void setNotification(boolean notification) {
            this.notification = notification;
        }
    }
}

