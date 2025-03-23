package net.cytonic.cytosis.menus.utils;

import eu.koboo.minestom.invue.api.PlayerView;
import eu.koboo.minestom.invue.api.component.ViewComponent;
import eu.koboo.minestom.invue.api.interaction.Interactions;
import eu.koboo.minestom.invue.api.item.ViewItem;
import eu.koboo.minestom.invue.api.pagination.ViewPagination;
import eu.koboo.minestom.invue.api.slots.ViewPattern;
import net.cytonic.cytosis.menus.MenuUtils;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class PaginatedBorder extends ViewComponent {
    ViewPagination pagination;
    ViewPattern pattern;


    public PaginatedBorder(ViewPagination pagination, ViewPattern pattern) {
        this.pagination = pagination;
        this.pattern = pattern;
        addChild(pagination);
    }

    @Override
    public void onStateUpdate(@NotNull PlayerView view, @NotNull Player player) {
        if (!pagination.hasNextPage()) {
            ViewItem.bySlot(view, pattern.getSlot('>'))
                    .applyPrebuilt(MenuUtils.BORDER);
        } else {
            ViewItem.bySlot(view, pattern.getSlot('>'))
                    .material(Material.ARROW)
                    .name("<green>Next (" + pagination.getCurrentPage() + ")")
                    .interaction(Interactions.toNextPage(pagination));
        }

        if (!pagination.hasPreviousPage()) {
            ViewItem.bySlot(view, pattern.getSlot('<'))
                    .applyPrebuilt(MenuUtils.BORDER);
        } else {
            ViewItem.bySlot(view, pattern.getSlot('<'))
                    .material(Material.ARROW)
                    .name("<green>Previous (" + pagination.getCurrentPage() + ")")
                    .interaction(Interactions.toPreviousPage(pagination));
        }
    }

    @Override
    public void onOpen(@NotNull PlayerView view, @NotNull Player player) {
        try {
            ViewItem.bySlot(view, pattern.getSlot('X'))
                    .material(Material.BARRIER)
                    .name("Close")
                    .closeInventoryInteraction();
        } catch (IllegalArgumentException ignored) {

        }
        pattern.getSlots('#').forEach(slot -> ViewItem.bySlot(view, slot).applyPrebuilt(MenuUtils.BORDER));
    }


}
