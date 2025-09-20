package net.cytonic.cytosis.menus;

import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.TooltipDisplay;

public interface MenuUtils {
    ItemStack BORDER = ItemStack.builder(Material.BLACK_STAINED_GLASS_PANE)
            .set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.EMPTY).build();
}
