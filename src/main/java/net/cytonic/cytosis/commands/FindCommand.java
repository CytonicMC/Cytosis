package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.objects.PlayerServer;
import net.cytonic.cytosis.utils.MiniMessageTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.*;
import net.kyori.adventure.text.format.*;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

public class FindCommand extends Command {

    public FindCommand() {
        super("find");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.find"));
        setDefaultExecutor((sender, _) -> sender.sendMessage(MiniMessageTemplate.MM."<RED>You must specify a player!"));
        var playerArgument = ArgumentType.Word("player");
        playerArgument.setSuggestionCallback((_, _, suggestion) -> Cytosis.getCytonicNetwork().getNetoworkPlayersOnServers().values().forEach(v -> suggestion.addEntry(new SuggestionEntry(v.playerName()))));
        playerArgument.setCallback((sender, exception) -> sender.sendMessage(Component.text(STR."The player \{exception.getInput()} is invalid!", NamedTextColor.RED)));
        addSyntax((sender, context) -> {
            if (sender.hasPermission("cytosis.commands.find")) if (sender instanceof final Player player) {
                String playerName = context.get(playerArgument);
                for (PlayerServer server : Cytosis.getCytonicNetwork().getNetoworkPlayersOnServers().values()) {
                    if (server.playerName().equalsIgnoreCase(playerName)) {
                        Component message = Component.text(STR."The player \{playerName} is online on server \{server.server().id()} ",NamedTextColor.YELLOW)
                        .append(Component.text("[GO THERE]", NamedTextColor.GREEN, TextDecoration.BOLD)
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(STR."Click to travel to server: '\{server.server().id()}'"))))
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, STR."/server \{server.server().id()}"));
                        player.sendMessage(message);
                    }
                }
            }
        }, playerArgument);
    }
}
