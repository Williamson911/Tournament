package be.technifutur.tournament.api;

import be.technifutur.tournament.dal.MatchDAO;
import be.technifutur.tournament.dtl.RecordResultDto;
import be.technifutur.tournament.dl.entity.Match;
import be.technifutur.tournament.dal.MatchDAO;
import be.technifutur.tournament.dtl.match.MatchDTO;
import be.technifutur.tournament.dl.entity.Match;
import io.swagger.v3.oas.annotations.tags.Tag;
import be.technifutur.tournament.bl.MatchResultService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static be.technifutur.tournament.dtl.match.MatchDTO.toDTO;

@Tag(name ="Match", description = "crud Match")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/matches")
public class MatchResource {

    @Inject
    private MatchDAO matchDao;

    @Inject
    private MatchResultService matchResultService;

    @POST
    public Response create(Match match) {
        Match created = matchDao.save(match);
        return Response.status(Response.Status.CREATED)
                .entity(toDTO(created))
                .build();
    }

    @GET
    public Response findAll() {
        List<MatchDTO> dtos = matchDao.findAllWithRelations()
                .stream()
                .map(MatchDTO::toDTO)
                .toList();

        return Response.ok().entity(dtos).build();
    }

    @GET
    @Path("/id/{id}")
    public Response findByID(@PathParam("id") Integer id) {
        Match match = matchDao.findById(id).orElseThrow();
        return Response.ok().entity(toDTO(match)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, Match match) {

        Match existing = matchDao.findById(id).orElseThrow();
        existing.setNumberRounds(match.getNumberRounds());
        existing.setBracketStage(match.getBracketStage());
        existing.setBracketPosition(match.getBracketPosition());
        existing.setPlayer1(match.getPlayer1());
        existing.setPlayer2(match.getPlayer2());
        existing.setPlayer1Score(match.getPlayer1Score());
        existing.setPlayer2Score(match.getPlayer2Score());
        existing.setScheduledAt(match.getScheduledAt());
        existing.setStartedAt(match.getStartedAt());
        existing.setFinishedAt(match.getFinishedAt());
        existing.setFinishType(match.getFinishType());
        existing.setRoundNumber(match.getRoundNumber());
        existing.setTournament(match.getTournament());
        existing.setStatus(match.getStatus());
        Match updated = matchDao.update(existing);
        return Response.ok().entity(MatchDTO.toDTO(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        matchDao.delete(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/result")
    @Consumes("application/json")
    @Produces("application/json")
    public Response recordResult(@PathParam("id") int id, RecordResultDto dto) {
        var bracketData = matchResultService.recordResult(id, dto.player1Score(), dto.player2Score(), dto.finishType());
        return Response.ok(bracketData).build();
    }
}