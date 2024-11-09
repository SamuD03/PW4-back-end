package its.incom.webdev.rest;

import its.incom.webdev.persistence.model.Product;
import its.incom.webdev.service.ProductService;
import its.incom.webdev.service.exception.SessionNotFoundException;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/product")
public class ProductResource {
    @Inject
    ProductService productService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProducts(@CookieParam("SESSION_ID") String sessionId) {
        try{
            List<Product> products = productService.getAll(sessionId);
            return Response.ok(products).build();
        } catch (SessionNotFoundException e){
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (PersistenceException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error: " + e.getMessage()).build();
        }
    }
}
