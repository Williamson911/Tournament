package be.technifutur.tournament.resources;

import be.technifutur.tournament.daos.FighterDao;
import be.technifutur.tournament.entities.Fighter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/fighterResource")
public class FighterResources {

    private final FighterDao characterDao = new FighterDao();

    @GET
    @Path("/{name}")
    @Produces("application/json")
    public Response findByName(@PathParam("name") String name){
        Fighter fighter = characterDao.getFighterByName(name);
        if (fighter == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().entity(fighter).build();
    }

}
