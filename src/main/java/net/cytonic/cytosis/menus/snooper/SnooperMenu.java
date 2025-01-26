package net.cytonic.cytosis.menus.snooper;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.containers.snooper.QueriedSnoop;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.menus.*;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Utils;
import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;
import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM_WRAP;

public class SnooperMenu extends Menu {
    // todo: look out for memory leaks
    private static final Map<UUID, SnooperMenu> instances = new ConcurrentHashMap<>();

    private static final ClickableItem LOADING = new DummyItem(
            NamespaceID.from("cytosis", "snooper_loading"),
            unused -> ItemStack.builder(Material.GRAY_STAINED_GLASS_PANE).customName(MM."<gray>Loading Data...").hideExtraTooltip().build()
    );

    private static final ClickableItem NONE = new DummyItem(
            NamespaceID.from("cytosis", "snooper_none"),
            unused -> ItemStack.builder(Material.RED_STAINED_GLASS_PANE).customName(MM."<red>No data!")
                    .lore(MM."<gray><i>Try widening your search!").hideExtraTooltip().build()
    );

    private static final ClickableItem ERROR = new DummyItem(
            NamespaceID.from("cytosis:snooper_error_query"),
            player -> ItemStack.builder(Material.BARRIER).customName(MM."<red>ERROR!")
                    .lore(MM."<gray>Check the server console for details!", MM."<gray>Server ID: \{Cytosis.SERVER_ID}").build()
    );

    private static final ClickableItem TOGGLE_DATE_RANGE = new Button(
            NamespaceID.from("cytosis:snooper_toggle_date"),
            p -> {
                if (!instances.containsKey(p.getUuid()))
                    return ItemStack.builder(Material.BEDROCK).customName(MM."<red><b>ERROR!").build();
                SnooperMenu menu = instances.get(p.getUuid());
                DateRange range = menu.range;
                List<Component> lore = new ArrayList<>();
                for (DateRange value : DateRange.values()) {
                    String formatted = Utils.captializeFirstLetters(value.name().toLowerCase().replace("_", " "));
                    if (value == range) {
                        lore.add(MM."<aqua>» \{formatted}");
                        continue;
                    }

                    lore.add(MM."<dark_aqua>\{formatted}");
                }
                lore.add(MM."");
                lore.add(MM."<yellow>Click to cycle!");
                return ItemStack.builder(Material.CLOCK)
                        .hideExtraTooltip()
                        .lore(lore)
                        .customName(MM."<yellow>Change Date Range").build();
            },
            (p, e) -> {
                if (!instances.containsKey(p.getUuid())) return;
                SnooperMenu menu = instances.get(p.getUuid());
                if (e.getInventory() != menu) return;

                e.setCancelled(true);

                DateRange range = menu.range;
                if (e.getClickType() == ClickType.RIGHT_CLICK) {
                    menu.range = range.previous();
                } else if (e.getClickType() == ClickType.LEFT_CLICK) {
                    menu.range = range.next();
                } else return;

                // rerender the page
                menu.refreshData();
                menu.rerender(p);
            }
    );

