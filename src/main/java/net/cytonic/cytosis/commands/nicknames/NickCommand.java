package net.cytonic.cytosis.commands.nicknames;

import net.minestom.server.command.builder.Command;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.player.CytosisPlayer;

public class NickCommand extends Command {

    public NickCommand() {
        super("nick", "nickname");
        setCondition(CommandUtils.IS_STAFF);

        addSubcommand(new NickRevealCommand());
        addSubcommand(new NickRandomCommand());
        addSubcommand(new NickResetCommand());
        addSubcommand(new NickHelpCommand());
        addSubcommand(new NickSetupCommand());

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            player.dispatchCommand("nick help");
        });
    }
}
