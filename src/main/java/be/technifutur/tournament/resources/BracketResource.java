package be.technifutur.tournament.resources;

import be.technifutur.tournament.services.BracketService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/tournaments")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class BracketResource {

    @Inject
    private BracketService bracketService;

    @GET
    @Path("/{id}/bracket")
    public Response getBracket(@PathParam("id") int id) {
        try {
            return Response.ok(bracketService.buildBracketData(id)).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }
}
