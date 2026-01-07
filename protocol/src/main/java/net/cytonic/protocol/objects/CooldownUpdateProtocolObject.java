package net.cytonic.protocol.objects;

import java.time.Instant;
import java.util.UUID;

import net.kyori.adventure.key.Key;

import net.cytonic.protocol.GsonSerializer;
import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.Serializer;
import net.cytonic.protocol.objects.CooldownUpdateProtocolObject.Packet;

public class CooldownUpdateProtocolObject extends NoResponse<Packet> {

    @Override
    public Serializer<Packet> getSerializer() {
        return new GsonSerializer<>(Packet.class);
    }

    @Override
    public String getSubject() {
        return "cooldown.update";
    }

    public enum Type {
        GLOBAL,
        PERSONAL
    }

    public record Packet(Key namespace, Instant expiry, UUID userUUID, Type type) implements Message<Packet, Void> {

        @Override
        public ProtocolObject<Packet, Void> getProtocolObject() {
            return new CooldownUpdateProtocolObject();
        }
    }
}
