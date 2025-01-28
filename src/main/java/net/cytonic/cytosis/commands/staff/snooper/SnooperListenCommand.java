package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.Command;

public class SnooperListenCommand extends Command {
    public SnooperListenCommand() {
        super("listen");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((s, ignored) -> s.sendMessage(Msg.whoops("You need to specify a channel!")));


        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            String rawChannel = context.get(SnooperCommand.CHANNELS);
            if (!Cytosis.getSnooperManager().getAllChannels(player).contains(rawChannel)) {
                player.sendMessage(Msg.whoops("The channel '" + rawChannel + "' either doesn't exist, or you don't have access to it."));
                return;
            }
            Cytosis.getSnooperManager().snoop(player, rawChannel);
        }, SnooperCommand.CHANNELS);
    }
}
