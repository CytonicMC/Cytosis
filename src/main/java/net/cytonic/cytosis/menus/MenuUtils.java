package net.cytonic.cytosis.menus;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.utils.Range;

import java.util.*;

public interface MenuUtils {
    EnumSet<InventoryType> SUPPORTS_BORDERS = EnumSet.of(InventoryType.CHEST_2_ROW,
            InventoryType.CHEST_3_ROW, InventoryType.CHEST_4_ROW, InventoryType.CHEST_5_ROW, InventoryType.CHEST_6_ROW,
            InventoryType.SHULKER_BOX);

    static Range.Int pageNumberToRange(int pageNumber) {
        return new Range.Int(pageNumber * 28, pageNumber * 28 + 28); // 28 items per page by default
    }

    static Range.Int pageNumberToRange(int pageNumber, int itemsPerPage) {
        return new Range.Int(pageNumber * itemsPerPage, pageNumber * itemsPerPage + itemsPerPage);
    }

    static int[] borderSlots(Inventory inventory) {
        if (!SUPPORTS_BORDERS.contains(inventory.getInventoryType()))
            throw new IllegalArgumentException("Inventory type not supported");
        int size = inventory.getSize();
        List<Integer> slots = new ArrayList<>();
        int lastRowStart = size - 9; // Precompute last row start index

        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= lastRowStart || i % 9 == 0 || (i + 1) % 9 == 0) {
                slots.add(i);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    static int[] nonBorderSlots(Inventory inventory) {
        Set<Integer> excludedSet = new HashSet<>();
        for (int slot : borderSlots(inventory)) {
            excludedSet.add(slot);
        }

        List<Integer> remainingSlots = new ArrayList<>();

        for (int i = 0; i < inventory.getSize(); i++) {
            if (!excludedSet.contains(i)) {
                remainingSlots.add(i);
            }
        }

        return remainingSlots.stream().mapToInt(Integer::intValue).toArray();
    }
}
