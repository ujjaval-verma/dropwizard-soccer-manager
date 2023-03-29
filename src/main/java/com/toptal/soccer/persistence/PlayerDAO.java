package com.toptal.soccer.persistence;

import com.google.common.base.Strings;
import com.toptal.soccer.entities.Player;
import com.toptal.soccer.entities.Transfer;
import com.toptal.soccer.utils.Helper;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class PlayerDAO extends AbstractDAO<Player> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerDAO.class);
    public PlayerDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
    public Optional<Player> getPlayerById(long id) {
        return Optional.ofNullable(get(id));
    }
    public Optional<Player> createPlayer(Player player) {
        return Optional.of(persist(player));
    }
    public Optional<Player> updatePlayerDetails(Player player) {
        Player entity = get(player.getId());
        if (entity != null) {
            if (!Strings.isNullOrEmpty(player.getFirstName())) {
                entity.setFirstName(player.getFirstName());
            }
            if (!Strings.isNullOrEmpty(player.getLastName())) {
                entity.setLastName(player.getLastName());
            }
            if (!Strings.isNullOrEmpty(player.getCountry())) {
                entity.setCountry(player.getCountry());
            }
            return Optional.ofNullable(persist(entity));
        }
        LOGGER.warn("No player found for playerId: " + player.getId());
        return Optional.empty();
    }

    public Optional<Player> updatePlayerAfterTransfer(Transfer transfer) {
        Player entity = get(transfer.getPlayerId());
        if (entity != null) {
            // Update player's team
            entity.setTeamId(transfer.getTransferTo());

            // Increase player value by 10% - 100%
            entity.setValue(Helper.increasePlayerValue(entity.getValue()));

            return Optional.ofNullable(persist(entity));
        }
        LOGGER.warn("No player found for playerId: " + transfer.getPlayerId());
        return Optional.empty();
    }

    public List<Player> getPlayersByTeamId(long teamId) {
        return list(namedTypedQuery("com.toptal.soccer.entities.Player.findByTeamId")
                .setParameter("teamId", teamId));
    }
}
