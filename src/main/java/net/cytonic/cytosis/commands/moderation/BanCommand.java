package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.auditlog.Category;
import net.cytonic.cytosis.auditlog.Entry;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.data.enums.BanReason;
import net.cytonic.cytosis.data.enums.KickReason;
import net.cytonic.cytosis.data.objects.BanData;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.time.Instant;

/**
 * A command that allows authorized players to ban players.
 */
public class BanCommand extends Command {
    /**
     * Creates the command and sets the consumers
     */
    public BanCommand() {
        super("ban");
        setCondition(CommandUtils.IS_MODERATOR);

        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, cmdc, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(Msg.mm("<green>Fetching players..."));
            }
            Cytosis.getCytonicNetwork().getLifetimePlayers().forEach((uuid, name) ->
                    suggestion.addEntry(new SuggestionEntry(name)));
        });
        var durationArg = ArgumentType.Word("duration");
        var reasonArg = ArgumentType.Enum("reason", BanReason.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                if (!actor.isModerator()) {
                    actor.sendMessage(Msg.mm("<red>You don't have permission to use this command!"));
                    return;
                }
                final String player = context.get(playerArg);
                final String reason = context.get(reasonArg).getReason();
                final String rawDur = context.get(durationArg);
                final Instant dur = DurationParser.parse(rawDur);

                if (!Cytosis.getCytonicNetwork().getLifetimePlayers().containsValue(player)) {
                    sender.sendMessage(Msg.mm("<red>The player " + context.get(playerArg) + " doesn't exist!"));
                    return;
                }
                Cytosis.getDatabaseManager().getMysqlDatabase().findUUIDByName(player).whenComplete((uuid, throwable) -> {
                    if (throwable != null) {
                        sender.sendMessage(Msg.mm("<red>An error occured whilst finding " + player + "!"));
                        Logger.error("error", throwable);
                        return;
                    }
                    Cytosis.getDatabaseManager().getMysqlDatabase().isBanned(uuid).whenComplete((banned, throwable1) -> {
                        if (throwable1 != null) {
                            sender.sendMessage(Msg.mm("<red>An error occured whilst finding if " + player + " is banned!"));
                            Logger.error("error", throwable1);
                            return;
                        }
                        if (banned.isBanned()) {
                            sender.sendMessage(Msg.mm("<red>" + player + " is already banned!"));
                            return;
                        }
                        Cytosis.getDatabaseManager().getMysqlDatabase().getPlayerRank(uuid).whenComplete((playerRank, throwable2) -> {
                            if (throwable2 != null) {
                                sender.sendMessage(Msg.mm("<red>An error occured whilst finding " + player + "'s rank!"));
                                Logger.error("error", throwable2);
                                return;
                            }
                            if (playerRank.isStaff()) {
                                sender.sendMessage(Msg.mm("<red>" + player + " cannot be banned!"));
                                return;
                            }

                            Cytosis.getDatabaseManager().getMysqlDatabase().banPlayer(uuid, reason, dur).whenComplete((ignored, throwable3) -> {
                                if (throwable3 != null) {
                                    actor.sendMessage(Msg.mm("<red>An error occured whilst banning " + player + "!"));
                                    return;
                                }
                                Cytosis.getNatsManager().kickPlayer(uuid, KickReason.BANNED, Msg.formatBanMessage(new BanData(reason, dur, true)), new Entry(uuid, actor.getUuid(), Category.KICK, "ban_command"));
                                actor.sendMessage(Msg.mm("<green>" + player + " was successfully banned for " + DurationParser.unparseFull(dur) + "."));
                                Cytosis.getDatabaseManager().getMysqlDatabase().addAuditLogEntry(new Entry(uuid, actor.getUuid(), Category.BAN, reason));
                            });
                        });
                    });
                });
            }
        }, playerArg, durationArg, reasonArg);
    }
}