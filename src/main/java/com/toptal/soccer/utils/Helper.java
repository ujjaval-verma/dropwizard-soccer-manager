package com.toptal.soccer.utils;

import com.google.common.hash.Hashing;
import com.toptal.soccer.entities.Player;
import com.toptal.soccer.entities.Team;
import com.toptal.soccer.entities.User;
import com.toptal.soccer.enums.Role;
import com.toptal.soccer.persistence.TeamDAO;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Helper {
    public static final String USER_HEADER = "X-SOCCER-USER";
    public static final String TOKEN_HEADER = "X-AUTH-TOKEN";
    public static final long INIT_PLAYER_VALUE = 1000000; // initial player value = 1 million
    public static final long INIT_FUNDS_VALUE = 5000000; // initial funds available = 5 million
    public static final int INIT_GOALKEEPER_COUNT = 3;
    public static final int INIT_DEFENDER_COUNT = 6;
    public static final int INIT_MIDFIELDER_COUNT = 6;
    public static final int INIT_ATTACKER_COUNT = 5;
    public static final long PLAYER_MIN_AGE = 18;
    public static final long PLAYER_MAX_AGE = 40;

    public static Player buildPlayer(Team team, Role role, String appender) {
        return Player.builder()
                .firstName(team.getTeamName().substring(0, Math.min(team.getTeamName().length(), 10)))
                .lastName(role.name().substring(0, Math.min(role.name().length(), 8)) + " " + appender)
                .age(getRandomNumber(PLAYER_MIN_AGE, PLAYER_MAX_AGE))
                .country(team.getTeamCountry())
                .role(role)
                .teamId(team.getId())
                .value(INIT_PLAYER_VALUE)
                .build();
    }

    public static List<Player> buildPlayers(Team team, Role role, int count) {
        List<Player> players = new ArrayList<>();
        for (int i=0; i < count; i++) {
            String appender = Long.toHexString(getRandomNumber(16, 255)); // generate a random appender of length 2
            Player player = buildPlayer(team, role, appender);
            players.add(player);
        }
        return players;
    }

    public static List<Player> buildTeamPlayers(Team team) {
        List<Player> players = new ArrayList<>();
        players.addAll(buildPlayers(team, Role.GOALKEEPER, INIT_GOALKEEPER_COUNT));
        players.addAll(buildPlayers(team, Role.MIDFIELDER, INIT_MIDFIELDER_COUNT));
        players.addAll(buildPlayers(team, Role.DEFENDER, INIT_DEFENDER_COUNT));
        players.addAll(buildPlayers(team, Role.ATTACKER, INIT_ATTACKER_COUNT));
        return players;
    }

    public static long getRandomNumber(long min, long max) {
        return (long) ((Math.random() * (1 + max - min)) + min);
    }

    public static String getSHA256Hash(String value) {
        return Hashing.sha256()
                .hashString(value, StandardCharsets.UTF_8)
                .toString();
    }

    public static String extractHeader(ContainerRequestContext context, String headerName) {
        return context.getHeaderString(headerName);
    }

    public static User getUserFromTeam(Team team) {
        return User.builder()
                .userName(team.getUserName())
                .secret(team.getSecret())
                .teamName(team.getTeamName())
                .country(team.getTeamCountry())
                .build();
    }

    public static String getUserNameFromHeaders(HttpHeaders headers) {
        return headers.getRequestHeaders().getFirst(USER_HEADER);
    }

    public static long getHoursSinceZeroEpoch() {
        return System.currentTimeMillis() / 3600000;
    }

    public static long increasePlayerValue(long value) {
        // increase input value by 10% - 100%
        // only increases by integral percentages. does not do fractional percentage increases
        // i.e. 57% increase is possible but 57.3% increase is not
        double increaseFraction = (100 + getRandomNumber(10, 100)) / 100.0;
        double increased = value * increaseFraction;
        return (long) increased;
    }

    public static long getTeamIdByUserName(TeamDAO dao, String userName) {
        return dao.findByUserName(userName).get().getId();
    }
}
