package net.cytonic.protocol;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.IndexView;

import net.cytonic.protocol.notify.NotifyHandlerListener;
import net.cytonic.protocol.notify.NotifyListener;
import net.cytonic.protocol.utils.ExcludeFromIndex;
import net.cytonic.protocol.utils.IndexHolder;
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

    @SuppressWarnings("unchecked")
    public static void init() {
        if (started) return;

        IndexView index = IndexHolder.get();

        index.getAllKnownSubclasses(ProtocolObject.class).stream()
            .filter(classInfo -> !classInfo.isAbstract() && !classInfo.isInterface())
            .filter(classInfo -> !classInfo.hasAnnotation(ExcludeFromIndex.class))
            .map(classInfo -> {
                try {
                    return (ProtocolObject<?, ?>) Class.forName(classInfo.name().toString(), true,
                            Thread.currentThread().getContextClassLoader())
                        .getDeclaredConstructor()
                        .newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + classInfo.name(), e);
                }
            })
            .forEach(o -> ProtocolHelper.PROTOCOL_OBJECTS.put(ReflectionUtils.getTypeName(o.getClass(), 0), o));

        List<NotifyListener<?>> notifyListeners = new ArrayList<>();
        index.getAllKnownSubclasses(NotifyListener.class).stream()
            .filter(classInfo -> !classInfo.isAbstract() && !classInfo.isInterface())
            .filter(classInfo -> !classInfo.hasAnnotation(ExcludeFromIndex.class))
            .map(classInfo -> {
                try {
                    return (NotifyListener<?>) Class.forName(classInfo.name().toString(), true,
                            Thread.currentThread().getContextClassLoader())
                        .getDeclaredConstructor()
                        .newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + classInfo.name(), e);
                }
            })
            .forEach(notifyListeners::add);

        index.getAnnotations(NotifyHandler.class).stream()
            .filter(annotationInstance -> annotationInstance.target().kind() == Kind.METHOD)
            .filter(annotationInstance -> !annotationInstance.target().hasAnnotation(ExcludeFromIndex.class))
            .map(annotationInstance -> annotationInstance.target().asMethod())
            .forEach(methodInfo -> {
                try {
                    Class<?> clazz = Class.forName(methodInfo.declaringClass().name().toString(), true,
                        Thread.currentThread().getContextClassLoader());
                    System.err.println(clazz.getName() + " <--- CLASS NAME");
                    System.err.println(methodInfo.name() + " <--- METHOD NAME");
                    Method method = clazz.getDeclaredMethod(
                        methodInfo.name(),
                        methodInfo.parameterTypes().stream()
                            .map(type -> {
                                try {
                                    return Class.forName(type.name().toString(), true,
                                        Thread.currentThread().getContextClassLoader());
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .toArray(Class[]::new)
                    );
                    notifyListeners.add(new NotifyHandlerListener<>(method, method.getAnnotation(NotifyHandler.class)));
                } catch (Exception e) {
                    throw new RuntimeException(
                        "Failed to resolve method " + methodInfo.name() + " in class " + methodInfo.declaringClass()
                            .name().toString(), e);
                }
            });

        notifyListeners.forEach(ProtocolHelper::registerNotifyListener);

        index.getAllKnownImplementations(Endpoint.class).stream()
            .filter(classInfo -> !classInfo.isAbstract() && !classInfo.isInterface())
            .filter(classInfo -> !classInfo.hasAnnotation(ExcludeFromIndex.class))
            .map(classInfo -> {
                try {
                    return (Endpoint) Class.forName(classInfo.name().toString(), true,
                            Thread.currentThread().getContextClassLoader())
                        .getDeclaredConstructor()
                        .newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + classInfo.name(), e);
                }
            })
            .forEach(endpoint ->
                NatsAPI.INSTANCE.subscribe(endpoint.getSubject(), message -> {
                    try {
                        Object packet = endpoint.getProtocolObject()
                            .deserializeFromString(new String(message.getData()));
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
