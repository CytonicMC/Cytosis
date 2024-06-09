package net.cytonic.cytosis.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A class holding utility methods
 */
public class Utils {

    /**
     * Creates a MUTABLE list from a vararg, for immutable lists, use {@link List#of(Object...)}
     *
     * @param vararg The elements to be added to the list
     * @return The elements as a List object
     */
    @SafeVarargs
    public static <E> List<E> list(E... vararg) {
        return new ArrayList<>(List.of(vararg));
    }
}
