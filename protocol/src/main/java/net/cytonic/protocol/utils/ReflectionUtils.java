package net.cytonic.protocol.utils;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ReflectionUtils {

    public static <T> T newInstance(Class<T> clazz) throws NoSuchMethodException {
        return newInstance(clazz, new LinkedHashMap<>());
    }

    public static <T> T newInstance(Class<T> clazz, LinkedHashMap<Class<?>, Object> parameters)
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
}
