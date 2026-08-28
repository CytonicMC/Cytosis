package net.cytonic.cytosis;

import java.util.List;

import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;

public class StaticInitializers {

    static {
        // set some server flags
        System.setProperty("minestom.shutdown-on-signal", "false");
        System.setProperty("minestom.new-socket-write-lock", "true");
        System.setProperty("minestom.chunk-view-distance", "12");
        System.setProperty("event.multiple-parents", "true");

        // preinitialize the minecraft server :)
        MinecraftServer.init(new Auth.Velocity("TESTSECRET"));

        // forces initialization order
        var ignoredTheClasses = List.of(AnvilLoader.class, DynamicChunk.class, Player.class, InstanceContainer.class);
    }
}
