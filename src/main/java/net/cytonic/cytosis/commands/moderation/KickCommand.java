package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.auditlog.Category;
import net.cytonic.cytosis.auditlog.Entry;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.data.enums.KickReason;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.util.UUID;


public class KickCommand extends Command {

    public KickCommand() {
        super("kick");
        setCondition(CommandUtils.IS_MODERATOR);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.mm("<RED>Usage: /kick <player> [reason]")));
        var reasonArg = ArgumentType.StringArray("reason");
        reasonArg.setDefaultValue(new String[]{"No", "reason", "specified."});
        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, ignored, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(Msg.mm("<green>Fetching players..."));
                Cytosis.getCytonicNetwork().getOnlinePlayers().forEach((ignored1, name) -> suggestion.addEntry(new SuggestionEntry(name)));
            }
        });

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                if (!actor.isModerator()) {
                    actor.sendMessage(Msg.mm("<red>You don't have permission to use this command!"));
                    return;
                }
                final String player = context.get(playerArg);
                final String reason = String.join(" ", context.get(reasonArg));
                if (!Cytosis.getCytonicNetwork().getOnlineFlattened().containsValue(player.toLowerCase())) {
                    sender.sendMessage(Msg.mm("<red>The player " + context.get(playerArg) + " doesn't exist or is not online!"));
                    return;
                }

                UUID uuid = Cytosis.getCytonicNetwork().getOnlineFlattened().getByValue(player.toLowerCase());

                Cytosis.getDatabaseManager().getMysqlDatabase().getPlayerRank(uuid).whenComplete((playerRank, throwable2) -> {
                    if (throwable2 != null) {
                        sender.sendMessage(Msg.mm("<red>An error occured whilst finding " + player + "'s rank!"));
                        Logger.error("error", throwable2);
                        return;
                    }
                    if (playerRank.isStaffNotHelper()) {
                        sender.sendMessage(Msg.mm("<red>" + player + " cannot be kicked!"));
                        return;
                    }
                    Cytosis.getNatsManager().kickPlayer(uuid, KickReason.COMMAND, Msg.mm("\n<red>You have been kicked. \n<aqua>Reason: " + reason), new Entry(uuid, actor.getUuid(), Category.KICK, "kick_command"));
                });
            }
        }, playerArg, reasonArg);
    }
}
