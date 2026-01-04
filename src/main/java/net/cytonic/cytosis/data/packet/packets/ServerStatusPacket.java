package net.cytonic.cytosis.data.packet.packets;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.CytosisContext;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.utils.Utils;

@Getter
@AllArgsConstructor
public class ServerStatusPacket extends Packet<ServerStatusPacket> {

    private final String type;
    private final String ip;
    private final String id;
    private final int port;
    @Nullable
    private final Instant last_seen;
    private final String group;
    private final transient boolean isStartup;

    /**
     * Creates a new {@link CytonicServer} object using the server's IP, id, port, type, and group.
     *
     * @return A new {@link CytonicServer} object representing the server.
     * @see CytonicServer
     */
    public CytonicServer server() {
        return new CytonicServer(ip, id, port, type, group);
    }

    public static ServerStatusPacket createDefault(boolean isStartup) {
        return new ServerStatusPacket(
            Cytosis.CONTEXT.getServerGroup().type(),
            Utils.getServerIP(),
            CytosisContext.SERVER_ID,
            Cytosis.get(CytosisSettings.class).getServerConfig().getPort(),
            Instant.now(),
            Cytosis.CONTEXT.getServerGroup().group(),
            isStartup
        );
    }

    @Override
    public String getSubject() {
        return isStartup ? Subjects.SERVER_REGISTER : Subjects.SERVER_SHUTDOWN;
    }

    @Override
    protected Serializer<ServerStatusPacket> getSerializer() {
        return new DefaultGsonSerializer<>(ServerStatusPacket.class);
    }
}
