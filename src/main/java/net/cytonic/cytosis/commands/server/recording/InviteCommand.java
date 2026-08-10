package net.cytonic.cytosis.commands.server.recording;


import net.kyori.adventure.text.event.ClickEvent;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.replay.instance.ReplayInstance;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Players;

//todo: make this support inviting from across the network
class InviteCommand extends CytosisCommand {

    InviteCommand() {
        super("invite");
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            player.whoops("You must specify a player!");
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            if (!(player.getInstance() instanceof ReplayInstance i)) {
                player.whoops("You are not in a replay!");
                return;
            }

            CytosisPlayer target = context.get(CommandUtils.ONLINE_PLAYERS);
            if (target.getUuid().equals(player.getUuid())) {
                player.whoops("You cannot invite yourself; you're already here!");
                return;
            }
            target.sendMessage(Msg.darkGreenSplash("INVITED!", "%s invited you to view a replay with them!",
                Players.trueMiniName(player.getUuid())).append(
                Msg.green(" [JOIN THEM]").clickEvent(ClickEvent.callback(_ -> {
                        target.setInstance(i);
                        player.sendMessage(Msg.darkGreenSplash("ACCEPTED!", "%s accepted your invitation!",
                            Players.trueMiniName(player.getUuid())));
                    })
                )));

            player.success("You invited %s to view the replay with you!", Players.trueMiniName(target.getUuid()));

        }, CommandUtils.ONLINE_PLAYERS);
    }
}
