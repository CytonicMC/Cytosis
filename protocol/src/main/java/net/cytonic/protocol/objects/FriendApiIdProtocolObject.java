package net.cytonic.protocol.objects;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;

@NoArgsConstructor
@AllArgsConstructor
@Internal
public class FriendApiIdProtocolObject extends
    ProtocolObject<FriendApiIdProtocolObject, FriendApiProtocolObject.Response> {

    private String subject;

    @Override
    public String getSubject() {
        return subject;
    }

    @Internal
    public record Packet(@SerializedName("request_id") UUID requestId) implements
        Message<FriendApiIdProtocolObject, FriendApiProtocolObject.Response> {

    }
}
