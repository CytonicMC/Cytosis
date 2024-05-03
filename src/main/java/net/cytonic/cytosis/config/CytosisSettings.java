package net.cytonic.cytosis.config;

import net.cytonic.cytosis.logging.Logger;

import java.util.Map;

/**
 * This class is used to store all the cached configuration values.
 */
public class CytosisSettings {
    public static boolean LOG_PLAYER_IPS = true;
    public static boolean LOG_PLAYER_JOINS = true;
    public static boolean LOG_PLAYER_QUITS = true;
    public static boolean LOG_PLAYER_COMMANDS = true;
    public static boolean LOG_PLAYER_CHAT = true;
    public static String DATABASE_USER = "";
    public static String DATABASE_PASSWORD = "";
    public static String DATABASE_HOST = "";
    public static int DATABASE_PORT = 3306;
    public static String DATABASE_NAME = "";
    public static boolean DATABASE_USE_SSL = false;

    public static void inportConfig(Map<String, Object> config) {
        Logger.info("Importing config!");
        config.forEach((key, value) -> {
            Logger.info(STR."Config key: \{key}");
            try {
                switch (key.replace("\"", "")) {
                    case "logging.player_ip" -> LOG_PLAYER_IPS = (boolean) value;
                    case "logging.player_join" -> LOG_PLAYER_JOINS = (boolean) value;
                    case "logging.player_quit" -> LOG_PLAYER_QUITS = (boolean) value;
                    case "logging.player_command" -> LOG_PLAYER_COMMANDS = (boolean) value;
                    case "logging.player_chat" -> LOG_PLAYER_CHAT = (boolean) value;
                    case "database.user" -> DATABASE_USER = (String) value;
                    case "database.password" -> DATABASE_PASSWORD = (String) value;
                    case "database.host" -> DATABASE_HOST = (String) value;
                    case "database.port" -> DATABASE_PORT = toInt(value);
                    case "database.name" -> DATABASE_NAME = (String) value;
                    case "database.use_ssl" -> DATABASE_USE_SSL = (boolean) value;
                    default -> { /*Do nothing*/ }
                }
            } catch (ClassCastException e) {
                Logger.error(STR."Could not import config key: \{key}", e);
            }
        });
        Logger.info("Config imported!");
    }

    private static int toInt(Object key) {
        return Integer.parseInt(key.toString());
    }
}