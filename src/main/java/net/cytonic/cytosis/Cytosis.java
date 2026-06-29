package net.cytonic.cytosis;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import dev.minestomunited.common.config.format.JsonCodecConfigFormat;
import dev.minestomunited.common.config.source.EnvironmentVariableConfigSource;
import dev.minestomunited.common.config.source.JsonFileConfigSource;
import dev.minestomunited.entrypoint.EntryPoint;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

import net.cytonic.cytosis.adapters.KeyAdapter;
import net.cytonic.cytosis.config.CytosisConfig;
import net.cytonic.cytosis.environments.Environment;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.server.AbstractCytosisServer;
import net.cytonic.cytosis.utils.InstantAdapter;

/**
 * The main class for Cytosis
 */
@Getter
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public final class Cytosis {

    // The instance of Gson for serializing and deserializing objects. (Mostly for preferences).
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Key.class, new KeyAdapter())
        .registerTypeAdapter(Instant.class, new InstantAdapter())
        .registerTypeAdapterFactory(new KeyAdapter())
        .enableComplexMapKeySerialization()
        .setStrictness(Strictness.LENIENT)
        .serializeNulls()
        .create();

    public static final Gson GO_GSON = GSON.newBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX")
        .create();

    public static final boolean IS_NOMAD = System.getenv().containsKey("NOMAD_JOB_ID");
    public static final CytosisContext CONTEXT = new CytosisContext();

    private Cytosis() {
    }

    public static <T extends AbstractCytosisServer<? extends CytosisPlayer>> EntryPoint.Builder<T> applyToBuilder(
        EntryPoint.Builder<T> builder) {
        builder.registerConfig(CytosisConfig.class);
        builder.addConfigSource(new JsonFileConfigSource(Path.of("")));
        builder.addConfigSource(new EnvironmentVariableConfigSource());
        builder.addConfigFormat(new JsonCodecConfigFormat(Map.of(
            CytosisConfig.class, CytosisConfig.CODEC
        )));

        System.setProperty("minestom.shutdown-on-signal", "false");
        return builder;
    }

    public static void init(AbstractCytosisServer<? extends CytosisPlayer> server) {
        new CytosisBootstrap(server, CONTEXT).run();
    }

    /**
     * Gets the players currently on THIS instance every object the server makes is a CytosisPlayer -- or descendant
     * from one
     *
     * @return a set of players
     */
    public static <P extends CytosisPlayer> Set<P> getOnlinePlayers() {
        Set<P> players = new HashSet<>();
        for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            //noinspection unchecked
            players.add((P) p);
        }

        return players;
    }

    public static <P extends CytosisPlayer> Set<P> getOnlinePlayers(Class<P> clazz) {
        return getOnlinePlayers();
    }

    /**
     * Gets the player if they are on THIS instance, by USERNAME
     *
     * @param username The name to fetch the player by
     * @return The optional holding the player if they exist
     */
    public static <P extends CytosisPlayer> Optional<P> getPlayer(String username) {
        if (username == null) {
            return Optional.empty();
        }
        //noinspection unchecked
        return Optional.ofNullable((P) MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(username));
    }

    public static <P extends CytosisPlayer> Optional<P> getPlayer(String username, Class<P> clazz) {
        return getPlayer(username);
    }

    /**
     * Gets the player by UUID
     *
     * @param uuid The uuid to fetch the player by
     * @return The optional holding the player if they exist
     */
    public static <P extends CytosisPlayer> Optional<P> getPlayer(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }
        //noinspection unchecked
        return Optional.ofNullable((P) MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid));
    }

    public static <P extends CytosisPlayer> Optional<P> getPlayer(UUID uuid, Class<P> clazz) {
        return getPlayer(uuid);
    }

    public static <S extends AbstractCytosisServer<P>, P extends CytosisPlayer> S getServer() {
        //noinspection unchecked
        return (S) CONTEXT.getComponent(AbstractCytosisServer.class);
    }

    public static <C> C get(Class<C> clazz) {
        return CONTEXT.getComponent(clazz);
    }

    public static <T> T getGeneric(Class<?> clazz) {
        //noinspection unchecked
        return (T) Cytosis.get(clazz);
    }

    public static boolean isDev() {
        return Cytosis.get(Environment.class) == Environment.DEVELOPMENT;
    }

    public static boolean isStandalone() {
        return get(CytosisConfig.class).standalone();
    }
}
