package net.cytonic.cytosis.commands.party;

import java.util.UUID;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Players;
import net.cytonic.protocol.impl.responses.GenericResponse;

class JoinCommand extends CytosisCommand {

    JoinCommand() {
        super("join");

        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("You must specify a player.")));

        addSyntax((s, context) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            //todo: do something about deanonymizing players
            final UUID playerID = Players.resolveUuid(context.get(CommandUtils.NETWORK_PLAYERS));
            if (playerID == null) {
                player.whoops("Could not find the player '%s'", context.get(CommandUtils.NETWORK_PLAYERS));
                return;
            }

            Cytosis.get(PartyManager.class).joinParty(playerID, player.getUuid())
                .exceptionally(throwable -> {
                    Logger.error("Failed to process party join: ", throwable);
                    return new GenericResponse(false, "INTERNAL_ERROR");
                }).thenAccept(p -> {
                    if (p.success()) return;
                    switch (p.message()) {
                        case "INTERNAL_ERROR" -> player.error("An error occurred whilst processing your request.");
                        case "TARGET_NOT_IN_PARTY", "INVALID_PARTY" -> player.whoops("%s is not in a party.",
                            Players.miniName(playerID));
                        case "SENDER_NOT_FOUND" ->
                            player.whoops("Somehow you went missing! Please explain how you managed that ;)");
                        case "ERR_ALREADY_IN_PARTY" -> player.whoops("You're already in a party.");
                        case "ERR_NO_INVITE" -> player.whoops("You do not have an invite to join %s<gray>'s party.",
                            Players.miniName(playerID));
                        default -> player.whoops("An unknown error occurred while processing your request <red>(%s)",
                            p.message());
                    }
                });

        }, CommandUtils.NETWORK_PLAYERS);
    }
}
