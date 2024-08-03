package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * The class representing the clearchat command
 */
public class ClearchatCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public ClearchatCommand() {
        super("clearchat", "cc");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.clearchat"));
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof Player player) {
                if (sender.hasPermission("cytosis.commands.clearchat")) {
                    for (Player online : Cytosis.getOnlinePlayers()) {
                        if (online.hasPermission("cytosis.commands.clearchat")) {
                            online.sendMessage(MM."<AQUA>[STAFF] <GREEN>Chat has been cleared by ".append(Cytosis.getRankManager().getPlayerRank(online.getUuid()).orElseThrow().getPrefix().append(Component.text(player.getUsername()))).append(MM."<GREEN>!"));
                        }
                        else {
                            for (int i = 0; i < 150; i++)
                                online.sendMessage(" ");
                            online.sendMessage(MM."<GREEN>Chat has been cleared!");
                        }
                    }
                }
            }
            else {
                sender.sendMessage(MM."<RED>Only players may execute this command!");
            }
        });
    }
}