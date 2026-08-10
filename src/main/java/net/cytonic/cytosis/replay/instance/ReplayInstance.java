package net.cytonic.cytosis.replay.instance;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemAnimation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Consumable;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket.CollisionRule;
import net.minestom.server.network.packet.server.play.TeamsPacket.CreateTeamAction;
import net.minestom.server.network.packet.server.play.TeamsPacket.NameTagVisibility;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.managers.ActionBarManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.replay.ReplayBundle;
import net.cytonic.cytosis.replay.ReplayEvent;
import net.cytonic.cytosis.replay.ReplayManager;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Players;
import net.cytonic.cytosis.world.AbstractWorld;

public class ReplayInstance extends AbstractWorld {


    private static final Consumable CONSUMABLE = new Consumable(Float.MAX_VALUE, ItemAnimation.NONE,
        SoundEvent.AMBIENT_UNDERWATER_LOOP, false, List.of());
    private static final DecimalFormat TIME_FORMAT = new DecimalFormat("0.#");
    public static final String TEAM_NAME = "replayteam";
    public static final int TRANSPOSAL_OFFSET = 8_000_000;
    /*
     * Instance tags (for thread safety)
     */
    public static final Tag<Map<Integer, UUID>> IDENTS_MAP = Tag.Transient("replay:idents");
    public static final Tag<Map<Integer, UUID>> UUID_MAP = Tag.Transient("replay:uuids");
    public static final Tag<Integer> CURRENT_TICK = Tag.Integer("replay:tick_index");
    public static final Tag<Integer> TICK_OFFSET = Tag.Integer("replay:tick_offset");
    public static final Tag<Integer> MAX_TICK = Tag.Integer("replay:max_ticks");
    public static final Tag<ScheduledFuture<?>> LOOP = Tag.Transient("replay:task");
    public static final Tag<PlaybackSpeed> PLAYBACK_SPEED = Tag.Transient("replay:speed");
    public static final Tag<Boolean> PAUSED = Tag.Boolean("replay:paused").defaultValue(true);
    public static final Tag<Boolean> PAUSE_BUTTON = Tag.Boolean("replay:pause_button").defaultValue(false);
    public static final Tag<Boolean> SKIP_FORWARD_BUTTON = Tag.Boolean("replay:skip_forward").defaultValue(false);
    public static final Tag<Boolean> SKIP_BACKWARD_BUTTON = Tag.Boolean("replay:skip_back").defaultValue(false);
    public static final Tag<Boolean> SPEED_BUTTON = Tag.Boolean("replay:leave_button").defaultValue(false);
    public static final Tag<Boolean> LEAVE_BUTTON = Tag.Boolean("replay:speed_button").defaultValue(false);
    public static final Tag<Boolean> FINISHED = Tag.Boolean("replay:finished").defaultValue(false);
    public static final Tag<ScheduledExecutorService> SCHEDULER = Tag.Transient("replay:scheduler");
    public static final Tag<Integer> EVENTS_INDEX = Tag.Integer("replay:event_index");
    public static final Tag<BossBar> BOSSBAR = Tag.Transient("replay:bossbar");
    public static final Tag<Boolean> CHAT_MESSAGS = Tag.Boolean("repaly:show_messages").defaultValue(true);
    public static final Tag<Boolean> COMMANDS = Tag.Boolean("repaly:show_commands").defaultValue(true);
    /*
     * Replay items
     */
    private final ItemStack LEAVE_ITEM = ItemStack.builder(Material.BARRIER)
        .customName(Msg.red("<b>Leave Replay")).set(DataComponents.CONSUMABLE, CONSUMABLE).set(LEAVE_BUTTON, true)
        .build();
    private final Supplier<ItemStack> PAUSE_ITEM = () -> ItemStack.builder(
            getTag(PAUSED) ? Material.CLOSED_EYEBLOSSOM : Material.OPEN_EYEBLOSSOM)
        .customName(Msg.mm("%s", getTag(PAUSED) ? "<green>Play" : "<red>Pause"))
        .set(DataComponents.CONSUMABLE, CONSUMABLE).set(PAUSE_BUTTON, true).build();
    private final ItemStack SKIP_FORWARD_ITEM = ItemStack.builder(Material.ARROW)
        .customName(Msg.yellow("Skip Forward"))
        .lore(Msg.mm(""), Msg.yellow("<red>'</red>\uD83D\uDDB0 Left Click to skip 30 seconds"),
            Msg.yellow("\uD83D\uDDB0<red>'</red> Right Click to skip 10 seconds"))
        .set(DataComponents.CONSUMABLE, CONSUMABLE)
        .set(SKIP_FORWARD_BUTTON, true).build();
    private final ItemStack SKIP_BACKWARD_ITEM = ItemStack.builder(Material.ARROW)
        .customName(Msg.yellow("Skip Backwards"))
        .lore(Msg.mm(""), Msg.yellow("<red>'</red>\uD83D\uDDB0 Left Click to skip 30 seconds"),
            Msg.yellow("\uD83D\uDDB0<red>'</red> Right Click to skip 10 seconds"))
        .set(DataComponents.CONSUMABLE, CONSUMABLE)
        .set(SKIP_BACKWARD_BUTTON, true).build();
    private final Supplier<ItemStack> SPEED_ITEM = () -> ItemStack.builder(Material.CLOCK)
        .customName(Msg.aqua("Change Speed"))
        .lore(Msg.mm(""), Msg.aqua("Current Speed: <gold>%s", getTag(PLAYBACK_SPEED).getDisplay()), Msg.mm(""),
            Msg.yellow("<red>'</red>\uD83D\uDDB0 Left Click to Decrease"),
            Msg.yellow("\uD83D\uDDB0<red>'</red> Right Click to Increase")).set(DataComponents.CONSUMABLE, CONSUMABLE)
        .set(SPEED_BUTTON, true).build();
    private final ReplayBundle replay;

