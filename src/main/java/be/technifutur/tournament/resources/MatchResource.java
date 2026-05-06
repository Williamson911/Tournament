package be.technifutur.tournament.resources;

import be.technifutur.tournament.daos.MatchDAO;
import be.technifutur.tournament.entities.Fighter;
import be.technifutur.tournament.entities.Match;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/matchResource")


public class MatchResource {

        @Inject
        private MatchDAO matchDao;


        @POST
        @Consumes("application/json")
        @Produces("application/json")
        @Operation(summary = "Create match")
        @ApiResponse(responseCode = "201", description = "Match created")
        public Response create(Match match){
            Match created = matchDao.save(match);
            return Response.status(Response.Status.CREATED).entity(created).build();
        }

        @GET
        @Produces("application/json")
        @Operation(summary = "Get all matches")
        public Response findAll(){
            List<Match> allMatches = matchDao.findAll();

            return Response.ok()
                    .entity(allMatches)
                    .build();
        }

        @GET
        @Path("/id/{id}")
        @Produces("application/json")
        @Operation(summary = "Get match by id")
        public Response findByName(@PathParam("id") Integer id){
            Match match = matchDao.findById(id).orElseThrow();

            return Response.ok().entity(match).build();
        }


        @PUT
        @Path("/{id}")
        @Consumes("application/json")
        @Produces("application/json")
        @Operation(summary = "Update match")
        @ApiResponse(responseCode = "200", description = "Match updated")
        @ApiResponse(responseCode = "404", description = "Match not found")
        public Response update(@PathParam("id") Integer id, Match match){
            Match existing = matchDao.findById(id).orElseThrow();
            existing.setNumberRounds(match.getNumberRounds());
            Match updated = matchDao.update(existing);
            return Response.ok(updated).build();
        }

        @DELETE
        @Path("/{id}")
        @Operation(summary = "Delete match")
        @ApiResponse(responseCode = "204", description = "Match deleted")
        @ApiResponse(responseCode = "404", description = "Match not found")
        public Response delete(@PathParam("id") Integer id){
            matchDao.delete(id);
            return Response.noContent().build();
        }
    }