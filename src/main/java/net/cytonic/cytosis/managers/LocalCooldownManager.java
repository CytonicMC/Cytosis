package net.cytonic.cytosis.managers;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Bootstrappable;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
public class LocalCooldownManager implements Bootstrappable {
    private final Map<Key, Instant> server = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Key, Instant>> personal = new ConcurrentHashMap<>();

    @Override
    public void init() {
        startExpiryTask();
    }

    public void startExpiryTask() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            new HashMap<>(server).forEach((key, instant) -> {
                if (instant.isAfter(Instant.now())) return;
                server.remove(key);
            });
            personal.forEach((u, map) -> new HashMap<>(map).forEach((key, i) -> {
                if (i.isAfter(Instant.now())) return;
                personal.get(u).remove(key);
            }));

            //todo: maybe make it more frequent?
        }).repeat(TaskSchedule.seconds(10)).schedule();
    }

    public void setGlobalCooldown(Key key, Instant instant) {
        server.put(key, instant);
    }

    public void setPersonalCooldown(UUID uuid, Key key, Instant instant) {
        personal.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(key, instant);
    }

    public boolean isOnServerCooldown(Key key) {
        if (!server.containsKey(key)) {
            return false;
        }
        Instant expire = server.get(key);
        if (expire.isAfter(Instant.now())) {
            return true;
        }
        server.remove(key);
        return false;
    }

    public boolean isOnPersonalCooldown(UUID uuid, Key key) {
        if (!personal.containsKey(uuid)) {
            return false;
        }
        if (!personal.get(uuid).containsKey(key)) {
            return false;
        }
        Instant expire = personal.get(uuid).get(key);
        if (expire.isAfter(Instant.now())) {
            return true;
        }
        personal.get(uuid).remove(key);
        return false;
    }

    public void resetPersonalCooldown(UUID uuid, Key id) {
        if (!personal.containsKey(uuid)) return;
        personal.get(uuid).remove(id);
    }

    public void resetServerCooldown(Key id) {
        if (!server.containsKey(id)) return;
        server.remove(id);
    }

    @Nullable
    public Instant getServerExpiry(Key id) {
        Instant expire = server.get(id);
        if (expire == null || expire.isBefore(Instant.now())) {
            server.remove(id);
            return null;
        }
        return expire;
    }

    @Nullable
    public Instant getPersonalExpiry(UUID uuid, Key id) {
        if (!personal.containsKey(uuid)) {
            return null;
        }
        Instant expire = personal.get(uuid).get(id);
        if (expire == null || expire.isBefore(Instant.now())) {
            personal.get(uuid).remove(id);
            return null;
        }
        return expire;
    }
}