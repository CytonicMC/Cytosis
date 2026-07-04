package net.cytonic.protocol.notify;

import net.minestom.server.codec.Codec;

import net.cytonic.protocol.NotifyData;
import net.cytonic.protocol.ProtocolHelper;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.Subject;
import net.cytonic.protocol.serializer.Serializable;

public interface NotifyListener<T> extends Subject, Serializable<T> {

    default <R> ProtocolObject<T, R> getProtocolObject() {
        return ProtocolHelper.getProtocolObject(getClass(), 0);
    }

    @Override
    default Codec<T> getCodec() {
        return getProtocolObject().getCodec();
    }

    void onMessage(T message, NotifyData notifyData);

    @Override
    default String getSubject() {
        return getProtocolObject().getSubject();
    }
}
