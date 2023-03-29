package com.toptal.soccer.persistence;

import com.google.common.base.Strings;
import com.toptal.soccer.entities.Team;
import com.toptal.soccer.entities.Transfer;
import com.toptal.soccer.enums.ErrorCode;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;


public class TeamDAO extends AbstractDAO<Team> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeamDAO.class);
    public TeamDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<Team> findAll() {
        return list(namedTypedQuery("com.toptal.soccer.entities.Team.findAll"));
    }
    public Optional<Team> findById(long id) {
        return Optional.ofNullable(get(id));
    }

    public Optional<Team> createTeam(Team team) {
        if (findByUserName(team.getUserName()).isEmpty())
            return Optional.ofNullable(persist(team));
        throw new WebApplicationException(ErrorCode.DUPLICATE_ENTITY.getDescription(), Response.Status.BAD_REQUEST);
    }

    public Optional<Team> updateTeam(Team team) {
        Team entity = get(team.getId());
        if (entity != null) {
            if (!Strings.isNullOrEmpty(team.getTeamName())) {
                entity.setTeamName(team.getTeamName());
            }
            if (!Strings.isNullOrEmpty(team.getTeamCountry())) {
                entity.setTeamCountry(team.getTeamCountry());
            }
            return Optional.ofNullable(persist(entity));
        }
        LOGGER.warn("No team found for teamId: " + team.getId());
        return Optional.empty();
    }

    public Optional<Team> findByUserName(String userName) {
        List<Team> teams = getTeams(userName);
        if (teams.isEmpty())
            return Optional.empty();
        return Optional.ofNullable(teams.get(0));
    }

    public ErrorCode updateTeamBudgets(Transfer transfer) {
        long transferValue = transfer.getAskingPrice();
        Team prevOwner = get(transfer.getTransferFrom());
        Team newOwner = get(transfer.getTransferTo());
        if (prevOwner == null || newOwner == null) {
            // cannot update team budget with missing team information
            return ErrorCode.INCOMPLETE_TRANSFER;
        }
        if (newOwner.getFundsAvailable() < transferValue) {
            // the bidding team doesn't have enough funds to buy this player at the asking price
            return ErrorCode.INSUFFICIENT_BALANCE;
        }
        prevOwner.setFundsAvailable(prevOwner.getFundsAvailable() + transferValue);
        newOwner.setFundsAvailable(newOwner.getFundsAvailable() - transferValue);
        persist(prevOwner);
        persist(newOwner);
        return null;
    }

    private List<Team> getTeams(String userName) {
        return list(namedTypedQuery("com.toptal.soccer.entities.Team.findByUserName")
                .setParameter("userName", userName));
    }

}
