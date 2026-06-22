package net.cytonic.protocol.impl.notify;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.impl.notify.ServerStatusNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class ServerStatusNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException("ServerStatusNotifyPacket does not have a default subject!");
    }

    public record Packet(
        String ip,
        String id,
        int port,
        @Nullable
        @SerializedName("last_seen")
        Instant lastSeen,
        Key type
    ) implements Message<Packet, Void> {

    }
}
