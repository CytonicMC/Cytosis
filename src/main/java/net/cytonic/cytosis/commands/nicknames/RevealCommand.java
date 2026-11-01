package net.cytonic.cytosis.commands.nicknames;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

@SubCommand(NickCommand.class)
class RevealCommand extends CytosisCommand {

    RevealCommand() {
        super("reveal");
        setCondition(CommandUtils.IS_STAFF);

        setDefaultExecutor((sender, ignored) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            if (!player.isNicked()) {
                player.whoops("You are not currently nicked!");
                return;
            }
            sender.sendMessage(Msg.goldSplash("REVEALED!", "Your nickname is currently ")
                .append(player.formattedName()));
        });

        addSyntax((sender, context) -> {
            CytosisPlayer player = context.get(CommandUtils.ONLINE_PLAYERS);

            if (!player.isNicked()) {
                sender.sendMessage(Msg.whoops("").append(player.formattedName().append(Msg.grey(" is not nicked!"))));
                return;
            }

            sender.sendMessage(Msg.goldSplash("REVEALED!", "").append(player.trueFormattedName())
                .append(Msg.grey(" is currently nicked as ")).append(player.formattedName())
                .append(Msg.grey(" <click:copy_to_clipboard:%s>(UUID: %s)", player.getUuid()
                    .toString(), player.getUuid()
                    .toString())));
        }, CommandUtils.ONLINE_PLAYERS);
    }
}
