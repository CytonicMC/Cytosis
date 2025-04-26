package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.util.CommandUtils;
import net.cytonic.cytosis.commands.util.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.SnoopUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.util.UUID;

public class WarnCommand extends CytosisCommand {

    public WarnCommand() {
        super("warn");
        setCondition(CommandUtils.IS_HELPER);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.mm("<RED>Usage: /warn <player> [reason]")));
        var reasonArg = ArgumentType.StringArray("reason");
        reasonArg.setDefaultValue(new String[]{""});
        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, ignored, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(Msg.mm("<green>Fetching players..."));
                Cytosis.getCytonicNetwork().getOnlinePlayers().forEach((ignored1, name) -> suggestion.addEntry(new SuggestionEntry(name)));
            }
        });

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                if (!actor.isHelper()) {
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


                    if (playerRank.isStaff()) {
                        sender.sendMessage(Msg.mm("<red>" + player + " cannot be warned!"));
                        return;
                    }
                    Component string = Component.empty();
                    if (!reason.isEmpty()) {
                        string = Msg.mm("\n<aqua>Reason: " + reason + "");
                    }
                    actor.sendMessage(Msg.mm("<green>Warned " + player + ".").append(string));

                    Component component = Msg.mm("<red>You have been warned.").append(string);
                    Component snoop = actor.formattedName().append(Msg.mm("<gray> warned ")).append(SnoopUtils.toTarget(uuid)).append(Msg.mm("<gray> for <yellow>" + reason + "</yellow>."));
                    Cytosis.getSnooperManager().sendSnoop(CytosisSnoops.PLAYER_WARN, SnoopUtils.toSnoop(snoop));
                });
            }
        }, playerArg, reasonArg);
    }
}
