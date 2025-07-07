package net.cytonic.cytosis.commands.debug.particles;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.particles.ParticleEngine;
import net.cytonic.cytosis.particles.effects.fixed.PatternedEffect;
import net.cytonic.cytosis.particles.util.ParticleData;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.particle.Particle;

import java.util.Map;

public class PatternedCommand extends CytosisCommand {
    public PatternedCommand() {
        super("patterned");
        setDefaultExecutor((sender, ignored) -> {
            PatternedEffect patterned1 = new PatternedEffect(.2, .2, new char[][]{
                    new char[]{' ', ' ', ' ', ' ', ' ', ' ', ' '},
                    new char[]{' ', 'X', ' ', 'X', ' ', '#', ' '},
                    new char[]{' ', '#', ' ', 'X', ' ', ' ', ' '},
                    new char[]{' ', '#', 'X', '#', ' ', 'X', ' '},
                    new char[]{' ', 'X', ' ', '#', ' ', '#', ' '},
                    new char[]{' ', '#', ' ', 'X', ' ', '#', ' '},
                    new char[]{' ', ' ', ' ', ' ', ' ', ' ', ' '},
            }, Map.of(
                    'X', () -> ParticleData.simple(Particle.END_ROD),
                    '#', () -> ParticleData.simple(Particle.FLAME)
            ), new Pos(44, 88, -31)
            );
            ParticleEngine.playStatic(patterned1);
        });
    }
}
