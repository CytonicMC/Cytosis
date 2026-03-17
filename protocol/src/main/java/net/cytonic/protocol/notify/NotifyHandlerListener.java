package net.cytonic.protocol.notify;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import net.cytonic.protocol.NotifyData;
import net.cytonic.protocol.ProtocolHelper;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.utils.ExcludeFromIndex;
import net.cytonic.protocol.utils.NotifyHandler;
import net.cytonic.protocol.utils.ReflectionUtils;

@Slf4j
@ExcludeFromIndex
public class NotifyHandlerListener<T> implements NotifyListener<T> {

    private final Method method;
    private final NotifyHandler handler;
    private final ProtocolObject<T, ?> protocolObject;

    @SuppressWarnings("unchecked")
    public NotifyHandlerListener(Method method, NotifyHandler handler) {
        this.method = method;
        this.handler = handler;

        Type[] paramTypes = method.getGenericParameterTypes();

        if (paramTypes.length == 0) {
            throw new IllegalArgumentException("Method must have at least one parameter " + format(method));
        }

        String className = getTypeNameFromType(paramTypes[0]);
        ProtocolObject<T, ?> test = ProtocolHelper.getProtocolObject(className);
        if (test == null) {
            throw new IllegalStateException("No protocol object for " + format(method));
        }

        String subject = handler.subject();
        boolean hasSubject = subject != null && !subject.isEmpty();
        boolean hasStringConstructor = hasConstructor(test.getClass(), new Class[]{String.class});

        if (hasSubject && hasStringConstructor) {
            Map<Class<?>, Object> map = new HashMap<>();
            map.put(String.class, subject);
            try {
                protocolObject = ReflectionUtils.newInstance(test.getClass(), map);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Failed to create instance " + method.getName(), e);
            }
            return;
        }

        if (!hasSubject && hasStringConstructor) {
            throw new RuntimeException(
                "ProtocolObject has a String constructor but no subject was provided. Using no-arg constructor "
                    + method);
        }

        try {
            protocolObject = ReflectionUtils.newInstance(test.getClass());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No no-arg constructor found. " +
                "Consider adding @NotifyHandler(subject=\"...\") if this class requires a subject." + format(
                method));
        }
    }

    private static String format(Method method) {
        return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }

    @Override
    public ProtocolObject<T, ?> getProtocolObject() {
        return protocolObject;
    }

    @Override
    public void onMessage(T message, NotifyData notifyData) {
        try {
            Object instance = ReflectionUtils.newInstance(method.getDeclaringClass());
            method.setAccessible(true);
            if (method.getParameterCount() == 1) {
                method.invoke(instance, message);
                return;
            }

            if (method.getParameterCount() != 2) {
                throw new IllegalArgumentException(
                    "Method " + method.getName() + " must have exactly two parameters");
            }
            if (method.getParameterTypes()[1] != NotifyData.class) {
                throw new IllegalArgumentException(
                    "Method " + method.getName() + "'s second parameter must be NotifyData");
            }
            method.invoke(instance, message, notifyData);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasConstructor(Class<?> clazz, Class<?>[] classes) {
        try {
            clazz.getDeclaredConstructor(classes);
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

    private String getTypeNameFromType(Type type) {
        if (type instanceof Class<?> clazz) {
            // For nested classes, include the enclosing class name
            if (clazz.getEnclosingClass() != null) {
                return clazz.getEnclosingClass().getSimpleName() + "." + clazz.getSimpleName();
            }
            return clazz.getSimpleName();
        } else if (type instanceof ParameterizedType paramType) {
            Type rawType = paramType.getRawType();
            if (rawType instanceof Class<?> clazz) {
                // For nested classes, include the enclosing class name
                if (clazz.getEnclosingClass() != null) {
                    return clazz.getEnclosingClass().getSimpleName() + "." + clazz.getSimpleName();
                }
                return clazz.getSimpleName();
            }
        }
        return type.getTypeName();
    }

    @Override
    public String getSubject() {
        if (handler.subject().isEmpty()) {
            return protocolObject.getSubject();
        }

        return handler.subject();
    }
}
