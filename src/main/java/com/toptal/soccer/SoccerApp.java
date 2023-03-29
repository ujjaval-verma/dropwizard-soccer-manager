package com.toptal.soccer;

import com.toptal.soccer.entities.Player;
import com.toptal.soccer.entities.Team;
import com.toptal.soccer.entities.Transfer;
import com.toptal.soccer.filters.AuthenticateFilter;
import com.toptal.soccer.persistence.PlayerDAO;
import com.toptal.soccer.persistence.TeamDAO;
import com.toptal.soccer.persistence.TransferDAO;
import com.toptal.soccer.resources.PlayerResource;
import com.toptal.soccer.resources.TeamResource;
import com.toptal.soccer.resources.TransferResource;
import com.toptal.soccer.resources.UserResource;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class SoccerApp extends Application<SoccerConfig> {
    private final HibernateBundle<SoccerConfig> hibernate = new HibernateBundle<SoccerConfig>(
            Team.class,
            Player.class,
            Transfer.class
    ) {

        @Override
        public DataSourceFactory getDataSourceFactory(SoccerConfig soccerConfig) {
            return soccerConfig.getDataSourceFactory();
        }
    };

    public static void main(String[] args) throws Exception{
        new SoccerApp().run(args);
    }

    @Override
    public void initialize(Bootstrap<SoccerConfig> bootstrap) {
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(SoccerConfig config, Environment env) {
        final TeamDAO teamDAO = new TeamDAO(hibernate.getSessionFactory());
        final PlayerDAO playerDAO = new PlayerDAO(hibernate.getSessionFactory());
        final TransferDAO transferDAO = new TransferDAO(hibernate.getSessionFactory());

        env.jersey().register(new AuthenticateFilter(hibernate.getSessionFactory(), teamDAO, config.getToken()));
        env.jersey().register(new TeamResource(teamDAO, playerDAO));
        env.jersey().register(new UserResource(teamDAO, playerDAO, config.getToken()));
        env.jersey().register(new TransferResource(teamDAO, playerDAO, transferDAO));
        env.jersey().register(new PlayerResource(playerDAO, teamDAO));
        env.healthChecks().register("template",
                new SoccerHealthCheck(config.getVersion()));
    }
}
