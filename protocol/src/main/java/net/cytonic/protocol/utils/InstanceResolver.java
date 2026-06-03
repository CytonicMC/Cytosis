package net.cytonic.protocol.utils;

import net.kyori.adventure.util.Services;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface InstanceResolver {

    InstanceResolver INSTANCE = Services.service(InstanceResolver.class)
        .orElse(ReflectionUtils::newInstance);

    @Nullable
    <T> T resolve(Class<T> clazz) throws Exception;
}
