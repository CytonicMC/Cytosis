package net.cytonic.cytosis.data.packet.packets.parties;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.Packet;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@Setter
@AllArgsConstructor
public class PartyStatePacket extends Packet<PartyStatePacket> {

    @SerializedName("party_id")
    private final UUID party;
    @SerializedName("player_id")
    private final UUID player;
    @SerializedName("state")
    private final boolean state;
    private transient Type type;

    @Override
    protected Serializer<PartyStatePacket> getSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(String subject, PartyStatePacket value) {
                return new DefaultGsonSerializer<>(PartyStatePacket.class).serialize(subject, value);
            }

            @Override
            public PartyStatePacket deserialize(String subject, String json) {
                PartyStatePacket packet =
                    new DefaultGsonSerializer<>(PartyStatePacket.class).deserialize(subject, json);
                packet.setType(Type.findBySubject(subject));
                return packet;
            }
        };
    }

    @Getter
    @AllArgsConstructor
    public enum Type {
        MUTE(Subjects.PARTY_STATE_MUTE_REQUEST),
        OPEN(Subjects.PARTY_STATE_OPEN_REQUEST),
        OPEN_INVITES(Subjects.PARTY_STATE_OPEN_REQUEST);
        private final String subject;

        public static Type findBySubject(String subject) {
            for (Type type : Type.values()) {
                if (type.subject.matches(subject)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown subject '" + subject + "'");
        }
    }
}
