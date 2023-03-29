package com.toptal.soccer.enums;
/*
 * @created 05/06/2022
 * @author  ujjaval.verma
 */

import lombok.Getter;

public enum ErrorCode {
    MISSING_FIELDS("Missing fields in the request body"),
    DUPLICATE_USER("another user with this username already exists"),
    DUPLICATE_TRANSFER("This player already has an active transfer listing"),
    DUPLICATE_ENTITY("Duplicate entity already exists"),
    FORBIDDEN_BUY("The selling team and buying  team cannot be same"),
    FORBIDDEN_PLAYER("Only player of own team can be edited"),
    INSUFFICIENT_BALANCE("Insufficient funds to complete this transfer"),
    INCOMPLETE_TRANSFER("Cannot perform this operation on an incomplete transfer"),
    INCORRECT_PLAYER("the player doesn't belong to this user");

    @Getter
    private final String description;
    ErrorCode(String description) {
        this.description = description;
    }
}
