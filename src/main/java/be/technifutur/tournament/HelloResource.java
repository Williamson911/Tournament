package be.technifutur.tournament;

import be.technifutur.tournament.utils.EMFProvider;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/hello-world")
public class HelloResource {

    @Inject
    private EMFProvider emfProvider;

    @GET
    @Produces("text/plain")
    public String hello() {
        EntityManager em = emfProvider.get().createEntityManager();

        return "Hello with " + em + " for persistence unit "+ emfProvider.getPersistenceName();
    }
}