package net.cytonic.cytosis.commands.party;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.protocol.publishers.PartyPacketsPublisher;
import net.cytonic.protocol.impl.responses.GenericResponse;

@SubCommand(PartyCommand.class)
class LeaveCommand extends CytosisCommand {

    LeaveCommand() {
        super("leave");
        setDefaultExecutor((s, _) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            Cytosis.get(PartyPacketsPublisher.class)
                .leaveParty(player.getUuid())
                .exceptionally(throwable -> {
                    Logger.error("Failed to process party leave: ", throwable);
                    return new GenericResponse(false, "INTERNAL_ERROR");
                })
                .thenAccept(p -> {
                    if (p.success()) return;
                    switch (p.message()) {
                        case "INTERNAL_ERROR" -> player.error("An error occurred whilst processing your request.");
                        case "ERR_NOT_IN_PARTY", "NOT_IN_PARTY", "INVALID_PARTY" ->
                            player.whoops("You are not in a party.");
                        default -> player.whoops("An unknown error occurred while processing your request <red>(%s)",
                            p.message());
                    }
                });
        });
    }
}
