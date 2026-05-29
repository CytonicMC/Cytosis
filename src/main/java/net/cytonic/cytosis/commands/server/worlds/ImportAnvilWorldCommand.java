package net.cytonic.cytosis.commands.server.worlds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.hollowcube.polar.AnvilPolar;
import net.hollowcube.polar.PolarWorld;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentResourceLocation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.environments.Environment;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.InstanceManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class ImportAnvilWorldCommand extends CytosisCommand {

    public ImportAnvilWorldCommand() {
        super("anvil");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(
            Msg.whoops("Usage: /importworld anvil \"<path/to/world/folder>\" <key>")));

        ArgumentWord path = new ArgumentWord("path");
        ArgumentResourceLocation keyArgument = new ArgumentResourceLocation("key");
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer)) return;
            Path readPath = Path.of(context.get(path));
            if (!(Files.exists(readPath) && Files.isReadable(readPath))) {
                sender.sendMessage(Msg.whoops("The path you specified does not exist!"));
                return;
            }
            if (!readPath.toFile().isDirectory()) {
                sender.sendMessage(Msg.whoops("The path you specified is not a directory!"));
                return;
            }

            PolarWorld world;
            try {
                world = AnvilPolar.anvilToPolar(readPath);
            } catch (IOException e) {
                sender.sendMessage(Msg.serverError("Failed to convert world! (%s)", e.getMessage()));
                Logger.error("Failed to convert world!", e);
                return;
            }

            Key key = context.get(keyArgument);
            Cytosis.get(InstanceManager.class).saveWorld(key, world).whenComplete((_, throwable) -> {
                if (throwable != null) {
                    sender.sendMessage(Msg.whoops("An error occurred! " + throwable.getMessage()));
                    Logger.error("An error occurred!", throwable);
                    return;
                }
                sender.sendMessage(
                    Msg.success("Successfully imported world '%s' into %s MinIO!",
                        context.get(keyArgument).asString(), Cytosis.get(Environment.class)));
            });
        }, path, keyArgument);
    }
}
