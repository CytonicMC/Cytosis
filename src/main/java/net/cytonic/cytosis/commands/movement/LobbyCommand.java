package net.cytonic.cytosis.commands.movement;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.cytosis.player.CytosisPlayer;

public class LobbyCommand extends CytosisCommand {

    public LobbyCommand() {
        super("lobby", "l");
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Cytosis.CONTEXT.getComponent(NatsManager.class)
                .sendPlayerToGenericServer(player.getUuid(), "cytonic", "lobby", "a Lobby");
        });
    }
}