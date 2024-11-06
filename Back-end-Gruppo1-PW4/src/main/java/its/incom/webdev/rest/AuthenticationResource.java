package its.incom.webdev.rest;

import its.incom.webdev.rest.model.CreateUserRequest;
import its.incom.webdev.persistence.model.User;
import its.incom.webdev.persistence.repository.UserRepository;
import its.incom.webdev.service.AuthenticationService;
import its.incom.webdev.service.EmailService;
import its.incom.webdev.service.HashCalculator;
import its.incom.webdev.service.UserService;
import its.incom.webdev.service.exception.ExistingSessionException;
import its.incom.webdev.service.exception.SessionCreationException;
import its.incom.webdev.service.exception.WrongUsernameOrPasswordException;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import io.quarkus.mailer.Mailer;

@Path("/auth")
public class AuthenticationResource {
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final HashCalculator hashCalculator;
    private final EmailService emailService;

    @Inject
    Mailer mailer;

    @Inject
    public AuthenticationResource(UserRepository userRepository, AuthenticationService authenticationService, UserService userService, HashCalculator hashCalculator, EmailService emailService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.hashCalculator = hashCalculator;
        this.emailService = emailService;
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(JsonObject loginRequest) {
        String email = loginRequest.getString("email");
        String password = loginRequest.getString("password");

        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            // Check if user exists
            if (userOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Wrong username or password").build();
            }

            User user = userOpt.get();

            // Check if email is verified
            if (!user.isEmailVerified()) {
                // Generate and send a verification token
                String token = UUID.randomUUID().toString();
                emailService.storeVerificationToken(email, token);
                emailService.sendVerificationEmail(email, token);

                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Email not verified. A verification email has been sent.\"}")
                        .build();
            }

            // Proceed with login if email is verified
            String sessionId = authenticationService.login(email, password);
            NewCookie sessionCookie = new NewCookie("SESSION_ID", sessionId, "/", null, null, NewCookie.DEFAULT_MAX_AGE, false);

            return Response.ok().cookie(sessionCookie).entity("{\"message\": \"Login successful\"}").build();
        } catch (WrongUsernameOrPasswordException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Wrong username or password").build();
        } catch (ExistingSessionException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (SessionCreationException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating session").build();
        }
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(CreateUserRequest cur) {
        try {
            // Controlla se esiste già un utente con la stessa email
            Optional<User> existingUser = userRepository.findByEmail(cur.getEmail());
            if (existingUser.isPresent()) {
                // Se esiste, restituisci un messaggio JSON appropriato
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Utente già presente\"}")
                        .build();
            }

            // Se non esiste, crea un nuovo utente
            User u = userService.ConvertRequestToUtente(cur);
            User u1 = userRepository.createUtente(u);
            return Response.status(Response.Status.CREATED)
                    .entity(u1)
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore del server, la registrazione non è andata a buon fine")
                    .build();
        }
    }

    @DELETE
    @Path("/logout")
    public Response logout(@CookieParam("SESSION_ID") String sessionId) {
        try {
            authenticationService.delete(sessionId);
            NewCookie sessionCookie = new NewCookie("SESSION_ID", null, "/", null, null, 0, false);
            return Response.noContent().cookie(sessionCookie).build();
        } catch (RuntimeException e) {
            // Restituisce 404 se la sessione non esiste
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Session not found: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            // Restituisce 500 in caso di errore generico
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error logging out: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/confirm")
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmEmail(@QueryParam("token") String token) {
        try {
            Optional<String> emailOpt = emailService.getEmailByVerificationToken(token);
            if (emailOpt.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Invalid or expired token\"}")
                        .build();
            }

            String email = emailOpt.get();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                // directly update emailVerified to true for the user
                userRepository.updateEmailVerified(email, true);

                emailService.deleteVerificationToken(token);

                return Response.ok("{\"message\": \"Email verified successfully\"}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"User not found\"}")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error verifying email: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}