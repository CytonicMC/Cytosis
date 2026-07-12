package net.cytonic.cytosis.commands.debug.particles;

import java.util.Map;

import net.minestom.server.particle.Particle;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.particles.ParticleEngine;
import net.cytonic.cytosis.particles.effects.fixed.PatternedEffect;
import net.cytonic.cytosis.particles.util.ParticleData;
import net.cytonic.cytosis.player.CytosisPlayer;

public class PatternedCommand extends CytosisCommand {

    public PatternedCommand() {
        super("patterned");
        setDefaultExecutor((sender, ignored) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            ParticleEngine engine = player.getInstance().getTag(ParticleEngine.TAG);
            if (engine == null) {
                player.whoops("Your instance does not have a particle engine!");
                return;
            }
            PatternedEffect patterned1 = new PatternedEffect(.2, .2,
                new char[][]{new char[]{' ', ' ', ' ', ' ', ' ', ' ', ' '},
                    new char[]{' ', 'X', ' ', 'X', ' ', '#', ' '}, new char[]{' ', '#', ' ', 'X', ' ', ' ', ' '},
                    new char[]{' ', '#', 'X', '#', ' ', 'X', ' '}, new char[]{' ', 'X', ' ', '#', ' ', '#', ' '},
                    new char[]{' ', '#', ' ', 'X', ' ', '#', ' '}, new char[]{' ', ' ', ' ', ' ', ' ', ' ', ' '},
                },
                Map.of('X', () -> ParticleData.simple(Particle.END_ROD), '#',
                    () -> ParticleData.simple(Particle.FLAME)),
                player.getPosition().direction().normalize().mul(5).add(player.getPosition()).asPos());
            engine.playStatic(patterned1);
        });
    }
}
