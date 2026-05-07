package be.technifutur.tournament.resources;

import be.technifutur.tournament.daos.MatchDAO;
import be.technifutur.tournament.dtos.MatchDTO;
import be.technifutur.tournament.entities.Match;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static be.technifutur.tournament.dtos.MatchDTO.toDTO;

@Path("/matches")
@ApplicationScoped
public class MatchResource {

    @Inject
    private MatchDAO matchDao;

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response create(Match match) {
        Match created = matchDao.save(match);
        return Response.status(Response.Status.CREATED)
                .entity(toDTO(created))
                .build();
    }

    @GET
    @Produces("application/json")
    public Response findAll() {
        List<MatchDTO> dtos = matchDao.findAllWithRelations()
                .stream()
                .map(MatchDTO::toDTO)
                .toList();

        return Response.ok(dtos).build();
    }

    @GET
    @Path("/id/{id}")
    public Response findByID(@PathParam("id") Integer id) {
        Match match = matchDao.findById(id).orElseThrow();
        return Response.ok(toDTO(match)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
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

        return Response.ok(MatchDTO.toDTO(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        matchDao.delete(id);
        return Response.noContent().build();
    }
}