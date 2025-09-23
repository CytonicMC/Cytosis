package net.cytonic.cytosis.managers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.nicknames.NicknameManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.sideboard.DefaultCreator;
import net.cytonic.cytosis.sideboard.Sideboard;
import net.cytonic.cytosis.sideboard.SideboardCreator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A manager class for sideboards
 */
@NoArgsConstructor
@CytosisComponent(dependsOn = {NicknameManager.class})
public class SideboardManager implements Bootstrappable {
    private final Map<UUID, Sideboard> sideboards = new ConcurrentHashMap<>();
    @Getter
    @Nullable
    private Task task = null;
    @Getter
    @Setter
    private SideboardCreator sideboardCreator = new DefaultCreator();

    @Override
    public void init() {
        autoUpdateBoards(TaskSchedule.seconds(1));
    }

    @Override
    public void shutdown() {
        cancelUpdates();
    }

    /**
     * Adds a player to the sideboard manager
     *
     * @param player the player
     */
    public void addPlayer(CytosisPlayer player) {
        sideboards.put(player.getUuid(), sideboardCreator.sideboard(player));
    }

    /**
     * Removes a player from the sideboard manager
     *
     * @param player the player
     */
    public void removePlayer(Player player) {
        sideboards.remove(player.getUuid());
    }

    /**
     * Removes a player from the sideboard manager
     *
     * @param player the player
     */
    public void removePlayer(UUID player) {
        sideboards.remove(player);
    }

    public void updatePlayersNow() {
        sideboards.forEach((uuid, sideboard) -> {
            Optional<CytosisPlayer> player = Cytosis.getPlayer(uuid);
            if (player.isEmpty()) {
                sideboard.delete();
                removePlayer(uuid);
                return;
            }
            sideboard.updateLines(sideboardCreator.lines(player.get()));
            sideboard.updateTitle(sideboardCreator.title(player.get()));
        });
    }

    /**
     * schedule the sideboard updater.
     */
    public void autoUpdateBoards(TaskSchedule schedule) {
        task = MinecraftServer.getSchedulerManager()
                .buildTask(this::updatePlayersNow)
                .repeat(schedule)
                .schedule();
    }

    /**
     * Shuts down the repeating task. It can be reenabled with {@link SideboardManager#autoUpdateBoards(TaskSchedule)}
     */
    public void cancelUpdates() {
        if (task == null) return;
        task.cancel();
        task = null;
    }
}