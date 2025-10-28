package net.cytonic.cytosis.data.packets.friends;

import net.cytonic.cytosis.data.packets.Packet;

import java.util.UUID;

public record OrganicFriendResponse(UUID sender, UUID recipient) implements Packet {
}
