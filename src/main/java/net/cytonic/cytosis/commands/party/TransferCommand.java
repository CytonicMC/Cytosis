package net.cytonic.cytosis.commands.party;

import java.util.UUID;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.cytosis.parties.packets.PartyResponsePacket;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

class TransferCommand extends CytosisCommand {

    TransferCommand() {
        super("transfer");

        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("You must specify a player.")));

        addSyntax((s, context) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            String rawPlayer = context.get(PartyCommand.PARTY_PLAYER);
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
            Cytosis.get(PartyManager.class).transferPlayer(player.getUuid(), playerID)
                .exceptionally(throwable -> {
                    Logger.error("Failed to process party transfer: ", throwable);
                    return new PartyResponsePacket(false, "INTERNAL_ERROR");
                }).thenAccept(p -> {
                    if (p.success()) return;
                    switch (p.message()) {
                        case "INTERNAL_ERROR" ->
                            s.sendMessage(Msg.serverError("An error occurred whilst processing your request."));
                        case "NOT_IN_PARTY", "ERR_INVALID_PARTY" -> s.sendMessage(Msg.whoops("You are not in a party!"));
                        case "ERR_TARGET_NOT_IN_PARTY" -> s.sendMessage(
                            Msg.whoops("%s<gray> is not in the party.",
                                Cytosis.get(CytonicNetwork.class).getMiniName(finalPlayerID)));
                        case "ERR_NOT_LEADER" ->
                            s.sendMessage(Msg.whoops("You must be the party leader to promote someone."));
                        default -> s.sendMessage(
                            Msg.serverError("An unknown error occurred while processing your request <red>(%s)",
                                p.message()));
                    }
                });

        }, PartyCommand.PARTY_PLAYER);
    }
}
