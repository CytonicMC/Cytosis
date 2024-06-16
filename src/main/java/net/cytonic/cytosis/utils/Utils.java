package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.logging.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
}
