package net.cytonic.cytosis.commands.nicknames;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

@SubCommand(NickCommand.class)
class HelpCommand extends CytosisCommand {

    private static final Component STAFF_HELP = Msg.mm("""
        <#be9025>NICKNAME HELP!</#be9025> <gray>Available nickname-related commands:</gray>
          <#f5c526>/NICK HELP</#f5c526> <gray>Displays this help menu to describe the available commands.</gray>
          <#f5c526>/NICK RANDOM</#f5c526> <gray>Randomizes your apparent identity. It changes the way other players see your rank, skin, and player name.</gray>
          <#f5c526>/NICK RESET</#f5c526> <gray>Resets your nickname, if you have one. It reverts your apparent rank, skin, and name to their original values.</gray>
          <#f5c526>/NICK SETUP</#f5c526> <gray>Opens a dialog to customize your apparent identity to other players.</gray>
          <#f5c526>/NICK REVEAL <PLAYER></#f5c526> <gray>Reveal the true identity of a player.</gray>
        """);

    private static final Component PUBLIC_HELP = Msg.mm("""
        <#be9025>NICKNAME HELP!</#be9025> <gray>Available nickname-related commands:</gray>
          <#f5c526>/NICK HELP</#f5c526> <gray>Displays this help menu to describe the available commands.</gray>
          <#f5c526>/NICK RANDOM</#f5c526> <gray>Randomizes your apparent identity. It changes the way other players see your rank, skin, and player name.</gray>
          <#f5c526>/NICK RESET</#f5c526> <gray>Resets your nickname, if you have one. It reverts your apparent rank, skin, and name to their original values.</gray>
        """);

    HelpCommand() {
        super("help");
        setDefaultExecutor((sender, ignored) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            if (player.isStaff()) {
                player.sendMessage(STAFF_HELP);
                return;
            }
            player.sendMessage(PUBLIC_HELP);
        });
    }
}
