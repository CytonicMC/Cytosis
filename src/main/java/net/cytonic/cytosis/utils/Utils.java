package net.cytonic.cytosis.utils;

import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.reflect.TypeToken;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.data.objects.TypedNamespace;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.data.packet.packets.ServerStatusPacket;
import net.cytonic.cytosis.logging.Logger;

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
    public final static Type UUID_LIST = new TypeToken<List<UUID>>() {
    }.getType();
    /**
     * A type token for a set of UUIDs
     */
    public final static Type UUID_SET = new TypeToken<Set<UUID>>() {
    }.getType();

    public static final Type SERVER_LIST = new TypeToken<List<ServerStatusPacket>>() {
    }.getType();
    /**
     * A type token for a map of namespaced preferences keyed by NamespaceID
     */
    public final static TypeToken<Map<TypedNamespace<?>, Preference<?>>> PREFERENCE_MAP = new TypeToken<>() {
    };

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
     * Gets the server's IP address. Does **NOT** include the port. An example is: {@code 127.0.0.1}
     *
     * @return the string of the IP address
     */
    public static String getServerIP() {
        String serverIP;
        try {
            serverIP = getInternalIP();
            if (serverIP == null) {
                serverIP = InetAddress.getLocalHost().getHostAddress();
            }
        } catch (UnknownHostException e) {
            Logger.error("An error occurred whilst fetching this server's IP address! Bailing out!", e);
            return "ERROR";
        }
        return serverIP;
    }

    // the IP of this machine on the tailscale network
    @Nullable
    @SneakyThrows
    public static String getInternalIP() {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            if (iface.getDisplayName().contains("tailscale")) {
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) { // Change to Inet6Address for IPv6
                        return addr.getHostAddress();
                    }
                }
            }
        }
        return null; // No other IP found (Docker or Tailscale)
    }

    @SuppressWarnings("unchecked")
    public static <T> T clone(T value) {
        if (value == null) {
            return null; // Null safety
        }

        // Handle immutable or simple types
        if (value instanceof UUID || value instanceof Number || value instanceof Boolean || value.getClass().isEnum()) {
            return value; // Immutable types can be returned as-is
        }

        if (value instanceof String) {
            return (T) value.toString();
        }

        // Handle Cloneable types
        if (value instanceof Cloneable) {
            try {
                return (T) value.getClass().getMethod("clone").invoke(value);
            } catch (Exception e) {
                throw new RuntimeException("Failed to clone " + value.getClass().getName() + " using clone()", e);
            }
        }

        throw new IllegalArgumentException("Unsupported type for cloning: " + value.getClass());
    }

    /**
     * Capitalizes the first letter of each word in the string, separated by a delimiter of " ".
     *
     * @param input The input
     * @return The captialized string
     */
    public static String captializeFirstLetters(String input) {
        String[] words = input.split(" ");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase())
                    .append(" ");
            }
        }

        return capitalized.toString().trim();
    }
}
