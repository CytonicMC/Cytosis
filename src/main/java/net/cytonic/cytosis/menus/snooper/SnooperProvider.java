package net.cytonic.cytosis.menus.snooper;

import eu.koboo.minestom.invue.api.PlayerView;
import eu.koboo.minestom.invue.api.ViewBuilder;
import eu.koboo.minestom.invue.api.ViewType;
import eu.koboo.minestom.invue.api.component.ViewProvider;
import eu.koboo.minestom.invue.api.item.PrebuiltItem;
import eu.koboo.minestom.invue.api.item.ViewItem;
import eu.koboo.minestom.invue.api.pagination.Pagifier;
import eu.koboo.minestom.invue.api.pagination.ViewPagination;
import eu.koboo.minestom.invue.api.slots.ViewPattern;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.containers.snooper.QueriedSnoop;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.menus.MenuUtils;
import net.cytonic.cytosis.menus.utils.PaginatedBorder;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SnooperProvider extends ViewProvider {

    public final byte permission;
    private final String id;
    private final ViewPattern pattern;
    private ViewPagination pagination;
    private boolean ascending;
    private DateRange date = DateRange.SEVEN_DAYS;
    private String search;

    public SnooperProvider(@NotNull final String id, String search, boolean ascending) {
        super(Cytosis.VIEW_REGISTRY, ViewBuilder.of(ViewType.SIZE_6_X_9)
                .disableClickTypes(ClickType.DOUBLE_CLICK, ClickType.START_DOUBLE_CLICK)
                .title(id));

        this.id = id;
        this.search = search;
        this.ascending = ascending;

        permission = Objects.requireNonNull(Cytosis.getSnooperManager().getChannel(Key.key(id))).recipients();

        pattern = registry.pattern(
                "#########",
                "#1111111#",
                "#1111111#",
                "#1111111#",
                "#1111111#",
                "<#O#X#D#>");
    }

    private static PrebuiltItem generateItem(QueriedSnoop snoop) {

        List<Component> lore = new ArrayList<>();
        lore.add(Msg.mm("<yellow>Channel: '<light_purple>" + snoop.channel() + "</light_purple>'"));
        lore.add(Msg.mm("<yellow>Content:</yellow>"));
        lore.addAll(Msg.wrap(snoop.rawContent()));
        lore.add(Msg.mm(""));
        lore.add(Msg.mm("<yellow>Sent: <light_purple>" + DurationParser.unparseFull(snoop.timestamp().toInstant()) + "</light_purple> ago."));

        ItemStack item = ItemStack.builder(Material.PAPER)
                .hideExtraTooltip()
                .customName(Msg.mm("Snoop #" + snoop.id()))
                .lore(lore)
                .build();

        return PrebuiltItem.of(item).cancelClicking();
    }

    @Override
    public void onStateUpdate(@NotNull PlayerView view, @NotNull Player player) {
        ViewItem.bySlot(view, pattern.getSlot('O'))
                .applyPrebuilt(toggleOrder());
        ViewItem.bySlot(view, pattern.getSlot('D'))
                .applyPrebuilt(dateRange());
        fetchData().whenComplete((prebuiltItems, throwable) -> {
            if (throwable != null) {
                player.sendMessage(Msg.serverError("An error occurred while fetching data", throwable));
                return;
            }
            pagination.reloadItems(view);
        });
        Logger.debug("Snooper provider onStateUpdate");
    }

    @Override
    public void onOpen(@NotNull PlayerView view, @NotNull Player player) {
        ViewItem.bySlot(view, pattern.getSlot('O'))
                .applyPrebuilt(toggleOrder());
        ViewItem.bySlot(view, pattern.getSlot('D'))
                .applyPrebuilt(dateRange());
    }

    public PrebuiltItem toggleOrder() {
        return PrebuiltItem.empty()
                .material(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE)
                .displayName(Msg.mm("<yellow>Change Order"))
                .addComponent(ItemComponent.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .lore(
                        "<b>" + (ascending ? "<green>Ascending" : "<red>Descending"),
                        "",
                        "<yellow>Click to toggle"
                ).interaction(action -> {
                    ascending = !ascending;
                    action.getView().updateState();
                    action.getEvent().setCancelled(true);
                });
    }

    public PrebuiltItem dateRange() {

        List<Component> lore = new ArrayList<>();
        for (DateRange value : DateRange.values()) {
            String formatted = Utils.captializeFirstLetters(value.name().toLowerCase().replace("_", " "));
            if (value == date) {
                lore.add(Msg.mm("<aqua>» " + formatted));
                continue;
            }

            lore.add(Msg.mm("<dark_aqua>" + formatted));
        }
        lore.add(Msg.mm(""));
        lore.add(Msg.mm("<yellow>Click to cycle!"));

        return PrebuiltItem.empty()
                .material(Material.CLOCK)
                .name("<yellow>Change Date Range")
                .loreComponents(lore)
                .addComponent(ItemComponent.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .interaction(action -> {
                    action.getEvent().setCancelled(true);

                    if (action.getEvent().getClickType() == ClickType.RIGHT_CLICK) {
                        date = date.previous();
                    } else if (action.getEvent().getClickType() == ClickType.LEFT_CLICK) {
                        date = date.next();
                    }
                    action.getView().updateState();
                });
    }

    /**
     * Creates a new {@link PlayerView} for the given {@link Player}
     * and opens it.
     *
     * @param player The {@link Player}, which gets the open inventory.
     */
    @Override
    public void open(Player player) {
        player.sendMessage(Msg.darkGreySplash("LOADING!", "We're crunching the data for you, just a moment!"));

        fetchData().whenComplete((result, throwable) -> {
            pagination = registry.pageable(
                    new SnooperLoader(result),
                    pattern.getMergedSlots('1'),
                    MenuUtils.BORDER.getItem()
            );

            addChild(new PaginatedBorder(pagination, pattern));
            super.open(player);
        });
    }

    private CompletableFuture<List<PrebuiltItem>> fetchData() {

        CompletableFuture<List<PrebuiltItem>> future = new CompletableFuture<>();

        Cytosis.getSnooperManager().getPersistenceManager()
                .query(id, permission, date.instantValue(), Instant.now(), this.ascending, this.search)
                .whenComplete((result, error) -> {
                    List<PrebuiltItem> list = new ArrayList<>();
                    if (error != null) {
                        list.add(PrebuiltItem.empty()
                                .material(Material.RED_STAINED_GLASS_PANE)
                                .name("<red>Failed to load data!")
                                .cancelClicking());

                        Logger.error("Snooper audit loading failed", error);
                        return;
                    } else {
                        if (result.isEmpty()) {
                            list.add(PrebuiltItem.empty()
                                    .material(Material.GRAY_STAINED_GLASS_PANE)
                                    .name("No data found!")
                                    .cancelClicking());
                        } else {
                            for (QueriedSnoop snoop : result) {
                                list.add(generateItem(snoop));
                            }
                        }
                    }
                    future.complete(list);
                    if (pagination != null) {
                        Pagifier<PrebuiltItem> pagifier = pagination.getPager();
                        assert pagifier != null : "pager is null";
                        pagifier.clear();
                        pagifier.addItems(list);
                    }
                });

        return future;
    }
}
