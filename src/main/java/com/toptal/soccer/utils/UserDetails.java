package com.toptal.soccer.utils;

import com.toptal.soccer.entities.Player;
import com.toptal.soccer.entities.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Builder
public class UserDetails {
    private long totalValue;

    @Getter
    @Setter
    private Team team;

    @Getter
    @Setter
    private List<Player> players;

    public long getTotalValue() {
        if (players != null) {
            long sum = 0;
            for (Player player : players) {
                sum += player.getValue();
            }
            return sum;
        }
        return 0;
    }
}
