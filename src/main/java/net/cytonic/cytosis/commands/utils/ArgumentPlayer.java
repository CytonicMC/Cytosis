package net.cytonic.cytosis.commands.utils;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.nicknames.NicknameManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class ArgumentPlayer extends Argument<CytosisPlayer> {

    public ArgumentPlayer() {
        super("player");
        setSuggestionCallback((sender, context, suggestion) -> {
            for (CytosisPlayer player : Cytosis.getOnlinePlayers()) {
                suggestion.addEntry(new SuggestionEntry(player.getUsername()));
            }
        });
        setCallback((sender, exception) -> sender.sendMessage(Msg.whoops(exception.getMessage())));
    }

    @Override
    public @NotNull CytosisPlayer parse(@NotNull CommandSender sender, @NotNull String input)
        throws ArgumentSyntaxException {
        CytosisPlayer player = Cytosis.getPlayer(input).orElse(null);
        if (player == null) {
            // not known by this name
            player = Cytosis.CONTEXT.getComponent(NicknameManager.class).getPlayerByNickname(input);
        }
        return player;
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.STRING;
    }

    @Override
    public byte[] nodeProperties() {
        return NetworkBuffer.makeArray(NetworkBuffer.VAR_INT, 0); // Single word
    }
}
