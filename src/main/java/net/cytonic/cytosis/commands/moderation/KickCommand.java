package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.auditlog.Category;
import net.cytonic.cytosis.auditlog.Entry;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.enums.KickReason;
import net.cytonic.objects.OfflinePlayer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.UUID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

public class KickCommand extends Command {

    public KickCommand() {
        super("kick");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.moderation.kick"));
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<RED>Usage: /kick <player> [reason]"));
        var reasonArg = ArgumentType.StringArray("reason");
        reasonArg.setDefaultValue(new String[]{"No", "reason", "specified."});
        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, _, suggestion) -> {
            if (sender instanceof Player player) {
                player.sendActionBar(MM."<green>Fetching players...");
                Cytosis.getCytonicNetwork().getOnlinePlayers().forEach((_, name) -> suggestion.addEntry(new SuggestionEntry(name)));
            }
        });

        addSyntax((sender, context) -> {
            if (sender instanceof Player actor) {
                if (!actor.hasPermission("cytosis.commands.moderation.kick")) {
                    actor.sendMessage(MM."<red>You don't have permission to use this command!");
                    return;
                }
                final String player = context.get(playerArg);
                final String reason = String.join(" ", context.get(reasonArg));
                if (!Cytosis.getCytonicNetwork().getOnlineFlattened().containsValue(player.toLowerCase())) {
                    sender.sendMessage(MM."<red>The player \{context.get(playerArg)} doesn't exist or is not online!");
                    return;
                }

                UUID uuid = Cytosis.getCytonicNetwork().getOnlineFlattened().getByValue(player.toLowerCase());

                Cytosis.getDatabaseManager().getMysqlDatabase().getPlayerRank(uuid).whenComplete((playerRank, throwable2) -> {
                    if (throwable2 != null) {
                        sender.sendMessage(MM."<red>An error occured whilst finding \{player}'s rank!");
                        Logger.error("error", throwable2);
                        return;
                    }
                    OfflinePlayer op = new OfflinePlayer(player, uuid, playerRank);
                    if (op.hasPermission("cytosis.moderation.kick_immune")) {
                        sender.sendMessage(MM."<red>\{player} cannot be kicked!");
                        return;
                    }
                    Cytosis.getDatabaseManager().getRedisDatabase().kickPlayer(op, KickReason.COMMAND, MM."\n<red>You have been kicked. \n<aqua>Reason: \{reason}", new Entry(uuid, actor.getUuid(), Category.KICK, "kick_command"));
                });
            }
        }, playerArg, reasonArg);
    }
}
