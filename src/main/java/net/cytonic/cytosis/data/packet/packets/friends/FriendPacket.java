package net.cytonic.cytosis.data.packet.packets.friends;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import net.cytonic.cytosis.data.packet.packets.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.packets.Packet;
import net.cytonic.cytosis.data.packet.packets.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendPacket extends Packet<FriendPacket> {

    private UUID sender;
    private UUID recipient;
    private transient Type type;
    private transient boolean isNotify;

    @Override
    protected Serializer<FriendPacket> getSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(String subject, FriendPacket value) {
                return new DefaultGsonSerializer<>(FriendPacket.class).serialize(subject, value);
            }

            @Override
            public FriendPacket deserialize(String subject, String json) {
                FriendPacket packet = new DefaultGsonSerializer<>(FriendPacket.class).deserialize(subject, json);
                packet.setType(Type.findBySubject(subject));
                return packet;
            }
        };
    }

    @Override
    public String getSubject() {
        return isNotify ? type.notifySubject : type.subject;
    }

    @AllArgsConstructor
    public enum Type {
        ACCEPT(Subjects.FRIEND_ACCEPT, Subjects.FRIEND_ACCEPTANCE_NOTIFY),
        DECLINE(Subjects.FRIEND_DECLINE, Subjects.FRIEND_DECLINATION_NOTIFY),
        EXPIRE(Subjects.FRIEND_EXPIRE_NOTIFY, Subjects.FRIEND_EXPIRE_NOTIFY),
        REMOVE(Subjects.FRIEND_REMOVE, Subjects.FRIEND_REMOVE),
        REQUEST(Subjects.FRIEND_REQUEST, Subjects.FRIEND_REQUEST_NOTIFY);
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
