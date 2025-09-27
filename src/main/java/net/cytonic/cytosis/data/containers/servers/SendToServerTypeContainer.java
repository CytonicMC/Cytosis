package net.cytonic.cytosis.data.containers.servers;

import java.util.UUID;

import net.cytonic.cytosis.Cytosis;

public record SendToServerTypeContainer(UUID player, String group, String type) {

    public byte[] serialize() {
        return Cytosis.GSON.toJson(this).getBytes();
    }
}
