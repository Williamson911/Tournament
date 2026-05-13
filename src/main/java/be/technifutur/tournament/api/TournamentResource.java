package be.technifutur.tournament.api;


import be.technifutur.tournament.dtl.*;
import be.technifutur.tournament.dl.entity.Player;
import be.technifutur.tournament.bl.TournamentService;
import be.technifutur.tournament.bl.TournamentSimulationService;
import be.technifutur.tournament.dtl.tournament.TournamentActionRequestDTO;
import be.technifutur.tournament.dtl.tournament.TournamentBrawlDTO;
import be.technifutur.tournament.dtl.tournament.TournamentIdDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Inject
    TournamentService tournamentService;
    @Inject
    TournamentSimulationService simulationService;

    @GET
    @Operation(summary = "Get all tournament")
    public Response findAll() {
        return Response.ok(tournamentService.findAll())
                       .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get tournament by id")
    public Response findById(@PathParam("id") int id) {
        return Response.ok(tournamentService.findById(id))
                       .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update tournament")
    @ApiResponse(responseCode = "200", description = "Tournament updated")
    @ApiResponse(responseCode = "404", description = "Tournament not found")
    public Response update(@PathParam("id") int id, CreateTournamentDto dto) {
        var updated = tournamentService.update(
                id, dto.name(), dto.startDate(),
                dto.registrationEndDate(), dto.maxParticipants());
        return Response.ok(updated)
                       .build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete tournament")
    @ApiResponse(responseCode = "204", description = "Tournament deleted")
    @ApiResponse(responseCode = "404", description = "Tournament not found")
    public Response delete(@PathParam("id") int id) {
        tournamentService.delete(id);
        return Response.noContent()
                       .build();
    }

    private <T> T getDto(TournamentActionRequestDTO requestDTO, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(requestDTO.dto(), clazz);
    }

    @POST
    @Operation(summary = "apply an action on tournament(s)")
    public Response apply(TournamentActionRequestDTO requestDTO) {

        return (switch (requestDTO.action()) {
            case CREATE -> {
                CreateTournamentDto dto = getDto(requestDTO, CreateTournamentDto.class);
                var tournament = tournamentService.create(
                        dto.name(), dto.startDate(),
                        dto.registrationEndDate(), dto.maxParticipants());
                yield Response.status(Response.Status.CREATED)
                              .entity(tournament);
            }
            case REGISTER -> {
                RegisterPlayerDto dto = getDto(requestDTO, RegisterPlayerDto.class);
                var registration = tournamentService.register(dto.tournamentId(), dto.playerId());
                yield Response.status(Response.Status.CREATED)
                              .entity(registration);
            }
            case GENERATE_BRACKET -> {
                TournamentIdDTO dto = getDto(requestDTO, TournamentIdDTO.class);
                yield Response.ok(tournamentService.generateBracket(dto.tournamentId()));
            }
            case LAUNCH_GROUP_STAGE -> {
                TournamentIdDTO dto = getDto(requestDTO, TournamentIdDTO.class);
                List<Player> qualifiers = tournamentService.launchGroupStage(dto.tournamentId());
                yield Response
                        .ok(qualifiers.stream()
                                      .map(Player::getId)
                                      .toList());
            }
            case SIMULATE_ONE_MATCH -> {
                TournamentBrawlDTO dto = getDto(requestDTO, TournamentBrawlDTO.class);
                yield Response.ok(simulationService.simulateOneMatch(dto.tournamentId()));
            }
            case SIMULATE_NEXT_ROUND -> {
                TournamentBrawlDTO dto = getDto(requestDTO, TournamentBrawlDTO.class);
                yield Response.ok(simulationService.simulateNextRound(dto.tournamentId()));
            }
            case APPLY_BRAWL -> {
                TournamentBrawlDTO dto = getDto(requestDTO, TournamentBrawlDTO.class);
                yield Response.ok(simulationService.applyBrawl(dto.tournamentId(), Integer.MAX_VALUE, dto.winnerSet()));
            }
            case APPLY_DUEL -> {
                TournamentBrawlDTO dto = getDto(requestDTO, TournamentBrawlDTO.class);
                yield Response.ok(simulationService.applyBrawl(dto.tournamentId(), 1, dto.winnerSet()));
            }
            case RESET -> {
                TournamentIdDTO dto = getDto(requestDTO, TournamentIdDTO.class);
                yield Response.ok(tournamentService.resetTournament(dto.tournamentId()));
            }
            default -> Response.status(Response.Status.BAD_REQUEST)
                               .entity("Unsupported operation");
        }).build();
    }

    @DELETE
    @Path("/{id}/registrations/{playerId}")
    public Response unregister(@PathParam("id") int id, @PathParam("playerId") int playerId) {
        tournamentService.unregister(id, playerId);
        return Response.noContent()
                       .build();
    }

    @PUT
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") int id, UpdateStatusDto dto) {
        var tournament = tournamentService.updateStatus(id, dto.status());
        return Response.ok(tournament)
                       .build();
    }
}
