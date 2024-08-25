package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.auditlog.Category;
import net.cytonic.cytosis.auditlog.Entry;
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
                player.sendActionBar(MM."<green>Fetching banned players...");
                Cytosis.getCytonicNetwork().getBannedPlayers().forEach((uuid, _) -> suggestion.addEntry(new SuggestionEntry(Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid))));
            }
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player actor)) {
                return;
            }
            if (!actor.hasPermission("cytosis.commands.moderation.unban")) {
                actor.sendMessage(MM."<red>You don't have permission to use this command!");
            }

            final String player = context.get(playerArg);
            if (!Cytosis.getCytonicNetwork().getLifetimePlayers().containsValue(player)) {
                sender.sendMessage(MM."<red>The player \{player} doesn't exist!");
                return;
            }
            UUID uuid = Cytosis.getCytonicNetwork().getLifetimePlayers().getByValue(player);
            if (!Cytosis.getCytonicNetwork().getBannedPlayers().containsKey(uuid)) {
                sender.sendMessage(MM."<red>\{player} is not banned!");
                return;
            }
            Cytosis.getDatabaseManager().getMysqlDatabase().unbanPlayer(uuid,new Entry(uuid, actor.getUuid(), Category.UNBAN, "command"));
            sender.sendMessage(MM."<GREEN><b>UNBANNED!</green><gray> \{player} was successfully unbanned!");
        }, playerArg);
    }
}
