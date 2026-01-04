package net.cytonic.cytosis.commands.party;

import java.util.UUID;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.packet.packets.parties.PartyResponsePacket;
import net.cytonic.cytosis.data.packet.publishers.PartyPacketsPublisher;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

class InviteCommand extends CytosisCommand {

    InviteCommand() {
        super("invite");

        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("You must specify a player.")));

        addSyntax((s, context) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            String rawPlayer = context.get(CommandUtils.NETWORK_PLAYERS);
            UUID playerID;
            try {
                playerID = UUID.fromString(rawPlayer);
            } catch (IllegalArgumentException ignored) {
                playerID = Cytosis.get(CytonicNetwork.class).getOnlineFlattened().getByValue(rawPlayer.toLowerCase());
            }
            if (playerID == null) {
                s.sendMessage(Msg.whoops("Could not find the player '%s'", rawPlayer));
                return;
            }

            final UUID finalPlayerID = playerID;
            Cytosis.get(PartyPacketsPublisher.class).sendInvite(player.getUuid(), playerID)
                .exceptionally(throwable -> {
                    Logger.error("Failed to process party invite: ", throwable);
                    return new PartyResponsePacket(false, "INTERNAL_ERROR");
                }).thenAccept(p -> {
                    if (p.isSuccess()) return;
                    switch (p.getMessage()) {
                        case "INTERNAL_ERROR" ->
                            s.sendMessage(Msg.serverError("An error occurred whilst processing your request."));
                        case "ERR_SEND_TO_SELF" -> s.sendMessage(Msg.whoops("You cannot invite yourself!"));
                        case "ERR_ALREADY_IN_PARTY" -> s.sendMessage(Msg.whoops("%s<gray> is already in the party.",
                            Cytosis.get(CytonicNetwork.class).getMiniName(finalPlayerID)));
                        case "ERR_NO_PERMISSION" ->
                            s.sendMessage(Msg.whoops("You don't have permission to send invitations."));
                        case "ERR_ALREADY_INVITED" ->
                            s.sendMessage(Msg.whoops("%s<gray> has already been invited to the party.",
                                Cytosis.get(CytonicNetwork.class).getMiniName(finalPlayerID)));
                        default -> s.sendMessage(
                            Msg.whoops("An unknown error occurred while processing your request <red>(%s)",
                                p.getMessage()));
                    }
                });

        }, CommandUtils.NETWORK_PLAYERS);
    }
}
