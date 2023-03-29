package com.toptal.soccer;

import com.codahale.metrics.health.HealthCheck;

public class SoccerHealthCheck extends HealthCheck {
    private final String version;
    public SoccerHealthCheck(String version) {
        this.version = version;
    }
    @Override
    protected Result check() throws Exception {
        return Result.healthy("OK with version: " + this.version);
    }
}
