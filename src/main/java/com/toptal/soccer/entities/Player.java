package com.toptal.soccer.entities;

import com.toptal.soccer.utils.Helper;
import com.toptal.soccer.enums.Role;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "players")

@NamedQueries({
        @NamedQuery(name = "com.toptal.soccer.entities.Player.findByTeamId",
                query = "select e from Player e "
                        + "where e.teamId = :teamId")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    /**
     * Entity's unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private long id;

    @NotEmpty
    @Getter
    @Setter
    @Column(name = "first_name")
    private String firstName;

    @Getter
    @Setter
    @Column(name = "last_name")
    private String lastName;

    @Getter
    @Setter
    @Column(name = "country")
    private String country;

    @Min(Helper.PLAYER_MIN_AGE)
    @Max(Helper.PLAYER_MAX_AGE)
    @Getter
    @Setter
    @Column(name = "age")
    private long age;

    @Getter
    @Setter
    @Column(name = "team_id")
    private long teamId;

    @Getter
    @Setter
    @Column(name = "role")
    private Role role;

    @Min(Helper.INIT_PLAYER_VALUE)
    @Getter
    @Setter
    @Column(name = "value")
    private long value;
}
