package net.cytonic.cytosis.commands.moderation;

import java.time.Instant;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.config.Snoops;
import net.cytonic.cytosis.data.GlobalDatabase;
import net.cytonic.cytosis.data.enums.BanReason;
import net.cytonic.cytosis.data.objects.BanData;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;
import net.cytonic.cytosis.utils.SnoopUtils;
import net.cytonic.protocol.data.enums.KickReason;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.impl.notify.PlayerKickNotifyPacket;

/**
 * A command that allows authorized players to ban players.
 */
public class BanCommand extends CytosisCommand {

    private final GlobalDatabase gdb = Cytosis.get(GlobalDatabase.class);

    /**
     * Creates the command and sets the consumers
     */
    public BanCommand() {
        super("ban");
        setCondition(CommandUtils.IS_MODERATOR);

        ArgumentWord durationArg = ArgumentType.Word("duration");
        ArgumentEnum<@NotNull BanReason> reasonArg = ArgumentType.Enum("reason", BanReason.class)
            .setFormat(ArgumentEnum.Format.LOWER_CASED);

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

                UUID uuid = PlayerUtils.resolveUuid(player);
                if (uuid == null) {
                    sender.sendMessage(Msg.red("The player %s doesn't exist!", player));
                    return;
                }

                banPlayer(sender, actor, uuid, player, reason, dur);

            }
        }, CommandUtils.LIFETIME_PLAYERS, durationArg, reasonArg);
    }

    private void banPlayer(CommandSender sender, CytosisPlayer actor, UUID uuid, String player, String reason,
        Instant dur) {
        gdb.isBanned(uuid)
            .thenAccept(banData -> handleBanCheck(sender, actor, uuid, player, reason, dur, banData))
            .exceptionally(throwable -> handleBanCheckError(sender, player, throwable));
    }

    private void handleBanCheck(CommandSender sender, CytosisPlayer actor, UUID uuid, String player, String reason,
        Instant dur, BanData banData) {
        if (banData.isBanned()) {
            sender.sendMessage(Msg.red("%s is already banned!", player));
            return;
        }

        checkPlayerRankAndBan(sender, actor, uuid, player, reason, dur);
    }

    private void checkPlayerRankAndBan(CommandSender sender, CytosisPlayer actor, UUID uuid, String player,
        String reason, Instant dur) {
        gdb.getPlayerRank(uuid).whenComplete((playerRank, throwable) -> {
            if (throwable != null) {
                handleRankCheckError(sender, player, throwable);
                return;
            }

            if (playerRank.isStaff()) {
                sender.sendMessage(Msg.mm("%s cannot be banned!", player));
                return;
            }

            executeBan(actor, uuid, player, reason, dur);
        });
    }

    private void executeBan(CytosisPlayer actor, UUID uuid, String player, String reason, Instant dur) {
        gdb.banPlayer(uuid, reason, dur).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                handleBanExecutionError(actor, player, throwable);
                return;
            }

            handleSuccessfulBan(actor, uuid, player, reason, dur);
        });
    }

    private void handleSuccessfulBan(CytosisPlayer actor, UUID uuid, String player, String reason, Instant dur) {
        BanData banData = new BanData(reason, dur, true);
        new PlayerKickNotifyPacket.Packet(uuid, KickReason.BANNED,
            new JsonComponent(Msg.formatBanMessage(banData))).publish();

        String durationText = DurationParser.unparseFull(dur);
        actor.sendMessage(Msg.mm("<green>%s was successfully banned for %s.", player, durationText));

        sendBanSnoop(actor, uuid, reason, dur);
    }

    private void sendBanSnoop(CytosisPlayer actor, UUID uuid, String reason, Instant dur) {
        String durationText = DurationParser.unparseFull(dur);
        Component snoop = actor.formattedName().append(Msg.grey(" banned ")).append(SnoopUtils.toTarget(uuid))
            .append(Msg.grey(" for %s with the reason %s", durationText, reason));

        Cytosis.get(SnooperManager.class).sendSnoop(Snoops.PLAYER_BAN, Msg.snoop(snoop));
    }

    private Void handleBanCheckError(CommandSender sender, String player, Throwable throwable) {
        sender.sendMessage(Msg.red("An error occurred whilst finding if %s is banned!", player));
        Logger.error("Error checking ban status", throwable);
        return null;
    }

    private void handleRankCheckError(CommandSender sender, String player, Throwable throwable) {
        sender.sendMessage(Msg.red("An error occurred whilst finding %s's rank!", player));
        Logger.error("Error checking player rank", throwable);
    }

    private void handleBanExecutionError(CommandSender actor, String player, Throwable throwable) {
        actor.sendMessage(Msg.red("An error occurred whilst banning %s!", player));
        Logger.error("Error executing ban", throwable);
    }
}