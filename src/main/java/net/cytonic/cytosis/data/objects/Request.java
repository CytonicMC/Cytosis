package net.cytonic.cytosis.data.objects;

import java.time.Instant;
import java.util.UUID;

public class Request {
    private final Instant expiry;
    private final UUID author;
    private final UUID target;
    private final RequestType type;
    private final Instant issuedAt;
    private boolean accepted;
    private boolean declined;

    public Request(Instant expiry, UUID author, UUID target, RequestType type) {
        this.expiry = expiry;
        this.author = author;
        this.target = target;
        this.type = type;
        issuedAt = Instant.now();
        accepted = false;
        declined = false;
    }

    public Instant getExpiry() {
        return expiry;
    }

    public RequestType getType() {
        return type;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public UUID getAuthor() {
        return author;
    }

    public UUID getTarget() {
        return target;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isDeclined() {
        return declined;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public void setDeclined(boolean declined) {
        this.declined = declined;
    }

    public enum RequestType {
        FRIEND,
        LEAGUE,
        PARTY
    }
}
