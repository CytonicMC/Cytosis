package net.cytonic.cytosis.commands.server.worlds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarWorld;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentResourceLocation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.environments.Environment;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.WorldManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class ImportPolarWorldCommand extends CytosisCommand {

    public ImportPolarWorldCommand() {
        super("polar");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(
            Msg.whoops("Usage: /importworld polar <path/to/polar/world> <name> [type]")));

        ArgumentWord path = new ArgumentWord("path");
        ArgumentResourceLocation keyArgument = new ArgumentResourceLocation("key");

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
            PolarLoader loader;
            try {
                loader = new PolarLoader(readPath);
            } catch (IOException e) {
                Logger.error("Failed to load world!", e);
                player.sendMessage(Msg.serverError("Failed to load world! (%s)", e.getMessage()));
                return;
            }

            PolarWorld world = loader.world();

            Key key = context.get(keyArgument);
            Cytosis.get(WorldManager.class).saveWorld(key, world)
                .whenComplete((_, throwable) -> {
                    if (throwable != null) {
                        sender.sendMessage(Msg.whoops("An error occurred! " + throwable.getMessage()));
                        Logger.error("An error occurred!", throwable);
                        return;
                    }
                    sender.sendMessage(
                        Msg.success("Successfully imported world '%s' into %s Garage!",
                            context.get(keyArgument).asString(), Cytosis.get(Environment.class)));
                });
        }, path, keyArgument);
    }
}
