package net.cytonic.protocol.notifyPackets;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NotifyPacket;
import net.cytonic.protocol.notifyPackets.ServerStatusNotifyPacket.Packet;

@Internal
public class ServerStatusNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException("ServerStatusNotifyPacket does not have a default subject!");
    }

    @Internal
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
