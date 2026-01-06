package net.cytonic.cytosis.commands.party;

import java.util.UUID;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.packet.packets.parties.PartyResponsePacket;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;

class JoinCommand extends CytosisCommand {

    JoinCommand() {
        super("join");

        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("You must specify a player.")));

        addSyntax((s, context) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            //todo: do something about deanonymizing players
            final UUID playerID = PlayerUtils.resolveUuid(context.get(CommandUtils.NETWORK_PLAYERS));
            if (playerID == null) {
                s.sendMessage(
                    Msg.whoops("Could not find the player '%s'", context.get(CommandUtils.NETWORK_PLAYERS)));
                return;
            }


            Cytosis.get(PartyManager.class).joinParty(playerID, player.getUuid())
                .exceptionally(throwable -> {
                    Logger.error("Failed to process party join: ", throwable);
                    return new PartyResponsePacket(false, "INTERNAL_ERROR");
                }).thenAccept(p -> {
                    if (p.isSuccess()) return;
                    switch (p.getMessage()) {
                        case "INTERNAL_ERROR" ->
                            s.sendMessage(Msg.serverError("An error occurred whilst processing your request."));
                        case "TARGET_NOT_IN_PARTY", "INVALID_PARTY" -> s.sendMessage(Msg.whoops("%s is not in a party.",
                            Cytosis.get(CytonicNetwork.class).getMiniName(playerID)));
                        case "SENDER_NOT_FOUND" ->
                            s.sendMessage(Msg.whoops("Somehow you went missing! Please explain how you managed that ;)"));
                        case "ERR_ALREADY_IN_PARTY" -> s.sendMessage(Msg.whoops("You're already in a party."));
                        case "ERR_NO_INVITE" ->
                            s.sendMessage(Msg.whoops("You do not have an invite to join %s<gray>'s party.",
                                Cytosis.get(CytonicNetwork.class).getMiniName(playerID)));
                        default -> s.sendMessage(
                            Msg.whoops("An unknown error occurred while processing your request <red>(%s)",
                                p.getMessage()));
                    }
                });

        }, CommandUtils.NETWORK_PLAYERS);
    }
}
