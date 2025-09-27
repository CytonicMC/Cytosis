package net.cytonic.cytosis.commands.debug.cooldowns;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

public class CooldownHelpCommand extends CytosisCommand {

    private static final Component HELP = Msg.greenSplash("Cooldown Help", "").appendNewline()
        .append(Msg.grey("<i>A debug command for testing the Cytosis cooldown api.")).appendNewline()
        .appendNewline().appendNewline()
        .append(Msg.gold("/cooldown set_personal <node> <expiry><gray><i> Sets a personal cooldown"))
        .appendNewline()
        .append(Msg.gold("/cooldown set_global <node> <expiry><gray><i> Sets a global cooldown"))
        .appendNewline()
        .append(Msg.gold("/cooldown clear_personal <node><gray><i> Resets a personal cooldown"))
        .appendNewline()
        .append(Msg.gold("/cooldown clear_global <node><gray><i> Resets a global cooldown"))
        .appendNewline()
        .append(Msg.gold("/cooldown help<gray><i> Displays this message"))
        .appendNewline()
        .append(Msg.gold("/cooldown get_global <node><gray><i> Gets the expiry of a global cooldown"))
        .appendNewline()
        .append(Msg.gold("/cooldown get_personal <node><gray><i> Gets the expiry of a personal cooldown"));

    CooldownHelpCommand() {
        super("help");
        setDefaultExecutor((sender, context) -> sender.sendMessage(HELP));
    }
}
