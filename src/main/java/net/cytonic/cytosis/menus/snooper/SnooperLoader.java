package net.cytonic.cytosis.menus.snooper;

import eu.koboo.minestom.invue.api.item.PrebuiltItem;
import eu.koboo.minestom.invue.api.pagination.ItemLoader;
import eu.koboo.minestom.invue.api.pagination.Pagifier;
import net.minestom.server.item.Material;

import java.util.List;

public class SnooperLoader implements ItemLoader {

    List<PrebuiltItem> items;


    public SnooperLoader(List<PrebuiltItem> items) {
        this.items = items;
    }

    @Override
    public void load(Pagifier<PrebuiltItem> pagifier) {
        if (items.size() == 1 && !items.getFirst().hasMaterial(Material.PAPER)) {
            for (int i = 0; i < pagifier.getMaxItemsPerPage(); i++) {
                pagifier.addItem(items.getFirst());
            }
        } else {
            pagifier.addItems(items);
        }
    }
}
