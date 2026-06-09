package net.cytonic.cytosis.commands.movement;

import net.kyori.adventure.key.Key;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.protocol.publishers.SendPlayerToServerPacketPublisher;

public class LobbyCommand extends CytosisCommand {

    public LobbyCommand() {
        super("lobby", "l");
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Cytosis.get(SendPlayerToServerPacketPublisher.class)
                .sendPlayerToGenericServer(player.getUuid(), Key.key("lobby", "lobby"), "a Lobby");
        });
    }
}