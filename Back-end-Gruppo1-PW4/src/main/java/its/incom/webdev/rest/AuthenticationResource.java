package its.incom.webdev.rest;

import its.incom.webdev.rest.model.CreateUserRequest;
import its.incom.webdev.persistence.model.User;
import its.incom.webdev.persistence.repository.UserRepository;
import its.incom.webdev.service.*;
import its.incom.webdev.service.exception.ExistingSessionException;
import its.incom.webdev.service.exception.SessionCreationException;
import its.incom.webdev.service.exception.WrongUsernameOrPasswordException;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

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
    private final PhoneService phoneService;

    @Inject
    Mailer mailer;

    @Inject
    public AuthenticationResource(UserRepository userRepository, AuthenticationService authenticationService, UserService userService, HashCalculator hashCalculator, EmailService emailService, PhoneService phoneService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.hashCalculator = hashCalculator;
        this.emailService = emailService;
        this.phoneService = phoneService;
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(JsonObject loginRequest) {
        String email = loginRequest.containsKey("email") ? loginRequest.getString("email") : null;
        String password = loginRequest.getString("password");

        try {
            // fetch user by email or phone number
            Optional<User> userOpt;
            if (email != null) {
                userOpt = userRepository.findByEmail(email);
            } else {
                String phoneNumber = loginRequest.getString("number");
                userOpt = userRepository.findByNumber(phoneNumber);
            }

            // user exist?
            if (userOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Wrong username or password").build();
            }

            User user = userOpt.get();

            // verified?
            if (!user.isVerified()) {
                if (user.getEmail() != null) {
                    // email verify
                    String token = UUID.randomUUID().toString();
                    emailService.storeVerificationToken(user.getEmail(), token);
                    emailService.sendVerificationEmail(user.getEmail(), token);
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"message\": \"Email not verified. A verification email has been sent.\"}")
                            .build();
                } else if (user.getNumber() != null) {
                    // phone verify
                    phoneService.sendOtp(user.getNumber());
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"message\": \"Phone not verified. An OTP has been sent to your phone.\"}")
                            .build();
                } else {
                    // both null = error
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"message\": \"User has no email or phone number for verification.\"}")
                            .build();
                }
            }

            // login if verified
            String sessionId = authenticationService.login(email != null ? email : user.getNumber(), password);
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(CreateUserRequest cur) {
        // alr exist?
        Optional<User> existingUser = userRepository.findByEmail(cur.getEmail());
        if (existingUser.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"User already exists\"}")
                    .build();
        }

        // hash psw
        String hashedPassword = hashCalculator.calculateHash(cur.getPassword());

        User u = new User();
        u.setName(cur.getName());
        u.setSurname(cur.getSurname());
        u.setEmail(cur.getEmail());
        u.setPswHash(hashedPassword);
        u.setNumber(cur.getNumber());
        u.setAdmin(false);
        u.setVerified(false);

        // save user
        User u1 = userRepository.create(u);

        return Response.status(Response.Status.CREATED)
                .entity(u1)
                .build();
    }



    @DELETE
    @Path("/logout")
    public Response logout(@CookieParam("SESSION_ID") String sessionId) {
        try {
            authenticationService.delete(sessionId);
            NewCookie sessionCookie = new NewCookie("SESSION_ID", null, "/", null, null, 0, false);
            return Response.noContent().cookie(sessionCookie).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Session not found: " + e.getMessage())
                    .build();
        } catch (Exception e) {
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
                // set verified to true
                userRepository.updateVerified(email, true);

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

    @POST
    @Path("/verify-phone")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyPhone(JsonObject verificationRequest) {
        String phoneNumber = verificationRequest.getString("number");
        String otp = verificationRequest.getString("otp");

        try {
            // validate the OTP
            boolean isOtpValid = phoneService.validateOtp(phoneNumber, otp);
            if (!isOtpValid) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Invalid or expired OTP\"}")
                        .build();
            }

            // find by phone
            Optional<User> userOpt = userRepository.findByNumber(phoneNumber);
            if (userOpt.isPresent()) {
                String email = userOpt.get().getEmail();
                System.out.println("Updating verified status for phone number: " + phoneNumber);

                userRepository.updateVerifiedWithPhone(phoneNumber, true);

                // did update suceed?
                Optional<User> updatedUser = userRepository.findByNumber(phoneNumber);
                if (updatedUser.isPresent() && updatedUser.get().isVerified()) {
                    return Response.ok("{\"message\": \"Phone verified successfully\"}").build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"message\": \"Failed to update verified status\"}")
                            .build();
                }
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"User not found\"}")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error verifying phone: " + e.getMessage() + "\"}")
                    .build();
        }
    }


    @POST
    @Path("/send-otp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendOtp(JsonObject request) {
        String phoneNumber = request.getString("number");

        try {
            phoneService.sendOtp(phoneNumber);
            return Response.ok("{\"message\": \"OTP sent successfully\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error sending OTP: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
