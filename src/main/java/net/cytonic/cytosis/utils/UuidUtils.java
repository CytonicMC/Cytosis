package net.cytonic.cytosis.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.experimental.UtilityClass;
import net.cytonic.cytosis.logging.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.UUID;

/**
 * A class that provides utilities for dealing with UUIDs
 */
@UtilityClass
public final class UuidUtils {

    private static final String UUID_URL_TEMPLATE = "https://api.mojang.com/users/profiles/minecraft/%s";

    /**
     * Gets the UUID of a player by their username
     *
     * @param username the username
     * @return the UUID
     */
    @Nullable
    public static String UNSAFE_getMojandUUID(final String username) {
        URL url;
        try {
            url = new URI(String.format(UUID_URL_TEMPLATE, username)).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            Logger.error("An error occourd whilst initializing the UUID_URL.", e);
            return null;
        }
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            if (con.getResponseCode() == 200) {
                InputStreamReader reader = new InputStreamReader(con.getInputStream());
                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                con.disconnect();
                return obj.get("id").getAsString();
            }
            con.disconnect();
        } catch (IOException e) {
            Logger.error("An error occoured whilst fetching " + username + "'s UUID from Mojang's API.");
        }
        return null;
    }


    /**
     * Gets a UUID from a username
     *
     * @param username username
     * @return the UUID
     */
    public static UUID UNSAFE_getUUID(final String username) {
        String raw = UNSAFE_getMojandUUID(username);
        if (raw == null) throw new IllegalArgumentException("A player by the name '" + username + "' does not exist!");
        if (raw.length() != 32)
            throw new IllegalArgumentException("Raw UUID provided is not 32 characters! '" + raw + "' is " + raw.length());
        return UUID.fromString(raw.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

    /**
     * Converts a UUID to a mojang UUID by removing the dashes
     *
     * @param uuid the UUID
     * @return the mojang UUID
     */
    public static String toMojang(UUID uuid) {
        return uuid.toString().replace("-", "");
    }
}