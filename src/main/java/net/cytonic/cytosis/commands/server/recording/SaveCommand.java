package net.cytonic.cytosis.commands.server.recording;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.replay.ReplayBundle;
import net.cytonic.cytosis.replay.instance.InstanceRecorder;
import net.cytonic.cytosis.replay.io.BundleIO;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;

class SaveCommand extends CytosisCommand {

    SaveCommand() {
        super("save");
        ArgumentWord a = ArgumentType.Word("duration");
        addSubcommands(new GarageCommand(a), new LocalCommand(a));
    }

    @Nullable
    private ReplayBundle extractBundle(ArgumentWord arg, CommandContext ctx, CytosisPlayer player) {
        String rawDur = ctx.get(arg);
        Duration dur = DurationParser.parse(rawDur);
        if (dur == null || dur.equals(Duration.ZERO)) {
            player.whoops("%s is not a valid duration.", rawDur);
            return null;
        }
        if (dur.compareTo(Duration.ofSeconds(30)) < 0) {
            player.whoops("The minimum duration for a recording is 30 seconds.");
            return null;
        }
        if (!player.getInstance().hasTag(InstanceRecorder.RECORDER)) {
            player.whoops("Your instance is not recording.");
            return null;
        }
        InstanceRecorder ir = player.getInstance().getTag(InstanceRecorder.RECORDER);
        ir.flushBuffers();
        ReplayBundle bundle = ir.flush(Instant.now(), dur);
        player.sendMessage(Msg.grey("<i>Bundle successfully aggregated & normalized!"));
        return bundle;
    }

    class GarageCommand extends CytosisCommand {

        GarageCommand(ArgumentWord durArg) {
            super("garage");
            addSyntax((sender, context) -> {
                if (!(sender instanceof CytosisPlayer player)) return;
                ReplayBundle bundle = extractBundle(durArg, context, player);
                if (bundle == null) return;

                Cytosis.get(BundleIO.class).writeFileToGarage(bundle)
                    .thenAccept(_ -> player.success(
                        "<click:suggest_command:/recording view %s>Recording saved! <gray>UUID:%s. Click to view!",
                        bundle.uuid(), bundle.uuid()))
                    .exceptionally(e -> {
                        player.error("Failed to save your recording! <red>(%s)", e.getMessage());
                        Logger.error("Failed to save recording in Garage: ", e);
                        return null;
                    });
            }, durArg);
        }
    }

    class LocalCommand extends CytosisCommand {

        LocalCommand(ArgumentWord durArg) {
            super("local");
            ArgumentWord nameArg = ArgumentType.Word("recording-name");
            nameArg.setDefaultValue(() -> Instant.now().toString());
            addSyntax((sender, context) -> {
                if (!(sender instanceof CytosisPlayer player)) return;
                ReplayBundle bundle = extractBundle(durArg, context, player);
                if (bundle == null) return;
                Path p;
                try {
                    p = Path.of("recordings/" + context.get(nameArg) + ".cytosisreplay");
                } catch (InvalidPathException e) {
                    player.whoops("Invalid file name. (Contains illegal path characters)");
                    return;
                }
                try {
                    Cytosis.get(BundleIO.class).writeToFile(p, bundle);
                } catch (Exception e) {
                    player.error("Failed to save your recording! <red>(%s)", e.getMessage());
                    Logger.error("Failed to save recording: ", e);
                    return;
                }
                player.success("<click:suggest_command:/recording view %s>Recording saved! Click here to view it!",
                    context.get(nameArg));
            }, durArg, nameArg);
        }
    }
}
