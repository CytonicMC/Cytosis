package net.cytonic.protocol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import net.cytonic.protocol.utils.ClassGraphUtils;
import net.cytonic.protocol.utils.ClassGraphUtils.AnnotatedMethod;
import net.cytonic.protocol.utils.NatsAPI;
import net.cytonic.protocol.utils.ReflectionUtils;

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

    @SuppressWarnings("unchecked")
    private static void registerNotifiable() {
        List<NotifyListener<?>> notifyListenerList = new ArrayList<>();
        ClassGraphUtils.getImplementedClasses(NotifyListener.class, PACKAGE).forEach(notifyListenerList::add);
        notifyListenerList.forEach(ProtocolHelper::registerNotifiable);

        for (AnnotatedMethod<?> annotatedMethod : ClassGraphUtils.getAnnotatedMethods(NotifyHandler.class, PACKAGE)) {
            registerNotifiable(new Listener<>((AnnotatedMethod<NotifyHandler>) annotatedMethod));
        }
    }

    @ExcludeFromClassGraph
    public static class Listener<T> implements NotifyListener<T> {

        private final AnnotatedMethod<NotifyHandler> annotatedMethod;
        private ProtocolObject<?, ?> protocolObject;

        public Listener(AnnotatedMethod<NotifyHandler> annotatedMethod) {
            this.annotatedMethod = annotatedMethod;
        }

        @SuppressWarnings("unchecked")
        @Override
        public ProtocolObject<T, ?> getProtocolObject() {
            if (protocolObject == null) {
                NotifyHandler handler = annotatedMethod.annotation();
                if (Objects.equals(handler.subject(), "")) {
                    try {
                        protocolObject = ReflectionUtils.newInstance(handler.value());
                    } catch (NoSuchMethodException e) {
                        log.error("No such method {} found", handler.value(), e);
                    }
                } else {
                    LinkedHashMap<Class<?>, Object> parameters = new LinkedHashMap<>();
                    parameters.put(String.class, handler.subject());
                    try {
                        protocolObject = ReflectionUtils.newInstance(handler.value(), parameters);
                    } catch (NoSuchMethodException e) {
                        log.error("""
                                No such method {} found
                                This might be because you added a subject to @NotifyHandler when the ProtocolObject doesnt take it!
                                at {}#{}""",
                            handler.value(),
                            annotatedMethod.foundClass().getName(),
                            annotatedMethod.method().getName(),
                            e);
                    }
                }
            }
            return (ProtocolObject<T, ?>) protocolObject;
        }

        @Override
        public void onMessage(Object message) {
            try {
                Object instance = ReflectionUtils.newInstance(annotatedMethod.foundClass());
                Method method = annotatedMethod.method();
                method.setAccessible(true);
                method.invoke(instance, message);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static <T> void registerNotifiable(NotifyListener<T> notifyListener) {
        ProtocolObject<?, ?> protocolObject = notifyListener.getProtocolObject();
        NatsAPI.INSTANCE.subscribe(notifyListener.getSubject(), (message) -> {
            try {
                //noinspection unchecked
                T data = (T) protocolObject.deserializeFromString(new String(message.getData()));
                notifyListener.onMessage(data);
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
