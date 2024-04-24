package dev.foxikle.cytosis.events;

import dev.foxikle.cytosis.Cytosis;
import dev.foxikle.cytosis.config.CytosisSettings;
import dev.foxikle.cytosis.logging.Logger;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;

public class ServerEventListeners {

    public static void initServerEvents() {

        Logger.info("Registering player configuration event.");
        Cytosis.getEventHandler().registerGlobalEvent(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(Cytosis.getDefaultInstance());
            player.setRespawnPoint(new Pos(0, 45, 0));
        });

        Logger.info("Registering player spawn event.");
        Cytosis.getEventHandler().registerGlobalEvent(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();
            if (CytosisSettings.LOG_PLAYER_IPS)
                Logger.info(event.getPlayer().getUsername() + " (" + event.getPlayer().getUuid() + ") joined with the ip: " + player.getPlayerConnection().getServerAddress());
            else
                Logger.info(event.getPlayer().getUsername() + " (" + event.getPlayer().getUuid() + ") joined.");
            player.sendMessage("Hello!");
        });
    }
}
