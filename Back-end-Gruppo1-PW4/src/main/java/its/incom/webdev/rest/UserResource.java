package its.incom.webdev.rest;

import its.incom.webdev.persistence.model.User;
import its.incom.webdev.persistence.repository.UserRepository;
import its.incom.webdev.rest.model.CreateUserResponse;
import its.incom.webdev.service.AuthenticationService;
import its.incom.webdev.service.SessionService;
import its.incom.webdev.service.UserService;
import its.incom.webdev.service.exception.WrongUsernameOrPasswordException;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.Optional;


@Path("/user")
public class UserResource {


    @Inject
    private SessionService sessionService;

    @Inject
    private UserRepository userRepository;

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Inject
    public UserResource(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }
    @GET
    @Path("/profile")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserProfile(@CookieParam("SESSION_ID") Cookie sessionCookie) {
        try {
            // check if the session cookie is present
            if (sessionCookie == null || sessionCookie.getValue().isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Session ID is missing").build();
            }

            // retrieve the session ID from the cookie
            String sessionId = sessionCookie.getValue();

            // retrieve the user ID from the session using SessionService
            Integer userId = sessionService.findUserIdBySessionId(sessionId);
            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid session or user ID not found").build();
            }

            // fetch user information from the database
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }

            // return the user information
            User user = optionalUser.get();
            return Response.ok(user).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Unexpected error: " + e.getMessage() + "\"}")
                    .build();
        }
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

