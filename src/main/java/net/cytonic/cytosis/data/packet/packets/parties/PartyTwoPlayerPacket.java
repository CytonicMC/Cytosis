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
public class PartyTwoPlayerPacket extends Packet<PartyTwoPlayerPacket> {

    @SerializedName("party_id")
    private final UUID party;
    @SerializedName("player_id")
    private final UUID player;
    @SerializedName("sender_id")
    private final UUID sender;
    private transient Type type;

    @Override
    public String getSubject() {
        return type.subject;
    }

    @Override
    protected Serializer<PartyTwoPlayerPacket> getSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(String subject, PartyTwoPlayerPacket value) {
                return new DefaultGsonSerializer<>(PartyTwoPlayerPacket.class).serialize(subject, value);
            }

            @Override
            public PartyTwoPlayerPacket deserialize(String subject, String json) {
                PartyTwoPlayerPacket packet =
                    new DefaultGsonSerializer<>(PartyTwoPlayerPacket.class).deserialize(subject, json);
                packet.setType(Type.findBySubject(subject));
                return packet;
            }
        };
    }

    @Getter
    @AllArgsConstructor
    public enum Type {
        //when 1 player promotes another player
        PROMOTE(Subjects.PARTY_PROMOTE_REQUEST, Subjects.PARTY_PROMOTE_NOTIFY),
        //when 1 player kicks another player
        KICK(Subjects.PARTY_KICK_REQUEST, Subjects.PARTY_KICK_NOTIFY),
        //when 1 player transfers the party to another player
        TRANSFER(Subjects.PARTY_TRANSFER_REQUEST, Subjects.PARTY_TRANSFER_NOTIFY);
        private final String subject;
        private final String notifySubject;

        public static Type findBySubject(String subject) {
            for (Type type : Type.values()) {
                if (type.subject.equals(subject) || type.notifySubject.equals(subject)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown subject '" + subject + "'");
        }
    }
}
