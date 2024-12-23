package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.auditlog.Category;
import net.cytonic.cytosis.auditlog.Entry;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.MessageUtils;
import net.cytonic.enums.BanReason;
import net.cytonic.enums.KickReason;
import net.cytonic.objects.BanData;
import net.cytonic.objects.OfflinePlayer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.time.Instant;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A command that allows authorized players to ban players.
 */
public class BanCommand extends Command {
    /**
     * Creates the command and sets the consumers
     */
    public BanCommand() {
        super("ban");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.moderation.ban"));

        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, _, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(MM."<green>Fetching players...");
            }
            Cytosis.getCytonicNetwork().getLifetimePlayers().forEach((_, name) ->
                    suggestion.addEntry(new SuggestionEntry(name)));
        });
        var durationArg = ArgumentType.Word("duration");
        var reasonArg = ArgumentType.Enum("reason", BanReason.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                if (!actor.hasPermission("cytosis.commands.moderation.ban")) {
                    actor.sendMessage(MM."<red>You don't have permission to use this command!");
                    return;
                }
                final String player = context.get(playerArg);
                final String reason = context.get(reasonArg).getReason();
                final String rawDur = context.get(durationArg);
                final Instant dur = DurationParser.parse(rawDur);

                if (!Cytosis.getCytonicNetwork().getLifetimePlayers().containsValue(player)) {
                    sender.sendMessage(MM."<red>The player \{context.get(playerArg)} doesn't exist!");
                    return;
                }
                Cytosis.getDatabaseManager().getMysqlDatabase().findUUIDByName(player).whenComplete((uuid, throwable) -> {
                    if (throwable != null) {
                        sender.sendMessage(MM."<red>An error occured whilst finding \{player}!");
                        Logger.error("error", throwable);
                        return;
                    }
                    Cytosis.getDatabaseManager().getMysqlDatabase().isBanned(uuid).whenComplete((banned, throwable1) -> {
                        if (throwable1 != null) {
                            sender.sendMessage(MM."<red>An error occured whilst finding if \{player} is banned!");
                            Logger.error("error", throwable1);
                            return;
                        }
                        if (banned.isBanned()) {
                            sender.sendMessage(MM."<red>\{player} is already banned!");
                            return;
                        }
                        Cytosis.getDatabaseManager().getMysqlDatabase().getPlayerRank(uuid).whenComplete((playerRank, throwable2) -> {
                            if (throwable2 != null) {
                                sender.sendMessage(MM."<red>An error occured whilst finding \{player}'s rank!");
                                Logger.error("error", throwable2);
                                return;
                            }
                            OfflinePlayer op = new OfflinePlayer(player, uuid, playerRank);
                            if (op.hasPermission("cytosis.moderation.ban_immune")) {
                                sender.sendMessage(MM."<red>\{player} cannot be banned!");
                                return;
                            }

                            Cytosis.getDatabaseManager().getMysqlDatabase().banPlayer(uuid, reason, dur).whenComplete((_, throwable3) -> {
                                if (throwable3 != null) {
                                    actor.sendMessage(MM."<red>An error occured whilst banning \{player}!");
                                    return;
                                }
                                Cytosis.getDatabaseManager().getRedisDatabase().kickPlayer(op, KickReason.BANNED, MessageUtils.formatBanMessage(new BanData(reason, dur, true)), new Entry(uuid, actor.getUuid(), Category.KICK, "ban_command"));
                                actor.sendMessage(MM."<green>\{player} was successfully banned for \{DurationParser.unparseFull(dur)}.");
                                Cytosis.getDatabaseManager().getMysqlDatabase().addAuditLogEntry(new Entry(uuid, actor.getUuid(), Category.BAN, reason));
                            });
                        });
                    });
                });
            }
        }, playerArg, durationArg, reasonArg);
    }
}