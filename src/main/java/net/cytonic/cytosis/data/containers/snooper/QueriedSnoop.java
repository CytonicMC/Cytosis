package net.cytonic.cytosis.data.containers.snooper;

import java.sql.Timestamp;

public record QueriedSnoop(int id, byte permission, String rawContent, String channel, Timestamp timestamp) {

}
