package net.cytonic.cytosis.data.containers.servers;

import net.cytonic.cytosis.Cytosis;

public record CreateInstanceContainer(String instanceType, int quantity) {

    public static CreateInstanceContainer parse(byte[] data) {
        return Cytosis.GSON.fromJson(new String(data), CreateInstanceContainer.class);
    }

    public byte[] serialize() {
        return Cytosis.GSON.toJson(this).getBytes();
    }
}
