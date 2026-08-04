package net.cytonic.protocol.utils;

import java.util.ServiceLoader;

import net.kyori.adventure.util.Services;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface InstanceResolver {

    InstanceResolver INSTANCE = Services.service(
            ServiceLoader.load(InstanceResolver.class, InstanceResolver.class.getClassLoader()),
            InstanceResolver.class)
        .orElse(ReflectionUtils::newInstance);

    @Nullable
    <T> T resolve(Class<T> clazz) throws Exception;
}
