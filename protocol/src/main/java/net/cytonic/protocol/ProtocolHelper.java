package net.cytonic.protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

import net.cytonic.protocol.utils.ClassGraphUtils;
import net.cytonic.protocol.utils.NatsAPI;
import net.cytonic.protocol.utils.ReflectionUtils;

@Slf4j
public class ProtocolHelper {

    private static final String PACKAGE = "net.cytonic";
    public static final Map<String, ProtocolObject<?, ?>> PROTOCOL_OBJECTS = new HashMap<>();
    private static boolean started = false;

    public static <T, R> ProtocolObject<T, R> getProtocolObject(Class<?> clazz, int index) {
        return getProtocolObject(ReflectionUtils.getTypeName(clazz, index));
    }

    @SuppressWarnings("unchecked")
    public static <T, R> ProtocolObject<T, R> getProtocolObject(String className) {
        return (ProtocolObject<T, R>) PROTOCOL_OBJECTS.get(className);
    }

    @SuppressWarnings("unchecked")
    public static void init() {
        if (started) return;
        ClassGraphUtils.getExtendedClasses(ProtocolObject.class, PACKAGE).forEach(protocolObject ->
            ProtocolHelper.PROTOCOL_OBJECTS.put(
                ReflectionUtils.getTypeName(protocolObject.getClass(), 0), protocolObject));

        List<NotifyListener<?>> notifyListeners = new ArrayList<>();

        ClassGraphUtils.getImplementedClasses(NotifyListener.class, PACKAGE).forEach(notifyListeners::add);

        ClassGraphUtils.getAnnotatedMethods(NotifyHandler.class, PACKAGE).forEach(annotatedMethod ->
            notifyListeners.add(new NotifyHandlerListener<>(annotatedMethod)));

        notifyListeners.forEach(ProtocolHelper::registerNotifyListener);

        ClassGraphUtils.getImplementedClasses(Endpoint.class, PACKAGE).forEach(endpoint ->
            NatsAPI.INSTANCE.subscribe(endpoint.getSubject(), message -> {
                try {
                    Object packet = endpoint.getProtocolObject().deserializeFromString(new String(message.getData()));
                    CompletableFuture<Object> responseFuture = endpoint.onMessage(packet, new NotifyData(message));
                    if (responseFuture == null) {
                        return;
                    }
                    responseFuture.whenComplete(((response, throwable) -> {
                        if (throwable != null) {
                            log.error("Error publishing response", throwable);
                            return;
                        }
                        if (response == null) {
                            return;
                        }
                        NatsAPI.INSTANCE.publish(message.getReplyTo(),
                            endpoint.getProtocolObject().serializeReturnToString(response));
                    }));
                } catch (Exception e) {
                    log.error("Error publishing response ", e);
                }
            }));

        started = true;
    }

    @SuppressWarnings("unchecked")
    private static <T> void registerNotifyListener(NotifyListener<T> notifyListener) {
        ProtocolObject<?, ?> protocolObject = notifyListener.getProtocolObject();
        NatsAPI.INSTANCE.subscribe(notifyListener.getSubject(), (message) -> {
            try {
                T data = (T) protocolObject.deserializeFromString(new String(message.getData()));
                notifyListener.onMessage(data, new NotifyData(message));
            } catch (Exception e) {
                log.error("Failed to handle message", e);
            }
        });
    }
}
