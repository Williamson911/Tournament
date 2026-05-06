package be.technifutur.tournament.resources;

import be.technifutur.tournament.daos.FighterDao;
import be.technifutur.tournament.entities.Fighter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/fighterResource")
public class FighterResource {

    @Inject
    private FighterDao fighterDao;


    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Creer un fighter")
    @ApiResponse(responseCode = "201", description = "Fighter cree")
    public Response create(Fighter fighter){
        Fighter created = fighterDao.save(fighter);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Produces("application/json")
    public Response findAll(){
        List<Fighter> allFighters = fighterDao.findAll();

        return Response.ok()
                .entity(allFighters)
                .build();
    }

    @GET
    @Path("/id/{id}")
    @Produces("application/json")
    public Response findByName(@PathParam("id") Integer id){
        Fighter fighter = fighterDao.findById(id).orElseThrow();

        return Response.ok().entity(fighter).build();
    }

    @GET
    @Path("/name/{name}")
    @Produces("application/json")
    public Response findByName(@PathParam("name") String name){
        Fighter fighter = fighterDao.getFighterByName(name);

        return Response.ok().entity(fighter).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Mettre a jour un fighter")
    @ApiResponse(responseCode = "200", description = "Fighter mis a jour")
    @ApiResponse(responseCode = "404", description = "Fighter introuvable")
    public Response update(@PathParam("id") Integer id, Fighter fighter){
        Fighter existing = fighterDao.findById(id).orElseThrow();
        existing.setName(fighter.getName());
        existing.setStyle(fighter.getStyle());
        existing.setOriginCountry(fighter.getOriginCountry());
        existing.setImage(fighter.getOriginCountry());
        Fighter updated = fighterDao.update(existing);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Supprimer un fighter")
    @ApiResponse(responseCode = "204", description = "Fighter supprime")
    @ApiResponse(responseCode = "404", description = "Fighter introuvable")
    public Response delete(@PathParam("id") Integer id){
        fighterDao.delete(id);
        return Response.noContent().build();
    }
}
