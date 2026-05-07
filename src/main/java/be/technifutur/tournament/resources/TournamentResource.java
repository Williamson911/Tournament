package be.technifutur.tournament.resources;

import be.technifutur.tournament.dtos.*;
import be.technifutur.tournament.services.TournamentService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/tournaments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TournamentResource {

    @Inject TournamentService tournamentService;

    @POST
    public Response create(CreateTournamentDto dto) {
        var tournament = tournamentService.create(dto.name(), dto.startDate());
        return Response.status(Response.Status.CREATED).entity(tournament).build();
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

    @PUT
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") int id, UpdateStatusDto dto) {
        var tournament = tournamentService.updateStatus(id, dto.status());
        return Response.ok(tournament).build();
    }
}
