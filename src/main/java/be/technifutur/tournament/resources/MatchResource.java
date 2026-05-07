package be.technifutur.tournament.resources;

import be.technifutur.tournament.daos.MatchDAO;
import be.technifutur.tournament.dtos.MatchDTO;
import be.technifutur.tournament.entities.Match;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

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
                .map(this::toDTO)
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
    public Response update(@PathParam("id") Integer id, Match match) {
        Match existing = matchDao.findById(id).orElseThrow();
        existing.setNumberRounds(match.getNumberRounds());

        Match updated = matchDao.update(existing);
        return Response.ok(toDTO(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        matchDao.delete(id);
        return Response.noContent().build();
    }

    private MatchDTO toDTO(Match m) {
        return new MatchDTO(
                m.getId(),
                m.getStatus().name(),
                m.getTournament() != null ? m.getTournament().getName() : null,
                m.getPlayer1() != null ? m.getPlayer1().getId() : null,
                m.getPlayer2() != null ? m.getPlayer2().getId() : null,
                m.getPlayer1Score(),
                m.getPlayer2Score()
        );
    }
}