    public ReplayInstance(ReplayBundle bundle) {
        super(UUID.randomUUID(), DimensionType.OVERWORLD, false);
        loadPolarWorld(bundle.meta().map()).join();
        replay = bundle;
        setTag(InstanceRecorder.RECORDING_EXEMPT, true); // don't record replays of recordings
        setTag(PAUSED, true);
        setTag(CURRENT_TICK, 0);
        setTag(TICK_OFFSET, bundle.events().getFirst().tick());
        setTag(MAX_TICK, bundle.events().getLast().tick());
        setTag(EVENTS_INDEX, 0);
        setTag(PLAYBACK_SPEED, PlaybackSpeed.ONE);
        setTag(IDENTS_MAP, bundle.idents());
        setTag(SCHEDULER, Executors.newSingleThreadScheduledExecutor());
        Map<Integer, UUID> uuids = new HashMap<>();
        bundle.idents().forEach((i, _) -> uuids.put(i, UUID.randomUUID()));
        setTag(UUID_MAP, uuids);
        setTag(BOSSBAR, BossBar.bossBar(Component.empty(), 0, Color.RED, Overlay.PROGRESS));

        schedule();
        updateBossBar();

        eventNode().addListener(PlayerSpawnEvent.class, e -> {
                if (!(e.getPlayer() instanceof CytosisPlayer player)) return;

                player.showBossBar(getTag(BOSSBAR));
                player.setAllowFlying(true);
                player.getInventory().clear();
                PlayerInventory inv = player.getInventory();
                inv.setItemStack(0, SPEED_ITEM.get());
                inv.setItemStack(3, SKIP_BACKWARD_ITEM);
                inv.setItemStack(4, PAUSE_ITEM.get());
                inv.setItemStack(5, SKIP_FORWARD_ITEM);
                inv.setItemStack(8, LEAVE_ITEM);
                player.sendPacket(new TeamsPacket(TEAM_NAME,
                    new CreateTeamAction(Msg.grey("REPLAY TEAM"), (byte) 0x00, NameTagVisibility.ALWAYS,
                        CollisionRule.PUSH_OWN_TEAM, NamedTextColor.GRAY, Msg.grey("「REPLAY」</gray>"), Component.empty(),
                        List.of())));
                Cytosis.get(ActionBarManager.class)
                    .addImmediate(player.getUuid(), Msg.green("Viewing Replay %s", bundle.uuid()), Integer.MAX_VALUE);
            }).addListener(PlayerBeginItemUseEvent.class, e -> {
                if (!(e.getPlayer() instanceof CytosisPlayer player)) return;
                if (e.getHand() != PlayerHand.MAIN) return;
                ItemStack i = e.getItemStack();
                if (i.getTag(PAUSE_BUTTON)) {
                    pause(player);
                }
                if (i.getTag(LEAVE_BUTTON)) {
                    leave(player);
                }
                if (i.getTag(SPEED_BUTTON)) {
                    increaseSpeed(player);
                }
                if (i.getTag(SKIP_FORWARD_BUTTON)) {
                    int ticks = Math.toIntExact(10_000 / getTag(PLAYBACK_SPEED).period);
                    skipForward(ticks);
                }
                if (i.getTag(SKIP_BACKWARD_BUTTON)) {
                    int ticks = Math.toIntExact(10_000 / getTag(PLAYBACK_SPEED).period);
                    skipBack(ticks);
                }
            })
            .addListener(ItemDropEvent.class, e -> e.setCancelled(true))
            .addListener(PlayerBlockBreakEvent.class, e -> e.setCancelled(true))
            .addListener(PlayerBlockPlaceEvent.class, e -> e.setCancelled(true))
            .addListener(InventoryPreClickEvent.class, e -> e.setCancelled(true))
            .addListener(PlayerHandAnimationEvent.class, e -> {
                if (!(e.getPlayer() instanceof CytosisPlayer player)) return;
                if (e.getHand() != PlayerHand.MAIN) return;
                ItemStack i = player.getItemInMainHand();
                if (i.getTag(SPEED_BUTTON)) {
                    decreaseSpeed(player);
                }
                if (i.getTag(PAUSE_BUTTON)) {
                    pause(player);
                }
                if (i.getTag(LEAVE_BUTTON)) {
                    leave(player);
                }
                if (i.getTag(SKIP_FORWARD_BUTTON)) {
                    int ticks = Math.toIntExact(30_000 / getTag(PLAYBACK_SPEED).period);
                    skipForward(ticks);
                }
                if (i.getTag(SKIP_BACKWARD_BUTTON)) {
                    int ticks = Math.toIntExact(30_000 / getTag(PLAYBACK_SPEED).period);
                    skipBack(ticks);
                }
            });
        Cytosis.get(InstanceManager.class).registerInstance(this);
        Cytosis.get(ReplayManager.class).registerPlayback(bundle.uuid(), getUuid());
    }

