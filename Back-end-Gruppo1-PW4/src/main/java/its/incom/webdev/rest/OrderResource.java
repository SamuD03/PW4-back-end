package its.incom.webdev.rest;

import its.incom.webdev.service.EmailService;
import its.incom.webdev.service.OrderService;
import its.incom.webdev.service.SessionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

@Path("/orders")
public class OrderResource {

    @Inject
    OrderService orderService;

    @Inject
    SessionService sessionService;

    @Inject
    EmailService emailService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrder(Map<String, Object> orderData, @CookieParam("SESSION_ID") Cookie sessionCookie) {
        try {
            // Validate session cookie
            if (sessionCookie == null || sessionCookie.getValue().isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Session ID is missing").build();
            }
            String sessionId = sessionCookie.getValue();

            // id from session
            Integer userId = sessionService.findUserIdBySessionId(sessionId);
            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid session or user ID not found").build();
            }

            // retrieve password hash from session
            String pswHash = sessionService.findPswHashBySessionId(sessionId); // Make sure to implement this method in your SessionService

            // he verified?
            if (!emailService.isEmailVerified(Integer.valueOf(String.valueOf(userId)))) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"message\": \"Email not verified. Please verify your email to place an order.\"}")
                        .build();
            }

            // validate and extract order data
            if (orderData == null || !orderData.containsKey("content") || !orderData.containsKey("pickup")) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing order details").build();
            }

            // extract content and validate
            List<Map<String, Object>> content;
            try {
                content = (List<Map<String, Object>>) orderData.get("content");
                if (content.isEmpty()) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Content cannot be empty").build();
                }
            } catch (ClassCastException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid content format").build();
            }

            // extract optional comment
            String comment = (String) orderData.getOrDefault("comment", "");

            // parse and validate pickup time
            String pickupTimeStr = (String) orderData.get("pickup");
            LocalDateTime pickupTime;
            try {
                pickupTime = LocalDateTime.parse(pickupTimeStr);
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid pickup time format").build();
            }

            // slot avaliable?
            if (!isTimeWithinAvailableSlots(pickupTime)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Pickup time is outside of available time slots.\"}")
                        .build();
            }

            // attempt to create
            boolean success = orderService.bookOrder(userId, pswHash, content, pickupTime, comment);

            if (success) {
                return Response.ok("{\"message\": \"Order created successfully\"}").build();
            } else {
                return Response.status(Response.Status.CONFLICT).entity("{\"message\": \"Time slot is already booked\"}").build();
            }
        } catch (Exception e) {
            // handle unexpected errors
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Unexpected error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // helper method to check if the pickup time falls within available slots
    private boolean isTimeWithinAvailableSlots(LocalDateTime pickupTime) {
        DayOfWeek day = pickupTime.getDayOfWeek();
        LocalTime time = pickupTime.toLocalTime();

        switch (day) {
            case MONDAY:
                return false; // Closed on Monday
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                // tuesday to friday: 08:00 - 13:00 and 14:30 - 19:00
                return (time.isAfter(LocalTime.of(7, 59)) && time.isBefore(LocalTime.of(13, 1))) ||
                        (time.isAfter(LocalTime.of(14, 29)) && time.isBefore(LocalTime.of(19, 1)));
            case SATURDAY:
            case SUNDAY:
                // saturday and sunday: 14:30 - 19:00
                return time.isAfter(LocalTime.of(14, 29)) && time.isBefore(LocalTime.of(19, 1));
            default:
                return false;
        }
    }
}
