package dev.foxikle.cytosis.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.foxikle.cytosis.logging.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.UUID;

public class UuidUtils {
    private static final String UUID_URL_TEMPLATE = "https://api.mojang.com/users/profiles/minecraft/%s";

    @Nullable
    public static String getMojandUUID(final String username) {
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


    public static UUID getUUID(final String username) {
        String raw = getMojandUUID(username);
        if (raw == null) throw new IllegalArgumentException("A player by the name '" + username + "' does not exist!");
        if (raw.length() != 32)
            throw new IllegalArgumentException("Raw UUID provided is not 32 characters! '" + raw + "' is " + raw.length());

        return UUID.fromString(raw.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

    public static String toMojang(UUID uuid) {
        return uuid.toString().replace("-", "");
    }
}
