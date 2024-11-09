package its.incom.webdev.rest;

import its.incom.webdev.persistence.model.Order;
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

            // retrieve the user ID from the session using SessionService
            Integer userId = sessionService.findUserIdBySessionId(sessionId);
            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid session or user ID not found").build();
            }

            String pswHash = sessionService.findPswHashBySessionId(sessionId);
            if (!emailService.isEmailVerified(userId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"message\": \"Email not verified. Please verify your email to place an order.\"}")
                        .build();
            }

            // Check for required order details
            if (orderData == null || !orderData.containsKey("content") || !orderData.containsKey("pickup")) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing order details").build();
            }

            // Parse and validate content and pickup time
            List<Map<String, Object>> content;
            try {
                content = (List<Map<String, Object>>) orderData.get("content");
                if (content.isEmpty()) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Content cannot be empty").build();
                }
            } catch (ClassCastException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid content format").build();
            }

            String comment = (String) orderData.getOrDefault("comment", "");
            String pickupTimeStr = (String) orderData.get("pickup");
            LocalDateTime pickupTime;
            try {
                pickupTime = LocalDateTime.parse(pickupTimeStr);
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid pickup time format").build();
            }

            // Check if the pickup time is within available slots
            if (!isTimeWithinAvailableSlots(pickupTime)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Pickup time is outside of available time slots.\"}")
                        .build();
            }

            // Use OrderService to create the order
            boolean success = orderService.bookOrder(userId, pswHash, content, pickupTime, comment);
            if (success) {
                return Response.ok("{\"message\": \"Order created successfully\"}").build();
            } else {
                return Response.status(Response.Status.CONFLICT).entity("{\"message\": \"Time slot is already booked\"}").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Unexpected error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/admin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllOrdersForAdmin(@CookieParam("SESSION_ID") Cookie sessionCookie) {
        try {
            if (sessionCookie == null || sessionCookie.getValue().isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Session ID is missing").build();
            }
            String sessionId = sessionCookie.getValue();

            Integer userId = sessionService.findUserIdBySessionId(sessionId);
            if (userId == null || !sessionService.isAdmin(userId)) {
                return Response.status(Response.Status.FORBIDDEN).entity("Access denied").build();
            }

            List<Order> orders = orderService.getAllOrders();
            return Response.ok(orders).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Unexpected error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrdersForCurrentUser(@CookieParam("SESSION_ID") Cookie sessionCookie) {
        try {
            if (sessionCookie == null || sessionCookie.getValue().isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Session ID is missing").build();
            }
            String sessionId = sessionCookie.getValue();

            Integer userId = sessionService.findUserIdBySessionId(sessionId);
            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid session or user ID not found").build();
            }

            List<Order> userOrders = orderService.getOrdersByUserId(String.valueOf(userId));
            return Response.ok(userOrders).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Unexpected error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // Helper method to check if the pickup time falls within available slots
    private boolean isTimeWithinAvailableSlots(LocalDateTime pickupTime) {
        DayOfWeek day = pickupTime.getDayOfWeek();
        LocalTime time = pickupTime.toLocalTime();

        switch (day) {
            case MONDAY:
                return false;
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                return (time.isAfter(LocalTime.of(7, 59)) && time.isBefore(LocalTime.of(13, 1))) ||
                        (time.isAfter(LocalTime.of(14, 29)) && time.isBefore(LocalTime.of(19, 1)));
            case SATURDAY:
            case SUNDAY:
                return time.isAfter(LocalTime.of(14, 29)) && time.isBefore(LocalTime.of(19, 1));
            default:
                return false;
        }
    }
}