    private static final ClickableItem TOGGLE_ORDER = new Button(
            NamespaceID.from("cytosis:snooper_toggle_order"),
            p -> {
                if (!instances.containsKey(p.getUuid()))
                    return ItemStack.builder(Material.BEDROCK).customName(MM."<red><b>ERROR!").build();
                SnooperMenu menu = instances.get(p.getUuid());

                return ItemStack.builder(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .hideExtraTooltip()
                        .lore(MM."<b>\{menu.ascending ? "<green>Ascending" : "<red>Descending"}", MM."", MM."<yellow>Click to toggle")
                        .customName(MM."<yellow>Change Order").build();
            },
            (p, e) -> {
                if (!instances.containsKey(p.getUuid())) return;
                SnooperMenu menu = instances.get(p.getUuid());
                if (e.getInventory() != menu) return;

                e.setCancelled(true);

                menu.ascending = !menu.ascending;

                // rerender the page
                menu.refreshData();
                menu.rerender(p);
            }
    );


    static {
        ClickableItemRegistry.getInstance().add(LOADING);
        ClickableItemRegistry.getInstance().add(NONE);
        ClickableItemRegistry.getInstance().add(TOGGLE_DATE_RANGE);
        ClickableItemRegistry.getInstance().add(TOGGLE_ORDER);
    }

    private final String id;
    private final CytosisPlayer player;
    private final byte permission;
    private boolean ascending = true;
    private int page = 0;
    private String search = null;

    // menu interactions
    private DateRange range = DateRange.SEVEN_DAYS;


    public SnooperMenu(@NotNull final String id, final CytosisPlayer player, String search, boolean ascending) {
        super(InventoryType.CHEST_6_ROW, Component.text(id));

        this.id = id;
        this.player = player;
        this.search = search;
        this.ascending = ascending;

        permission = Objects.requireNonNull(Cytosis.getSnooperManager().getChannel(NamespaceID.from(id))).recipients();

        instances.put(player.getUuid(), this);

        applyItem(Menu.createFillerItem(Material.BLACK_STAINED_GLASS_PANE), player, MenuUtils.borderSlots(this));
        setClickableItem(TOGGLE_DATE_RANGE, player, 46);
        setClickableItem(TOGGLE_ORDER, player, 47);
        refreshData();

        setClickableItem(ClickableItem.CLOSE_BUTTON, player, 49);
    }

    private static ClickableItem generateItem(QueriedSnoop snoop) {

        List<Component> lore = new ArrayList<>();
        lore.add(MM."<yellow>Channel: '<light_purple>\{snoop.channel()}</light_purple>'");
        lore.add(MM."<yellow>Content:</yellow>");
        lore.addAll(MM_WRAP."\{snoop.rawContent()}");
        lore.add(MM."");
        lore.add(MM."<yellow>Sent: <light_purple>\{DurationParser.unparseFull(snoop.timestamp().toInstant())}</light_purple> ago.");

        ItemStack item = ItemStack.builder(Material.PAPER)
                .hideExtraTooltip()
                .customName(MM."Snoop #\{snoop.id()}")
                .lore(lore)
                .build();

        return new DummyItem(NamespaceID.from("cytosis:", "snooper_generated_" + snoop.id()), (p) -> {
            if (p.canRecieveSnoop(snoop.permission())) return item;
            else return ItemStack.builder(Material.BARRIER).customName(MM."<red><b>NO PERMISSION!").build();
        });
    }

    private void refreshData() {
        clearSlots(MenuUtils.nonBorderSlots(this));

        applyItem(LOADING, player, 31);

        // maybe I'll add offsets from now
        Cytosis.getSnooperManager().getPersistenceManager().query(id, permission, MenuUtils.pageNumberToRange(page), range.instantValue(), Instant.now(), this.ascending, this.search).whenComplete((result, error) -> {

            if (error != null) {
                setClickableItem(ERROR, player, 31);
                fillEmpty(Material.RED_STAINED_GLASS_PANE);
                Logger.error("Snooper audit loading failed", error);
            }

            setItemStack(31, ItemStack.AIR); // reset it :)

            if (result.isEmpty()) {
                setClickableItem(NONE, player, 31);
                fillEmpty(Material.GRAY_STAINED_GLASS_PANE);
            }

            for (QueriedSnoop snoop : result) {
                ClickableItem item = generateItem(snoop);
                ClickableItemRegistry.getInstance().add(item);
                addItems(player, item);
            }

        });
    }

    private enum DateRange {
        ONE_HOUR,
        TWELVE_HOURS,
        ONE_DAY,
        SEVEN_DAYS,
        THIRTY_DAYS,
        ONE_HUNDRED_EIGHTY_DAYS,
        ONE_YEAR,
        ALL_TIME;

        public Instant instantValue() {
            return switch (this) {
                case ONE_HOUR -> Instant.now().minus(1, ChronoUnit.HOURS);
                case TWELVE_HOURS -> Instant.now().minus(12, ChronoUnit.HOURS);
                case ONE_DAY -> Instant.now().minus(1, ChronoUnit.DAYS);
                case SEVEN_DAYS -> Instant.now().minus(7, ChronoUnit.DAYS);
                case THIRTY_DAYS -> Instant.now().minus(30, ChronoUnit.DAYS);
                case ONE_HUNDRED_EIGHTY_DAYS -> Instant.now().minus(180, ChronoUnit.DAYS);
                case ONE_YEAR -> Instant.now().minus(365, ChronoUnit.DAYS);
                case ALL_TIME -> Instant.EPOCH; // effectively all time :)
            };
        }

        public DateRange next() {
            return this.ordinal() + 1 < values().length ? values()[this.ordinal() + 1] : ONE_HOUR;
        }

        public DateRange previous() {
            return this.ordinal() - 1 >= 0 ? values()[this.ordinal() - 1] : ALL_TIME;
        }
    }
}
