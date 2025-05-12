package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

public class SnooperHelpCommand extends CytosisCommand {
    public SnooperHelpCommand() {
        super("help");
        setCondition(CommandUtils.IS_STAFF);

        ArgumentWord cmd = ArgumentType.Word("command").from("about", "help", "listen", "mute", "unmute", "audit", "blind");

        setDefaultExecutor((s, ignored) -> s.sendMessage(
                Msg.splash("SNOOPER HELP!", "e829aa", "»").appendNewline()
                        .append(Msg.mm("<gray>/snooper <#ee61c0>about</#ee61c0>: Gives information about what snooper is, its limitations, and advantages.")).appendNewline()
                        .append(Msg.mm("<gray>/snooper <#ee61c0>help</#ee61c0>: Displays this message.")).appendNewline()
                        .append(Msg.mm("<gray>/snooper <#ee61c0>listen</#ee61c0> <channel>: Starts snooping on a specific channel.")).appendNewline()
                        .append(Msg.mm("<gray>/snooper <#ee61c0>mute</#ee61c0>: Completely disables receiving snoops. <i><dark_gray>This is required to be enabled in any public videos or screen recordings.")).appendNewline()
                        .append(Msg.mm("<gray>/snooper <#ee61c0>unmute</#ee61c0>: Reenables receiving snoops.")).appendNewline()
                        .append(Msg.mm("<gray>/snooper <#ee61c0>audit</#ee61c0> <channel>: Opens the GUI to \"audit\" past snoops, allowing you to view all previously sent snoops on a given channel.")).appendNewline()
                        .append(Msg.mm("<gray>/snooper <#ee61c0>blind</#ee61c0> <channel>: Stops snooping on a specific channel."))
        ));
        addSyntax((s, ctx) -> {
            String command = ctx.get(cmd);
            switch (command) {
                case "help" ->
                        s.sendMessage(Msg.splash("SNOOPER HELP!", "e829aa", "» /snooper <#ee61c0>help</#ee61c0>").appendNewline()
                                .append(Msg.mm("<gray>Sends a help message displaying the command usage and basic description of each command. Why are you looking at this!?"))
                        );
                case "about" ->
                        s.sendMessage(Msg.splash("SNOOPER HELP!", "e829aa", "» /snooper <#ee61c0>about</#ee61c0>").appendNewline()
                                .append(Msg.mm("<gray>Sends a message displaying the basic usage of the snooper system."))
                        );
                case "listen" ->
                        s.sendMessage(Msg.splash("SNOOPER HELP!", "e829aa", "» /snooper <#ee61c0>listen</#ee61c0> <channel>").appendNewline()
                                .append(Msg.mm("<gray>Starts listening to the specified channel. All channels are protected by rank, so only available channels are tab compeleted. Once listening to the channel, you will recieve a message any time an event is pushed over that channel. If you would like to temporarily silence all notifications from snooper, use \"/snooper mute\". To stop listening to the channel, use \"/snooper blind <channel>\", this will stop sending you notifications."))
                        );
                case "blind" ->
                        s.sendMessage(Msg.splash("SNOOPER HELP!", "e829aa", "» /snooper <#ee61c0>blind</#ee61c0> <channel>").appendNewline()
                                .append(Msg.mm("<gray>Stops listening to the specified channel. If you would like to temporarily silence all notifications from snooper, use \"/snooper mute\". You can reenable the notifications with \"/snooper unmute\"."))
                        );
                case "mute" ->
                        s.sendMessage(Msg.splash("SNOOPER HELP!", "e829aa", "» /snooper <#ee61c0>mute</#ee61c0>").appendNewline()
                                .append(Msg.mm("<gray>Temporarily silences all notifications from snooper. To reenable them, use \"/snooper unmute\". <b>You are required to mute snooper if you are recording a video intended for public release. This extends to live streaming on the server."))
                        );
                case "unmute" ->
                        s.sendMessage(Msg.splash("SNOOPER HELP!", "e829aa", "» /snooper <#ee61c0>unmute</#ee61c0>").appendNewline()
                                .append(Msg.mm("<gray>Reenables receiving all notifications from snooper. To disable them, use \"/snooper mute\"."))
                        );
                case "audit" ->
                        s.sendMessage(Msg.splash("SNOOPER HELP!", "e829aa", "» /snooper <#ee61c0>audit</#ee61c0>").appendNewline()
                                .append(Msg.mm("<gray>Opens a menu allowing you to audit past snoops on the specified channel. You can only audit snoops you have access to, based on rank. There are two 'modes' to the audit menu.<newline><newline> There is a basic mode, showing an unfiltered list, sorted by newest first. Good for looking at something that happened recently. <newline><newline> Then there is the advanced mode. There are several filtering and sorting options. For starters, there is a text search option, allowing you to filter the contents of snoops displayed. Prefix a word with '+' to include it, and prefix it with '-' to exlude options with it. Then, you can pick a date range for the creation of the snoop. There are a few options: last hour, 12 hours, 1 day, 30 days, 180 days, and 1 year. On top of that, you can decide the sort, either ascending by date (oldest first), or descending by date (newest first)."))
                        );
            }
        }, cmd);
    }
}
