package net.cytonic.cytosis.commands.nicknames;


import net.cytonic.cytosis.commands.util.CommandUtils;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.command.builder.Command;

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
