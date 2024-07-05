package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;

import java.time.Duration;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * The class representing the kaboom command
 */
public class KaboomCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public KaboomCommand() {
        super("kaboom");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.kaboom"));
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof final Player player) {
                if (player.hasPermission("cytosis.commands.kaboom")) {
                    for (Player online : Cytosis.getOnlinePlayers()) {
                        // Send messsage
                        online.sendMessage(MM."<GREEN>Kaboom!");

                        // Strike lightning
                        Instance instance = Cytosis.getDefaultInstance();
                        Pos spawnPosition = online.getPosition();
                        Entity lightning = new Entity(EntityType.LIGHTNING_BOLT);
                        lightning.setInstance(instance, spawnPosition);
                        online.playSound(Sound.sound(SoundEvent.ENTITY_LIGHTNING_BOLT_IMPACT, Sound.Source.WEATHER, 5f, 1f));

                        // Remove lightning
                        Scheduler scheduler = MinecraftServer.getSchedulerManager();
                        scheduler.buildTask(() -> {
                            lightning.remove();
                        }).delay(Duration.ofSeconds(1)).schedule();

                        // Launch player
                        online.setVelocity(new Vec(0, 50, 0));
                    }
                }
            } else {
                sender.sendMessage(MM."<RED>Only players may execute this command!");
            }
        });
    }
}