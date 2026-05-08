package be.technifutur.tournament.api;


import be.technifutur.tournament.dal.TournamentDAO;
import be.technifutur.tournament.dl.entity.Tournament;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Tag(name ="Tournament", description = "crud tournament")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/tournamentResource")
public class TournamentResource {

    @Inject
    private TournamentDAO tournamentDAO;

    @POST
    @Operation(summary = "Create tournament")
    @ApiResponse(responseCode = "201", description = "Tournament created")
    public Response create(Tournament tour){
        Tournament created = tournamentDAO.save(tour);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Operation(summary = "Get all tournament")
    public Response findAll(){
        List<Tournament> allTournaments = tournamentDAO.findAll();

        return Response.ok()
                .entity(allTournaments)
                .build();
    }

    @GET
    @Path("/id/{id}")
    @Operation(summary = "Get tournament by id")
    public Response findByName(@PathParam("id") Integer id){
        Tournament tournament = tournamentDAO.findById(id).orElseThrow();

        return Response.ok().entity(tournament).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update tournament")
    @ApiResponse(responseCode = "200", description = "tournament updated")
    @ApiResponse(responseCode = "404", description = "tournament not found")
    public Response update(@PathParam("id") Integer id, Tournament tour){
        Tournament existing = tournamentDAO.findById(id).orElseThrow();
        existing.setName(tour.getName());
        existing.setStatus(tour.getStatus());
        existing.setStartDate(tour.getStartDate());
        existing.setEndDate(tour.getEndDate());
        Tournament updated = tournamentDAO.update(existing);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete tournament")
    @ApiResponse(responseCode = "204", description = "tournament deleted")
    @ApiResponse(responseCode = "404", description = "tournament not found")
    public Response delete(@PathParam("id") Integer id){
        tournamentDAO.delete(id);
        return Response.noContent().build();
    }
}
