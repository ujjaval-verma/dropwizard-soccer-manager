package com.toptal.soccer.entities;
/*
 * @created 05/06/2022
 * @author  ujjaval.verma
 */

import com.toptal.soccer.enums.Role;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "transfers")

@NamedQueries({
        @NamedQuery(name = "com.toptal.soccer.entities.Transfer.findActiveTransfers",
                query = "select e from Transfer e where e.completed = false order by e.createdAt desc"),
        @NamedQuery(name = "com.toptal.soccer.entities.Transfer.findActiveTransferByPlayerId",
                query = "select e from Transfer e "
                        + "where e.completed = false and e.playerId = :playerId")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {
    /**
     * Entity's unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private long id;

    @NotNull
    @Getter
    @Setter
    @Column(name = "player_id")
    private long playerId;

    @NotNull
    @Getter
    @Setter
    @Column(name = "player_role")
    private Role playerRole;

    @NotNull
    @Getter
    @Setter
    @Column(name = "asking_price")
    private long askingPrice;

    @NotNull
    @Getter
    @Setter
    @Column(name = "transfer_from")
    private long transferFrom;

    @Getter
    @Setter
    @Column(name = "transfer_to")
    private long transferTo;

    @Getter
    @Setter
    @Column(name = "completed")
    private boolean completed;

    @Getter
    @Setter
    @Column(name = "created_at")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}
