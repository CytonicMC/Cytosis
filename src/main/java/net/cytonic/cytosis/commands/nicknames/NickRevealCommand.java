package net.cytonic.cytosis.commands.nicknames;

import net.cytonic.cytosis.commands.util.CommandUtils;
import net.cytonic.cytosis.commands.util.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class NickRevealCommand extends CytosisCommand {
    public NickRevealCommand() {
        super("reveal");
        setCondition(CommandUtils.IS_STAFF);

        setDefaultExecutor((sender, ignored) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            if (!player.isNicked()) {
                player.sendMessage(Msg.whoops("You are not currently nicked!"));
                return;
            }
            sender.sendMessage(Msg.goldSplash("REVEALED!", "Your nickname is currently ").append(player.formattedName()));
        });

        addSyntax((sender, context) -> {
            CytosisPlayer player = context.get(CommandUtils.ONLINE_PLAYERS);
            if (player == null) {
                sender.sendMessage(Msg.whoops("Player not found!"));
                return;
            }

            if (!player.isNicked()) {
                sender.sendMessage(Msg.whoops("").append(player.formattedName().append(Msg.grey(" is not nicked!"))));
                return;
            }

            sender.sendMessage(Msg.goldSplash("REVEALED!", "")
                    .append(player.trueFormattedName())
                    .append(Msg.grey(" is currently nicked as "))
                    .append(player.formattedName())
                    .append(Msg.grey(" <click:copy_to_clipboard:%s>(UUID: %s)", player.getUuid().toString(), player.getUuid().toString()))
            );
        }, CommandUtils.ONLINE_PLAYERS);
    }
}
