package net.cytonic.cytosis.snooper;

import java.sql.Timestamp;

public record QueriedSnoop(int id, byte permission, String rawContent, String channel, Timestamp timestamp) {
}
