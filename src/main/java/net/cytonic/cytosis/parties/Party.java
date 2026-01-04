package net.cytonic.cytosis.parties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import net.cytonic.cytosis.Cytosis;

@Data
public class Party {

    private final UUID id;
    @SerializedName("current_leader")
    private UUID leader;
    private boolean muted;
    private boolean open;
    @SerializedName("open_invited")
    private boolean openInvites;
    private List<UUID> moderators;
    private List<UUID> members;
    @SerializedName("active_invites")
    private Map<UUID, PartyInvite> activeInvites;

    public static Party deserialize(byte[] data) {
        return Cytosis.GSON.fromJson(new String(data), Party.class);
    }

    public List<UUID> getAllPlayers() {
        List<UUID> list = new ArrayList<>(members);
        list.addAll(moderators);
        list.add(leader);
        return list;
    }
}
