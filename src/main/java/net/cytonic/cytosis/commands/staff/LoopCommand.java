package net.cytonic.cytosis.commands.staff;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.timer.TaskSchedule;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A command that loops other commands
 */
public class LoopCommand extends Command {
    /**
     * A constructor for the loop command.
     */
    public LoopCommand() {
        super("loop");

        var iterationsArg = ArgumentType.Integer("iterations");
        var periodArg = ArgumentType.Integer("period");
        var commandArg = ArgumentType.StringArray("command").setDefaultValue(new String[0]);

        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.staff.loop"));

        setDefaultExecutor((commandSender, _) -> {
            if (!(commandSender instanceof CytosisPlayer player)) {
                commandSender.sendMessage("Only players can use this command.");
                return;
            }
            player.sendMessage(MM."<red><b>WHOOPS!</b></red> <gray>Invalid syntax! Usage: /loop <iterations> <period> [command....]");
        });

        addSyntax((commandSender, context) -> {
            if (!(commandSender instanceof CytosisPlayer player)) {
                commandSender.sendMessage("Only players can use this command.");
                return;
            }

            int iterations = context.get(iterationsArg);
            int period = context.get(periodArg);
            String[] command = context.get(commandArg);

            if (command.length == 0) {
                player.sendMessage(MM."<red><b>WHOOPS!</b></red> <gray>You need to specify a command to loop!");
            }

            String commandStr = String.join(" ", command);

            // don't schedule
            if (period == 0) {
                for (int i = 1; i <= iterations; i++) {
                    player.dispatchCommand(commandStr.replace("%i%", String.valueOf(i)));
                }
            } else {
                for (int i = 1; i <= iterations; i++) {
                    int finalI = i;
                    MinecraftServer.getSchedulerManager().buildTask(() -> player.dispatchCommand(commandStr.replace("%i%", String.valueOf(finalI)))).delay(TaskSchedule.tick(i * period)).schedule();
                }
            }

        }, iterationsArg, periodArg, commandArg);
    }
}
