package net.cytonic.cytosis.commands.server.recording;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

public class RecordingCommand extends CytosisCommand {

    public RecordingCommand() {
        super("recording", "replay");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((s, _) -> s.sendMessage(Msg.whoops("Use a subcommand, doofus.")));
        addSubcommands(new SaveCommand(), new ViewCommand(), new LeaveCommand(), new InviteCommand());
    }
}
