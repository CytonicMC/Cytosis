package net.cytonic.cytosis.commands.movement;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.command.builder.Command;

public class LobbyCommand extends Command {

    public LobbyCommand() {
        super("lobby", "l");
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Cytosis.getNatsManager().sendPlayerToServer(player.getUuid(), "cytonic", "lobby", "a Lobby");
        });
    }
}
