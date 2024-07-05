package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.objects.PlayerServer;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.ranks.PlayerRank;
import net.cytonic.cytosis.utils.MiniMessageTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.StringJoiner;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class MsgCommand extends Command {

    public MsgCommand() {
        super("msg");
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<RED>You must specify a player!"));
        var playerArgument = ArgumentType.Word("player");
        playerArgument.setSuggestionCallback((_, _, suggestion) -> Cytosis.getCytonicNetwork().getNetoworkPlayersOnServers().values().forEach(v -> suggestion.addEntry(new SuggestionEntry(v.playerName()))));
        playerArgument.setCallback((sender, exception) -> sender.sendMessage(Component.text(STR."The player \{exception.getInput()} is invalid!", NamedTextColor.RED)));
        var messageArgument = ArgumentType.StringArray("message");

        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                String playerName = context.get(playerArgument);
                StringJoiner sj = new StringJoiner(" ");
                for (String s : context.get(messageArgument)) sj.add(s);
                String msg = sj.toString();
                Logger.debug(STR."message = \{msg}");
                PlayerRank rank = Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElseThrow();
                Component message = MM."<dark_aqua>From <reset>\{rank.getPrefix()} \{player.getUsername()}<reset> <dark_gray>»</dark_gray> <reset><white>\{msg}";
                Cytosis.getDatabaseManager().getMysqlDatabase().findUUIDByName(playerName).whenComplete((uuid, throwable) -> {
                    if (throwable != null) {
                        Logger.error("An error occurred whilst finding a player!", throwable);
                    } else {
                        Cytosis.getDatabaseManager().getRedisDatabase().sendMessage(message, uuid);
                    }
                });
            }
        }, playerArgument, messageArgument);
    }
}
