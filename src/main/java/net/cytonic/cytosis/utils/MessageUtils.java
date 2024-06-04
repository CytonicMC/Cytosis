package net.cytonic.cytosis.utils;

import net.kyori.adventure.text.Component;

public class MessageUtils {

    public static Component formatBanMessage(BanData banData) {
        if (!banData.isBanned()) return Component.empty();
        Component finalComp = Component.empty()
                .append(MiniMessageTemplate.MM."<red>You are currently banned from the Cytonic Network!".appendNewline().appendNewline())
                .append(MiniMessageTemplate.MM."<gray>Reason:<gray> <white>\{banData.reason()}</white>".appendNewline())
                .append(MiniMessageTemplate.MM."<gray>Expires:</gray> <white>\{DurationParser.unparse(banData.expiry(), " ")}</white>".appendNewline().appendNewline())
                .append(MiniMessageTemplate.MM."<gray>Appeal at: </gray><aqua><underlined>https://cytonic.net</underlined></aqua>".appendNewline());

        return finalComp;
    }
}
