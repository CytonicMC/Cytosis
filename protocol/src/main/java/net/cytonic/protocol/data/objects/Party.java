package net.cytonic.protocol.data.objects;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Party {

    private final UUID id;
    @SerializedName("current_leader")
    private UUID leader;
    private boolean muted;
    private boolean open;
    @SerializedName("open_invited")
    private boolean openInvites;
    private Set<UUID> moderators;
    private Set<UUID> members;
    @SerializedName("active_invites")
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
