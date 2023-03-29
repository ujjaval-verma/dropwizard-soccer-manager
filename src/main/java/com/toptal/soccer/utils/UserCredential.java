package com.toptal.soccer.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
public class UserCredential {
    /**
     * User's email address
     */
    @Getter
    @Setter
    @NotEmpty
    @JsonProperty("userName")
    private String userName;

    /**
     * user's password.
     */
    @Getter
    @Setter
    @NotEmpty
    @JsonProperty("password")
    private String password;
}
