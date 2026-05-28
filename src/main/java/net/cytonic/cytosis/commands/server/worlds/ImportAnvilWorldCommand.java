//todo
//package net.cytonic.cytosis.commands.server.worlds;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//import net.hollowcube.polar.AnvilPolar;
//import net.hollowcube.polar.PolarLoader;
//import net.hollowcube.polar.PolarWorld;
//import net.hollowcube.polar.PolarWriter;
//import net.minestom.server.command.builder.arguments.ArgumentWord;
//import net.minestom.server.instance.InstanceContainer;
//import net.minestom.server.instance.InstanceManager;
//import net.minestom.server.instance.anvil.AnvilLoader;
//
//import net.cytonic.cytosis.Cytosis;
//import net.cytonic.cytosis.commands.utils.CommandUtils;
//import net.cytonic.cytosis.commands.utils.CytosisCommand;
//import net.cytonic.cytosis.logging.Logger;
//import net.cytonic.cytosis.player.CytosisPlayer;
//import net.cytonic.cytosis.utils.Msg;
//import net.cytonic.cytosis.utils.polar.EntityAnvilLoader;
//import net.cytonic.cytosis.utils.polar.PolarExtension;
//
//public class ImportAnvilWorldCommand extends CytosisCommand {
//
//    public ImportAnvilWorldCommand() {
//        super("anvil");
//        setCondition(CommandUtils.IS_ADMIN);
//        setDefaultExecutor((sender, ignored) -> sender.sendMessage(
//            Msg.whoops("Usage: /importworld anvil \"<path/to/world/folder>\" <name> [type]")));
//
//        ArgumentWord path = new ArgumentWord("path");
//        ArgumentWord outputPathArg = new ArgumentWord("output");
//        addSyntax((sender, context) -> {
//            if (!(sender instanceof CytosisPlayer player)) return;
//            Path readPath = Path.of(context.get(path));
//            if (!(Files.exists(readPath) && Files.isReadable(readPath))) {
//                sender.sendMessage(Msg.whoops("The path you specified does not exist!"));
//                return;
//            }
//            if (!readPath.toFile().isDirectory()) {
//                sender.sendMessage(Msg.whoops("The path you specified is not a directory!"));
//                return;
//            }
//            Path outputPath = Path.of(context.get(outputPathArg));
//            if (Files.exists(outputPath)) {
//                sender.sendMessage(Msg.whoops("The path you specified is already exists!"));
//                return;
//            }
//
//            AnvilLoader loader = new EntityAnvilLoader(readPath);
//            InstanceManager instanceManager = Cytosis.get(InstanceManager.class);
//            InstanceContainer c = instanceManager.createInstanceContainer(loader);
//            player.setInstance(c);
//
//            PolarWorld world;
//            try {
//                world = AnvilPolar.anvilToPolar(readPath);
//            } catch (IOException e) {
//                sender.sendMessage(Msg.serverError("Failed to convert world! (%s)", e.getMessage()));
//                Logger.error("Failed to convert world!", e);
//                return;
//            }
//
//            PolarLoader polarLoader = new PolarLoader(world);
//            polarLoader.setWorldAccess(new PolarExtension());
//            polarLoader.saveInstance(c);
//
//            try {
//                Files.write(outputPath, PolarWriter.write(world));
//            } catch (IOException e) {
//                sender.sendMessage(Msg.serverError("Failed to save world! (%s)", e.getMessage()));
//                Logger.error("Failed to save world!", e);
//                return;
//            }
//
//            sender.sendMessage(Msg.success("Successfully converted world to polar"));
//        }, path, outputPathArg);
//    }
//}
