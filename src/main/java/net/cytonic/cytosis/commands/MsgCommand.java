package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.ranks.PlayerRank;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class MsgCommand extends Command {

    public MsgCommand() {
        super("msg");
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<RED>You must specify a player!"));
        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, _, suggestion) -> {
            if (sender instanceof Player player) {
                player.sendActionBar(MM."<green>Fetching online players...");
            }
            Cytosis.getCytonicNetwork().getNetworkPlayers().forEach(player ->
                    suggestion.addEntry(new SuggestionEntry(player)));
        });
        var msgArgument = ArgumentType.StringArray("broadcastArgument");

        addSyntax((sender, context) -> {
        }, playerArg, msgArgument);
    }
}
