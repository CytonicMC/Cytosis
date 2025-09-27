package net.cytonic.cytosis.menus;

import eu.koboo.minestom.stomui.api.item.PrebuiltItem;
import net.minestom.server.item.Material;

public interface MenuUtils {

    PrebuiltItem BORDER = PrebuiltItem.empty().material(Material.BLACK_STAINED_GLASS_PANE).name(" ").hideTooltip(true)
        .cancelClicking();
}
