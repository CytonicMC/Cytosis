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
import net.cytonic.protocol.responses.PartyResponse;

class PromoteCommand extends CytosisCommand {

    PromoteCommand() {
        super("promote");

        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("You must specify a player.")));

        addSyntax((s, context) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            final UUID playerID = PlayerUtils.resolveUuid(context.get(PartyCommand.PARTY_PLAYER));
            if (playerID == null) {
                s.sendMessage(
                    Msg.whoops("Could not find the player '%s'", context.get(PartyCommand.PARTY_PLAYER)));
                return;
            }

            Cytosis.get(PartyManager.class).promotePlayer(player.getUuid(), playerID)
                .exceptionally(throwable -> {
                    Logger.error("Failed to process party join: ", throwable);
                    return new PartyResponse(false, "INTERNAL_ERROR");
                }).thenAccept(p -> {
                    if (p.success()) return;
                    switch (p.message()) {
                        case "INTERNAL_ERROR" ->
                            s.sendMessage(Msg.serverError("An error occurred whilst processing your request."));
                        case "NOT_IN_PARTY", "ERR_INVALID_PARTY" -> s.sendMessage(Msg.whoops("You are not in a party!"));
                        case "ERR_TARGET_NOT_IN_PARTY" -> s.sendMessage(
                            Msg.whoops("%s<gray> is not in the party.",
                                Cytosis.get(CytonicNetwork.class).getMiniName(playerID)));
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
