package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.MiniMessageTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

public class ServerCommand extends Command {

    public ServerCommand() {
        super("cytosis:server");
        try {
            setCondition((sender, _) -> sender.hasPermission("cytosis.commands.server"));
            setDefaultExecutor((sender, context) -> sender.sendMessage(MiniMessageTemplate.MM."<RED>You must specify a server!"));
            var serverArgument = ArgumentType.Word("server");
            serverArgument.setCallback((sender, exception) -> sender.sendMessage(Component.text(STR."The server \{exception.getInput()} is invalid!", NamedTextColor.RED)));
            serverArgument.setSuggestionCallback((_, _, suggestion) -> {
                Logger.debug("this has been called");
                for (CytonicServer server : Cytosis.getCytonicNetwork().getServers()) {
                    suggestion.addEntry(new SuggestionEntry(server.id()));
                    Logger.debug(STR."command server id = \{server.id()}");
                }
            });
            addSyntax(((sender, context) -> {
                if (sender instanceof Player player)
                    if (player.hasPermission("cytosis.commands.server")) {
                        if (context.get(serverArgument).isEmpty()) {
                            StringBuilder builder = new StringBuilder();
                            Cytosis.getCytonicNetwork().getServers().forEach(server -> builder.append(STR."\{server.id()} "));
                            player.sendMessage(Component.text(builder.toString()));
                            return;
                        }
                        for (CytonicServer server : Cytosis.getCytonicNetwork().getServers()) {
                            if (server.id().equals(context.get(serverArgument))) {
                                player.sendMessage(Component.text(STR."Connecting to \{server.id()}", NamedTextColor.GREEN));
                                Cytosis.getDatabaseManager().getRedisDatabase().sendPlayerToServer(player, server);
                            }
                        }
                    }
            }), serverArgument);
        } catch (Exception e) {
            Logger.error("error", e);
        }
    }

}
