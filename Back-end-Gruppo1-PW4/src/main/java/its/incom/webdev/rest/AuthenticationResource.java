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
        String phoneNumber = loginRequest.containsKey("number") ? loginRequest.getString("number") : null;

        try {
            // Fetch user by email or phone number
            Optional<User> userOpt = userRepository.findByEmailOrNumber(email, phoneNumber);
            if (userOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Wrong username or password")
                        .build();
            }

            User user = userOpt.get();

            // Check if the user is verified
            if (!user.isVerified()) {
                String token = UUID.randomUUID().toString();

                if (user.getEmail() != null) {
                    // Send verification email
                    emailService.storeVerificationToken(user.getEmail(), token);
                    emailService.sendVerificationEmail(user.getEmail(), token);
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"message\": \"Email not verified. A verification email has been sent.\"}")
                            .build();
                } else if (user.getNumber() != null) {
                    // Generate and send OTP to the user's phone number
                    String otp = phoneService.generateOtp(); // Method to generate a random 6-digit OTP
                    System.out.println("Sending OTP to phone number: " + user.getNumber());
                    phoneService.sendOtp(user.getNumber(), "Your OTP is: " + otp); // Pass the OTP message

                    // Store the OTP in a secure place for later validation (e.g., in a cache or database)
                    phoneService.storeOtpForValidation(user.getNumber(), otp); // Implement this method

                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"message\": \"Phone not verified. An OTP has been sent to your phone.\"}")
                            .build();
                } else {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"message\": \"User has no email or phone number for verification.\"}")
                            .build();
                }
            }

            // If verified, proceed to login
            String sessionId = authenticationService.login(email, password, phoneNumber);
            NewCookie sessionCookie = new NewCookie("SESSION_ID", sessionId, "/", null, null, NewCookie.DEFAULT_MAX_AGE, false);

            return Response.ok()
                    .cookie(sessionCookie)
                    .entity("{\"message\": \"Login successful\"}")
                    .build();
        } catch (WrongUsernameOrPasswordException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Wrong username or password")
                    .build();
        } catch (ExistingSessionException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (SessionCreationException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating session")
                    .build();
        }
    }


    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(CreateUserRequest cur) {
        // Check if user already exists by email or phone number
        Optional<User> existingUser = userRepository.findByEmailOrNumber(cur.getEmail(), cur.getNumber());
        if (existingUser.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"User already exists\"}")
                    .build();
        }

        // Hash the password
        String hashedPassword = hashCalculator.calculateHash(cur.getPassword());

        User u = new User();
        u.setName(cur.getName());
        u.setSurname(cur.getSurname());
        u.setEmail(cur.getEmail());
        u.setPswHash(hashedPassword);
        u.setNumber(cur.getNumber());
        u.setAdmin(false);
        u.setVerified(false);

        // Save user
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
            // Use findByEmailOrNumber to search for the user
            Optional<User> userOpt = userRepository.findByEmailOrNumber(email, null);
            if (userOpt.isPresent()) {
                // Set verified to true
                userRepository.updateVerified(email, true);

                // Delete the verification token
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
            // Validate the OTP
            boolean isOtpValid = phoneService.validateOtp(phoneNumber, otp);
            if (!isOtpValid) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Invalid or expired OTP\"}")
                        .build();
            }

            // Find the user by phone number
            Optional<User> userOpt = userRepository.findByEmailOrNumber(null, phoneNumber);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("Updating verified status for phone number: " + phoneNumber);

                // Update verified status for the phone number
                userRepository.updateVerifiedWithPhone(phoneNumber, true);

                // Verify that the update succeeded
                Optional<User> updatedUser = userRepository.findByEmailOrNumber(null, phoneNumber);
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
    @Path("/send-phone-otp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendPhoneOtp(JsonObject request) {
        String phoneNumber = request.getString("number");

        try {
            // Fetch user by phone number
            Optional<User> userOpt = userRepository.findNumber(phoneNumber);
            if (userOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"User not found\"}")
                        .build();
            }

            User user = userOpt.get();

            // Check if the user is verified
            if (!user.isVerified()) {
                // Generate and send OTP to the user's phone number
                String otp = phoneService.generateOtp(); // Method to generate a random 6-digit OTP
                System.out.println("Sending OTP to phone number: " + user.getNumber());
                phoneService.sendOtp(user.getNumber(), "Your OTP is: " + otp); // Pass the OTP message

                // Store the OTP in a secure place for later validation (e.g., in a cache or database)
                phoneService.storeOtpForValidation(user.getNumber(), otp); // Implement this method

                return Response.status(Response.Status.OK)
                        .entity("{\"message\": \"An OTP has been sent to your phone.\"}")
                        .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"User is already verified.\"}")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error sending OTP: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
