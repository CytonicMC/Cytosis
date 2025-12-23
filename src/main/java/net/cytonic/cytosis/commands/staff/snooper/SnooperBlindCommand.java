package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class SnooperBlindCommand extends CytosisCommand {

    public SnooperBlindCommand() {
        super("blind");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((s, ignored) -> s.sendMessage(Msg.whoops("You need to specify a channel!")));

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            String rawChannel = context.get(SnooperCommand.CHANNELS);
            SnooperManager snooperManager = Cytosis.get(SnooperManager.class);
            if (!snooperManager.getAllChannels(player).contains(rawChannel)) {
                player.sendMessage(Msg.whoops(
                    "The channel '" + rawChannel + "' either doesn't exist, or you don't have access to it."));
                return;
            }
            snooperManager.blind(player, rawChannel);
        }, SnooperCommand.CHANNELS);
    }
}