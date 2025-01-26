package net.cytonic.cytosis.menus;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents an item that can be clicked in an inventory GUI.
 */
public interface ClickableItem {

    /**
     * A pre-defined close button that can be used in GUIs.
     * When clicked, it closes the player's inventory.
     */
    Button CLOSE_BUTTON = new Button(
            new NamespaceID("cytosis", "close"),
            (unused) -> ItemStack.of(Material.BARRIER)
                    .withCustomName(Component.text("Close", NamedTextColor.RED)
                            .decoration(TextDecoration.ITALIC, false)),
            (player, event) -> {
                event.setCancelled(true);
                player.closeInventory();
            }
    );

    /**
     * Returns the unique identifier for this clickable item.
     *
     * @return The NamespacedId of this item.
     */
    @NotNull NamespaceID id();

    /**
     * Provides a function to generate the ItemStack for this clickable item.
     *
     * @return A function that takes a SkyblockPlayer and returns an ItemStack.
     */
    @NotNull Function<CytosisPlayer, ItemStack> item();

    /**
     * Defines the action to be performed when this item is clicked.
     *
     * @return A BiConsumer that takes a SkyblockPlayer and an InventoryPreClickEvent.
     */
    @NotNull BiConsumer<CytosisPlayer, InventoryPreClickEvent> onClick();
}
