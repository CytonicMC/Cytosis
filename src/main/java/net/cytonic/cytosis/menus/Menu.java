package net.cytonic.cytosis.menus;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Menu extends Inventory {
    public static final Tag<String> BUTTON_TAG = Tag.String("button");
    Map<Integer, ClickableItem> clickables = new HashMap<>();

    /**
     * Constructs a new Inventory.
     *
     * @param inventoryType the type of inventory to create
     * @param title         the title of the inventory
     */
    public Menu(@NotNull InventoryType inventoryType, @NotNull Component title) {
        super(inventoryType, title);
    }

    /**
     * Creates an item with hidden tooltips and an empty name.
     *
     * @param material the Material to use for the filler item
     * @return an ItemStack configured as a filler item
     */
    public static ClickableItem createFillerItem(Material material) {
        ItemStack.Builder builder = ItemStack.of(material).builder();
        builder.set(ItemComponent.HIDE_TOOLTIP, Unit.INSTANCE);
        builder.set(ItemComponent.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        builder.set(ItemComponent.CUSTOM_NAME, Component.empty());
        ItemStack item = builder.build();
        return new DummyItem(NamespaceID.from("cytosis_generated:dummy_item_" + material.id()), (ignored) -> item);
    }

    /**
     * Fills the entire inventory with the specified item.
     *
     * @param item the ItemStack to fill the inventory with
     */
    public void fill(@NotNull ItemStack item) {
        for (int i = 0; i < getInventoryType().getSize(); i++) {
            setItemStack(i, item);
        }
    }

    /**
     * Adds a clickable item to the inventory at the specified slot.
     *
     * @param clickable the ClickableItem to add
     * @param player    the CytosisPlayer for whom the item is being added
     * @param slot      the inventory slot to place the item in
     */
    public void setClickableItem(@NotNull ClickableItem clickable, @NotNull CytosisPlayer player, int slot) {
        clickables.put(slot, clickable);
        ItemStack itemStack = clickable.item().apply(player);
        itemStack = itemStack.withTag(BUTTON_TAG, clickable.id().toString());
        setItemStack(slot, itemStack);
    }

    /**
     * Clears the entire inventory, setting all slots to air.
     */
    public void clear() {
        for (int i = 0; i < getInventoryType().getSize(); i++) {
            setItemStack(i, ItemStack.AIR);
        }
    }

    public void applyItem(ClickableItem itemStack, CytosisPlayer renderer, int... slots) {
        for (int slot : slots) {
            setClickableItem(itemStack, renderer, slot);
        }
    }

    public void addItems(CytosisPlayer renderer, ClickableItem... items) {

        ArrayDeque<ClickableItem> itemStacks = new ArrayDeque<>(Arrays.asList(items));
        for (int i = 0; i < getSize(); i++) {
            if (getItemStack(i) == ItemStack.AIR) {
                ClickableItem clickable = itemStacks.poll();
                if (clickable == null) break;
                clickables.put(i, clickable);
                ItemStack item = clickable.item().apply(renderer);
                setItemStack(i, item.withTag(BUTTON_TAG, clickable.id().toString()));
            }
        }

    }

    public void fillEmpty(Material material) {
        ItemStack filler = createFillerItem(material).item().apply(null);
        for (int i = 0; i < getInventoryType().getSize(); i++) {
            if (getItemStack(i) == null || getItemStack(i) == ItemStack.AIR) {
                clickables.remove(i);
                setItemStack(i, filler);
            }
        }
    }

    public void clearSlots(int... slots) {
        for (int slot : slots) {
            setItemStack(slot, ItemStack.AIR);
        }
    }

    public void rerender(CytosisPlayer renderer) {
        List<Integer> toRemove = new ArrayList<>();
        clickables.forEach((index, clickable) -> {
            if (getItemStack(index) == ItemStack.AIR) {
                toRemove.add(index);
                return;
            }
            if (!getItemStack(index).getTag(BUTTON_TAG).equals(clickable.id().toString())) {
                toRemove.add(index);
                return;
            }

            setItemStack(index, clickable.item().apply(renderer).withTag(BUTTON_TAG, clickable.id().toString()));
        });
        toRemove.forEach(clickables::remove);
    }
}
