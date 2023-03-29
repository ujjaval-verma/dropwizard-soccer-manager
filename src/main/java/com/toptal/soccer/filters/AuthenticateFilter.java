package com.toptal.soccer.filters;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import com.toptal.soccer.entities.Team;
import com.toptal.soccer.persistence.TeamDAO;
import com.toptal.soccer.resources.UserResource;
import com.toptal.soccer.utils.Helper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Authenticator
public class AuthenticateFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);
    private TeamDAO teamDAO;
    private SessionFactory sessionFactory;
    private String salt;

    public AuthenticateFilter(SessionFactory sessionFactory, TeamDAO teamDAO, String salt) {
        this.sessionFactory = sessionFactory;
        this.teamDAO = teamDAO;
        this.salt = salt;
    }

    @Override
    public void filter(ContainerRequestContext context) {
        final String userName = Helper.extractHeader(context, Helper.USER_HEADER);
        if (Strings.isNullOrEmpty(userName)) {
            context.abortWith(responseMissingHeader(Helper.USER_HEADER));
            return;
        }

        final String token = Helper.extractHeader(context, Helper.TOKEN_HEADER);
        if (Strings.isNullOrEmpty(token)) {
            context.abortWith(responseMissingHeader(Helper.TOKEN_HEADER));
            return;
        }
        LOGGER.info("UserName: {}, Token: {}", userName, token);
        if (!authenticate(userName, token)) {
            context.abortWith(responseUnauthorized());
        }
    }

    private boolean authenticate(String userName, String token) {
        String secret = getUserSecret(userName);
        // generate test token for current, previous and next hours
        // authenticate successfully if there is a match
        // this ensures auto token expiry after 2 hours
        // match with next hour is done to account for clock drifts
        if (!Strings.isNullOrEmpty(secret)) {
            long hours = Helper.getHoursSinceZeroEpoch();

            String currentRaw = secret + this.salt + hours;
            String prevRaw = secret + this.salt + (hours - 1);
            String nextRaw = secret + this.salt + (hours + 1);
            return token.equals(Helper.getSHA256Hash(currentRaw))
                    || token.equals(Helper.getSHA256Hash(prevRaw))
                    || token.equals(Helper.getSHA256Hash(nextRaw));
        }
        return false;
    }

    @Timed
    // @UnitOfWork only works in resource class
    // manually manage hibernate session to query teamDAO
    private String getUserSecret(String userName) {
        Session session = sessionFactory.openSession();
        ManagedSessionContext.bind(session);
        Transaction transaction = session.beginTransaction();
        Optional<Team> optional = teamDAO.findByUserName(userName);
        transaction.commit();
        ManagedSessionContext.unbind(sessionFactory);
        session.close();
        if (optional.isPresent()) {
            return optional.get().getSecret();
        }
        return null;
    }

    private Response responseMissingHeader(String name) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .entity("Header '" + name + "' is required.")
                .build();
    }
    private Response responseUnauthorized() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .entity("Unauthorized")
                .build();
    }
}
