package com.toptal.soccer.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toptal.soccer.entities.Team;
import com.toptal.soccer.filters.Authenticator;
import com.toptal.soccer.persistence.PlayerDAO;
import com.toptal.soccer.persistence.TeamDAO;
import com.toptal.soccer.utils.Helper;
import io.dropwizard.hibernate.UnitOfWork;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/team")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticator
public class TeamResource {
    private TeamDAO teamDAO;
    private PlayerDAO playerDAO;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(TeamResource.class);
    public TeamResource(TeamDAO teamDAO, PlayerDAO playerDAO) {
        this.teamDAO = teamDAO;
        this.playerDAO = playerDAO;
    }

    @GET
    @UnitOfWork
    @Timed
    @SneakyThrows
    public Response getTeam(@Context HttpHeaders headers) {
        String userName = Helper.getUserNameFromHeaders(headers);
        Optional<Team> optional = teamDAO.findByUserName(userName);
        if (optional.isPresent()) {
            LOGGER.info(objectMapper.writeValueAsString(optional.get()));
            return Response.ok(optional.get()).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @UnitOfWork
    @Timed
    @Path("/all")
    // TODO: support pagination
    public Response getAllTeams() {
        List<Team> teams = teamDAO.findAll();
        if (teams != null && teams.size() > 0) {
            return Response.ok(teams).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @UnitOfWork
    @Timed
    @SneakyThrows
    public Response updateTeam(@Context HttpHeaders headers, Team team) {
        String userName = Helper.getUserNameFromHeaders(headers);
        team.setId(teamDAO.findByUserName(userName).get().getId());
        Optional<Team> optional = teamDAO.updateTeam(team);
        if (optional.isPresent()) {
            LOGGER.info(objectMapper.writeValueAsString(optional.get()));
            return Response.ok(optional.get()).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
