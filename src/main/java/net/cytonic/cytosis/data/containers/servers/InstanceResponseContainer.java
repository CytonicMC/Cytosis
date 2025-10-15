package net.cytonic.cytosis.data.containers.servers;

import net.cytonic.cytosis.Cytosis;

public record InstanceResponseContainer(boolean success, String message) {

    public static InstanceResponseContainer parse(byte[] data) {
        return Cytosis.GSON.fromJson(new String(data), InstanceResponseContainer.class);
    }

    public byte[] serialize() {
        return Cytosis.GSON.toJson(this).getBytes();
    }
}
