package net.cytonic.cytosis.menus;

import net.cytonic.cytosis.utils.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class ClickableItemRegistry extends Registry<NamespaceID, ClickableItem> {
    private static final ClickableItemRegistry INSTANCE = new ClickableItemRegistry();

    public static ClickableItemRegistry getInstance() {
        return INSTANCE;
    }

    public void registerAll() {
        add(ClickableItem.CLOSE_BUTTON);
    }

    public void add(@NotNull ClickableItem clickableItem) {
        try {
            add(clickableItem.id(), clickableItem);
        } catch (IllegalArgumentException e) {
            // duplicate keys do not matter in this case
        }
    }
}
