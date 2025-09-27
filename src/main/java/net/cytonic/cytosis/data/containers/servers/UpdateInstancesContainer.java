package net.cytonic.cytosis.data.containers.servers;

import net.cytonic.cytosis.Cytosis;

public record UpdateInstancesContainer(String instanceType) {

    public static UpdateInstancesContainer parse(byte[] data) {
        return Cytosis.GSON.fromJson(new String(data), UpdateInstancesContainer.class);
    }

    public byte[] serialize() {
        return Cytosis.GSON.toJson(this).getBytes();
    }
}
