package net.cytonic.cytosis.data.packet.publishers;

import lombok.NoArgsConstructor;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.packet.packets.ServerStatusPacket;
import net.cytonic.cytosis.data.packet.packets.servers.FetchServersPacket;
import net.cytonic.cytosis.logging.Logger;

@CytosisComponent(dependsOn = CytonicNetwork.class)
@NoArgsConstructor
public class FetchServersPublisher {

    public void sendFetchServersPacket() {
        new FetchServersPacket().request((response, throwable) -> {
            if (throwable != null) {
                Logger.error("failed to fetch active servers!", throwable);
                return;
            }

            for (ServerStatusPacket server : response.getServers()) {
                Cytosis.get(CytonicNetwork.class).getServers().put(server.getId(), server.server());
                Logger.info("Loaded server '" + server.getId() + "' from Cydian!");
            }
            Logger.info("Loaded " + response.getServers().size() + " active servers from Cydian!");
        });
    }
}
