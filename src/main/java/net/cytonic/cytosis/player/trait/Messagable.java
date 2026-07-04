package net.cytonic.cytosis.player.trait;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.utils.Msg;

public interface Messagable {

    default void whoops(String template, Object... args) {
        sendMessage(Msg.whoops(template, args));
    }

    default void network(String template, Object... args) {
        sendMessage(Msg.network(template, args));
    }

    default void success(String template, Object... args) {
        sendMessage(Msg.success(template, args));
    }

    default void error(String template, Object... args) {
        sendMessage(Msg.error(template, args));
    }

    default void tip(String template, Object... args) {
        sendMessage(Msg.tip(template, args));
    }

    /**
     * Formatting: {@code ENABLED! <template> is now enabled.}
     */
    default void enabledSingle(String template, Object... args) {
        sendMessage(Msg.enabledSingle(template, args));
    }

    /**
     * Formatting: {@code ENABLED! <template> are now ensabled.}
     */
    default void enabledPlural(String template, Object... args) {
        sendMessage(Msg.enabledPlural(template, args));
    }

    /**
     * Formatting: {@code DISABLED! <template> is now disabled.}
     */
    default void disbledSingle(String template, Object... args) {
        sendMessage(Msg.disabledSingle(template, args));
    }

    /**
     * Formatting: {@code DISABLED! <template> are now disabled.}
     */
    default void disbledPlural(String template, Object... args) {
        sendMessage(Msg.disabledPlural(template, args));
    }

    void sendMessage(Component component);

}
