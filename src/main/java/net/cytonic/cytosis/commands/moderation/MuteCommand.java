package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.commands.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.SnoopUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.time.Instant;


public class MuteCommand extends CytosisCommand {

    public MuteCommand() {
        super("mute");
        setCondition(CommandUtils.IS_MODERATOR);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.mm("<RED>Usage: /mute (player) (duration)")));
        ArgumentWord playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, ignored, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(Msg.mm("<green>Fetching players..."));
            }
            Cytosis.getCytonicNetwork().getLifetimePlayers().forEach((uuid, name) ->
                    suggestion.addEntry(new SuggestionEntry(name)));
        });
        ArgumentWord durationArg = ArgumentType.Word("duration");
        durationArg.setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("1h"));
            suggestion.addEntry(new SuggestionEntry("12h"));
            suggestion.addEntry(new SuggestionEntry("1d"));
            suggestion.addEntry(new SuggestionEntry("7d"));
            suggestion.addEntry(new SuggestionEntry("30d"));
        });

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                if (!actor.isModerator()) {
                    actor.sendMessage(Msg.mm("<red>You don't have permission to use this command!"));
                }

                final String target = context.get(playerArg);
                final String rawDur = context.get(durationArg);
                final Instant dur = DurationParser.parse(rawDur);

                if (!Cytosis.getCytonicNetwork().getLifetimePlayers().containsValue(target)) {
                    sender.sendMessage(Msg.mm("<red>The player " + target + " doesn't exist!"));
                    return;
                }
                Cytosis.getDatabaseManager().getMysqlDatabase().findUUIDByName(target).whenComplete((uuid, throwable) -> {
                    if (throwable != null) {
                        sender.sendMessage(Msg.serverError("An error occured whilst finding %s!", target));
                        Logger.error("error", throwable);
                        return;
                    }
                    Cytosis.getDatabaseManager().getMysqlDatabase().isMuted(uuid).whenComplete((muted, throwable1) -> {
                        if (throwable1 != null) {
                            sender.sendMessage(Msg.serverError("<red>An error occured whilst finding if %s is muted!", target));
                            Logger.error("error", throwable1);
                            return;
                        }
                        if (muted) {
                            sender.sendMessage(Msg.mm("%s is already muted!", target));
                            return;
                        }
                        Cytosis.getDatabaseManager().getMysqlDatabase().getPlayerRank(uuid).whenComplete((playerRank, throwable2) -> {
                            if (throwable2 != null) {
                                sender.sendMessage(Msg.serverError("An error occured whilst finding %s's rank!", target));
                                Logger.error("error", throwable2);
                                return;
                            }
                            if (playerRank.isStaff()) {
                                sender.sendMessage(Msg.whoops("%s cannot be muted!", target));
                                return;
                            }
                            Component snoop = actor.formattedName().append(Msg.mm("<gray> muted ")).append(SnoopUtils.toTarget(uuid))
                                    .append(Msg.mm("<gray> for " + DurationParser.unparseFull(dur) + "."));

                            Cytosis.getSnooperManager().sendSnoop(CytosisSnoops.PLAYER_MUTE, SnoopUtils.toSnoop(snoop));
                            Cytosis.getDatabaseManager().getMysqlDatabase().mutePlayer(uuid, dur).whenComplete((ignored, throwable3) -> {
                                if (throwable3 != null) {
                                    actor.sendMessage(Msg.serverError("An error occured whilst muting %s!", target));
                                    return;
                                }
                                actor.sendMessage(Msg.greenSplash("MUTED!", "%s was successfully muted for %s.", target, DurationParser.unparseFull(dur)));
                            });
                        });
                    });
                });
            }
        }, playerArg, durationArg);
    }
}
