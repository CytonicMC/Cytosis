package net.cytonic.protocol;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import net.cytonic.protocol.utils.ClassGraphUtils;
import net.cytonic.protocol.utils.NatsAPI;

@Slf4j
public class ProtocolHelper {

    private static final String PACKAGE = "net.cytonic";
    public static final Map<String, ProtocolObject<?, ?>> PROTOCOL_OBJECTS = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T, R> ProtocolObject<T, R> getProtocolObject(String simpleClassName) {
        return (ProtocolObject<T, R>) PROTOCOL_OBJECTS.get(simpleClassName);
    }

    public static void init() {
        registerProtocolObjects();
        registerNotifiable();
    }

    private static void registerProtocolObjects() {
        List<ProtocolObject<?, ?>> protocolObjects = new ArrayList<>();
        ClassGraphUtils.getExtendedClasses(ProtocolObject.class, PACKAGE).forEach(protocolObjects::add);

        protocolObjects.forEach(protocolObject -> {
            String requestTypeName = getRequestTypeName(protocolObject);
            ProtocolHelper.PROTOCOL_OBJECTS.put(requestTypeName, protocolObject);
        });
    }

    private static void registerNotifiable() {
        List<Notifiable<?>> notifiableList = new ArrayList<>();
        ClassGraphUtils.getImplementedClasses(Notifiable.class, PACKAGE).forEach(notifiableList::add);
        notifiableList.forEach(ProtocolHelper::registerNotifiable);
    }

    private static <T> void registerNotifiable(Notifiable<T> notifiable) {
        Serializer<?> serializer = notifiable.getProtocolObject().getSerializer();
        NatsAPI.INSTANCE.subscribe(notifiable.getSubject(), (message) -> {
            try {
                //noinspection unchecked
                T data = (T) serializer.deserialize(new String(message.getData()));
                notifiable.onMessage(data);
            } catch (Exception e) {
                log.error("Failed to handle message", e);
            }
        });
    }

    private static String getRequestTypeName(ProtocolObject<?, ?> protocolObject) {
        Class<?> clazz = protocolObject.getClass();
        Type genericSuperclass = clazz.getGenericSuperclass();

        if (genericSuperclass instanceof ParameterizedType paramType) {
            Type[] typeArguments = paramType.getActualTypeArguments();
            if (typeArguments.length > 0) {
                Type firstTypeArg = typeArguments[0];
                if (firstTypeArg instanceof Class) {
                    return ((Class<?>) firstTypeArg).getSimpleName();
                } else {
                    // Handle cases where T might be another generic type
                    return firstTypeArg.getTypeName();
                }
            }
        }

        throw new IllegalArgumentException("Could not determine the type T for the given ProtocolObject");
    }
}
