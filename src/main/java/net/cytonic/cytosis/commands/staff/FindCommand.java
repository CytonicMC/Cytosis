package net.cytonic.cytosis.commands.staff;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.objects.PlayerServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.util.UUID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * Locates a player on the network
 */
public class FindCommand extends Command {

    /**
     * A command to find a player on the network
     */
    public FindCommand() {
        super("find");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.staff.find"));
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<RED>You must specify a player!"));
        var playerArgument = ArgumentType.Word("player");
        playerArgument.setSuggestionCallback((sender, _, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(MM."<green>Fetching players...");
                Cytosis.getCytonicNetwork().getNetworkPlayersOnServers().forEach((uuid, _) -> {
                    String name = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid);
                    suggestion.addEntry(new SuggestionEntry(name));
                });
            }
        });
        addSyntax((sender, context) -> {
            if (sender.hasPermission("cytosis.commands.find"))
                if (sender instanceof final CytosisPlayer player) {
                    String target = context.get(playerArgument);
                    if (!Cytosis.getCytonicNetwork().getLifetimeFlattened().containsValue(target.toLowerCase())) {
                        sender.sendMessage(MM."<red>The player \{target} doesn't exist!");
                        return;
                    }
                    UUID playerUUID = Cytosis.getCytonicNetwork().getLifetimeFlattened().getByValue(target.toLowerCase());
                    PlayerServer server = Cytosis.getCytonicNetwork().getNetworkPlayersOnServers().getByKey(playerUUID);
                    if (server == null) {
                        player.sendMessage(Component.text(STR."The player \{target} is not online!", NamedTextColor.RED));
                        return;
                    }
                    Component message = Component.text(STR."The player \{target} is online on server \{server.server().id()} ", NamedTextColor.YELLOW)
                            .append(Component.text("[GO THERE]", NamedTextColor.GREEN, TextDecoration.BOLD)
                                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(STR."Click to travel to server: '\{server.server().id()}'"))))
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, STR."/server \{server.server().id()}"));
                    player.sendMessage(message);
                }
        }, playerArgument);
    }
}
