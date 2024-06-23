package net.cytonic.cytosis.data.objects;

import net.cytonic.cytosis.Cytosis;
import java.util.UUID;

public record PlayerServer(String playerName, UUID playerUUID, CytonicServer server) {

    public static PlayerServer deserialize(String serialized) {
        //<PLAYER_NAME>|:|<PLAYER_UUID>|:|<OLD_SERVER_NAME>|:|<NEW_SERVER_NAME>
        String[] parts = serialized.split("\\|:\\|");
        return new PlayerServer(parts[0], UUID.fromString(parts[1]), Cytosis.getCytonicNetwork().getServers().get(parts[3]));
    }
}
