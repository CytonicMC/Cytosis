package net.cytonic.cytosis.commands.server.worlds;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.hollowcube.polar.AnvilPolar;
import net.hollowcube.polar.PolarWorld;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.timer.TaskSchedule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ImportAnvilWorldCommand extends CytosisCommand {
    public ImportAnvilWorldCommand() {
        super("anvil");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.whoops("Usage: /importworld anvil \"<path/to/world/folder>\" <name> [type]")));

        ArgumentWord path = new ArgumentWord("path");
        ArgumentWord name = new ArgumentWord("name");
        ArgumentWord type = new ArgumentWord("type");
        type.setDefaultValue("anvil-imported");
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Path readPath = Path.of(context.get(path));
            ;
            if (!(Files.exists(readPath) && Files.isReadable(readPath))) {
                sender.sendMessage(Msg.whoops("The path you specified does not exist!"));
                return;
            }
            if (!readPath.toFile().isDirectory()) {
                sender.sendMessage(Msg.whoops("The path you specified is not a directory!"));
                return;
            }

            AnvilLoader loader = new AnvilLoader(readPath);
            InstanceContainer c = Cytosis.getMinestomInstanceManager().createInstanceContainer(loader);
            player.setInstance(c);


            PolarWorld world;
            try {
                world = AnvilPolar.anvilToPolar(readPath);
            } catch (IOException e) {
                sender.sendMessage(Msg.serverError("Failed to convert world! (%s)", e.getMessage()));
                Logger.error("Failed to convert world!", e);
                return;
            }

            Logger.debug(world.userData().length + " bytes of user data serialized for world '%s'", context.get(name).replace("_", ""));

            UUID uuid = UUID.randomUUID();
            Cytosis.getDatabaseManager().getMysqlDatabase().addWorld(
                    context.get(name),
                    context.get(type),
                    world,
                    Pos.ZERO,
                    uuid
            ).whenComplete((result, error) -> {
                MinecraftServer.getSchedulerManager().buildTask(() -> Cytosis.getMinestomInstanceManager().unregisterInstance(c)).delay(TaskSchedule.seconds(1)).schedule();
                sender.sendMessage(Msg.success("Successfully imported world '%s'. UUID: %s", context.get(name).replace("_", ""), uuid.toString()));
            });

        }, path, name, type);
    }
}
