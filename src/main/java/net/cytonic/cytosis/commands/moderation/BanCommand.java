package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.enums.BanReason;
import net.cytonic.cytosis.data.enums.KickReason;
import net.cytonic.cytosis.data.objects.BanData;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.SnoopUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;

import java.time.Instant;
import java.util.UUID;

/**
 * A command that allows authorized players to ban players.
 */
public class BanCommand extends CytosisCommand {
    /**
     * Creates the command and sets the consumers
     */
    public BanCommand() {
        super("ban");
        setCondition(CommandUtils.IS_MODERATOR);

        var durationArg = ArgumentType.Word("duration");
        var reasonArg = ArgumentType.Enum("reason", BanReason.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                if (!actor.isModerator()) {
                    actor.sendMessage(Msg.mm("<red>You don't have permission to use this command!"));
                    return;
                }
                final String player = context.get(CommandUtils.LIFETIME_PLAYERS);
                final String reason = context.get(reasonArg).getReason();
                final String rawDur = context.get(durationArg);
                final Instant dur = DurationParser.parse(rawDur);

                UUID uuid = CommandUtils.resolveUuid(player);
                if (uuid == null) {
                    sender.sendMessage(Msg.mm("<red>The player %s doesn't exist!", player));
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
                            Cytosis.getNatsManager().kickPlayer(uuid, KickReason.BANNED, Msg.formatBanMessage(new BanData(reason, dur, true)));
                            actor.sendMessage(Msg.mm("<green>" + player + " was successfully banned for " + DurationParser.unparseFull(dur) + "."));

                            Component snoop = actor.formattedName().append(Msg.mm("<gray> banned ")).append(SnoopUtils.toTarget(uuid)).append(Msg.mm("<gray> for " + DurationParser.unparseFull(dur) + " with the reason " + reason));
                            Cytosis.getSnooperManager().sendSnoop(CytosisSnoops.PLAYER_BAN, SnoopUtils.toSnoop(snoop));
                        });
                    });
                });

            }
        }, CommandUtils.LIFETIME_PLAYERS, durationArg, reasonArg);
    }
}