package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import static net.cytonic.utils.MiniMessageTemplate.MM;

public class MsgCommand extends Command {

    public MsgCommand() {
        super("msg", "message", "whisper");

        var msgArgument = ArgumentType.StringArray("msg");
        var playerArg = ArgumentType.Word("player");
        playerArg.setSuggestionCallback((_, _, suggestion) -> {
            Cytosis.getCytonicNetwork().getOnlinePlayers().getValues().forEach(player -> {
                Logger.debug(STR."Hey I'm adding a suggestion!! (\{player})");
                suggestion.addEntry(new SuggestionEntry(player));
            });
                });
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<RED>Usage: /msg (player) (message)"));
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                return;
            }
            String message = String.join(" ", context.get(msgArgument));
            Cytosis.getDatabaseManager().getMysqlDatabase().findUUIDByName(context.get(playerArg)).whenComplete((uuid, throwable) -> {
                if (throwable != null) {
                    Logger.error("error", throwable);
                    return;
                }
                Cytosis.getDatabaseManager().getRedisDatabase().sendPlayerMessage(Component.text(STR."hi this message is (\{message})"), uuid);
                player.sendMessage(MM."<green>Message sent to \{context.get(playerArg)}!");
                player.sendMessage(STR."the message is \{message}");
            });
        }, playerArg, msgArgument);
    }
}
