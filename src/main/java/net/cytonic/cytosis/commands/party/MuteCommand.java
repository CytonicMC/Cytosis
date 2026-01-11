package net.cytonic.cytosis.commands.party;

import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.command.builder.arguments.ArgumentType;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.data.objects.Party;
import net.cytonic.protocol.responses.PartyResponse;

class MuteCommand extends CytosisCommand {

    private final PartyManager pm = Cytosis.get(PartyManager.class);

    MuteCommand() {
        super("mute");
        ArgumentBoolean stateArg = ArgumentType.Boolean("state");
        stateArg.setDefaultValue(s -> {
            if (!(s instanceof CytosisPlayer player)) return false;
            Party party = pm.getPlayerParty(player.getUuid());
            if (party == null) return false;
            return !party.isMuted();
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            pm.muteParty(player.getUuid(), context.get(stateArg))
                .exceptionally(throwable -> {
                    Logger.error("An error occurred whilst muting a party:", throwable);
                    return new PartyResponse(false, "INTERNAL_ERROR");
                })
                .thenAccept(p -> {
                    if (p.success()) return;
                    switch (p.message()) {
                        case "INTERNAL_ERROR" ->
                            sender.sendMessage(Msg.serverError("An error occurred whilst muting the party."));
                        case "NOT_IN_PARTY", "ERR_INVALID_PARTY" ->
                            sender.sendMessage(Msg.whoops("You are not in a party."));
                        case "ERR_NO_PERMISSION" ->
                            sender.sendMessage(Msg.whoops("You must be the party leader to mute the party."));
                        default ->
                            sender.sendMessage(Msg.serverError("An unknown error occurred. <red>(%s)", p.message()));
                    }
                });
        }, stateArg);
    }
}
