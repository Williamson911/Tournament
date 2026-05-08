package be.technifutur.tournament.api;

import be.technifutur.tournament.dal.FighterDao;
import be.technifutur.tournament.dl.entities.Fighter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Tag(name ="Fighter", description = "crud Fighter")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/fighterResource")
public class FighterResource {

    @Inject
    private FighterDao fighterDao;


    @POST
    @Operation(summary = "Create fighter")
    @ApiResponse(responseCode = "201", description = "Fighter created")
    public Response create(Fighter fighter){
        Fighter created = fighterDao.save(fighter);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Operation(summary = "Get all fighters")
    public Response findAll(){
        List<Fighter> allFighters = fighterDao.findAll();

        return Response.ok()
                .entity(allFighters)
                .build();
    }

    @GET
    @Path("/id/{id}")
    @Operation(summary = "Get fighter by id")
    public Response findByName(@PathParam("id") Integer id){
        Fighter fighter = fighterDao.findById(id).orElseThrow();

        return Response.ok().entity(fighter).build();
    }

    @GET
    @Path("/name/{name}")
    @Operation(summary = "Get fighter by name")
    public Response findByName(@PathParam("name") String name){
        Fighter fighter = fighterDao.getFighterByName(name);

        return Response.ok().entity(fighter).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update fighter")
    @ApiResponse(responseCode = "200", description = "Fighter updated")
    @ApiResponse(responseCode = "404", description = "Fighter not found")
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
    @Operation(summary = "Delete fighter")
    @ApiResponse(responseCode = "204", description = "Fighter deleted")
    @ApiResponse(responseCode = "404", description = "Fighter not found")
    public Response delete(@PathParam("id") Integer id){
        fighterDao.delete(id);
        return Response.noContent().build();
    }
}
