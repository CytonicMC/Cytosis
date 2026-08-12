package net.cytonic.cytosis.commands.debug.preferences;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

public class PreferenceCommand extends CytosisCommand {

    public PreferenceCommand() {
        super("preference", "pref");
        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.red("Please specify an operation!")));
    }
}
