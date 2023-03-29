package com.toptal.soccer.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name="teams")
@NamedQueries({
        @NamedQuery(name = "com.toptal.soccer.entities.Team.findAll",
                query = "select e from Team e"),
        @NamedQuery(name = "com.toptal.soccer.entities.Team.findByUserName",
                query = "select e from Team e "
                        + "where e.userName = :userName")
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Team {
    /**
     * Entity's unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private long id;

    /**
     * User's email address
     */
    @Column(name = "user_name")
    @Getter
    @Setter
    @NotEmpty
    private String userName;

    /**
     * SHA256 hash of user's password.
     */
    @Column(name = "secret")
    @Getter
    @Setter
    @NotEmpty
    private String secret;

    @Column(name = "team_name")
    @Getter
    @Setter
    @NotNull
    private String teamName;

    @Column(name = "team_country")
    @Getter
    @Setter
    private String teamCountry;

    @Column(name = "funds_available")
    @Getter
    @Setter
    private long fundsAvailable;

    @Override
    public int hashCode() {
        return Objects.hash(id, userName, teamName, teamCountry);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Team)) {
            return false;
        }

        Team team = (Team) o;

        return id == team.id &&
                Objects.equals(userName, team.userName) &&
                Objects.equals(teamName, team.teamName);
    }
}