    private void pause(@Nullable CytosisPlayer player) {
        if (getTag(FINISHED)) {
            if (player == null) return;
            Sound sound = Sound.sound(SoundEvent.ENTITY_ENDERMAN_HURT, Sound.Source.PLAYER, .7f, 0.75F);
            player.playSound(sound);
            player.whoops("This replay has already finished!");
            return;
        }
        boolean newVal = !getTag(PAUSED);
        setTag(PAUSED, newVal);
        Component msg = newVal ? Msg.redSplash("PAUSED!", "Playback has been paused.")
            : Msg.greenSplash("PLAYING!", "Playback has been resumed.");
        Sound sound = Sound.sound(SoundEvent.ITEM_FLINTANDSTEEL_USE, Sound.Source.PLAYER, .7f, 0.8F);
        for (Player p : getPlayers()) {
            p.playSound(sound);
            p.sendMessage(msg);
            p.getInventory().setItemStack(4, PAUSE_ITEM.get());
        }
    }

    private void updateSpeed(PlaybackSpeed speed) {
        if (getTag(PLAYBACK_SPEED) == speed) return;
        setTag(PLAYBACK_SPEED, speed);
        ItemStack speedItem = SPEED_ITEM.get();
        Component msg = Msg.darkAquaSplash("UPDATED!", "The playback speed was updated to <gold>%s</gold>!",
            speed.getDisplay());
        Sound sound = Sound.sound(SoundEvent.BLOCK_LAVA_POP, Sound.Source.PLAYER, .7f, 1.0F);
        getPlayers().forEach(player -> {
            player.playSound(sound);
            player.getInventory().setItemStack(0, speedItem);
            player.sendMessage(msg);
        });
        schedule();
    }

