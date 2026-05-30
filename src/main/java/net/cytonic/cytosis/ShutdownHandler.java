package net.cytonic.cytosis;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;

import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Events;
import net.cytonic.cytosis.utils.Msg;

public class ShutdownHandler {

    public static void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ShutdownHandler.shutdown().get();
            } catch (Throwable e) {
                System.err.println("An error occurred while shutting down");
                throw new RuntimeException(e);
            }
        }));
    }

    public static CompletableFuture<Void> shutdown() {
        if (Cytosis.CONTEXT.isStopping()) return CompletableFuture.completedFuture(null);

        Logger.info("Shutting down...");
        Cytosis.CONTEXT.setStopping(true);

        if (Cytosis.getOnlinePlayers().isEmpty()) {
            internalShutdown();
            return CompletableFuture.completedFuture(null);
        }

        if (Cytosis.isDev()) {
            for (CytosisPlayer player : Cytosis.getOnlinePlayers()) {
                player.kickInternal(Msg.red("Dev server shutting down"));
            }
            internalShutdown();
            return CompletableFuture.completedFuture(null);
        }

        return slowShutdown().thenCompose(_ -> internalShutdown());
    }

    private static CompletableFuture<Void> slowShutdown() {
        int seconds = Cytosis.CONTEXT.getShutdownDuration();
        Instant shutdown = Instant.now().plusSeconds(seconds);
        Cytosis.CONTEXT.setShutdownAt(shutdown);
        Logger.info("Shutting server down in %d seconds.", seconds);
        BossBar bb = BossBar.bossBar(Msg.red("<b>Server Shutdown</b><white> %s",
            DurationParser.unparse(shutdown, " ")), 1F, Color.RED, BossBar.Overlay.PROGRESS);
        MinecraftServer.getSchedulerManager().buildTask(() -> {
                bb.progress(Instant.now().until(shutdown, ChronoUnit.SECONDS) / (float) seconds);
                bb.name(Msg.red("<b>Server Shutdown</b><white> %s", DurationParser.unparse(shutdown, " ")));
            }).repeat(Duration.ofSeconds(1))
            .schedule();
        Cytosis.getOnlinePlayers().forEach(player -> {
            player.showBossBar(bb);
            player.showTitle(Title.title(Msg.redSplash("SERVER SHUTTING DOWN", ""),
                Msg.mm("This server will shut down in %s",
                    DurationParser.unparse(shutdown, " ")), 10, 40, 10));
        });

        CompletableFuture<Void> future = new CompletableFuture<>();
        Events.onPlayerDisconnect(_ -> {
            if (Cytosis.getOnlinePlayers().isEmpty()) {
                future.complete(null);
            }
        });

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(seconds * 1000L);
            } catch (InterruptedException e) {
                System.err.println("Interrupted!");
                Thread.currentThread().interrupt();
            }
            Cytosis.getOnlinePlayers().forEach(player ->
                player.kickInternal(Msg.red("Server shutting down!")));
            if (Cytosis.getOnlinePlayers().isEmpty()) {
                future.complete(null);
            }
        });

        return future;
    }

    private static CompletableFuture<Void> internalShutdown() {
        Cytosis.getServer().onShutdown();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        MinecraftServer.stopCleanly();
        shutdownComponents();
        Logger.info("Cytosis has shut down");
        return CompletableFuture.completedFuture(null);
    }

    private static void shutdownComponents() {
        Map<Class<?>, Object> components = Cytosis.CONTEXT.getComponents();
        components.values().stream().filter(component -> component instanceof Bootstrappable)
            .map(Bootstrappable.class::cast).forEach(Bootstrappable::shutdown);
    }
}
