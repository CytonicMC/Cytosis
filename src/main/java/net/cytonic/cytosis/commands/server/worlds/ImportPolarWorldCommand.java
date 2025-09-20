package net.cytonic.cytosis.commands.server.worlds;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.DatabaseManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarWorld;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.timer.TaskSchedule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ImportPolarWorldCommand extends CytosisCommand {
    public ImportPolarWorldCommand() {
        super("polar");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.whoops("Usage: /importworld polar <path/to/polar/world> <name> [type]")));

        ArgumentWord path = new ArgumentWord("path");
        ArgumentWord name = new ArgumentWord("name");
        ArgumentWord type = new ArgumentWord("type");
        type.setDefaultValue("polar-imported");
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Path readPath = Path.of(context.get(path));
            if (!(Files.exists(readPath) && Files.isReadable(readPath))) {
                sender.sendMessage(Msg.whoops("The path you specified does not exist!"));
                return;
            }
            if (readPath.toFile().isDirectory()) {
                sender.sendMessage(Msg.whoops("The path you specified is a directory!"));
                return;
            }
            PolarLoader loader = null;
            try {
                loader = new PolarLoader(readPath);
            } catch (IOException e) {
                Logger.error("Failed to load world!", e);
                player.sendMessage(Msg.serverError("Failed to load world! (%s)", e.getMessage()));
            }
            InstanceManager instanceManager = Cytosis.CONTEXT.getComponent(InstanceManager.class);
            InstanceContainer c = instanceManager.createInstanceContainer(loader);
            player.setInstance(c);
            if (loader == null) {
                player.sendMessage(Msg.serverError("Failed to load world! World is null!"));
                return;
            }
            PolarWorld world = loader.world();

            Logger.debug(world.userData().length + " bytes of user data serialized for world '%s'", context.get(name).replace("_", ""));

            UUID uuid = UUID.randomUUID();
            Cytosis.CONTEXT.getComponent(DatabaseManager.class).getMysqlDatabase().addWorld(
                    context.get(name),
                    context.get(type),
                    world,
                    Pos.ZERO,
                    uuid
            ).whenComplete((result, error) -> {
                MinecraftServer.getSchedulerManager().buildTask(() -> instanceManager.unregisterInstance(c)).delay(TaskSchedule.seconds(1)).schedule();
                sender.sendMessage(Msg.success("Successfully imported world '%s'. UUID: %s", context.get(name).replace("_", ""), uuid.toString()));
            });

        }, path, name, type);
    }
}
