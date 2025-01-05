package net.cytonic.cytosis.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

/**
 * A class that holds utils for formatting messages
 */
public final class MiniMessageTemplate {
    /**
     * Parses MiniMessage into a Component
     */
    @SuppressWarnings("preview")
    public static final StringTemplate.Processor<Component, RuntimeException> MM = stringTemplate -> {
        String interpolated = STR.process(stringTemplate);
        return MiniMessage.miniMessage().deserialize(interpolated);
    };
    /**
     * Parses and wraps a component with a width of 36
     */
    @SuppressWarnings("preview")
    public static final StringTemplate.Processor<List<Component>, RuntimeException> MM_WRAP = stringTemplate -> {
        String interpolated = STR.process(stringTemplate);
        return ComponentWrapper.wrap(MiniMessage.miniMessage().deserialize(interpolated), 36);
    };

    private MiniMessageTemplate() {
    }
}