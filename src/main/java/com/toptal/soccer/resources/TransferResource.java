package com.toptal.soccer.resources;
/*
 * @created 05/06/2022
 * @author  ujjaval.verma
 */

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toptal.soccer.entities.Player;
import com.toptal.soccer.entities.Team;
import com.toptal.soccer.entities.Transfer;
import com.toptal.soccer.enums.ErrorCode;
import com.toptal.soccer.filters.Authenticator;
import com.toptal.soccer.persistence.PlayerDAO;
import com.toptal.soccer.persistence.TeamDAO;
import com.toptal.soccer.persistence.TransferDAO;
import com.toptal.soccer.utils.Helper;
import com.toptal.soccer.utils.TransferRequest;
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

@Path("/transfer")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticator
public class TransferResource {
    private TeamDAO teamDAO;
    private PlayerDAO playerDAO;
    private TransferDAO transferDAO;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferResource.class);

    public TransferResource(TeamDAO teamDAO, PlayerDAO playerDAO, TransferDAO transferDAO) {
        this.teamDAO = teamDAO;
        this.playerDAO = playerDAO;
        this.transferDAO = transferDAO;
    }

    @GET
    @Path("/list")
    @UnitOfWork
    @Timed
    public Response listActiveTransfers() {
        List<Transfer> transfers = transferDAO.getActiveTransfers();
        LOGGER.info("found {} active transfers", transfers.size());
        return Response.ok(transfers).build();
    }

    @POST
    @Path("/initiate")
    @UnitOfWork
    @Timed
    public Response addPlayerToTransferList(@Context HttpHeaders headers, @Valid TransferRequest request) {
        String userName = Helper.getUserNameFromHeaders(headers);

        Transfer transfer = validateInitiationRequest(userName, request);
        if (transfer != null) {
            Transfer entity = transferDAO.createTransfer(transfer).get();
            LOGGER.info("Added entry to transfer list: {}", entity);
            return Response.ok(entity).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/purchase/{playerId}")
    @UnitOfWork
    @Timed
    public Response buyPlayerFromTransferList(@Context HttpHeaders headers, @PathParam("playerId") @Valid long playerId) {
        String userName = Helper.getUserNameFromHeaders(headers);
        LOGGER.info("Transferring playerId: {} to user: {}", playerId, userName);
        Transfer transfer = validateCompletionRequest(userName, playerId);
        if (transfer != null) {
            // complete the transfer
            Transfer persistedTransfer = transferDAO.completeTransfer(transfer).get();

            // Update team budgets
            ErrorCode err = teamDAO.updateTeamBudgets(persistedTransfer);
            if (err != null) {
                throw new WebApplicationException(err.getDescription(), Response.Status.BAD_REQUEST);
            }

            // Update player team
            playerDAO.updatePlayerAfterTransfer(persistedTransfer);
            return Response.ok(persistedTransfer).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @SneakyThrows
    private Transfer validateInitiationRequest(String userName, TransferRequest request) {
        if (request.getPlayerId() == 0 || request.getAskingPrice() == 0) {
            // zero values for long fields imply missing fields in the request
            LOGGER.warn("missing params in request: {}", objectMapper.writeValueAsString(request));
            throw new WebApplicationException(ErrorCode.MISSING_FIELDS.getDescription(), Response.Status.BAD_REQUEST);
        }
        Optional<Player> optional = playerDAO.getPlayerById(request.getPlayerId());
        if (optional.isPresent()) {
            Player player = optional.get();
            Optional<Team> optTeam = teamDAO.findByUserName(userName);
            if (optTeam.isEmpty()) {
                // no team found for this userName
                throw new WebApplicationException("no team found for userName: " + userName, Response.Status.NOT_FOUND);
            }
            Team team = optTeam.get();
            Optional<Transfer> existingTransfer = transferDAO.getActiveTransferByPlayerId(player.getId());
            if (existingTransfer.isPresent()) {
                // the player is already on an active transfer list
                throw new WebApplicationException(ErrorCode.DUPLICATE_TRANSFER.getDescription(), Response.Status.CONFLICT);
            }

            if (team.getId() != player.getTeamId()) {
                // the player doesn't belong to this user
                throw new WebApplicationException(ErrorCode.INCORRECT_PLAYER.getDescription(), Response.Status.UNAUTHORIZED);
            }
            return buildTransfer(player, team, request.getAskingPrice());
        }
        throw new WebApplicationException("no player found for playerId: " + request.getPlayerId(), Response.Status.NOT_FOUND);
    }

    private Transfer validateCompletionRequest(String userName, long playerId) {
        Optional<Transfer> optTransfer = transferDAO.getActiveTransferByPlayerId(playerId);
        if (optTransfer.isEmpty()) {
            throw new WebApplicationException("no active transfer found for playerId: " + playerId, Response.Status.NOT_FOUND);
        }
        Optional<Team> optTeam = teamDAO.findByUserName(userName);
        if (optTeam.isEmpty()) {
            // no team found for this userName
            throw new WebApplicationException("no team found for userName: " + userName, Response.Status.NOT_FOUND);
        }
        Transfer transfer = optTransfer.get();
        Team toTeam = optTeam.get();

        if (transfer.getAskingPrice() > toTeam.getFundsAvailable()) {
            throw new WebApplicationException(ErrorCode.INSUFFICIENT_BALANCE.getDescription(), Response.Status.BAD_REQUEST);
        }

        // mark toTeam for this transfer
        transfer.setTransferTo(toTeam.getId());

        if (transfer.getTransferFrom() == transfer.getTransferTo()) {
            throw new WebApplicationException(ErrorCode.FORBIDDEN_BUY.getDescription(), Response.Status.FORBIDDEN);
        }

        return transfer;
    }

    private Transfer buildTransfer(Player player, Team team, long askingPrice) {
        return Transfer.builder()
                .playerId(player.getId())
                .playerRole(player.getRole())
                .askingPrice(askingPrice)
                .transferFrom(team.getId())
                .completed(false)
                .build();
    }
}
