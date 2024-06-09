package net.cytonic.cytosis.managers;

import lombok.Getter;
import lombok.Setter;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.sideboard.DefaultCreator;
import net.cytonic.cytosis.sideboard.Sideboard;
import net.cytonic.cytosis.sideboard.SideboardCreator;
import net.minestom.server.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SideboardManager {
    private final Map<UUID, Sideboard> sideboards = new ConcurrentHashMap<>();
    private final ScheduledExecutorService updateExecutor = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("Cytosis-Sideboard-Updater").factory());
    @Getter
    @Setter
    private SideboardCreator sideboardCreator = new DefaultCreator();

    public void addPlayer(Player player) {
        sideboards.put(player.getUuid(), sideboardCreator.sideboard(player));
    }

    public void removePlayer(Player player) {
        sideboards.remove(player.getUuid());
    }

    public void removePlayer(UUID player) {
        sideboards.remove(player);
    }

    private void updatePlayer() {
        sideboards.forEach((uuid, sideboard) -> {
            Optional<Player> player = Cytosis.getPlayer(uuid);
            if (player.isEmpty()) {
                sideboard.delete();
                removePlayer(uuid);
                return;
            }
//            player.get().sendMessage("Hi updating ur scoreboard");
            sideboard.updateLines(sideboardCreator.lines(player.get()));
            sideboard.updateTitle(sideboardCreator.title(player.get()));
        });
    }

    public void updateBoards() {
        updateExecutor.scheduleAtFixedRate(this::updatePlayer, 1, 1, TimeUnit.SECONDS);
    }

    public void shutdown() {
        updateExecutor.shutdown();
    }
}
