package net.cytonic.protocol.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ReflectionUtils {

    public static <T> T newInstance(Class<T> clazz) throws NoSuchMethodException {
        return newInstance(clazz, new LinkedHashMap<>());
    }

    public static <T> T newInstance(Class<T> clazz, Map<Class<?>, Object> parameters)
        throws NoSuchMethodException {
        try {
            Constructor<T> constructor = parameters.isEmpty()
                ? clazz.getDeclaredConstructor()
                : clazz.getDeclaredConstructor(parameters.keySet().toArray(Class[]::new));

            constructor.setAccessible(true);
            return parameters.isEmpty()
                ? constructor.newInstance()
                : constructor.newInstance(parameters.values().toArray());
        } catch (Exception e) {
            log.error("An error occurred while instantiating {}", clazz.getName(), e);
        }
        return null;
    }

    public static String getTypeName(Class<?> clazz, int index) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        String result = extractTypeAtIndex(genericSuperclass, index);
        if (result != null) {
            return result;
        }

        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            result = extractTypeAtIndex(genericInterface, index);
            if (result != null) {
                return result;
            }
        }

        throw new IllegalArgumentException(
            "Could not determine the type at index " + index + " for the given Class " + clazz.getName());
    }

    public static String extractTypeAtIndex(Type type, int index) {
        if (type instanceof ParameterizedType paramType) {
            Type[] typeArguments = paramType.getActualTypeArguments();
            if (typeArguments.length > index) {
                Type targetType = typeArguments[index];

                if (targetType instanceof Class<?> clazz) {
                    if (clazz.getEnclosingClass() != null) {
                        return clazz.getEnclosingClass().getSimpleName() + "." + clazz.getSimpleName();
                    }
                    return clazz.getSimpleName();
                }

                if (targetType instanceof TypeVariable) {
                    return ((TypeVariable<?>) targetType).getName();
                }

                return targetType.getTypeName();
            }
        }
        return null;
    }
}
