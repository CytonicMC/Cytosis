package net.cytonic.cytosis.npcs.dialogs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player finishes a dialog
 */
@AllArgsConstructor
@Getter
@ToString
public class DialogFinishEvent implements PlayerEvent {

    private final Dialog dialog;
    private final CytosisPlayer player;

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
