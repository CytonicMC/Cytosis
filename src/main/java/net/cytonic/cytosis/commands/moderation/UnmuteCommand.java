package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.commands.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.SnoopUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.util.UUID;

public class UnmuteCommand extends CytosisCommand {

    public UnmuteCommand() {
        super("unmute");
        setCondition(CommandUtils.IS_MODERATOR);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.mm("<RED>Usage: /unmute (player)")));
        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, ignored, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(Msg.mm("<green>Fetching muted players..."));
                Cytosis.getCytonicNetwork().getMutedPlayers().forEach((uuid, ignored1) -> suggestion.addEntry(new SuggestionEntry(Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid))));
            }
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer actor)) {
                return;
            }

            final String player = context.get(playerArg);
            if (!Cytosis.getCytonicNetwork().getLifetimePlayers().containsValue(player)) {
                sender.sendMessage(Msg.mm("<red>The player " + player + " doesn't exist!"));
                return;
            }
            UUID uuid = Cytosis.getCytonicNetwork().getLifetimePlayers().getByValue(player);
            if (!Cytosis.getCytonicNetwork().getMutedPlayers().containsKey(uuid)) {
                sender.sendMessage(Msg.mm("<red>" + player + " is not muted!"));
                return;
            }


            Component snoop = actor.formattedName().append(Msg.mm("<gray> unmuted ")).append(SnoopUtils.toTarget(uuid)).append(Msg.mm("<gray>."));

            Cytosis.getSnooperManager().sendSnoop(CytosisSnoops.PLAYER_UNMUTE, SnoopUtils.toSnoop(snoop));

            Cytosis.getDatabaseManager().getMysqlDatabase().unmutePlayer(uuid);
            sender.sendMessage(Msg.mm("<green><b>UNMUTED!</green> <gray>" + player + " was successfully unmuted!"));
        }, playerArg);
    }
}
