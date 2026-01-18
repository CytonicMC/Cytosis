package net.cytonic.cytosis.commands.party;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.responses.PartyResponse;

class YoinkCommand extends CytosisCommand {

    YoinkCommand() {
        super("yoink");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((s, _) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            Cytosis.get(PartyManager.class).yoinkParty(player.getUuid())
                .exceptionally(throwable -> {
                    Logger.error("Failed to process party disband: ", throwable);
                    return new PartyResponse(false, "INTERNAL_ERROR");
                }).thenAccept(p -> {
                    if (p.success()) return;
                    switch (p.message()) {
                        case "INTERNAL_ERROR" ->
                            s.sendMessage(Msg.serverError("An error occurred whilst processing your request."));
                        case "ERR_NOT_IN_PARTY", "NOT_IN_PARTY", "INVALID_PARTY" ->
                            s.sendMessage(Msg.whoops("You are not in a party."));
                        case "ERR_ALREADY_LEADER" -> s.sendMessage(Msg.whoops("You are already the leader of this party!"));
                        default ->
                            s.sendMessage(Msg.whoops("An unknown error occurred while processing your request <red>(%s)",
                                p.message()));
                    }
                });
        });
    }
}
