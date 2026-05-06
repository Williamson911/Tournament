package be.technifutur.tournament.resources;

import be.technifutur.tournament.daos.FighterDao;
import be.technifutur.tournament.daos.PlayerDao;
import be.technifutur.tournament.entities.Fighter;
import be.technifutur.tournament.entities.Player;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/playerResource")
public class PlayerResource {

    @Inject
    private PlayerDao playerDao;

    @Inject
    private FighterDao fighterDao;


    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Create player")
    @ApiResponse(responseCode = "201", description = "Player created")
    public Response create(Player player){
        Player created = playerDao.save(player);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Produces("application/json")
    @Operation(summary = "Get all Players")
    public Response findAllWithFighter(){
        List<Player> allPlayers = playerDao.findAll();

        return Response.ok().entity(allPlayers).build();
    }

    @GET
    @Path("/id/{id}")
    @Produces("application/json")
    @Operation(summary = "Get Player by id")
    public Response findByIdWithFighter(@PathParam("id") Integer id){
        Player player = playerDao.findByIdWithFighter(id).orElseThrow();

        return Response.ok().entity(player).build();
    }

    @GET
    @Path("/username/{username}")
    @Produces("application/json")
    @Operation(summary = "Get Player by username")
    public Response findByUsernameWithFighter(@PathParam("username") String username){
        Player player = playerDao.findByUsernameWithFighter(username).orElseThrow();

        return Response.ok().entity(player).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Update player")
    @ApiResponse(responseCode = "200", description = "Player updated")
    @ApiResponse(responseCode = "404", description = "Player not found")
    public Response update(@PathParam("id") Integer id, Player player){
        Player existing = playerDao.findByIdWithFighter(id).orElseThrow();
        existing.setUsername(player.getUsername());
        existing.setEmail(player.getEmail());
        existing.setAge(player.getAge());
        existing.setImage(player.getImage());
        existing.setEmail(player.getEmail());
//        existing.setFighterMain(player.getFighterMain());
        Fighter managedFighter = fighterDao.findById(player.getFighterMain().getId())
                .orElseThrow();

        player.setFighterMain(managedFighter);

        Player updated = playerDao.update(existing);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete player")
    @ApiResponse(responseCode = "204", description = "Player deleted")
    @ApiResponse(responseCode = "404", description = "Player not found")
    public Response delete(@PathParam("id") Integer id){
        playerDao.delete(id);
        return Response.noContent().build();
    }
}
