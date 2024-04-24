package net.cytonic.cytosis.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.util.List;

public final class MiniMessageTemplate {
    private MiniMessageTemplate() {
    }

    @SuppressWarnings("preview")
    public static final StringTemplate.Processor<Component, RuntimeException> MM = stringTemplate -> {
        String interpolated = STR.process(stringTemplate);
        return MiniMessage.miniMessage().deserialize(interpolated);
    };

    @SuppressWarnings("preview")
    public static final StringTemplate.Processor<List<Component>, RuntimeException> MM_WRAP = stringTemplate -> {
        String interpolated = STR.process(stringTemplate);
        return ComponentWrapper.wrap(MiniMessage.miniMessage().deserialize(interpolated), 36);
    };
}