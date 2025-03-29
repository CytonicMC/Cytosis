package net.cytonic.cytosis.commands;

import lombok.experimental.UtilityClass;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.condition.CommandCondition;

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

    public static CommandCondition withRank(PlayerRank rank) {
        return (sender, commandString) -> {
            if (!(sender instanceof CytosisPlayer player)) return false;
            return player.getRank() == rank;
        };
    }

    public static final Component COMMAND_DISABLED = Msg.mm("<red><b>DISABLED!</b></red><gray> This command has been disabled.</gray>");

}
