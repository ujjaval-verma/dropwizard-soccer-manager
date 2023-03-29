package com.toptal.soccer.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.toptal.soccer.entities.Player;
import com.toptal.soccer.entities.Team;
import com.toptal.soccer.entities.User;
import com.toptal.soccer.enums.ErrorCode;
import com.toptal.soccer.filters.Authenticator;
import com.toptal.soccer.persistence.PlayerDAO;
import com.toptal.soccer.persistence.TeamDAO;
import com.toptal.soccer.utils.Helper;
import com.toptal.soccer.utils.UserCredential;
import com.toptal.soccer.utils.UserDetails;
import io.dropwizard.hibernate.UnitOfWork;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private String token;
    private TeamDAO teamDAO;
    private PlayerDAO playerDAO;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

    public UserResource(TeamDAO teamDAO, PlayerDAO playerDAO, String token) {
        this.teamDAO = teamDAO;
        this.playerDAO = playerDAO;
        this.token = token;
    }

    @POST
    @Path("/login")
    @UnitOfWork
    @Timed
    /*
      Get auth token for user
     **/
    public Response getAuthToken(@Valid UserCredential cred) {
        String secret = getSecretHash(cred);
        if (Strings.isNullOrEmpty(secret)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String rawAuth = secret + this.token + Helper.getHoursSinceZeroEpoch();
        String authToken = Helper.getSHA256Hash(rawAuth);
        return Response.ok(authToken).build();
    }

    @GET
    @UnitOfWork
    @Timed
    @Authenticator
    @SneakyThrows
    /*
      Get user summary.
     **/
    public Response getUser(@Context HttpHeaders headers) {
        String userName = Helper.getUserNameFromHeaders(headers);
        Optional<Team> optional = teamDAO.findByUserName(userName);
        if (optional.isPresent()) {
            User user = Helper.getUserFromTeam(optional.get());
            LOGGER.info(objectMapper.writeValueAsString(user));
            return Response.ok(user).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("details")
    @UnitOfWork
    @Timed
    @Authenticator
    @SneakyThrows
    /*
      Get user details. includes total team value, available funds and player details
     **/
    public Response getUserDetails(@Context HttpHeaders headers) {
        String userName = Helper.getUserNameFromHeaders(headers);
        Optional<Team> optional = teamDAO.findByUserName(userName);
        if (optional.isPresent()) {
            Team team = optional.get();
            List<Player> players = playerDAO.getPlayersByTeamId(team.getId());
            UserDetails details = UserDetails.builder()
                    .team(team)
                    .players(players)
                    .build();
            LOGGER.info(objectMapper.writeValueAsString(details));
            return Response.ok(details).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/create")
    @UnitOfWork
    @Timed
    /*
    Create a new user and its corresponding team and players
     **/
    public Response createUser(@Valid User user) {
        checkDuplicateUser(user);
        Team team = Team.builder()
                .userName(user.getUserName())
                .secret(Helper.getSHA256Hash(user.getSecret())) // store sha256 hash of password
                .teamName(user.getTeamName())
                .teamCountry(user.getCountry())
                .fundsAvailable(Helper.INIT_FUNDS_VALUE)
                .build();
        Optional<Team> optional = teamDAO.createTeam(team);
        if (optional.isEmpty()) {
            LOGGER.error("Unable to create team for user: " + user.getUserName());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<Player> players = Helper.buildTeamPlayers(team);
        for (Player player : players) {
            playerDAO.createPlayer(player);
        }

        UserDetails details = UserDetails.builder()
                .team(team)
                .players(players)
                .build();
        return Response.ok(details).build();
    }

    private String getSecretHash(UserCredential cred) {
        Optional<Team> optional = teamDAO.findByUserName(cred.getUserName());
        if (optional.isPresent()) {
            String sha256 = Helper.getSHA256Hash(cred.getPassword());
            if (sha256.equals(optional.get().getSecret()))
                return sha256;
        }
        return null;
    }

    private void checkDuplicateUser(User user) {
        Optional<Team> optional = teamDAO.findByUserName(user.getUserName());
        if (optional.isPresent()) {
            throw new WebApplicationException(ErrorCode.DUPLICATE_USER.getDescription(), Response.Status.BAD_REQUEST);
        }
    }
}
