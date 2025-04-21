package net.cytonic.cytosis.data.containers.servers;

import net.cytonic.cytosis.Cytosis;

public record DeleteAllInstancesContainer(String instanceType) {
    public static DeleteAllInstancesContainer parse(byte[] data) {
        return Cytosis.GSON.fromJson(new String(data), DeleteAllInstancesContainer.class);
    }

    public byte[] serialize() {
        return Cytosis.GSON.toJson(this).getBytes();
    }
}
