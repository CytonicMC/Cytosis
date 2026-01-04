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
public class PartyOnePlayerPacket extends Packet<PartyOnePlayerPacket> {

    @SerializedName("party_id")
    private final UUID party;
    @SerializedName("player_id")
    private final UUID player;
    private transient Type type;

    @Override
    protected Serializer<PartyOnePlayerPacket> getSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(String subject, PartyOnePlayerPacket value) {
                return new DefaultGsonSerializer<>(PartyOnePlayerPacket.class).serialize(subject, value);
            }

            @Override
            public PartyOnePlayerPacket deserialize(String subject, String json) {
                PartyOnePlayerPacket packet =
                    new DefaultGsonSerializer<>(PartyOnePlayerPacket.class).deserialize(subject, json);
                packet.setType(Type.findBySubject(subject));
                return packet;
            }
        };
    }

    @Override
    public String getSubject() {
        return type.subject;
    }

    @Getter
    @AllArgsConstructor
    public enum Type {
        //when 1 player joins a party
        JOIN_BYPASS(Subjects.PARTY_JOIN_REQUEST_BYPASS, Subjects.PARTY_JOIN_NOTIFY),
        JOIN_COMMAND(Subjects.PARTY_JOIN_REQUEST_COMMAND, Subjects.PARTY_JOIN_NOTIFY),
        //when 1 player leaves a party
        LEAVE(Subjects.PARTY_LEAVE_REQUEST, Subjects.PARTY_LEAVE_NOTIFY),
        //when 1 player yoinks the party
        YOINK(Subjects.PARTY_YOINK_REQUEST, Subjects.PARTY_YOINK_NOTIFY),
        //when 1 player disbands a party
        DISBAND(Subjects.PARTY_DISBAND_REQUEST, Subjects.PARTY_DISBAND_NOTIFY),
        //when 1 player leaves or joins the network
        STATUS("todo fix", Subjects.PARTY_STATUS_NOTIFY);
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
