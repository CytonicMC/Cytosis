package net.cytonic.protocol.impl.notifyPackets;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.impl.notifyPackets.ServerStatusNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class ServerStatusNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException("ServerStatusNotifyPacket does not have a default subject!");
    }

    public record Packet(
        String type,
        String ip,
        String id,
        int port,
        @Nullable
        @SerializedName("last_seen")
        Instant lastSeen,
        String group
    ) implements Message<Packet, Void> {

    }
}
