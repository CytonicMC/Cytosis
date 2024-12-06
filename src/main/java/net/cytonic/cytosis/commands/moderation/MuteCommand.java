package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.auditlog.Category;
import net.cytonic.cytosis.auditlog.Entry;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.objects.OfflinePlayer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.time.Instant;

import static net.cytonic.utils.MiniMessageTemplate.MM;

public class MuteCommand extends Command {

    public MuteCommand() {
        super("mute");
        setCondition((player, _) -> player.hasPermission("cytosis.commands.moderation.mute"));
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<RED>Usage: /mute <player> (duration)"));
        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, _, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(MM."<green>Fetching players...");
            }
            Cytosis.getCytonicNetwork().getLifetimePlayers().forEach((_, name) ->
                    suggestion.addEntry(new SuggestionEntry(name)));
        });
        var durationArg = ArgumentType.Word("duration");

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                if (!actor.hasPermission("cytosis.commands.moderation.mute")) {
                    actor.sendMessage(MM."<red>You don't have permission to use this command!");
                }

                final String target = context.get(playerArg);
                final String rawDur = context.get(durationArg);
                final Instant dur = DurationParser.parse(rawDur);

                if (!Cytosis.getCytonicNetwork().getLifetimePlayers().containsValue(target)) {
                    sender.sendMessage(MM."<red>The player \{target} doesn't exist!");
                    return;
                }
                Cytosis.getDatabaseManager().getMysqlDatabase().findUUIDByName(target).whenComplete((uuid, throwable) -> {
                    if (throwable != null) {
                        sender.sendMessage(MM."<red>An error occured whilst finding \{target}!");
                        Logger.error("error", throwable);
                        return;
                    }
                    Cytosis.getDatabaseManager().getMysqlDatabase().isMuted(uuid).whenComplete((muted, throwable1) -> {
                        if (throwable1 != null) {
                            sender.sendMessage(MM."<red>An error occured whilst finding if \{target} is muted!");
                            Logger.error("error", throwable1);
                            return;
                        }
                        if (muted) {
                            sender.sendMessage(MM."<red>\{target} is already muted!");
                            return;
                        }
                        Cytosis.getDatabaseManager().getMysqlDatabase().getPlayerRank(uuid).whenComplete((playerRank, throwable2) -> {
                            if (throwable2 != null) {
                                sender.sendMessage(MM."<red>An error occured whilst finding \{target}'s rank!");
                                Logger.error("error", throwable2);
                                return;
                            }
                            OfflinePlayer op = new OfflinePlayer(target, uuid, playerRank);
                            if (op.hasPermission("cytosis.moderation.mute_immune")) {
                                sender.sendMessage(MM."<red>\{target} cannot be muted!");
                                return;
                            }

                            Cytosis.getDatabaseManager().getMysqlDatabase().mutePlayer(uuid, dur, new Entry(uuid, actor.getUuid(), Category.MUTE, "command")).whenComplete((_, throwable3) -> {
                                if (throwable3 != null) {
                                    actor.sendMessage(MM."<red>An error occured whilst muting \{target}!");
                                    return;
                                }
                                actor.sendMessage(MM."<GREEN><b>MUTED!</green> <gray>\{target} was successfully muted for \{DurationParser.unparseFull(dur)}.");
                            });
                        });
                    });
                });
            }
        }, playerArg, durationArg);
    }
}
