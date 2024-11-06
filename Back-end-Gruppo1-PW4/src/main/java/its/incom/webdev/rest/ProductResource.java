package its.incom.webdev.rest;

import its.incom.webdev.persistence.model.Product;
import its.incom.webdev.service.ProductService;
import its.incom.webdev.service.exception.SessionNotFoundException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/product")
public class ProductResource {

    private final ProductService productService;

    public ProductResource(ProductService productService){
        this.productService = productService;
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@CookieParam("SESSION_ID") String sessionId, Product p){
        try {
            Product createdProduct = productService.create(sessionId, p.getName(), p.getDescription(), p.getQuantity(), p.getPrice(), p.getCategory());

            return Response.status(Response.Status.CREATED).entity(createdProduct).build();
        } catch (SessionNotFoundException e) {
            // Risposta 401 Unauthorized se la sessione non è valida
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non trovata: " + e.getMessage()).build();
        } catch (SecurityException e) {
            // Risposta 403 Forbidden se l'utente non ha i permessi di amministratore
            return Response.status(Response.Status.FORBIDDEN).entity("Accesso negato: " + e.getMessage()).build();
        } catch (Exception e) {
            // Risposta 500 Internal Server Error per altri tipi di errore
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Errore nel server: " + e.getMessage()).build();
        }
        }

        @PUT
        @Path("/{id}/update")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response update(@PathParam("id") Long productId, @CookieParam("SESSION_ID") String sessionId, Product updatedProduct){
            System.out.println("Id prodotto " + productId);
            try {
                // Chiama il service per aggiornare il prodotto
                Product product = productService.update(
                        sessionId,
                        productId,
                        updatedProduct.getName(),
                        updatedProduct.getDescription(),
                        updatedProduct.getQuantity(),
                        updatedProduct.getPrice(),
                        updatedProduct.getCategory()
                );

                // Restituisce la risposta con il prodotto aggiornato
                return Response.ok(product).build();

            } catch (SessionNotFoundException e) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Sessione non valida: " + e.getMessage())
                        .build();
            } catch (SecurityException e) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(e.getMessage())
                        .build();
            } catch (NotFoundException e) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Prodotto non trovato: " + e.getMessage())
                        .build();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Errore interno del server, riprova più tardi.")
                        .build();
            }
        }


}
