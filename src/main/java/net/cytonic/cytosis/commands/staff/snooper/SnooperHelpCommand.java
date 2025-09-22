package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.commands.staff.snooper.help.SnooperHelpAbout;
import net.cytonic.cytosis.commands.staff.snooper.help.SnooperHelpAudit;
import net.cytonic.cytosis.commands.staff.snooper.help.SnooperHelpBlind;
import net.cytonic.cytosis.commands.staff.snooper.help.SnooperHelpListen;
import net.cytonic.cytosis.commands.staff.snooper.help.SnooperHelpMute;
import net.cytonic.cytosis.commands.staff.snooper.help.SnooperHelpUnmute;
import net.cytonic.cytosis.commands.utils.CytosisCommand;

public class SnooperHelpCommand extends CytosisCommand {

    public SnooperHelpCommand() {
        super("help");
        addSubcommand(new SnooperHelpAbout());
        addSubcommand(new SnooperHelpAudit());
        addSubcommand(new SnooperHelpBlind());
        addSubcommand(new SnooperHelpListen());
        addSubcommand(new SnooperHelpMute());
        addSubcommand(new SnooperHelpUnmute());
    }
}
