package net.cytonic.cytosis.utils;

import com.google.common.reflect.TypeToken;
import net.cytonic.cytosis.logging.Logger;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A class holding utility methods
 */
public final class Utils {

    /**
     * Default constructor
     */
    private Utils() {
        // do nothing
    }

    /**
     * Creates a MUTABLE list from a vararg, for immutable lists, use {@link List#of(Object...)}
     *
     * @param vararg The elements to be added to the list
     * @param <E> The type of the list
     * @return The elements as a List object
     */
    @SafeVarargs
    public static <E> List<E> list(E... vararg) {
        return new ArrayList<>(List.of(vararg));
    }

    /**
     * Gets the server's IP address
     *
     * @return the string of the IP address
     */
    public static String getServerIP() {
        String serverIP;
        try {
            serverIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            Logger.error("An error occurred whilst fetching this server's IP address! Bailing out!", e);
            return "ERROR";
        }
        return serverIP;
    }

    public final static Type UUID_LIST = new TypeToken<List<UUID>>() {
    }.getType();
}
