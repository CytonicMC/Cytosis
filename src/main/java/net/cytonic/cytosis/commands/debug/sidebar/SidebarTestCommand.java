package net.cytonic.cytosis.commands.debug.sidebar;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

public class SidebarTestCommand extends CytosisCommand {

    public SidebarTestCommand() {
        super("sidebartest", "sbtest", "scoreboardtest");

        this.setCondition(CommandUtils.IS_ADMIN);
        this.setDefaultExecutor((sender, ctx) -> {
            sender.sendMessage(Msg.whoops("You have to specify a sub command!"));
        });
    }

}
