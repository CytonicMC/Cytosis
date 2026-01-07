package net.cytonic.protocol.objects;

import net.cytonic.protocol.GsonSerializer;
import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.Serializer;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.objects.BroadcastProtocolObject.Packet;

public class BroadcastProtocolObject extends NoResponse<Packet> {

    @Override
    public Serializer<Packet> getSerializer() {
        return new GsonSerializer<>(Packet.class);
    }

    @Override
    public String getSubject() {
        return "chat.broadcast";
    }

    public record Packet(JsonComponent message) implements Message<Packet, Void> {

        @Override
        public ProtocolObject<Packet, Void> getProtocolObject() {
            return new BroadcastProtocolObject();
        }
    }
}
