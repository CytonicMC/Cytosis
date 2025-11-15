package net.cytonic.cytosis.data.packets.friends;

import net.cytonic.cytosis.data.packets.Packet;

public record FriendApiResponse(boolean success, String code, String message) implements Packet {
}
