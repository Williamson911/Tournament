package be.technifutur.tournament;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.HashMap;
import java.util.Map;

@Path("/hello-world")
public class HelloResource {
    @GET
    @Produces("text/plain")
    public String hello() {
        String persistenceName = System.getenv().getOrDefault("PERSISTENCE_NAME", "tournament");

        Map<String, Object> props = new HashMap<>();

        String dbUrl = System.getenv("DB_URL");

        EntityManagerFactory emf;

        if (dbUrl != null && !dbUrl.isBlank()) {
            props.put("jakarta.persistence.jdbc.url", dbUrl);
            props.put("jakarta.persistence.jdbc.user", System.getenv("DB_USER"));
            props.put("jakarta.persistence.jdbc.password", System.getenv("DB_PASSWORD"));

            emf = Persistence.createEntityManagerFactory(persistenceName, props);
        } else {
            emf = Persistence.createEntityManagerFactory(persistenceName);
        }

        return "Hello, World! with config "+persistenceName;

    }
}