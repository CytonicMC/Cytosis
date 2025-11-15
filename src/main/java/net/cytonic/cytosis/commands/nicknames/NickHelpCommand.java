package net.cytonic.cytosis.commands.nicknames;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

public class NickHelpCommand extends CytosisCommand {

    public NickHelpCommand() {
        super("help");
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(
            Msg.splash("NICKNAME HELP!", "BE9025", "Available nickname-related commands:").appendNewline().append(
                    Msg.splash("/NICK HELP", "F5C526", "Displays this help menu to describe the available commands."))
                .appendNewline().append(Msg.splash("/NICK RESET", "F5C526",
                    "Resets your nickname, if you have one. It reverts your apparent rank, skin,"
                        + " and name to their original values."))
                .appendNewline().append(Msg.splash("/NICK RANDOM", "F5C526",
                    "Randomizes your apparent identity. It changes the way other players see your"
                        + " rank, skin, and player name."))
                .appendNewline().append(Msg.splash("/NICK SETUP", "F5C526",
                    "Opens a dialog to customize your apparent identity to other players.")).appendNewline()
            // .append(Msg.splash("/NICK ","F5C526", "")).appendNewline() // use to add more elements later ;)
        ));
    }
}
