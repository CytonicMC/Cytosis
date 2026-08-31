package net.cytonic.cytosis;

import java.util.List;

import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;

import net.cytonic.cytosis.player.CytosisPlayer;

public class StaticInitializers {

    public static final MinecraftServer SERVER;
    public static final String SECRET = System.getProperty("SERVER_SECRET", System.getenv("SERVER_SECRET"));

    static {
        // set some server flags
        System.setProperty("minestom.shutdown-on-signal", "false");
        System.setProperty("minestom.new-socket-write-lock", "true");
        System.setProperty("minestom.chunk-view-distance", "12");
        System.setProperty("event.multiple-parents", "true");


        // preinitialize the minecraft server :)
        SERVER = MinecraftServer.init(new Auth.Velocity("8a37c3b1939a798a40d63a8e44ab2254a7d151e0"));

        // forces initialization order
        var ignoredTheClasses = List.of(AnvilLoader.class, DynamicChunk.class, CytosisPlayer.class,
            InstanceContainer.class);
    }
}
