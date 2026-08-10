package net.cytonic.cytosis.commands.staff.snooper.help;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.utils.Msg;

@SubCommand
public class HelpMute extends CytosisCommand {

    private static final Component MESSAGE = Msg.splash("SNOOPER HELP!", "e829aa", "» /snooper <#ee61c0>mute</#ee61c0>")
        .appendNewline().append(Msg.grey("""
            Temporarily silences all notifications from snooper. To re-enable them, use "/snooper mute". <b>You are \
            required to mute snooper if you are recording a video intended for public release. This extends to live \
            streaming on the server.
            """));

    public HelpMute() {
        super("mute");
        setDefaultExecutor(((sender, _) -> sender.sendMessage(MESSAGE)));
    }
}
