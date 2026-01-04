package net.cytonic.cytosis.commands.party;

import java.util.UUID;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.cytosis.parties.packets.PartyResponsePacket;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

class AcceptCommand extends CytosisCommand {

    AcceptCommand() {
        super("accept");
        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("You must specify a player.")));

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            String rawPlayer = context.get(CommandUtils.NETWORK_PLAYERS);
            UUID playerID;
            try {
                playerID = UUID.fromString(rawPlayer);
            } catch (IllegalArgumentException ignored) {
                playerID = Cytosis.get(CytonicNetwork.class).getOnlineFlattened().getByValue(rawPlayer.toLowerCase());
            }
            if (playerID == null) {
                sender.sendMessage(Msg.whoops("Could not find the player '%s'", rawPlayer));
                return;
            }

            Cytosis.get(PartyManager.class).acceptInvite(player.getUuid(), playerID)
                .exceptionally(throwable -> {
                    Logger.error("Failed to process party invite accept: ", throwable);
                    return new PartyResponsePacket(false, "INTERNAL_ERROR");
                }).thenAccept(p -> {
                    if (p.success()) return;
                    sender.sendMessage(
                        Msg.whoops("An error occurred whilst processing your request: <red>%s</red>", p.message()));
                });

        }, CommandUtils.NETWORK_PLAYERS);
    }
}
