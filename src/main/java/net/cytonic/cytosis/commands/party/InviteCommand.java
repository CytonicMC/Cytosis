package net.cytonic.cytosis.commands.party;

import java.util.UUID;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.nicknames.NicknameManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.protocol.publishers.PartyPacketsPublisher;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;
import net.cytonic.cytosis.utils.Players;
import net.cytonic.cytosis.utils.Preferences;
import net.cytonic.protocol.impl.responses.GenericResponse;

class InviteCommand extends CytosisCommand {

    InviteCommand() {
        super("invite");

        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("You must specify a player.")));

        addSyntax((s, context) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            //todo: do something about deanonymizing players
            final UUID playerID = PlayerUtils.resolveUuid(context.get(CommandUtils.NETWORK_PLAYERS));
            if (playerID == null) {
                player.whoops("Could not find the player '%s'", context.get(CommandUtils.NETWORK_PLAYERS));
                return;
            }

            if (!Cytosis.get(PreferenceManager.class).getPlayerPreference(playerID, Preferences.ACCEPT_PARTY_INVITES) ||
                Cytosis.get(NicknameManager.class).isNicked(playerID)) {
                player.whoops("That player has party invites disabled!");
                return;
            }

            Cytosis.get(PartyPacketsPublisher.class).sendInvite(player.getUuid(), playerID)
                .exceptionally(throwable -> {
                    Logger.error("Failed to process party invite: ", throwable);
                    return new GenericResponse(false, "INTERNAL_ERROR");
                }).thenAccept(p -> {
                    if (p.success()) return;
                    switch (p.message()) {
                        case "INTERNAL_ERROR" -> player.error("An error occurred whilst processing your request.");
                        case "ERR_SEND_TO_SELF" -> player.whoops("You cannot invite yourself!");
                        case "ERR_ALREADY_IN_PARTY" -> player.whoops("%s<gray> is already in the party.",
                            Players.miniName(playerID));
                        case "ERR_NO_PERMISSION" -> player.whoops("You don't have permission to send invitations.");
                        case "ERR_ALREADY_INVITED" -> player.whoops("%s<gray> has already been invited to the party.",
                            Players.miniName(playerID));
                        default -> player.whoops("An unknown error occurred while processing your request <red>(%s)",
                            p.message());
                    }
                });

        }, CommandUtils.NETWORK_PLAYERS);
    }
}
