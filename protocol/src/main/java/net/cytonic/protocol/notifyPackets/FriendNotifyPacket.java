package net.cytonic.protocol.notifyPackets;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.notifyPackets.FriendNotifyPacket.Packet;
import net.cytonic.protocol.objects.FriendApiProtocolObject.Response;
import net.cytonic.protocol.utils.NatsAPI;

@NoArgsConstructor
@AllArgsConstructor
public class FriendNotifyPacket extends ProtocolObject<Packet, Response> {

    private String subject;

    @Override
    public String getSubject() {
        return subject;
    }

    public record Packet(UUID sender, UUID recipient) implements Message<Packet, Response> {

        @Override
        public ProtocolObject<Packet, Response> getProtocolObject() {
            return new FriendNotifyPacket();
        }

        public void publish(String subject) {
            NatsAPI.INSTANCE.publish(subject, getProtocolObject().serializeToString(this));
        }
    }
}
