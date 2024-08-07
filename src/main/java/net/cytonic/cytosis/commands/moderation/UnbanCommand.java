package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.UUID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

public class UnbanCommand extends Command {

    public UnbanCommand() {
        super("unban");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.moderation.unban"));
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<RED>Usage: /unban (player)"));
        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, _, suggestion) -> {
            if (sender instanceof Player player) {
                player.sendActionBar(MM."<green>Fetching players...");
            }
            Cytosis.getCytonicNetwork().getLifetimePlayers().forEach((_, name) ->
                    suggestion.addEntry(new SuggestionEntry(name)));
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player actor)) {
                return;
            }
            if (!actor.hasPermission("cytosis.commands.moderation.ban")) {
                actor.sendMessage(MM."<red>You don't have permission to use this command!");
            }

            final String player = context.get(playerArg);

            if (!Cytosis.getCytonicNetwork().getLifetimePlayers().containsValue(player)) {
                sender.sendMessage(MM."<red>The player \{context.get(playerArg)} doesn't exist!");
                return;
            }
            UUID uuid = Cytosis.getCytonicNetwork().getLifetimePlayers().getByValue(player);
            Cytosis.getDatabaseManager().getMysqlDatabase().isBanned(uuid).whenComplete((banned, throwable1) -> {
                if (throwable1 != null) {
                    sender.sendMessage(MM."<red>An error occured whilst finding if \{player} is banned!");
                    Logger.error("error; ", throwable1);
                    return;
                }
                if (!banned.isBanned()) {
                    sender.sendMessage(MM."<red>\{player} is not banned!");
                    return;
                }
                Cytosis.getDatabaseManager().getMysqlDatabase().unbanPlayer(uuid);
                sender.sendMessage(MM."<green>\{player} was successfully unbanned!");
            });
        }, playerArg);
    }
}
