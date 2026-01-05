package net.cytonic.cytosis.data.packet.packets.friends;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.IllegalSubjectException;
import net.cytonic.cytosis.data.packet.utils.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendPacket extends RequestPacket<FriendPacket, FriendApiResponsePacket> {

    private UUID sender;
    private UUID recipient;
    private transient boolean isNotify;

    @Override
    protected Serializer<FriendPacket> getSerializer() {
        return new DefaultGsonSerializer<>(FriendPacket.class);
    }

    @Override
    public String getSubject() {
        throw new IllegalSubjectException();
    }

    @Override
    protected Class<FriendApiResponsePacket> getResponseType() {
        return FriendApiResponsePacket.class;
    }

    @AllArgsConstructor
    public enum Type {
        ACCEPT(Subjects.FRIEND_ACCEPT, Subjects.FRIEND_ACCEPTANCE_NOTIFY),
        EXPIRE(Subjects.FRIEND_EXPIRE_NOTIFY, Subjects.FRIEND_EXPIRE_NOTIFY),
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
