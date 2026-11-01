package net.cytonic.cytosis.commands.debug.particles;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.particles.ParticleEngine;
import net.cytonic.cytosis.player.CytosisPlayer;

@SubCommand(ParticleCommand.class)
class CreateEngineCommand extends CytosisCommand {

    CreateEngineCommand() {
        super("createengine");
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            if (player.getInstance().hasTag(ParticleEngine.TAG)) {
                player.whoops("Your instance already has a Particle Engine!");
                return;
            }
            player.getInstance().setTag(ParticleEngine.TAG, new ParticleEngine(player.getInstance()));
            player.success("A particle engine has been created for your instance!");
        });
    }
}
