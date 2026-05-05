package be.technifutur.tournament;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/hello-world")
public class HelloResource {
    @GET
    @Produces("text/plain")
    public String hello() {
        // ici il faut mettre EXACTEMENT le même que dans persistence.xml persistence-unit name
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("tournament");
        return "Hello, World!";

    }
}