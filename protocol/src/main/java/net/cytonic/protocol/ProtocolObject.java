package net.cytonic.protocol;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.function.BiConsumer;

import net.cytonic.protocol.serializer.GsonSerializer;
import net.cytonic.protocol.serializer.ReturnSerializable;
import net.cytonic.protocol.serializer.Serializable;
import net.cytonic.protocol.serializer.Serializer;
import net.cytonic.protocol.utils.NatsAPI;

public abstract class ProtocolObject<T, R> implements Serializable<T>, ReturnSerializable<R>, Subject {

    public Serializer<T> getSerializer() {
        return new GsonSerializer<>(getSerializableType(0));
    }

    public Serializer<R> getReturnSerializer() {
        return new GsonSerializer<>(getSerializableType(1));
    }

    @SuppressWarnings("unchecked")
    public <X> Class<X> getSerializableType(int slot) {
        Class<?> declaringClass = null;
        int typeParameterIndex = -1;
        Class<?> targetInterface = slot == 0 ? Serializable.class : ReturnSerializable.class;
        int targetSlot = 0;

        Class<?> searchClass = getClass();
        while (searchClass != null && searchClass != Object.class) {
            Type[] interfaces = searchClass.getGenericInterfaces();
            for (Type genericInterface : interfaces) {
                if (genericInterface instanceof ParameterizedType paramType
                    && paramType.getRawType().equals(targetInterface)) {

                    Type actualType = paramType.getActualTypeArguments()[targetSlot];

                    if (actualType instanceof Class<?>) {
                        return (Class<X>) actualType;
                    }

                    if (actualType instanceof TypeVariable<?> tv) {
                        declaringClass = searchClass;
                        TypeVariable<?>[] typeParams = searchClass.getTypeParameters();
                        for (int i = 0; i < typeParams.length; i++) {
                            if (typeParams[i].getName().equals(tv.getName())) {
                                typeParameterIndex = i;
                                break;
                            }
                        }
                    }
                    break;
                }
            }

            if (declaringClass != null) break;
            searchClass = searchClass.getSuperclass();
        }

        if (declaringClass == null || typeParameterIndex == -1) {
            throw new IllegalStateException("Could not find Serializable interface for: " + getClass().getName());
        }

        searchClass = getClass();
        while (searchClass != null && searchClass != declaringClass) {
            Type genericSuperclass = searchClass.getGenericSuperclass();

            if (genericSuperclass instanceof ParameterizedType paramType) {
                Type resolvedType = paramType.getActualTypeArguments()[typeParameterIndex];

                if (resolvedType instanceof Class<?>) {
                    return (Class<X>) resolvedType;
                } else if (resolvedType instanceof ParameterizedType pt) {
                    return (Class<X>) pt.getRawType();
                }
            }

            searchClass = searchClass.getSuperclass();
        }

        throw new IllegalStateException("Could not resolve serializable type for: " + getClass().getName());
    }

    public void request(T message, BiConsumer<R, Throwable> onResponse) {
        request(getSubject(), message, onResponse);
    }

    public void request(String subject, T message, BiConsumer<R, Throwable> onResponse) {
        NatsAPI.INSTANCE.request(subject, serializeToString(message),
            (bytes, throwable) -> onResponse.accept(deserializeReturnFromString(new String(bytes)), throwable));
    }

    public abstract String getSubject();
}
