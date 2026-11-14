package net.cytonic.cytosis.replay.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

import com.github.luben.zstd.Zstd;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNullByDefault;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.GarageManager;
import net.cytonic.cytosis.replay.ReplayBundle;

@CytosisComponent(dependsOn = GarageManager.class)
@NotNullByDefault
public class BundleIO {

    public void writeToFile(Path file, ReplayBundle bundle) {
        if (file.toFile().exists()) {
            throw new IllegalArgumentException("File already exists!");
        }
        try {
            Files.write(file, write(bundle), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Void> writeFileToGarage(ReplayBundle bundle) {
        GarageManager gm = Cytosis.get(GarageManager.class);
        return gm.uploadObject("recordings", bundle.uuid() + ".cytosisrecording", write(bundle));
    }

    private byte[] write(ReplayBundle bundle) {
        byte[] raw = NetworkBuffer.makeArray(ReplayBundle.NETWORK_TYPE, bundle);
        byte[] compressed = Zstd.compress(raw, Zstd.maxCompressionLevel());
        int saved = raw.length - compressed.length;
        Logger.info("Saved %d bytes via compression of a replay bundle. That is %.2f percent!", saved,
            100 * ((double) saved / (double) raw.length));
        return compressed;
    }

}
