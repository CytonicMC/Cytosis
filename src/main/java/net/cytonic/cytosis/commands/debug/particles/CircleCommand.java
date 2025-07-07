package net.cytonic.cytosis.commands.debug.particles;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.particles.ParticleEngine;
import net.cytonic.cytosis.particles.effects.keyframed.BridgingStrategy;
import net.cytonic.cytosis.particles.effects.looping.Angles;
import net.cytonic.cytosis.particles.effects.looping.CircularLoopingEffect;
import net.cytonic.cytosis.particles.effects.looping.Phase;
import net.cytonic.cytosis.particles.util.ParticleData;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

public class CircleCommand extends CytosisCommand {
    public CircleCommand() {
        super("circle");
        setDefaultExecutor((sender, ignored) -> {
            if (!(sender instanceof CytosisPlayer p)) return;
            CircularLoopingEffect c1 = new CircularLoopingEffect(p::getPosition, 16, Phase.TWO,
                    angle -> new Vec(0, 2.5, 0),
                    rot -> .7, () -> ParticleData.simple(Particle.TOTEM_OF_UNDYING), BridgingStrategy.midpoint(), 0);
            CircularLoopingEffect c2 = new CircularLoopingEffect(p::getPosition, 16, Phase.FOUR,
                    angle -> new Vec(0, 2.5, 0),
                    rot -> .7, () -> ParticleData.simple(Particle.TOTEM_OF_UNDYING), BridgingStrategy.midpoint(), Angles.NINETY);
            CircularLoopingEffect c3 = new CircularLoopingEffect(p::getPosition, 16, Phase.TWO,
                    angle -> new Vec(0, 2.5, 0),
                    rot -> .7, () -> ParticleData.simple(Particle.TOTEM_OF_UNDYING), BridgingStrategy.midpoint(), Angles.NINETY);
            CircularLoopingEffect c4 = new CircularLoopingEffect(p::getPosition, 16, Phase.FOUR,
                    angle -> new Vec(0, 2.5, 0),
                    rot -> .7, () -> ParticleData.simple(Particle.TOTEM_OF_UNDYING), BridgingStrategy.midpoint(), 0);
            ParticleEngine.playLooping(c1, TaskSchedule.tick(3));
            ParticleEngine.playLooping(c2, TaskSchedule.tick(3));
            ParticleEngine.playLooping(c3, TaskSchedule.tick(3));
            ParticleEngine.playLooping(c4, TaskSchedule.tick(3));
        });
    }
}
