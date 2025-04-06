package net.cytonic.cytosis.commands.util.nicknames;


import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.command.builder.Command;

public class NickCommand extends Command {
    public NickCommand() {
        super("nick");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Cytosis.getNicknameManager().nicknamePlayer(player.getUuid());
            player.sendMessage("Nickname has been saved.");
        });
    }
}
