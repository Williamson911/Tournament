package be.technifutur.tournament.api;

import be.technifutur.tournament.dal.RegistrationDAO;
import be.technifutur.tournament.dl.entities.Registration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Tag(name ="Registration", description = "crud Registration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/registrationResource")
public class RegistrationResources {

    @Inject
    private RegistrationDAO registrationDAO;

    @POST
    @Operation(summary = "Create a new registration", description = "Creates a new registration for a player in a tournament.")
    @ApiResponse(responseCode = "201", description = "Registration created successfully" )
    public Response create(Registration registration) {
        Registration created = registrationDAO.save(registration);
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    @GET
    @Path("/id/{id}")
    @Operation(summary = "Get a registration by ID", description = "Retrieves a registration by its unique ID.")
    @ApiResponse(responseCode = "200", description = "Registration retrieved successfully" )
    public Response findById(@PathParam("id") Integer id) {
        Registration registration = registrationDAO.findById(id).orElseThrow(() -> new NotFoundException("Registration not found"));
        return Response.ok()
                .entity(registration)
                .build();
    }

    @GET
    @Operation(summary = "Get all registrations", description = "Retrieves all registrations.")
//    @ApiResponse(responseCode = "200", description = "Registrations retrieved successfully" )
    public Response findAll(){
        List<Registration> allRegistrations = registrationDAO.findAll();
        return Response.ok()
                .entity(allRegistrations)
                .build();
    }

    @PUT
    @Path("/id/{id}")
    @Operation(summary = "Update a registration", description = "Updates an existing registration by its unique ID.")
    @ApiResponse(responseCode = "200", description = "Registration updated successfully")
    public Response update(@PathParam("id") Integer id, Registration registration){
        Registration existing = registrationDAO.findById(id).orElseThrow(() -> new NotFoundException("Registration not found"));
        existing.setRegisteredAt(registration.getRegisteredAt());
        existing.setRegistrationStatus(registration.getRegistrationStatus());
        Registration updated = registrationDAO.save(existing);
        return Response.ok()
                .entity(updated)
                .build();
    }

    @DELETE
    @Path("/id/{id}")
    @Operation(summary = "Delete a registration", description = "Deletes an existing registration by its unique ID.")
    @ApiResponse(responseCode = "204", description = "Registration deleted successfully")
    public Response delete(@PathParam("id") Integer id) {
        registrationDAO.delete(id);
        return Response.noContent()
                .build();
    }
}
