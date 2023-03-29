package com.toptal.soccer.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    /**
     * User's email address
     */
    @Getter
    @Setter
    @NotEmpty
    @JsonProperty("userName")
    private String userName;

    /**
     * SHA256 hash of user's password.
     */
    @Getter
    @Setter
    @NotEmpty
    @JsonProperty("secret")
    private String secret;

    @Getter
    @Setter
    @NotNull
    @JsonProperty("teamName")
    private String teamName;

    @Getter
    @Setter
    @NotNull
    @JsonProperty("country")
    private String country;
}
