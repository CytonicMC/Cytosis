package net.cytonic.protocol.objects;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.FriendApiProtocolObject.Response;

@NoArgsConstructor
@AllArgsConstructor
public class FriendApiIdProtocolObject extends
    ProtocolObject<FriendApiIdProtocolObject, FriendApiProtocolObject.Response> {

    private String subject;

    @Override
    public String getSubject() {
        return subject;
    }

    public record Packet(@SerializedName("request_id") UUID requestId) implements
        Message<FriendApiIdProtocolObject, FriendApiProtocolObject.Response> {

        @Override
        public ProtocolObject<FriendApiIdProtocolObject, Response> getProtocolObject() {
            return new FriendApiIdProtocolObject();
        }
    }
}
