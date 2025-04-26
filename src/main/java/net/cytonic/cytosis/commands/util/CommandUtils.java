package net.cytonic.cytosis.commands.util;

import lombok.experimental.UtilityClass;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@UtilityClass
public class CommandUtils {

    public static final CommandCondition IS_MODERATOR = (commandSender, s) -> {
        if (!(commandSender instanceof CytosisPlayer player)) return false;
        return player.isModerator();
    };

    public static final CommandCondition IS_HELPER = (commandSender, s) -> {
        if (!(commandSender instanceof CytosisPlayer player)) return false;
        return player.isHelper();
    };

    public static final CommandCondition IS_ADMIN = (commandSender, s) -> {
        if (!(commandSender instanceof CytosisPlayer player)) return false;
        return player.isAdmin();
    };

    public static final CommandCondition IS_STAFF = (commandSender, s) -> {
        if (!(commandSender instanceof CytosisPlayer player)) return false;
        return player.isStaff();
    };
    public static final Component COMMAND_DISABLED = Msg.redSplash("DISABLED!", "This command has been disabled.");
    public static final ArgumentWord LIFETIME_PLAYERS = ArgumentType.Word("lifetime_player");
    public static final ArgumentWord NETWORK_PLAYERS = ArgumentType.Word("network_player");
    public static final ArgumentPlayer ONLINE_PLAYERS = new ArgumentPlayer();

    static {
        LIFETIME_PLAYERS.setSuggestionCallback((sender, ignored, suggestion) -> {
            Cytosis.getCytonicNetwork().getLifetimePlayers().forEach((uuid, name) -> suggestion.addEntry(new SuggestionEntry(name)));
            Cytosis.getNicknameManager().getNetworkNicknames().forEach(s -> suggestion.addEntry(new SuggestionEntry(s)));
        });
    }

    static {
        NETWORK_PLAYERS.setSuggestionCallback((sender, ignored, suggestion) -> {
            Cytosis.getCytonicNetwork().getOnlinePlayers().forEach((uuid, name) -> suggestion.addEntry(new SuggestionEntry(name)));
            Cytosis.getNicknameManager().getNetworkNicknames().forEach(s -> suggestion.addEntry(new SuggestionEntry(s)));
        });
    }

    public static CommandCondition withRank(PlayerRank rank) {
        return (sender, commandString) -> {
            if (!(sender instanceof CytosisPlayer player)) return false;
            return player.getTrueRank() == rank;
        };
    }

    public static @Nullable UUID resolveUuid(String input) {
        UUID cached = Cytosis.getCytonicNetwork().getLifetimePlayers().getByValue(input);
        if (cached != null) {
            return cached;
        }
        return Cytosis.getNicknameManager().deanonymizePlayer(input);
    }

}
