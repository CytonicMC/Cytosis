package net.cytonic.cytosis.data.packets;

import java.time.Instant;

import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.data.objects.CytonicServer;

public record ServerStatusPacket(String type, String ip, String id, int port, @Nullable Instant last_seen,
                                 String group) implements Packet {

    /**
     * Creates a new {@link CytonicServer} object using the server's IP, id, port, type, and group.
     *
     * @return A new {@link CytonicServer} object representing the server.
     * @see CytonicServer
     */
    public CytonicServer server() {
        return new CytonicServer(ip, id, port, type, group);
    }
}
