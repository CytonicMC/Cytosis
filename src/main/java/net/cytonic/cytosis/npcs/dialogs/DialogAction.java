package net.cytonic.cytosis.npcs.dialogs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.data.enums.NpcInteractType;
import net.cytonic.cytosis.data.objects.Tuple;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.npcs.Npc;
import net.cytonic.cytosis.npcs.NpcAction;
import net.cytonic.cytosis.player.CytosisPlayer;

@SuppressWarnings("unused")
public class DialogAction implements NpcAction {

    private static final Tag<@NotNull Long> LAST_INTERACTED = Tag.Long("dialog-interact-cooldown")
        .defaultValue(System.currentTimeMillis());

    private final Dialog dialog;
    private final Map<UUID, Integer> playerIndices = new HashMap<>();
    private final Map<UUID, Task> playerTasks = new HashMap<>();

    public DialogAction(Dialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void execute(Npc npc, NpcInteractType type, CytosisPlayer player) {
        if (!dialog.isClickingAdvances() && playerTasks.containsKey(player.getUuid())) return;
        if (System.currentTimeMillis() - player.getTag(LAST_INTERACTED) < 500) return;
        player.setTag(LAST_INTERACTED, System.currentTimeMillis());
        if (playerTasks.containsKey(player.getUuid())) {
            playerTasks.get(player.getUuid()).cancel();
        }
        advanceDialog(player);
    }

    public void advanceDialog(CytosisPlayer player) {
        int index = playerIndices.getOrDefault(player.getUuid(), 0);
        if (index >= dialog.getLines(player).size()) {
            playerIndices.remove(player.getUuid());
            playerTasks.remove(player.getUuid()).cancel();
            EventDispatcher.call(new DialogFinishEvent(dialog, player));
            return;
        }

        Tuple<Component, Integer> entry = dialog.getLines(player).get(index);
        if (entry == null) {
            Logger.warn("Null entry in NPC dialog!");
            return;
        }

        player.sendMessage(entry.getFirst());
        Task t = player.scheduler().buildTask(() -> advanceDialog(player)).delay(TaskSchedule.tick(entry.getSecond()))
            .schedule();

        playerTasks.put(player.getUuid(), t);
        playerIndices.put(player.getUuid(), index + 1);
    }
}
