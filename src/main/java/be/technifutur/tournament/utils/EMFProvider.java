package be.technifutur.tournament.utils;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class EMFProvider {

    private EntityManagerFactory emf;

    @Getter
    private String persistenceName;

    @PostConstruct
    public void init() {
        persistenceName = System.getenv().getOrDefault("PERSISTENCE_NAME", "tournament");

        Map<String, Object> props = new HashMap<>();
        String dbUrl = System.getenv("DB_URL");

        if (dbUrl != null && !dbUrl.isBlank()) {
            props.put("jakarta.persistence.jdbc.url", dbUrl);
            props.put("jakarta.persistence.jdbc.user", System.getenv("DB_USER"));
            props.put("jakarta.persistence.jdbc.password", System.getenv("DB_PASSWORD"));

            emf = Persistence.createEntityManagerFactory(persistenceName, props);
        } else {
            emf = Persistence.createEntityManagerFactory(persistenceName);
        }
    }

    public EntityManagerFactory get() {
        return emf;
    }

}
