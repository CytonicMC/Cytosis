package net.cytonic.cytosis.commands.party;

import java.util.UUID;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.packet.packets.parties.PartyResponsePacket;
import net.cytonic.cytosis.data.packet.publishers.PartyPacketsPublisher;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;

class AcceptCommand extends CytosisCommand {

    AcceptCommand() {
        super("accept");
        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("You must specify a player.")));

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;

            //todo: do something about deanonymizing players
            final UUID playerID = PlayerUtils.resolveUuid(context.get(CommandUtils.NETWORK_PLAYERS));
            if (playerID == null) {
                sender.sendMessage(
                    Msg.whoops("Could not find the player '%s'", context.get(CommandUtils.NETWORK_PLAYERS)));
                return;
            }

            Cytosis.get(PartyPacketsPublisher.class).acceptInvite(player.getUuid(), playerID)
                .exceptionally(throwable -> {
                    Logger.error("Failed to process party invite accept: ", throwable);
                    return new PartyResponsePacket(false, "INTERNAL_ERROR");
                }).thenAccept(p -> {
                    if (p.isSuccess()) return;
                    sender.sendMessage(
                        Msg.whoops("An error occurred whilst processing your request: <red>%s</red>", p.getMessage()));
                });

        }, CommandUtils.NETWORK_PLAYERS);
    }
}
