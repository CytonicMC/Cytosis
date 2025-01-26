package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.data.objects.BanData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;


/**
 * A class that holds utils for formatting messages
 */
public interface Msg {

    /**
     * Parses MiniMessage into a Component
     */
    static Component mm(String message) {
        return MiniMessage.miniMessage().deserialize(message).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    static List<Component> wrap(String minimessage) {
        return ComponentWrapper.wrap(mm(minimessage), 36);
    }

    static List<Component> wrap(String minimessage, int width) {
        return ComponentWrapper.wrap(mm(minimessage), width);
    }

    static Component whoops(String str) {
        return mm("<red><b>WHOOPS!</b></red><gray> " + str);
    }

    static Component serverError(String str) {
        return mm("<red><b>SERVER ERROR!</b></red><gray> " + str);
    }

    static Component success(String str) {
        return mm("<green><b>SUCCESS!</b></green><gray> " + str);
    }

    /**
     * Gets a formatted ban message
     *
     * @param banData the data to format
     * @return the formatted message in Component format
     */
    static Component formatBanMessage(BanData banData) {
        if (!banData.isBanned()) return Component.empty();

        return Component.empty()
                .append(Msg.mm("<red>You are currently banned from the Cytonic Network!").appendNewline().appendNewline())
                .append(Msg.mm("<gray>Reason:<gray> <white>" + banData.reason() + "</white>").appendNewline())
                .append(Msg.mm("<gray>Expires:</gray> <white>" + DurationParser.unparse(banData.expiry(), " ") + "</white>").appendNewline().appendNewline())
                .append(Msg.mm("<gray>Appeal at: </gray><aqua><underlined>https://cytonic.net</underlined></aqua>").appendNewline());
    }
}
