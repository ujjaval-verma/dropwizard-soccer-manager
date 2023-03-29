package com.toptal.soccer.persistence;

/*
 * @created 05/06/2022
 * @author  ujjaval.verma
 */

import com.toptal.soccer.entities.Transfer;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class TransferDAO extends AbstractDAO<Transfer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferDAO.class);

    public TransferDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Optional<Transfer> getTransferById(long id) {
        return Optional.ofNullable(get(id));
    }

    public Optional<Transfer> getActiveTransferByPlayerId(long playerId) {
        List<Transfer> transfers = getActiveTransfersByPlayerId(playerId);
        LOGGER.info("TransferDAO.getActiveTransferByPlayerId: playerId: {}, match found: {}", playerId, !transfers.isEmpty());
        if (transfers != null && !transfers.isEmpty()) {
            return Optional.ofNullable(transfers.get(0));
        }
        return Optional.empty();
    }

    public Optional<Transfer> createTransfer(Transfer transfer) {
        // always mark a new transfer as active
        transfer.setCompleted(false);
        return Optional.ofNullable(persist(transfer));
    }

    public Optional<Transfer> completeTransfer(Transfer transfer) {
        Optional<Transfer> optional = getActiveTransferByPlayerId(transfer.getPlayerId());
        if (optional.isPresent()) {
            Transfer entity = optional.get();
            entity.setCompleted(true);
            entity.setTransferTo(transfer.getTransferTo());
            return Optional.ofNullable(persist(entity));
        }
        LOGGER.warn("No active transfer found for playerId: {}", transfer.getPlayerId());
        return Optional.empty();
    }

    public List<Transfer> getActiveTransfers() {
        return list(namedTypedQuery("com.toptal.soccer.entities.Transfer.findActiveTransfers"));
    }

    private List<Transfer> getActiveTransfersByPlayerId(long playerId) {
        return list(namedTypedQuery("com.toptal.soccer.entities.Transfer.findActiveTransferByPlayerId")
                .setParameter("playerId", playerId));
    }
}
