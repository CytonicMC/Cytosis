package net.cytonic.cytosis.events;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerChatEvent;
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
                Logger.info(STR."\{event.getPlayer().getUsername()} (\{event.getPlayer().getUuid()}) joined with the ip: \{player.getPlayerConnection().getServerAddress()}");
            else
                Logger.info(STR."\{event.getPlayer().getUsername()} (\{event.getPlayer().getUuid()}) joined.");
            player.sendMessage("Hello!");
        });
        Logger.info("Registering player chat event.");
        Cytosis.getEventHandler().registerGlobalEvent(PlayerChatEvent.class, event -> {
            final Player player = event.getPlayer();
            Cytosis.getDatabase().addChat(player.getUuid(),event.getMessage());
        });
    }
}