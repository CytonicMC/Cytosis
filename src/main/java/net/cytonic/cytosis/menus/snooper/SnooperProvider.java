package net.cytonic.cytosis.menus.snooper;

import eu.koboo.minestom.invue.api.PlayerView;
import eu.koboo.minestom.invue.api.ViewBuilder;
import eu.koboo.minestom.invue.api.ViewType;
import eu.koboo.minestom.invue.api.component.ViewProvider;
import eu.koboo.minestom.invue.api.item.PrebuiltItem;
import eu.koboo.minestom.invue.api.item.ViewItem;
import eu.koboo.minestom.invue.api.pagination.ViewPagination;
import eu.koboo.minestom.invue.api.slots.ViewPattern;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.containers.snooper.QueriedSnoop;
import net.cytonic.cytosis.logging.Logger;
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

public class SnooperProvider extends ViewProvider {

    public final byte permission;
    private final String id;
    private final ViewPattern pattern;
    SnooperLoader loader = new SnooperLoader(new ArrayList<>());
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
                "<#O#X#D#>"
        );

        pagination = registry.pageable(
                loader,
                pattern.getSlots('1')
        );

        addChild(new PaginatedBorder(pagination, pattern));
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
    }

    @Override
    public void onOpen(@NotNull PlayerView view, @NotNull Player player) {
        ViewItem.bySlot(view, pattern.getSlot('O')).applyPrebuilt(toggleOrder());
        ViewItem.bySlot(view, pattern.getSlot('D')).applyPrebuilt(dateRange());
    }

    public PrebuiltItem toggleOrder() {
        return PrebuiltItem.empty()
                .material(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE)
                .displayName(Msg.mm("<yellow>Change Order"))
                .addComponent(ItemComponent.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .loreComponents(List.of(
                        Msg.mm("<b>" + (ascending ? "<green>Ascending" : "<red>Descending")),
                        Component.empty(),
                        Msg.mm("<yellow>Click to toggle")
                )).interaction(action -> {
                    ascending = !ascending;
                    action.getEvent().setCancelled(true);
                    fetchData(action.getView());
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
                .name(Msg.mm("<yellow>Change Date Range"))
                .loreComponents(lore)
                .addComponent(ItemComponent.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .interaction(action -> {
                    action.getEvent().setCancelled(true);

                    if (action.getEvent().getClickType() == ClickType.RIGHT_CLICK) {
                        date = date.previous();
                    } else if (action.getEvent().getClickType() == ClickType.LEFT_CLICK) {
                        date = date.next();
                    }
                    fetchData(action.getView());
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
        super.open(player);
        fetchData(Objects.requireNonNull(registry.getCurrentView(player)));
    }

    private void fetchData(PlayerView view) {
        pattern.getSlots('1').forEach(slot -> ViewItem.bySlot(view, slot).applyPrebuilt(PrebuiltItem.empty()
                .material(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .name("<gray>Loading data!")
                .cancelClicking()));

        Cytosis.getSnooperManager().getPersistenceManager()
                .query(id, permission, date.instantValue(), Instant.now(), this.ascending, this.search)
                .thenAccept((result) -> {
                    List<PrebuiltItem> list = new ArrayList<>();

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
                    loader.setItems(list);
                    pagination.reloadItems(view);

                }).exceptionally(error -> {
                    if (error != null) {
                        ViewItem.bySlot(view, 22).applyPrebuilt(PrebuiltItem.empty()
                                .material(Material.RED_STAINED_GLASS_PANE)
                                .name("<red>Failed to load data!")
                                .cancelClicking());
                        Logger.error("Snooper audit loading failed", error);
                    }
                    return null;
                });
    }
}
