package net.cytonic.cytosis.commands.debug.particles;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.particles.ParticleEngine;
import net.cytonic.cytosis.particles.effects.keyframed.BezierCurveEffect;
import net.cytonic.cytosis.particles.effects.keyframed.BridgingStrategy;
import net.cytonic.cytosis.particles.effects.keyframed.EasingFunction;
import net.cytonic.cytosis.particles.util.ParticleData;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.particle.Particle;

public class BezierCommand extends CytosisCommand {
    public BezierCommand() {
        super("bezier");
        setDefaultExecutor((sender, ignored) -> {
            BezierCurveEffect bezierCurveEffect = new BezierCurveEffect(100, 20,
                    EasingFunction.EXPONENTIAL, 200, BridgingStrategy.line(10), () -> ParticleData.simple(Particle.FLAME),
                    new Pos(-5, 120, 0), new Pos(0, 130, 0), new Pos(5, 120, 0)
            );
            ParticleEngine.playKeyframed(bezierCurveEffect);

            BezierCurveEffect bezierCurveEffect2 = new BezierCurveEffect(100, 20,
                    EasingFunction.LINEAR, 200, BridgingStrategy.line(10), () -> ParticleData.simple(Particle.FLAME),
                    new Pos(-5, 125, 0), new Pos(0, 135, 0), new Pos(5, 125, 0)
            );
            ParticleEngine.playKeyframed(bezierCurveEffect2);
        });
    }
}
