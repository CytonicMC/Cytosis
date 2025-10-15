package net.cytonic.cytosis.commands.staff.snooper.help;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

public class SnooperHelpAbout extends CytosisCommand {

    private static final Component MESSAGE =
        Msg.splash("SNOOPER HELP!", "e829aa", "» /snooper <#ee61c0>about</#ee61c0>")
            .appendNewline()
            .append(Msg.grey("Sends a message displaying the basic usage of the snooper system."));

    public SnooperHelpAbout() {
        super("about");
        setDefaultExecutor((sender, context) -> sender.sendMessage(MESSAGE));
    }
}
