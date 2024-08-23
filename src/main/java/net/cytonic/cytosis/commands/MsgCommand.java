package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.enums.PlayerRank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.UUID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

public class MsgCommand extends Command {

    public MsgCommand() {
        super("msg", "message", "whisper");

        var msgArgument = ArgumentType.StringArray("msg");
        msgArgument.setDefaultValue(new String[]{""});
        var playerArg = ArgumentType.Word("player");
        playerArg.setSuggestionCallback((_, _, suggestion) -> Cytosis.getCytonicNetwork().getOnlinePlayers().getValues().forEach(player -> suggestion.addEntry(new SuggestionEntry(player))));
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<RED>Usage: /msg <player> <message>"));
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                return;
            }
            String message = String.join(" ", context.get(msgArgument));
            PlayerRank rank = Cytosis.getCytonicNetwork().getPlayerRanks().get(player.getUuid());
            String targetName = context.get(playerArg);
            UUID targetUUID = Cytosis.getCytonicNetwork().getLifetimeFlattened().getByValue(targetName.toLowerCase());
            PlayerRank targetRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(targetUUID);

            Cytosis.getDatabaseManager().getRedisDatabase().sendPlayerMessage(MM."<dark_aqua>To <reset>".append(rank.getPrefix().append(MM."\{player.getUsername()}")).append(MM."<dark_aqua> » ").append(Component.text(message, NamedTextColor.WHITE)), targetUUID);
            player.sendMessage(MM."<dark_aqua>To <reset>".append(targetRank.getPrefix().append(MM."\{targetName}")).append(MM."<dark_aqua> » ").append(Component.text(message, NamedTextColor.WHITE)));
        }, playerArg, msgArgument);
    }
}
