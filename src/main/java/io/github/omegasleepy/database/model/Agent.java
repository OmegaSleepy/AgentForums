package io.github.omegasleepy.database.model;

import java.time.Instant;
import java.util.Objects;

public class Agent {
    private String id;
    private String username;
    private String email;
    private boolean active;
    private Instant createdAt;

    public Agent () {
    }

    public Agent (String id, String username, String email, boolean active, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.active = active;
        this.createdAt = createdAt;
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getUsername () {
        return username;
    }

    public void setUsername (String username) {
        this.username = username;
    }

    public String getEmail () {
        return email;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public boolean isActive () {
        return active;
    }

    public void setActive (boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt () {
        return createdAt;
    }

    public void setCreatedAt (Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return Objects.equals(id, agent.id);
    }

    @Override
    public int hashCode () {
        return Objects.hash(id);
    }
}