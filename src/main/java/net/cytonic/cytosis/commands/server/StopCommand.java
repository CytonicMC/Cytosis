package net.cytonic.cytosis.commands.server;

import java.io.IOException;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

/**
 * A class representing the stop command
 */
public class StopCommand extends CytosisCommand {

    /**
     * A simple command to stop the server
     */
    public StopCommand() {
        super("stop");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            kill(player);
        });

        ArgumentInteger secondsArg = ArgumentType.Integer("seconds");
        addSyntax((s, ctx) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            int seconds = ctx.get(secondsArg);
            Cytosis.get(CytosisSettings.class).getServerConfig().setShutdownDuration(seconds);
            Cytosis.CONTEXT.setSlowShutdown(true);
            kill(player);
        }, secondsArg);
    }

    private void kill(CytosisPlayer player) {
        try {
            new ProcessBuilder("kill", "-INT", String.valueOf(ProcessHandle.current().pid()))
                .inheritIO()
                .start();
        } catch (IOException e) {
            player.sendMessage(Msg.serverError("An error occurred! %s", e.getMessage()));
        }
        player.sendMessage(Msg.success("Dispatched the shutdown of this server!"));
    }
}