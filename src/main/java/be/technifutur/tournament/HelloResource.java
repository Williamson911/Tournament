package be.technifutur.tournament;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.HashMap;
import java.util.Map;

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