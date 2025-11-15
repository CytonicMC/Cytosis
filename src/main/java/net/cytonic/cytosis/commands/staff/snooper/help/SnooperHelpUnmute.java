package net.cytonic.cytosis.commands.staff.snooper.help;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

public class SnooperHelpUnmute extends CytosisCommand {

    private static final Component MESSAGE = Msg.splash("SNOOPER HELP!", "e829aa",
            "» /snooper <#ee61c0>unmute</#ee61c0>")
        .appendNewline()
        .append(Msg.grey("Re-enables receiving notifications from snooper. To disable them, use \"/snooper mute\"."));

    public SnooperHelpUnmute() {
        super("unmute");
        setDefaultExecutor((sender, context) -> sender.sendMessage(MESSAGE));
    }
}
