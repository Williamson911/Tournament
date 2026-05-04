package be.technifutur.tournament.resources;

import be.technifutur.tournament.daos.CharacterDao;
import be.technifutur.tournament.entities.Fighter;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/fighterResource")
public class FighterResources {

    @Inject
    private CharacterDao characterDao;

    @GET
    @Path("/{name}")
    @Produces("application/json")
    public Response findByName(@PathParam("name") String name){
        Fighter fighter = characterDao.getFighterByName(name);

        return Response.ok()
                .entity(fighter)
                .build();
    }

}
