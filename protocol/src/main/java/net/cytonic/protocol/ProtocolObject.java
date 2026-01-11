package net.cytonic.protocol;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.function.Consumer;

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

        Class<?> searchClass = getClass();
        while (searchClass != null && searchClass != Object.class) {
            Type[] interfaces = searchClass.getGenericInterfaces();
            for (Type genericInterface : interfaces) {
                if (genericInterface instanceof ParameterizedType paramType
                    && paramType.getRawType().equals(Serializable.class)) {

                    Type actualType = paramType.getActualTypeArguments()[slot];

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

    public void request(T message, Consumer<R> onResponse) {
        request(getSubject(), message, onResponse);
    }

    public void request(String subject, T message, Consumer<R> onResponse) {
        NatsAPI.INSTANCE.request(subject, serializeToString(message),
            bytes -> onResponse.accept(deserializeReturnFromString(new String(bytes))));
    }

    public abstract String getSubject();
    //todo figure out if we want simple class names or subjects
//    public String channel() {
//        return getClass().getSimpleName();
//    }
}
