package be.technifutur.tournament;

import be.technifutur.tournament.daos.FighterDao;
import be.technifutur.tournament.resources.FighterResources;
import jakarta.inject.Singleton;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class HelloApplication extends ResourceConfig {

    public HelloApplication() {
        register(FighterResources.class);
        register(HelloResource.class);
        register(JacksonFeature.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(FighterDao.class).in(Singleton.class);
            }
        });
    }
}
