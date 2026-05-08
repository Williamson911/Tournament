package be.technifutur.tournament;

import be.technifutur.tournament.utils.DataInitializer;
import be.technifutur.tournament.utils.EMFProvider;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.io.IOException;

@Path("/hello-world")
public class HelloResource {

    @Inject
    private EMFProvider emfProvider;

    @GET
    @Produces("text/plain")
    public String hello() throws IOException {
        EntityManager em = emfProvider.get().createEntityManager();

        new DataInitializer().initB(emfProvider.get().createEntityManager());

        return "Hello with " + em + " for persistence unit "+ emfProvider.getPersistenceName();
    }
}