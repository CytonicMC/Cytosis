package net.cytonic.cytosis.menus;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a button in a GUI.
 *
 * @param id   The unique identifier of the button.
 * @param item The item displayed on the button.
 * @param task The action to be performed when the button is clicked.
 */
public record Button(@NotNull NamespaceID id,
                     @NotNull Function<CytosisPlayer, ItemStack> item,
                     @NotNull BiConsumer<CytosisPlayer, InventoryPreClickEvent> task
) implements ClickableItem {

    @Override
    public @NotNull BiConsumer<CytosisPlayer, InventoryPreClickEvent> onClick() {
        return task;
    }
}
