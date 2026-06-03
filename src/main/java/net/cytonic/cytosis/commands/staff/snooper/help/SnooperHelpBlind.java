package net.cytonic.cytosis.commands.staff.snooper.help;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

public class SnooperHelpBlind extends CytosisCommand {

    private static final Component MESSAGE =
        Msg.splash("SNOOPER HELP!", "e829aa", "» /snooper <#ee61c0>blind</#ee61c0> <channel>")
            .appendNewline()
            .append(Msg.grey("""
                Stops listening to the specified channel. If you would like to temporarily silence all notifications \
                 from snooper, use "/snooper mute". You can re-enable the notifications with "/snooper mute".\
                """));

    public SnooperHelpBlind() {
        super("blind");
        setDefaultExecutor((sender, _) -> sender.sendMessage(MESSAGE));
    }

}
