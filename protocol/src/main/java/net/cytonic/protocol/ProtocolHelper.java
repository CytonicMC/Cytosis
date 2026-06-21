package net.cytonic.protocol;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

import net.cytonic.protocol.notify.NotifyHandlerListener;
import net.cytonic.protocol.notify.NotifyListener;
import net.cytonic.protocol.utils.JandexUtils;
import net.cytonic.protocol.utils.NatsAPI;
import net.cytonic.protocol.utils.NotifyHandler;
import net.cytonic.protocol.utils.ReflectionUtils;

@Slf4j
public class ProtocolHelper {

    public static final Map<String, ProtocolObject<?, ?>> PROTOCOL_OBJECTS = new HashMap<>();
    private static boolean started = false;

    public static <T, R> ProtocolObject<T, R> getProtocolObject(Class<?> clazz, int index) {
        return getProtocolObject(ReflectionUtils.getTypeName(clazz, index));
    }

    @SuppressWarnings("unchecked")
    public static <T, R> ProtocolObject<T, R> getProtocolObject(String className) {
        return (ProtocolObject<T, R>) PROTOCOL_OBJECTS.get(className);
    }

    public static void init() {
        if (started) return;
        for (ProtocolObject<?, ?> protocolObject : JandexUtils.getExtendedClasses(ProtocolObject.class)) {
            PROTOCOL_OBJECTS.put(ReflectionUtils.getTypeName(protocolObject.getClass(), 0), protocolObject);
        }

        JandexUtils.getImplementedClasses(NotifyListener.class).forEach(ProtocolHelper::registerNotifyListener);
        for (Method method : JandexUtils.getAnnotatedMethods(NotifyHandler.class)) {
            registerNotifyListener(new NotifyHandlerListener<>(method, method.getAnnotation(NotifyHandler.class)));
        }

        JandexUtils.getImplementedClasses(Endpoint.class).forEach(ProtocolHelper::registerEndpoint);

        started = true;
    }

    private static <T, R> void registerEndpoint(Endpoint<T, R> endpoint) {
        NatsAPI.INSTANCE.subscribe(endpoint.getSubject(), message -> {
            try {
                T packet = endpoint.getProtocolObject().deserializeFromString(new String(message.getData()));

                CompletableFuture<R> responseFuture = endpoint.onMessage(packet, new NotifyData(message));
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
        });
    }

    private static <T> void registerNotifyListener(NotifyListener<T> notifyListener) {
        ProtocolObject<T, ?> protocolObject = notifyListener.getProtocolObject();
        NatsAPI.INSTANCE.subscribe(notifyListener.getSubject(), (message) -> {
            try {
                notifyListener.onMessage(
                    protocolObject.deserializeFromString(new String(message.getData())), new NotifyData(message));
            } catch (Exception e) {
                log.error("Failed to handle message", e);
            }
        });
    }
}
