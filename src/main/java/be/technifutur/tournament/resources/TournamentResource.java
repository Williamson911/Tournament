package be.technifutur.tournament.resources;

import be.technifutur.tournament.dtos.*;
import be.technifutur.tournament.entities.Player;
import be.technifutur.tournament.services.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Tag(name = "Tournament", description = "crud tournament")
@ApplicationScoped
@Path("/tournaments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TournamentResource {

    @Inject TournamentService tournamentService;

    @POST
    @Operation(summary = "Create tournament")
    @ApiResponse(responseCode = "201", description = "Tournament created")
    public Response create(CreateTournamentDto dto) {
        var tournament = tournamentService.create(dto.name(), dto.startDate());
        return Response.status(Response.Status.CREATED).entity(tournament).build();
    }

    @GET
    @Operation(summary = "Get all tournament")
    public Response findAll() {
        return Response.ok(tournamentService.findAll()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get tournament by id")
    public Response findById(@PathParam("id") int id) {
        return Response.ok(tournamentService.findById(id)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update tournament")
    @ApiResponse(responseCode = "200", description = "Tournament updated")
    @ApiResponse(responseCode = "404", description = "Tournament not found")
    public Response update(@PathParam("id") int id, CreateTournamentDto dto) {
        var updated = tournamentService.update(id, dto.name(), dto.startDate());
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete tournament")
    @ApiResponse(responseCode = "204", description = "Tournament deleted")
    @ApiResponse(responseCode = "404", description = "Tournament not found")
    public Response delete(@PathParam("id") int id) {
        tournamentService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/registrations")
    public Response register(@PathParam("id") int id, RegisterPlayerDto dto) {
        var registration = tournamentService.register(id, dto.playerId());
        return Response.status(Response.Status.CREATED).entity(registration).build();
    }

    @DELETE
    @Path("/{id}/registrations/{playerId}")
    public Response unregister(@PathParam("id") int id, @PathParam("playerId") int playerId) {
        tournamentService.unregister(id, playerId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/generate-bracket")
    public Response generateBracket(@PathParam("id") int id) {
        var data = tournamentService.generateBracket(id);
        return Response.ok(data).build();
    }

    @POST
    @Path("/{id}/launch-group-stage")
    @Operation(summary = "Run group stage and select qualifiers")
    public Response launchGroupStage(@PathParam("id") int id) {
        List<Player> qualifiers = tournamentService.launchGroupStage(id);
        List<Integer> qualifierIds = qualifiers.stream().map(Player::getId).toList();
        return Response.ok(qualifierIds).build();
    }

    @PUT
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") int id, UpdateStatusDto dto) {
        var tournament = tournamentService.updateStatus(id, dto.status());
        return Response.ok(tournament).build();
    }
}
