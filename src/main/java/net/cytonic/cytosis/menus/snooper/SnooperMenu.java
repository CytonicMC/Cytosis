package net.cytonic.cytosis.menus.snooper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.ViewType;
import me.devnatan.inventoryframework.component.Pagination;
import me.devnatan.inventoryframework.context.IFContext;
import me.devnatan.inventoryframework.context.OpenContext;
import me.devnatan.inventoryframework.context.RenderContext;
import me.devnatan.inventoryframework.context.SlotClickContext;
import me.devnatan.inventoryframework.state.MutableState;
import me.devnatan.inventoryframework.state.State;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.containers.snooper.QueriedSnoop;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.menus.MenuUtils;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;

public class SnooperMenu extends View {
    private final MutableState<String> idState = initialState("id");
    private final MutableState<String> searchState = initialState("search");
    private final MutableState<Boolean> ascendingState = initialState("ascending");
    private final State<Byte> permissionState = lazyState(context ->
            Objects.requireNonNull(
                    Cytosis.CONTEXT
                            .getComponent(SnooperManager.class)
                            .getChannel(Key.key(idState.get(context))))
                    .recipients());
    private final State<Pagination> paginationState = buildLazyPaginationState(this::fetchData)
            .layoutTarget('1')
            .itemFactory((builder, snoop) -> {
        List<Component> lore = new ArrayList<>();
        lore.add(Msg.mm("<yellow>Channel: '<light_purple>" + snoop.channel() + "</light_purple>'"));
        lore.add(Msg.mm("<yellow>Content:</yellow>"));
        lore.addAll(Msg.wrap(snoop.rawContent()));
        lore.add(Msg.mm(""));
        lore.add(Msg.mm(
                "<yellow>Sent: <light_purple>"
                        + DurationParser.unparseFull(snoop.timestamp().toInstant())
                        + "</light_purple> ago."));

        ItemStack item = ItemStack.builder(Material.PAPER)
                .hideExtraTooltip()
                .customName(Msg.mm("Snoop #" + snoop.id()))
                .lore(lore)
                .build();
        builder.withItem(item);
    }).build();
    private final MutableState<DateRange> dateState = initialState("date");

    @Override
    public void onInit(@NotNull ViewConfigBuilder config) {
        config.type(ViewType.CHEST);
        config.size(6);
        config.cancelInteractions();
        config.layout(
                "#########",
                "#1111111#",
                "#1111111#",
                "#1111111#",
                "#1111111#",
                "<#o#X#D#>"
        );
    }

    @Override
    public void onFirstRender(@NotNull RenderContext context) {
        paginationState.get(context).update();
        context.layoutSlot('#', MenuUtils.BORDER);
        context.layoutSlot('>').onRender(slotRenderContext -> {
            if (paginationState.get(slotRenderContext).canAdvance()) {
                ItemStack itemStack = ItemStack.builder(Material.ARROW)
                        .set(
                                DataComponents.ITEM_NAME,
                                Msg.green(
                                        "Next (%d)",
                                        paginationState.get(slotRenderContext).nextPage()))
                        .hideExtraTooltip()
                        .build();
                slotRenderContext.setItem(itemStack);
            } else {
                slotRenderContext.setItem(MenuUtils.BORDER);
            }
        })
                .onClick(slotClickContext ->
                        paginationState.get(slotClickContext).advance())
                .updateOnStateChange(paginationState);

        context.layoutSlot('<').onRender(slotRenderContext -> {
            if (paginationState.get(slotRenderContext).canBack()) {
                ItemStack itemStack = ItemStack.builder(Material.ARROW)
                        .set(
                                DataComponents.ITEM_NAME,
                                Msg.green(
                                        "Previous (%d)",
                                        paginationState.get(slotRenderContext).lastPage()))
                        .hideExtraTooltip()
                        .build();
                slotRenderContext.setItem(itemStack);
            } else {
                slotRenderContext.setItem(MenuUtils.BORDER);
            }
        })
                .onClick(slotClickContext ->
                        paginationState.get(slotClickContext).back())
                .updateOnStateChange(paginationState);

        context.layoutSlot('X',
                ItemStack.builder(Material.BARRIER)
                        .customName(Msg.mm("Close"))
                        .build())
                .onClick(SlotClickContext::closeForPlayer);
        context.layoutSlot('#', MenuUtils.BORDER);

        context.layoutSlot('o').onRender(slotRenderContext -> {
            ItemStack itemStack = ItemStack.builder(Material.PAPER)
                    .set(DataComponents.ITEM_MODEL, "eye_armor_trim_smithing_template")
                    .set(DataComponents.ITEM_NAME, Msg.mm("<yellow>Change Order"))
                    .set(DataComponents.LORE, List.of(
                            Msg.mm(ascendingState.get(slotRenderContext) ? "<green>Ascending" : "<red>Descending"),
                            Component.empty(),
                            Msg.yellow("Click to toggle")
                    )).hideExtraTooltip().build();
            slotRenderContext.setItem(itemStack);
        })
                .onClick(slotClickContext ->
                        slotClickContext.openForPlayer(
                                SnooperMenu.class,
                                ImmutableMap.of(
                                        "id", idState.get(slotClickContext),
                                        "search", searchState.get(slotClickContext),
                                        "ascending", !ascendingState.get(slotClickContext),
                                        "date", dateState.get(slotClickContext))));

        context.layoutSlot('D').onRender(slotRenderContext -> {
            List<Component> lore = new ArrayList<>();
            for (DateRange value : DateRange.values()) {
                String formatted = Utils.captializeFirstLetters(value.name().toLowerCase().replace("_", " "));
                if (value == dateState.get(slotRenderContext)) {
                    lore.add(Msg.mm("<aqua>» " + formatted));
                    continue;
                }

                lore.add(Msg.mm("<dark_aqua>" + formatted));
            }
            lore.add(Msg.mm(""));
            lore.add(Msg.mm("<yellow>Click to cycle!"));

            ItemStack itemStack = ItemStack.builder(Material.CLOCK)
                    .set(DataComponents.ITEM_NAME, Msg.mm("<yellow>Change Date Range"))
                    .set(DataComponents.LORE, lore).hideExtraTooltip().build();
            slotRenderContext.setItem(itemStack);
        }).onClick(slotClickContext -> {
            DateRange dateRange = DateRange.SEVEN_DAYS;
            if (slotClickContext.isLeftClick()) {
                dateRange = dateState.get(slotClickContext).next();
            } else if (slotClickContext.isRightClick()) {
                dateRange = dateState.get(slotClickContext).previous();
            }
            slotClickContext.openForPlayer(SnooperMenu.class, ImmutableMap.of(
                    "id", idState.get(slotClickContext),
                    "search", searchState.get(slotClickContext),
                    "ascending", ascendingState.get(slotClickContext),
                    "date", dateRange));
        });
    }

    private List<QueriedSnoop> fetchData(IFContext context) {
        return Cytosis.CONTEXT
                .getComponent(SnooperManager.class)
                .getPersistenceManager()
                .query(
                        idState.get(context),
                        permissionState.get(context),
                        dateState.get(context).instantValue(),
                        Instant.now(),
                        ascendingState.get(context),
                        searchState.get(context))
                .join();
    }

    @Override
    public void onOpen(@NotNull OpenContext context) {
        context.getPlayer().sendMessage(
                Msg.darkGreySplash(
                        "LOADING!",
                        "We're crunching the data for you, just a moment!"));
    }
}
