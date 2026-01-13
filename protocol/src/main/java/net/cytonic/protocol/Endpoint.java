package net.cytonic.protocol;

import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.utils.ReflectionUtils;

public interface Endpoint<T, R> extends Subject {

    default ProtocolObject<T, R> getProtocolObject() {
        return ProtocolHelper.getProtocolObject(ReflectionUtils.getTypeName(getClass(), 0));
    }

    @Nullable
    R onMessage(T message, NotifyData extraData);

}
