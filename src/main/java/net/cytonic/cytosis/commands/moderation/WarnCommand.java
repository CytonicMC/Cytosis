package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.auditlog.Category;
import net.cytonic.cytosis.auditlog.Entry;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.util.EnumSet;
import java.util.UUID;

import static net.cytonic.enums.PlayerRank.*;
import static net.cytonic.utils.MiniMessageTemplate.MM;

public class WarnCommand extends Command {

    public WarnCommand() {
        super("warn");
        setCondition(CommandUtils.IS_HELPER);
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<RED>Usage: /warn <player> [reason]"));
        var reasonArg = ArgumentType.StringArray("reason");
        reasonArg.setDefaultValue(new String[]{""});
        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, _, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(MM."<green>Fetching players...");
                Cytosis.getCytonicNetwork().getOnlinePlayers().forEach((_, name) -> suggestion.addEntry(new SuggestionEntry(name)));
            }
        });

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                if (!actor.isHelper()) {
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


                    if (EnumSet.of(OWNER, ADMIN, MODERATOR, HELPER).contains(playerRank)) {
                        sender.sendMessage(MM."<red>\{player} cannot be warned!");
                        return;
                    }
                    Component string = Component.empty();
                    if (!reason.isEmpty()) {
                        string = MM."\n<aqua>Reason: \{reason}";
                    }
                    actor.sendMessage(MM."<green>Warned \{player}.".append(string));

                    Component component = MM."<red>You have been warned.".append(string);
                    Cytosis.getDatabaseManager().getRedisDatabase().warnPlayer(uuid, actor.getUuid(), component, reason, new Entry(uuid, actor.getUuid(), Category.WARN, "warn_command"));
                });
            }
        }, playerArg, reasonArg);
    }
}
