package net.cytonic.cytosis.data.packets.friends;

import java.util.UUID;

import net.cytonic.cytosis.data.packets.Packet;

public record OrganicFriendResponse(UUID sender, UUID recipient) implements Packet {

}
