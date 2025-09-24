package net.cytonic.cytosis.commands.moderation;

import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.SnoopUtils;

public class UnmuteCommand extends CytosisCommand {

    public UnmuteCommand() {
        super("unmute");
        setCondition(CommandUtils.IS_MODERATOR);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.mm("<red>Usage: /unmute (player)")));
        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, ignored, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(Msg.mm("<green>Fetching muted players..."));
                CytonicNetwork network = Cytosis.CONTEXT.getComponent(CytonicNetwork.class);
                network.getMutedPlayers()
                    .forEach((uuid, ignored1) -> suggestion.addEntry(new SuggestionEntry(network
                        .getLifetimePlayers()
                        .getByKey(uuid))));
            }
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer actor)) return;

            final String player = context.get(playerArg);
            CytonicNetwork network = Cytosis.CONTEXT.getComponent(CytonicNetwork.class);
            if (!network.getLifetimePlayers().containsValue(player)) {
                sender.sendMessage(Msg.whoops("The player %s doesn't exist!", player));
                return;
            }
            UUID uuid = network.getLifetimePlayers().getByValue(player);
            if (!network.getMutedPlayers().containsKey(uuid)) {
                sender.sendMessage(Msg.whoops("%s is not muted!", player));
                return;
            }

            Component snoop = actor.formattedName().append(Msg.mm("<gray> unmuted ")).append(SnoopUtils.toTarget(uuid))
                .append(Msg.mm("<gray>."));

            Cytosis.CONTEXT.getComponent(SnooperManager.class).sendSnoop(CytosisSnoops.PLAYER_UNMUTE, Msg.snoop(snoop));
            Cytosis.CONTEXT.getComponent(MysqlDatabase.class).unmutePlayer(uuid);
            sender.sendMessage(Msg.greenSplash("UNMUTED", "%s was successfully unmuted!", player));
        }, playerArg);
    }
}