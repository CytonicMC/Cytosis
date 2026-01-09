package net.cytonic.protocol.notifyPackets;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NotifyPacket;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.notifyPackets.ServerStatusNotifyPacket.Packet;

@NoArgsConstructor
@AllArgsConstructor
public class ServerStatusNotifyPacket extends NotifyPacket<Packet> {

    private Type type;

    @Override
    public String getSubject() {
        return type.getSubject();
    }

    public record Packet(
        String type,
        String ip,
        String id,
        int port,
        @Nullable
        @SerializedName("last_seen")
        Instant lastSeen,
        String group,
        Type subject
    ) implements Message<Packet, Void> {

        @Override
        public ProtocolObject<Packet, Void> getProtocolObject() {
            return new ServerStatusNotifyPacket();
        }

        @Override
        public void publish() {
            publish(subject.getSubject());
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Type {
        STARTUP("servers.register"),
        SHUTDOWN("servers.shutdown"),
        NOTIFY_SHUTDOWN("servers.proxy.shutdown.notify");
        private final String subject;
    }
}
