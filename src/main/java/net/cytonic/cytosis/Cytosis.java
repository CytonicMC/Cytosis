package net.cytonic.cytosis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import lombok.Getter;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.GlobalDatabase;
import net.cytonic.cytosis.data.adapters.InstantAdapter;
import net.cytonic.cytosis.data.adapters.KeyAdapter;
import net.cytonic.cytosis.data.adapters.PreferenceAdapter;
import net.cytonic.cytosis.data.adapters.TypedNamespaceAdapter;
import net.cytonic.cytosis.data.objects.TypedNamespace;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.data.serializers.KeySerializer;
import net.cytonic.cytosis.data.serializers.PosSerializer;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.polar.PolarExtension;
import net.hollowcube.polar.PolarLoader;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The main class for Cytosis
 */
@Getter
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public final class Cytosis {

    // The instance of Gson for serializing and deserializing objects. (Mostly for preferences).
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(TypedNamespace.class, new TypedNamespaceAdapter())
            .registerTypeAdapter(Preference.class, new PreferenceAdapter<>())
            .registerTypeAdapter(Key.class, new KeyAdapter())
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .registerTypeAdapterFactory(new TypedNamespaceAdapter())
            .registerTypeAdapterFactory(new PreferenceAdapter<>())
            .registerTypeAdapterFactory(new KeyAdapter())
            .enableComplexMapKeySerialization()
            .setStrictness(Strictness.LENIENT)
            .serializeNulls()
            .create();

    public static final GsonConfigurationLoader.Builder GSON_CONFIGURATION_LOADER = GsonConfigurationLoader.builder()
            .indent(0)
            .defaultOptions(opts -> opts.shouldCopyDefaults(true)
                    .serializers(builder -> {
                        builder.registerAnnotatedObjects(ObjectMapper.factory());
                        builder.register(Key.class, new KeySerializer());
                        builder.register(Pos.class, new PosSerializer());
                    }));

    // The version of Cytosis
    public static final String VERSION = "0.1";
    public static final boolean IS_NOMAD = System.getenv().containsKey("NOMAD_JOB_ID");
    public static final CytosisContext CONTEXT = new CytosisContext();

    private Cytosis() {
    }

    /**
     * The entry point for the Minecraft Server
     *
     * @param args Runtime flags
     */
    public static void main(String[] args) {
        new CytosisBootstrap(args, CONTEXT).run();
    }

    /**
     * Gets the players currently on THIS instance every object the server makes is a CytosisPlayer -- or descendant
     * from one
     *
     * @return a set of players
     */
    public static Set<CytosisPlayer> getOnlinePlayers() {
        Set<CytosisPlayer> players = new HashSet<>();
        for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (p instanceof CytosisPlayer cp) {
                players.add(cp);
            }
        }

        return players;
    }

    /**
     * Returns the online players as the specified player implementation
     *
     * @param clazz the player implementation class
     * @param <T>   the type
     * @return the players, potentially empty.
     */
    public static <T extends CytosisPlayer> Set<T> getOnlinePlayersAs(Class<T> clazz) {
        Set<T> players = new HashSet<>();
        for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (clazz.isInstance(p)) {
                players.add(clazz.cast(p));
            }
        }

        return players;
    }

    /**
     * Gets the player if they are on THIS instance, by USERNAME
     *
     * @param username The name to fetch the player by
     * @param clazz    The type to cast the player to
     * @param <T>      The new type of the player
     * @return The optional holding the player if they exist
     */
    public static <T extends CytosisPlayer> Optional<T> getPlayerAs(String username, Class<T> clazz) {
        return getPlayer(username).map(clazz::cast);
    }

    /**
     * Gets the player if they are on THIS instance, by UNIQUE ID
     *
     * @param player The name to fetch the player by
     * @param clazz  The type to cast the player to
     * @param <T>    The new type of the player
     * @return The optional holding the player if they exist
     */
    public static <T extends CytosisPlayer> Optional<T> getPlayerAs(UUID player, Class<T> clazz) {
        return getPlayer(player).map(clazz::cast);
    }

    /**
     * Gets the player if they are on THIS instance, by USERNAME
     *
     * @param username The name to fetch the player by
     * @return The optional holding the player if they exist
     */
    public static Optional<CytosisPlayer> getPlayer(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((CytosisPlayer) MinecraftServer.getConnectionManager()
                .getOnlinePlayerByUsername(username));
    }

    /**
     * Gets the player if they are on THIS instance, by UUID
     *
     * @param uuid The uuid to fetch the player by
     * @return The optional holding the player if they exist
     */
    public static Optional<CytosisPlayer> getPlayer(UUID uuid) {
        return uuid == null ? Optional.empty()
                : Optional.ofNullable((CytosisPlayer) MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid));
    }

    /**
     * Loads the world based on the settings
     */
    public static void loadWorld() {
        InstanceContainer defaultInstance = CONTEXT.getComponent(InstanceContainer.class);
        if (CytosisSettings.SERVER_WORLD_NAME.isEmpty()) {
            Logger.info("Generating basic world");
            defaultInstance.setGenerator(unit -> unit.modifier().fillHeight(0, 1, Block.WHITE_STAINED_GLASS));
            defaultInstance.setChunkSupplier(LightingChunk::new);
            Logger.info("Basic world loaded!");
            return;
        }

        Logger.info("Loading world '" + CytosisSettings.SERVER_WORLD_NAME + "'");
        CONTEXT.getComponent(GlobalDatabase.class).getWorld(CytosisSettings.SERVER_WORLD_NAME)
                .whenComplete((polarWorld, throwable) -> {
                    if (throwable != null) {
                        Logger.error("An error occurred whilst initializing the world! Reverting to a basic world",
                                throwable);
                        defaultInstance.setGenerator(unit -> unit.modifier().fillHeight(0, 1, Block.WHITE_STAINED_GLASS));
                        defaultInstance.setChunkSupplier(LightingChunk::new);
                    } else {
                        defaultInstance.setChunkLoader(new PolarLoader(polarWorld).setWorldAccess(new PolarExtension()));
                        defaultInstance.setChunkSupplier(LightingChunk::new);
                        defaultInstance.enableAutoChunkLoad(true);
                        Logger.info("World loaded!");
                    }
                });
    }
}
