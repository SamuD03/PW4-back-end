package its.incom.webdev.rest;

import its.incom.webdev.persistence.model.Ingredient;
import its.incom.webdev.persistence.model.Product;
import its.incom.webdev.rest.model.*;
import its.incom.webdev.service.DataExportService;
import its.incom.webdev.service.IngredientService;
import its.incom.webdev.service.ProductService;
import its.incom.webdev.service.UserService;
import its.incom.webdev.service.exception.ExportDataException;
import its.incom.webdev.service.exception.SessionNotFoundException;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

@Path("/admin")
public class AdminResource {

    private final UserService userService;
    private final DataExportService dataExportService;

    public AdminResource(UserService userService, DataExportService dataExportService){
        this.userService = userService;
        this.dataExportService = dataExportService;
    }
    @Inject
    IngredientService ingredientService;
    @Inject
    ProductService productService;

    @POST
    @Path("/product/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@CookieParam("SESSION_ID") String sessionId, ProductRequest request){
        try {
            Product createdProduct = productService.create(sessionId, request.getProductName(), request.getDescription(), request.getQuantity(), request.getPrice(), request.getCategory(), request.getIngredients());

            return Response.status(Response.Status.CREATED).entity(createdProduct).build();
        } catch (SessionNotFoundException e) {
            // Risposta 401 Unauthorized se la sessione non è valida
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non trovata: " + e.getMessage()).build();
        } catch (SecurityException e) {
            // Risposta 403 Forbidden se l'utente non ha i permessi di amministratore
            return Response.status(Response.Status.FORBIDDEN).entity("Accesso negato: " + e.getMessage()).build();
        } catch (IllegalArgumentException e){
            return Response.status(Response.Status.BAD_REQUEST).entity("Bad request: " + e.getMessage()).build();
        } catch (Exception e) {
            // Risposta 500 Internal Server Error per altri tipi di errore
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Errore nel server: " + e.getMessage()).build();
        }
    }


    @PUT
    @Path("/product/{id}/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Long productId, @CookieParam("SESSION_ID") String sessionId, ProductRequest request) {
        try {
            Product product = productService.update(
                    sessionId,
                    productId,
                    request.getProductName(),
                    request.getDescription(),
                    request.getQuantity(),
                    request.getPrice(),
                    request.getCategory(),
                    request.getIngredients(),
                    request.getUrl()
            );

            // Return the product in the desired structure
            ProductResponse response = new ProductResponse(product);
            return Response.ok(response).build();

        } catch (SessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Session not valid: " + e.getMessage())
                    .build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(e.getMessage())
                    .build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Product not found: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/product/{id}/delete")
    public Response delete(@CookieParam("SESSION_ID") String sessionId, @PathParam("id") Long productId){
        try{
            productService.delete(sessionId, productId);
            return Response.ok("Product deleted").build();

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

    @GET
    @Path("/users/{admin}")
    @Produces
    public Response getUsers(@CookieParam("SESSION_ID") String sessionId, @PathParam("admin") boolean admin){
        try{
            List<CreateUserResponse> users = userService.getUsers(sessionId, admin);
            return Response.ok(users).build();
        } catch (SessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Sessione non valida: " + e.getMessage())
                    .build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore interno del server, riprova più tardi." + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/ingredient/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@CookieParam("SESSION_ID") String sessionId, IngredientRequest request){
        try{
            Ingredient created = ingredientService.create(sessionId, request.getName());
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (SessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Sessione non valida: " + e.getMessage())
                    .build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(e.getMessage())
                    .build();
        } catch (PersistenceException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/ingredient/{id}/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@CookieParam("SESSION_ID") String sessionId, @PathParam("id") Integer ingredientId, IngredientRequest request){
        try{
            Ingredient updated = ingredientService.update(sessionId, ingredientId, request.getName());
            return Response.status(Response.Status.CREATED).entity(updated).build();
        } catch (SessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid session: " + e.getMessage())
                    .build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(e.getMessage())
                    .build();
        } catch (PersistenceException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error: " + e.getMessage())
                    .build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/ingredient")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllIngredients(@CookieParam("SESSION_ID") String sessionId){
        try{
            List<Ingredient> ingredients = ingredientService.getAll(sessionId);
            return Response.ok(ingredients).build();
        } catch (SessionNotFoundException e){
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (SecurityException e){
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (PersistenceException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error: " + e.getMessage()).build();
        }
    }
    @DELETE
    @Path("/ingredient/{id}/delete")
    public Response deleteIngredient(@CookieParam("SESSION_ID") String sessionId, @PathParam("id") Long ingredientId) {
        try {
            ingredientService.delete(sessionId, ingredientId);
            return Response.ok("Ingredient deleted").build();
        } catch (SessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid session: " + e.getMessage())
                    .build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(e.getMessage())
                    .build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Ingredient not found: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal server error: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/product/export")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response productsToExcel(@CookieParam("SESSION_ID") String sessionId){
        try{
            File file = dataExportService.exportProductsToExcel(sessionId);
            return Response.ok(file).header("Content-Disposition", "attachment; filename=\"Products.xlsx\"").build();
        } catch (SessionNotFoundException e){
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (SecurityException e){
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (ExportDataException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error exporting data: " + e.getMessage())
                    .build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected server error: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/order/{date}/export")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response orderToExcel(@CookieParam("SESSION_ID") String sessionId, @PathParam("date") String date){
        try{
            LocalDate localDate = LocalDate.parse(date);
            File file = dataExportService.exportOrdersToExcel(sessionId, localDate);
            return Response.ok(file).header("Content-Disposition", "attachment; filename=\"Orders_" + date + ".xlsx\"").build();
        } catch (SessionNotFoundException e){
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (SecurityException e){
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (ExportDataException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error exporting data: " + e.getMessage())
                    .build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected server error: " + e.getMessage())
                    .build();
        }
    }
    @POST
    @Path("/product/{id}/upload-image")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadImage(@CookieParam("SESSION_ID") String sessionId, @PathParam("id") Long productId, ImageUploadRequest request) {
        try {
            productService.uploadImage(sessionId, productId, request.getUrl());
            return Response.ok("Image URL successfully updated").build();
        } catch (SessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid session: " + e.getMessage()).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied: " + e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("Product not found: " + e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error: " + e.getMessage()).build();
        }
    }
}


