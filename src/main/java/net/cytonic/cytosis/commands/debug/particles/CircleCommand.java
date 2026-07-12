package net.cytonic.cytosis.commands.debug.particles;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.particle.Particle;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.particles.ParticleEngine;
import net.cytonic.cytosis.particles.effects.keyframed.BridgingStrategy;
import net.cytonic.cytosis.particles.effects.looping.Angles;
import net.cytonic.cytosis.particles.effects.looping.CircularLoopingEffect;
import net.cytonic.cytosis.particles.effects.looping.Phase;
import net.cytonic.cytosis.particles.util.ParticleData;
import net.cytonic.cytosis.player.CytosisPlayer;

public class CircleCommand extends CytosisCommand {

    public CircleCommand() {
        super("circle");
        setDefaultExecutor((sender, ignored) -> {
            if (!(sender instanceof CytosisPlayer p)) return;
            ParticleEngine engine = p.getInstance().getTag(ParticleEngine.TAG);
            if (engine == null) {
                p.whoops("Your instance does not have a particle engine!");
                return;
            }
            CircularLoopingEffect c1 = new CircularLoopingEffect(p::getPosition, 16, Phase.TWO,
                _ -> new Vec(0, 2.5, 0), _ -> .7, () -> ParticleData.simple(Particle.TOTEM_OF_UNDYING),
                BridgingStrategy.midpoint(), 0);
            CircularLoopingEffect c2 = new CircularLoopingEffect(p::getPosition, 16, Phase.FOUR,
                _ -> new Vec(0, 2.5, 0), _ -> .7, () -> ParticleData.simple(Particle.TOTEM_OF_UNDYING),
                BridgingStrategy.midpoint(), Angles.NINETY);
            CircularLoopingEffect c3 = new CircularLoopingEffect(p::getPosition, 16, Phase.TWO,
                _ -> new Vec(0, 2.5, 0), _ -> .7, () -> ParticleData.simple(Particle.TOTEM_OF_UNDYING),
                BridgingStrategy.midpoint(), Angles.NINETY);
            CircularLoopingEffect c4 = new CircularLoopingEffect(p::getPosition, 16, Phase.FOUR,
                _ -> new Vec(0, 2.5, 0), _ -> .7, () -> ParticleData.simple(Particle.TOTEM_OF_UNDYING),
                BridgingStrategy.midpoint(), 0);
            engine.playLooping(c1, 100);
            engine.playLooping(c2, 100);
            engine.playLooping(c3, 100);
            engine.playLooping(c4, 100);
        });
    }
}
