package com.toptal.soccer.utils;
/*
 * @created 05/06/2022
 * @author  ujjaval.verma
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {

    @Getter
    @Setter
    @JsonProperty("playerId")
    private long playerId;

    @Getter
    @Setter
    @JsonProperty("askingPrice")
    private long askingPrice;
}
