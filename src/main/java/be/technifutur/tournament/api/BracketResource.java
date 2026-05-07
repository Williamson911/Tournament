package be.technifutur.tournament.api;

import be.technifutur.tournament.bl.BracketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Tag(name ="Bracket", description = "crud Bracket")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/bracket")
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
