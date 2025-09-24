package net.cytonic.cytosis.commands.staff.snooper.help;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

public class SnooperHelpAudit extends CytosisCommand {

    private static final Component MESSAGE = Msg.splash("SNOOPER HELP!", "e829aa",
            "» /snooper <#ee61c0>audit</#ee61c0>")
        .appendNewline()
        .append(Msg.grey("""
            Opens a menu allowing you to audit past snoops on the specified channel. You can only audit snoops you have\
            access to, based on rank. There are two 'modes' to the audit menu.<newline><newline> There is a basic mode,\
            showing an unfiltered list, sorted by newest first. Good for looking at something that happened recently.\
            <newline><newline> Then there is the advanced mode. There are several filtering and sorting options.\
            For starters, there is a text search option, allowing you to filter the contents of snoops displayed.\
            Prefix a word with '+' to include it, and prefix it with '-' to exclude options with it. Then, you can\
            pick a date range for the creation of the snoop. There are a few options: last hour, 12 hours, 1 day,\
            30 days, 180 days, and 1 year. On top of that, you can decide the sort, either ascending by date\
            (oldest first), or descending by date (newest first).
            """));

    public SnooperHelpAudit() {
        super("audit");
        setDefaultExecutor(((sender, context) -> sender.sendMessage(MESSAGE)));
    }
}
