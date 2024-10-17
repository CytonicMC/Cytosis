package net.cytonic.cytosis.utils;

import com.google.common.reflect.TypeToken;
import lombok.experimental.UtilityClass;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.objects.Preference;
import net.cytonic.objects.TypedNamespace;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A class holding utility methods
 */
@UtilityClass
public final class Utils {

    /**
     * A decimal format rounding to two decimal places
     */
    public static final DecimalFormat TWO_PLACES = new DecimalFormat("0.00");
    /**
     * A type token for a list of UUIDs
     */
    public final static Type UUID_LIST = new TypeToken<List<UUID>>() {}.getType();
    /**
     * A type token for a map of namespaced preferences keyed by NamespaceID
     */
    public final static TypeToken<Map<TypedNamespace<?>, Preference<?>>> PREFERENCE_MAP = new TypeToken<>() {};

    /**
     * Creates a MUTABLE list from a vararg, for immutable lists, use {@link List#of(Object...)}
     *
     * @param vararg The elements to be added to the list
     * @param <E>    The type of the list
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
}
