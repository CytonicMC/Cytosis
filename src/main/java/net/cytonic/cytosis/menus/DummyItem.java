package net.cytonic.cytosis.menus;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record DummyItem(NamespaceID id, Function<CytosisPlayer, ItemStack> item) implements ClickableItem {


    /**
     * Cancels the event
     *
     * @return A BiConsumer that cancels the event
     */
    @Override
    public @NotNull BiConsumer<CytosisPlayer, InventoryPreClickEvent> onClick() {
        return (ignored, inventory) -> inventory.setCancelled(true);
    }
}
