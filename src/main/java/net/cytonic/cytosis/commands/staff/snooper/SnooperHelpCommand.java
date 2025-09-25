package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.commands.staff.snooper.help.SnooperHelpAbout;
import net.cytonic.cytosis.commands.staff.snooper.help.SnooperHelpAudit;
import net.cytonic.cytosis.commands.staff.snooper.help.SnooperHelpBlind;
import net.cytonic.cytosis.commands.staff.snooper.help.SnooperHelpListen;
import net.cytonic.cytosis.commands.staff.snooper.help.SnooperHelpMute;
import net.cytonic.cytosis.commands.staff.snooper.help.SnooperHelpUnmute;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

public class SnooperHelpCommand extends CytosisCommand {

    public SnooperHelpCommand() {
        super("help");

        setDefaultExecutor((sender, ignored) -> sender.sendMessage(
            Msg.splash("SNOOPER HELP!", "e829aa", "»").appendNewline().append(Msg.grey(
                """
                    /snooper <#ee61c0>about</#ee61c0>: Gives information about what snooper is, its\
                     limitations, and advantages.
                    /snooper <#ee61c0>help</#ee61c0>: Displays this message.
                    /snooper <#ee61c0>listen</#ee61c0> <channel>: Starts snooping on a specific channel.
                    /snooper <#ee61c0>mute</#ee61c0>: Completely disables receiving snoops. <i><dark_gray>This is required\
                     to be enabled in any public videos or screen recordings.</i>
                    /snooper <#ee61c0>unmute</#ee61c0>: Re-enables receiving snoops.
                    /snooper <#ee61c0>audit</#ee61c0> <channel>: Opens the GUI to "audit" past snoops, allowing you to\
                    view all previously sent snoops on a given channel.
                    /snooper <#ee61c0>blind</#ee61c0> <channel>: Stops snooping on a specific channel.
                    """))));

        addSubcommand(new SnooperHelpAbout());
        addSubcommand(new SnooperHelpAudit());
        addSubcommand(new SnooperHelpBlind());
        addSubcommand(new SnooperHelpListen());
        addSubcommand(new SnooperHelpMute());
        addSubcommand(new SnooperHelpUnmute());
    }
}
