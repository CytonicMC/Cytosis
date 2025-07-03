package net.cytonic.cytosis.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.event.trait.PlayerEvent;

@AllArgsConstructor
@Getter
public class VanishToggleEvent implements PlayerEvent {
    /**
     * The NEW vanished state.
     */
    private final boolean vanished;
    /**
     * The player who toggled their vanished status
     */
    private final CytosisPlayer player;
}
