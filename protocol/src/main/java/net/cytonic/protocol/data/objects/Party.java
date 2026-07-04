package net.cytonic.protocol.data.objects;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

@Data
@AllArgsConstructor
public class Party {

    public static final Codec<Party> CODEC = StructCodec.struct(
        "id", Codec.UUID_STRING, Party::getId,
        "current_leader", Codec.UUID_STRING, Party::getLeader,
        "muted", Codec.BOOLEAN, Party::isMuted,
        "open", Codec.BOOLEAN, Party::isOpen,
        "open_invited", Codec.BOOLEAN, Party::isOpenInvites,
        "moderators", Codec.UUID_STRING.set(), Party::getModerators,
        "members", Codec.UUID_STRING.set(), Party::getMembers,
        "active_invites", Codec.UUID_STRING.mapValue(PartyInvite.CODEC), Party::getActiveInvites,
        Party::new
    );
    private final UUID id;
    private UUID leader;
    private boolean muted;
    private boolean open;
    private boolean openInvites;
    private Set<UUID> moderators;
    private Set<UUID> members;
    private Map<UUID, PartyInvite> activeInvites;

    public Set<UUID> getAllPlayers() {
        Set<UUID> list = new HashSet<>(members);
        list.addAll(moderators);
        list.add(leader);
        return list;
    }

    public boolean hasAuthority(UUID player) {
        return leader.equals(player) || moderators.contains(player);
    }
}
