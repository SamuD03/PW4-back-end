package its.incom.webdev.rest;

import its.incom.webdev.service.OrderService;
import its.incom.webdev.persistence.repository.SessionRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Path("/orders")
public class OrderResource {

    @Inject
    OrderService orderService;

    @Inject
    SessionRepository sessionRepository;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrder(Map<String, Object> orderData, @CookieParam("SESSION_ID") Cookie sessionCookie) {
        try {
            // get session id from cookie
            if (sessionCookie == null || sessionCookie.getValue().isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Session ID is missing").build();
            }
            String sessionId = sessionCookie.getValue();

            // email from session
            String emailBuyer = sessionRepository.findEmailBySessionId(sessionId);
            if (emailBuyer == null || emailBuyer.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid session or email not found").build();
            }

            // validate e extract
            if (orderData == null || !orderData.containsKey("content") || !orderData.containsKey("pickup")) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing order details").build();
            }

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

            // parse pickup time
            LocalDateTime pickupTime;
            try {
                pickupTime = LocalDateTime.parse(pickupTimeStr);
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid pickup time format").build();
            }

            // call service create order
            boolean success = orderService.bookOrder(emailBuyer, content, pickupTime, comment);

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
}
