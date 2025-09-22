package net.cytonic.cytosis.commands.staff.snooper.help;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

public class SnooperHelpListen extends CytosisCommand {

    private static final Component MESSAGE =
        Msg.splash("SNOOPER HELP!", "e829aa", "» /snooper <#ee61c0>listen</#ee61c0> <channel>")
            .appendNewline()
            .append(Msg.grey("""
                Starts listening to the specified channel. All channels are protected by rank, so only available
                channels are tab completed. Once listening to the channel, you will receive a message any
                time an event is pushed over that channel. If you would like to temporarily silence all
                notifications from snooper, use "/snooper mute". To stop listening to the channel,
                use "/snooper blind <channel>", this will stop sending you notifications.
                """));

    public SnooperHelpListen() {
        super("listen");
        setDefaultExecutor((sender, context) -> sender.sendMessage(MESSAGE));
    }
}
