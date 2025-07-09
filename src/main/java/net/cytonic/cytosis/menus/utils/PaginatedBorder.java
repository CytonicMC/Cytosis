package net.cytonic.cytosis.menus.utils;


import eu.koboo.minestom.stomui.api.PlayerView;
import eu.koboo.minestom.stomui.api.component.ViewComponent;
import eu.koboo.minestom.stomui.api.interaction.Interactions;
import eu.koboo.minestom.stomui.api.item.ViewItem;
import eu.koboo.minestom.stomui.api.pagination.ViewPagination;
import eu.koboo.minestom.stomui.api.slots.ViewPattern;
import net.cytonic.cytosis.menus.MenuUtils;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class PaginatedBorder<T> extends ViewComponent {
    ViewPagination<T> pagination;
    ViewPattern pattern;


    public PaginatedBorder(ViewPagination<T> pagination, ViewPattern pattern) {
        this.pagination = pagination;
        this.pattern = pattern;
        addChild(pagination);
    }

    @Override
    public void onRebuild(@NotNull PlayerView view, @NotNull Player player) {
        if (!pagination.hasNextPage()) {
            ViewItem.bySlot(view, pattern.getSlot('>'))
                    .applyPrebuilt(MenuUtils.BORDER);
        } else {
            ViewItem.bySlot(view, pattern.getSlot('>'))
                    .material(Material.ARROW)
                    .name(Msg.mm("<green>Next (" + pagination.getNextPage() + ")"))
                    .hideTooltip(true)
                    .interaction(Interactions.toNextPage(pagination));
        }

        if (!pagination.hasPreviousPage()) {
            ViewItem.bySlot(view, pattern.getSlot('<'))
                    .applyPrebuilt(MenuUtils.BORDER);
        } else {
            ViewItem.bySlot(view, pattern.getSlot('<'))
                    .material(Material.ARROW)
                    .hideTooltip(true)
                    .name(Msg.mm("<green>Previous (" + pagination.getPreviousPage() + ")"))
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
