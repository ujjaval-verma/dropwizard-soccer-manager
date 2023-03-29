package com.toptal.soccer.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toptal.soccer.entities.Player;
import com.toptal.soccer.enums.ErrorCode;
import com.toptal.soccer.filters.Authenticator;
import com.toptal.soccer.persistence.PlayerDAO;
import com.toptal.soccer.persistence.TeamDAO;
import com.toptal.soccer.utils.Helper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/player")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticator
public class PlayerResource {
    private PlayerDAO playerDAO;
    private TeamDAO teamDAO;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerResource.class);

    public PlayerResource(PlayerDAO playerDAO, TeamDAO teamDAO) {
        this.playerDAO = playerDAO;
        this.teamDAO = teamDAO;
    }

    @GET
    @Path("/{id}")
    @UnitOfWork
    @Timed
    public Response getPlayer(@PathParam("id") @Valid long id) throws JsonProcessingException {
        Optional<Player> optional = playerDAO.getPlayerById(id);
        if (optional.isPresent()) {
            LOGGER.info(objectMapper.writeValueAsString(optional.get()));
            return Response.ok(optional.get()).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("/{id}")
    @UnitOfWork
    @Timed
    @SneakyThrows
    public Response updatePlayer(@Context HttpHeaders headers, @PathParam("id") @Valid long id, Player player) {
        String userName = Helper.getUserNameFromHeaders(headers);
        long teamId = Helper.getTeamIdByUserName(teamDAO, userName);
        if (teamId != player.getTeamId()) {
            throw new WebApplicationException(ErrorCode.FORBIDDEN_PLAYER.getDescription(), Response.Status.FORBIDDEN);
        }
        player.setId(id);
        Optional<Player> optional = playerDAO.updatePlayerDetails(player);
        if (optional.isPresent()) {
            LOGGER.info(objectMapper.writeValueAsString(optional.get()));
            return Response.ok(optional.get()).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("/bulkUpdate")
    @UnitOfWork
    @Timed
    @SneakyThrows
    public Response updatePlayers(@Context HttpHeaders headers, List<Player> players) {
        String userName = Helper.getUserNameFromHeaders(headers);
        long teamId = Helper.getTeamIdByUserName(teamDAO, userName);
        List<Player> updatedPlayers = new ArrayList<>();
        for (Player player : players) {
            if (teamId == player.getTeamId()) { // verify that this player is owned by the logged-in user
                Optional<Player> optional = playerDAO.updatePlayerDetails(player);
                if (optional.isPresent()) {
                    LOGGER.info(objectMapper.writeValueAsString(optional.get()));
                    updatedPlayers.add(optional.get());
                }
            }
        }

        if (updatedPlayers.size() > 0) {
            return Response.ok(updatedPlayers).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