    private void increaseSpeed(CytosisPlayer actor) {
        PlaybackSpeed current = getTag(PLAYBACK_SPEED);
        if (PlaybackSpeed.values().length == current.ordinal() + 1) {
            Sound sound = Sound.sound(SoundEvent.ENTITY_ENDERMAN_HURT, Sound.Source.PLAYER, .7f, 0.75F);
            actor.playSound(sound);
            actor.whoops("The playback can be at maximum <gold>%s</gold>", current.getDisplay());
            return;
        }
        updateSpeed(PlaybackSpeed.values()[current.ordinal() + 1]);
    }

    private void decreaseSpeed(CytosisPlayer actor) {
        PlaybackSpeed current = getTag(PLAYBACK_SPEED);
        if (current.ordinal() == 0) {
            Sound sound = Sound.sound(SoundEvent.ENTITY_ENDERMAN_HURT, Sound.Source.PLAYER, .7f, 0.75F);
            actor.playSound(sound);
            actor.whoops("The playback can be at minimum <gold>%s</gold>", current.getDisplay());
            return;
        }
        updateSpeed(PlaybackSpeed.values()[current.ordinal() - 1]);
    }

    private void updateBossBar() {
        BossBar bb = getTag(BOSSBAR);
        int current = getTag(CURRENT_TICK);
        int denominator = getTag(MAX_TICK) - getTag(TICK_OFFSET);

        double currentSeconds = current / 20.0;
        double totalSeconds = denominator / 20.0;

        boolean paused = getTag(PAUSED);
        String icon = paused ? "⏸" : "▶";
        String speed = getTag(PLAYBACK_SPEED).getDisplay();

        if (paused) {
            bb.color(Color.RED);
            bb.name(Msg.aqua("<red><b>%s</b></red> %s / %s <gray>(%s)", icon,
                formatTime(currentSeconds), formatTime(totalSeconds), speed));
        } else {
            bb.color(Color.GREEN);
            bb.name(Msg.aqua("<green><b>%s</b></green> %s / %s <gray>(%s)", icon,
                formatTime(currentSeconds), formatTime(totalSeconds), speed));
        }
        bb.progress(Math.clamp(current / (float) denominator, 0F, 1F));
    }

    private String formatTime(double seconds) {
        int mins = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", mins, secs);
    }

    private void cleanup() {
        for (Player player : getPlayers()) {
            player.hideBossBar(getTag(BOSSBAR));
            player.getInventory().clear();
            player.setInstance(Cytosis.get(InstanceContainer.class)).join();
        }
        getTag(LOOP).cancel(true);
        getTag(SCHEDULER).shutdownNow();
        scheduler().buildTask(() -> Cytosis.get(InstanceManager.class).unregisterInstance(this))
            .delay(TaskSchedule.seconds(5)).schedule();
        Cytosis.get(ReplayManager.class).removePlayback(replay.uuid());
    }

    /**
     * @return if the end was reached
     */
    private boolean playTick(boolean isSkip) {
        if (getPlayers().isEmpty()) {
            cleanup();
            return true;
        }
        updateBossBar();
        if (getTag(PAUSED) && !isSkip) return false;
        int currentTick = getTag(CURRENT_TICK);
        if (currentTick > getTag(MAX_TICK)) return true;

        int tick = getAndUpdateTag(CURRENT_TICK, i -> i + 1) + getTag(TICK_OFFSET);
        for (int i = getTag(EVENTS_INDEX); i < replay.events().size(); i++) {
            ReplayEvent e = replay.events().get(i);
            if (e.tick() > tick) {
                // we reached the end of the events this tick
                setTag(EVENTS_INDEX, i);
                return false;
            }
            e.play(this);
        }
        pause(null); // the end was reached
        setTag(FINISHED, true);
        return true;
    }

