package be.technifutur.tournament.api;


import be.technifutur.tournament.dal.TournamentDAO;
import be.technifutur.tournament.dl.entity.Tournament;
import be.technifutur.tournament.dtl.*;
import be.technifutur.tournament.dl.entity.Player;
import be.technifutur.tournament.bl.TournamentService;
import be.technifutur.tournament.bl.TournamentSimulationService;
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

    @Inject TournamentService tournamentService;
    @Inject TournamentSimulationService simulationService;
    @Inject
    private TournamentDAO tournamentDAO;

    @POST
    @Operation(summary = "Create tournament")
    @ApiResponse(responseCode = "201", description = "Tournament created")

    public Response create(CreateTournamentDto dto) {
        var tournament = tournamentService.create(dto.name(), dto.startDate());
        return Response.status(Response.Status.CREATED).entity(tournament).build();
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

    @POST
    @Path("/{id}/simulate-next-round")
    @Operation(summary = "Auto-simulate one round of ready matches")
    public Response simulateNextRound(@PathParam("id") int id) {
        var data = simulationService.simulateNextRound(id);
        return Response.ok(data).build();
    }

    @POST
    @Path("/{id}/reset")
    @Operation(summary = "Reset tournament to DRAFT (delete matches, restore registrations)")
    public Response reset(@PathParam("id") int id) {
        var tournament = tournamentService.resetTournament(id);
        return Response.ok(tournament).build();
    }

    @PUT
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") int id, UpdateStatusDto dto) {
        var tournament = tournamentService.updateStatus(id, dto.status());
        return Response.ok(tournament).build();
    }
}
