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
import net.cytonic.protocol.utils.ClassGraphUtils.AnnotatedMethod;
import net.cytonic.protocol.utils.ExcludeFromClassGraph;
import net.cytonic.protocol.utils.NotifyHandler;
import net.cytonic.protocol.utils.ReflectionUtils;

@Slf4j
@ExcludeFromClassGraph
public class NotifyHandlerListener<T> implements NotifyListener<T> {

    private final AnnotatedMethod<NotifyHandler> annotatedMethod;
    private final ProtocolObject<T, ?> protocolObject;

    @SuppressWarnings("unchecked")
    public NotifyHandlerListener(AnnotatedMethod<NotifyHandler> annotatedMethod) {
        this.annotatedMethod = annotatedMethod;

        Method method = annotatedMethod.method();
        Type[] paramTypes = method.getGenericParameterTypes();

        if (paramTypes.length == 0) {
            throw new IllegalArgumentException("Method must have at least one parameter " + format(annotatedMethod));
        }

        String className = getTypeNameFromType(paramTypes[0]);
        ProtocolObject<T, ?> test = ProtocolHelper.getProtocolObject(className);
        if (test == null) {
            throw new IllegalStateException("No protocol object for " + format(annotatedMethod));
        }

        String subject = annotatedMethod.annotation().subject();
        boolean hasSubject = subject != null && !subject.isEmpty();
        boolean hasStringConstructor = hasConstructor(test.getClass(), new Class[]{String.class});

        if (hasSubject && hasStringConstructor) {
            Map<Class<?>, Object> map = new HashMap<>();
            map.put(String.class, subject);
            try {
                protocolObject = ReflectionUtils.newInstance(test.getClass(), map);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Failed to create instance " + annotatedMethod, e);
            }
            return;
        }

        if (!hasSubject && hasStringConstructor) {
            throw new RuntimeException(
                "ProtocolObject has a String constructor but no subject was provided. Using no-arg constructor "
                    + annotatedMethod);
        }

        try {
            protocolObject = ReflectionUtils.newInstance(test.getClass());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No no-arg constructor found. " +
                "Consider adding @NotifyHandler(subject=\"...\") if this class requires a subject." + format(
                annotatedMethod));
        }
    }

    @Override
    public String getSubject() {
        if (annotatedMethod.annotation().subject().isEmpty()) {
            return protocolObject.getSubject();
        }

        return annotatedMethod.annotation().subject();
    }

    @Override
    public ProtocolObject<T, ?> getProtocolObject() {
        return protocolObject;
    }

    private String format(AnnotatedMethod<NotifyHandler> annotatedMethod) {
        return annotatedMethod.foundClass().getName() + "#" + annotatedMethod.method().getName();
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
    public void onMessage(T message, NotifyData notifyData) {
        try {
            Object instance = ReflectionUtils.newInstance(annotatedMethod.foundClass());
            Method method = annotatedMethod.method();
            method.setAccessible(true);
            if (method.getParameterCount() == 1) {
                method.invoke(instance, message);
                return;
            }

            if (method.getParameterCount() != 2) {
                throw new IllegalArgumentException(
                    "Method " + annotatedMethod.method().getName() + " must have exactly two parameters");
            }
            if (method.getParameterTypes()[1] != NotifyData.class) {
                throw new IllegalArgumentException(
                    "Method " + annotatedMethod.method().getName() + "'s second parameter must be NotifyData");
            }
            method.invoke(instance, message, notifyData);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
