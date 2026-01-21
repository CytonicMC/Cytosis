package net.cytonic.cytosis.commands.party;

import java.util.UUID;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;
import net.cytonic.protocol.impl.responses.PartyResponse;

class KickCommand extends CytosisCommand {

    KickCommand() {
        super("kick");

        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("You must specify a player.")));

        addSyntax((s, context) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            final UUID playerID = PlayerUtils.resolveUuid(context.get(PartyCommand.PARTY_PLAYER));
            if (playerID == null) {
                s.sendMessage(
                    Msg.whoops("Could not find the player '%s'", context.get(PartyCommand.PARTY_PLAYER)));
                return;
            }

            Cytosis.get(PartyManager.class).kickPlayer(player.getUuid(), playerID)
                .exceptionally(throwable -> {
                    Logger.error("Failed to process party join: ", throwable);
                    return new PartyResponse(false, "INTERNAL_ERROR");
                }).thenAccept(p -> {
                    if (p.success()) return;
                    switch (p.message()) {
                        case "INTERNAL_ERROR" ->
                            s.sendMessage(Msg.serverError("An error occurred whilst processing your request."));
                        case "NOT_IN_PARTY" -> s.sendMessage(Msg.whoops("You are not in a party!"));
                        case "ERR_TARGET_NOT_IN_PARTY", "INVALID_PARTY" -> s.sendMessage(
                            Msg.whoops("%s<gray> is not in the party.",
                                Cytosis.get(CytonicNetwork.class).getMiniName(playerID)));
                        case "ERR_CANNOT_KICK_SELF" ->
                            s.sendMessage(Msg.whoops("You cannot kick yourself. Use '/party leave' instead."));
                        case "ERR_CANNOT_KICK_LEADER" ->
                            s.sendMessage(Msg.whoops("You cannot kick the leader of the party."));
                        case "ERR_NO_KICK_PERMISSION" ->
                            s.sendMessage(Msg.whoops("You don't have the authority to kick players."));
                        case "ERR_INVALID_PARTY" -> s.sendMessage(Msg.whoops("You don't seem to be in a party."));
                        default -> s.sendMessage(
                            Msg.whoops("An unknown error occurred while processing your request <red>(%s)",
                                p.message()));
                    }
                });

        }, PartyCommand.PARTY_PLAYER);
    }
}
