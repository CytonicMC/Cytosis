package net.cytonic.cytosis.data.objects;

import net.kyori.adventure.key.Key;

import net.cytonic.protocol.impl.notify.ServerStatusNotifyPacket;

/**
 * A class that holds data about a Cytosis server
 *
 * @param id   The server ID
 * @param ip   The ip address of the server (for example, 127.0.0.1)
 * @param port The port of the server, usually 25565
 * @param type The type of the server
 */
@SuppressWarnings("unused")
public record CytonicServer(String ip, String id, int port, Key type) {

    public CytonicServer(ServerStatusNotifyPacket.Packet packet) {
        this(packet.ip(), packet.id(), packet.port(), packet.type());
    }
}
