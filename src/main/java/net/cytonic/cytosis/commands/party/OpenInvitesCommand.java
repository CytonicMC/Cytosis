package net.cytonic.cytosis.commands.party;

import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.command.builder.arguments.ArgumentType;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.protocol.data.objects.Party;
import net.cytonic.protocol.impl.responses.GenericResponse;

@SubCommand
class OpenInvitesCommand extends CytosisCommand {

    OpenInvitesCommand() {
        super("open-invites");
        ArgumentBoolean stateArg = ArgumentType.Boolean("state");
        stateArg.setDefaultValue(s -> {
            if (!(s instanceof CytosisPlayer player)) return false;
            Party party = Cytosis.get(PartyManager.class).getPlayerParty(player.getUuid());
            if (party == null) return false;
            return !party.isOpenInvites();
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            boolean state = context.get(stateArg);
            Cytosis.get(PartyManager.class).openPartyInvites(player.getUuid(), state)
                .exceptionally(throwable -> {
                    Logger.error("An error occurred whilst opening a party's invitations:", throwable);
                    return new GenericResponse(false, "INTERNAL_ERROR");
                })
                .thenAccept(p -> {
                    if (p.success()) return;
                    switch (p.message()) {
                        case "INTERNAL_ERROR" ->
                            player.error("An error occurred whilst opening the party's invitations.");
                        case "NOT_IN_PARTY", "ERR_INVALID_PARTY" -> player.whoops("You are not in a party.");
                        case "ERR_NO_PERMISSION" ->
                            player.whoops("You must be the party leader to open the party's invitations.");
                        case "ERR_ALREADY_STATE" ->
                            player.whoops("The party already has open invites %s.", state ? "enabled" : "disabled");
                        default -> player.error("An unknown error occurred. <red>(%s)", p.message());
                    }
                });
        }, stateArg);
    }
}
