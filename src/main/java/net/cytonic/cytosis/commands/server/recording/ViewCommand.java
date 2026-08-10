package net.cytonic.cytosis.commands.server.recording;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.UUID;

import com.github.luben.zstd.Zstd;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.NetworkBuffer;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.GarageManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.replay.ReplayBundle;
import net.cytonic.cytosis.replay.ReplayManager;
import net.cytonic.cytosis.replay.instance.ReplayInstance;
import net.cytonic.cytosis.utils.Msg;

class ViewCommand extends CytosisCommand {

    ViewCommand() {
        super("view");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            player.whoops("You must specify a replay ID or file path!");
        });

        ArgumentUUID uuidArg = ArgumentType.UUID("replay-id");
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            UUID uuid = context.get(uuidArg);
            ReplayManager rm = Cytosis.get(ReplayManager.class);
            // fetch bytes from garage
            Cytosis.get(GarageManager.class).downloadObject("recordings", uuid + ".cytosisrecording")
                .thenAccept(bytes -> {
                    if (bytes == null) {
                        player.error("Failed to download replay from garage.");
                        return;
                    }
                    if (rm.isPlaying(uuid)) {
                        player.sendMessage(
                            Msg.pinkSplash("INFO", "This replay is already loaded and actively in playback.")
                                .append(Msg.aqua("<b>[GO THERE]")
                                    .clickEvent(ClickEvent.callback(_ -> player.setInstance(rm.getPlayback(uuid))))
                                    .hoverEvent(HoverEvent.showText(Msg.grey("Adds you to the ongoing replay."))))
                                .append(Msg.blue("<b>[LOAD IT FRESH]")
                                    .clickEvent(ClickEvent.callback(_ -> playReplay(bytes, player)))
                                    .hoverEvent(HoverEvent.showText(Msg.grey("Loads your own copy of this replay.")))));
                        return;
                    }
                    playReplay(bytes, player);
                })
                .exceptionally(throwable -> {
                    Logger.error("Failed to download replay recording " + uuid, throwable);
                    return null;
                });
        }, uuidArg);

        ArgumentWord pathArg = ArgumentType.Word("path-to-file");
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Path path;
            try {
                path = Path.of("recordings/" + context.get(pathArg) + ".cytosisrecording");
            } catch (InvalidPathException e) {
                player.whoops("Invalid file path!");
                return;
            }

            Thread.ofVirtual().uncaughtExceptionHandler((_, e) -> {
                player.error("Error loading replay!");
                Logger.error("Failed to load replay: ", e);
            }).start(() -> {
                byte[] bytes;
                try {
                    bytes = Files.readAllBytes(path);
                } catch (IOException e) {
                    player.error("Failed to read file!");
                    Logger.error("Failed to read replay file!", e);
                    return;
                }
                playReplay(bytes, player);
            });
        }, pathArg);
    }

    void playReplay(byte[] raw, CytosisPlayer player) {
        try {
            player.sendMessage(Msg.grey("<i>Data loaded. Decompressing..."));
            byte[] inflated = Zstd.decompress(raw);
            NetworkBuffer buf = NetworkBuffer.wrap(inflated, 0, inflated.length);
            ReplayBundle bundle = buf.read(ReplayBundle.NETWORK_TYPE);
            player.success("Replay loaded!");
            Instance i = new ReplayInstance(bundle);
            player.setInstance(i);
            player.network("Entering replay: %s", bundle.uuid());
        } catch (Exception e) {
            player.error("Failed to load replay! <red>%s</red>", e.getMessage());
            Logger.error("Caught exception loading replay: ", e);
        }
    }

}
