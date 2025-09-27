package net.cytonic.cytosis.commands.moderation;

import java.time.Instant;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.SnoopUtils;

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
            Cytosis.CONTEXT.getComponent(CytonicNetwork.class).getLifetimePlayers()
                .forEach((uuid, name) -> suggestion.addEntry(new SuggestionEntry(name)));
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
                    actor.sendMessage(Msg.red("You don't have permission to use this command!"));
                }

                final String target = context.get(playerArg);
                final String rawDur = context.get(durationArg);
                final Instant dur = DurationParser.parse(rawDur);

                mutePlayer(actor, target, dur);
            }
        }, playerArg, durationArg);
    }

    private void mutePlayer(CytosisPlayer actor, String target, Instant duration) {
        CytonicNetwork network = Cytosis.CONTEXT.getComponent(CytonicNetwork.class);
        if (!network.getLifetimePlayers().containsValue(target)) {
            actor.sendMessage(Msg.red("The player " + target + " doesn't exist!"));
            return;
        }
        MysqlDatabase db = Cytosis.CONTEXT.getComponent(MysqlDatabase.class);
        UUID uuid = network.getLifetimeFlattened().getByValue(target.toLowerCase());
        db.isMuted(uuid).whenComplete((muted, throwable1) -> {
            if (throwable1 != null) {
                actor.sendMessage(Msg.serverError("An error occurred whilst finding if %s is muted!", target));
                Logger.error("error checking mute status", throwable1);
                return;
            }
            if (muted) {
                actor.sendMessage(Msg.whoops("%s is already muted!", target));
                return;
            }
            db.getPlayerRank(uuid).whenComplete((playerRank, throwable2) -> {
                if (throwable2 != null) {
                    actor.sendMessage(Msg.serverError("An error occurred whilst finding %s's rank!", target));
                    Logger.error("error", throwable2);
                    return;
                }
                if (playerRank.isStaff()) {
                    actor.sendMessage(Msg.whoops("%s cannot be muted!", target));
                    return;
                }
                Component snoop = actor.formattedName().append(Msg.grey(" muted "))
                    .append(SnoopUtils.toTarget(uuid))
                    .append(Msg.grey(" for " + DurationParser.unparseFull(duration) + "."));

                Cytosis.CONTEXT.getComponent(SnooperManager.class)
                    .sendSnoop(CytosisSnoops.PLAYER_MUTE, Msg.snoop(snoop));
                db.mutePlayer(uuid, duration).whenComplete((ignored, throwable3) -> {
                    if (throwable3 != null) {
                        actor.sendMessage(Msg.serverError("An error occurred whilst muting %s!", target));
                        return;
                    }
                    actor.sendMessage(Msg.greenSplash("MUTED!", "%s was successfully muted for %s.", target,
                        DurationParser.unparseFull(duration)));
                });
            });
        });
    }
}