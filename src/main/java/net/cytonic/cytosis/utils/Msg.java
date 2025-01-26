package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.data.objects.BanData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;


/**
 * A class that holds utils for formatting messages
 */
public final class Msg {
    /**
     * Defaut constructor
     */
    private Msg() {
    }

    /**
     * Parses MiniMessage into a Component
     */
    public static Component mm(String message) {
        return MiniMessage.miniMessage().deserialize(message).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    /**
     * Gets a formatted ban message
     *
     * @param banData the data to format
     * @return the formatted message in Component format
     */
    public static Component formatBanMessage(BanData banData) {
        if (!banData.isBanned()) return Component.empty();

        return Component.empty()
                .append(Msg.mm("<red>You are currently banned from the Cytonic Network!").appendNewline().appendNewline())
                .append(Msg.mm("<gray>Reason:<gray> <white>" + banData.reason() + "</white>").appendNewline())
                .append(Msg.mm("<gray>Expires:</gray> <white>" + DurationParser.unparse(banData.expiry(), " ") + "</white>").appendNewline().appendNewline())
                .append(Msg.mm("<gray>Appeal at: </gray><aqua><underlined>https://cytonic.net</underlined></aqua>").appendNewline());
    }
}