    /**
     * @return if the end was reached
     */
    private boolean playTickReverse() {
        if (getPlayers().isEmpty()) {
            cleanup();
            return true;
        }

        updateBossBar();
        int tick = getAndUpdateTag(CURRENT_TICK, i -> i - 1) + getTag(TICK_OFFSET);
        for (int i = getTag(EVENTS_INDEX); i >= 0; i--) {
            ReplayEvent e = replay.events().get(i);
            if (e.tick() < tick) {
                // we reached the end of the events this tick
                setTag(EVENTS_INDEX, i);
                return false;
            }
            ReplayEvent reverse = e.inverse();
            if (reverse != null) {
                reverse.play(this);
            }
        }
        pause(null); // the beginning was reached
        return true;
    }

    private void skipBack(int ticks) {
        int skipped = 0;
        for (int i = 0; i < ticks; i++) {
            skipped++;
            if (playTickReverse()) break;
        }
        if (skipped > 0) {
            setTag(FINISHED, false);
        }
        double secondsSkipped = getTag(PLAYBACK_SPEED).period * skipped / 1000.0;
        DecimalFormat df = new DecimalFormat("0.##");
        playSound(Sound.sound(SoundEvent.ENTITY_ENDERMAN_TELEPORT, Source.PLAYER, 1, 1));
        sendMessage(Msg.darkAquaSplash("«« SKIPPED", "Skipped %s seconds back in time!", df.format(secondsSkipped)));
    }

    private void skipForward(int ticks) {
        int skipped = 0;
        for (int i = 0; i < ticks; i++) {
            skipped++;
            if (playTick(true)) break;
        }
        double secondsSkipped = getTag(PLAYBACK_SPEED).period * skipped / 1000.0;
        DecimalFormat df = new DecimalFormat("0.##");
        playSound(Sound.sound(SoundEvent.ENTITY_ENDERMAN_TELEPORT, Source.PLAYER, 1, 1));
        sendMessage(Msg.darkAquaSplash("SKIPPED »»", "Skipped %s seconds forward in time!", df.format(secondsSkipped)));
    }

    private void schedule() {
        ScheduledExecutorService exec = getTag(SCHEDULER);

        ScheduledFuture<?> existing = getTag(LOOP);
        if (existing != null) {
            existing.cancel(false);
        }
        ScheduledFuture<?> task = exec.scheduleAtFixedRate(() -> playTick(false), 100,
            getTag(PLAYBACK_SPEED).getPeriod(),
            TimeUnit.MILLISECONDS);

        setTag(LOOP, task);
    }

    public void sendPackets(ServerPacket... packets) {
        PacketGroupingAudience aud = PacketGroupingAudience.of(getPlayers());
        for (ServerPacket packet : packets) {
            aud.sendGroupedPacket(packet);
        }
    }

    public void leave(CytosisPlayer player) {
        getTag(IDENTS_MAP).forEach((integer, _) -> {
            DestroyEntitiesPacket remove = new DestroyEntitiesPacket(TRANSPOSAL_OFFSET + integer);
            UUID fake = getTag(ReplayInstance.UUID_MAP).get(integer);
            PlayerInfoRemovePacket info = new PlayerInfoRemovePacket(fake);
            player.sendPackets(remove, info);
        });
        Sound sound = Sound.sound(SoundEvent.BLOCK_MOSS_BREAK, Sound.Source.PLAYER, 2.0f, 1.0F);
        player.playSound(sound);
        player.hideBossBar(getTag(BOSSBAR));
        player.getInventory().clear();
        player.setInstance(Cytosis.get(InstanceContainer.class));
        player.success("Left replay %s.", replay.uuid());
    }

    @NotNull
    public String mini(int entityId) {
        UUID target = getTag(IDENTS_MAP).getOrDefault(entityId, null);
        if (target == null) return "<pink>Unknown player</pink>";
        return Players.trueMiniName(target);
    }

    @AllArgsConstructor
    @Getter
    public enum PlaybackSpeed {
        ONE_TENTH(500, "0.1x"), ONE_QUARTER(200, "0.25x"), ONE_HALF(100, "0.5x"), ONE(50, "1x"), ONE_AND_HALF(33,
            "1.5x"), TWO(25, "2x"), THREE(17, "3x"), FIVE(10, "5x"), TEN(5, "10x");

        private final long period;
        private final String display;
    }
}
