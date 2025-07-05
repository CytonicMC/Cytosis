package net.cytonic.cytosis.commands.debug.particles;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

public class ParticleCommand extends CytosisCommand {
    public ParticleCommand() {
        super("particle");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.whoops("You have to specify a sub command!")));
        addSubcommand(new BezierCommand());
        addSubcommand(new PatternedCommand());
        addSubcommand(new CircleCommand());
    }
}
