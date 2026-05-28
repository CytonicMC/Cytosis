package net.cytonic.cytosis.nicknames;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.google.gson.JsonParser;
import dev.minestomunited.entrypoint.codec.ExtraCodecs;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.entity.PlayerSkin;

import net.cytonic.cytosis.Cytosis;

public class SkinParser {

    public static List<PlayerSkin> parseSkinData() {
        try (InputStream stream = Cytosis.class.getResourceAsStream("/skins.json")) {
            if (stream == null) throw new IllegalStateException("Skins file not found");

            return ExtraCodecs.PLAYER_SKIN.list().decode(Transcoder.JSON,
                    JsonParser.parseReader(new InputStreamReader(stream)))
                .orElseThrow("Failed to decode skins file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
