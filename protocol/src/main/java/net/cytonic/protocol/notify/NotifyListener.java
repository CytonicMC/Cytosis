package net.cytonic.protocol.notify;

import net.cytonic.protocol.NotifyData;
import net.cytonic.protocol.ProtocolHelper;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.Subject;
import net.cytonic.protocol.serializer.Serializable;
import net.cytonic.protocol.serializer.Serializer;

public interface NotifyListener<T> extends Subject, Serializable<T> {

    default ProtocolObject<T, ?> getProtocolObject() {
        return ProtocolHelper.getProtocolObject(getClass(), 0);
    }

    @Override
    default Serializer<T> getSerializer() {
        return getProtocolObject().getSerializer();
    }

    void onMessage(T message, NotifyData notifyData);

    @Override
    default String getSubject() {
        return getProtocolObject().getSubject();
    }
}
