package net.cytonic.cytosis.data.containers.servers;

import net.cytonic.cytosis.Cytosis;

import java.util.UUID;

public record SendToServerTypeContainer(UUID player, String group, String type) {
    public byte[] serialize() {
        return Cytosis.GSON.toJson(this).getBytes();
    }
}
