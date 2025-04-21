package net.cytonic.cytosis.data.containers.servers;

import net.cytonic.cytosis.Cytosis;

public record DeleteInstanceContainer(String instanceType, String allocId) {
    public static DeleteInstanceContainer parse(byte[] data) {
        return Cytosis.GSON.fromJson(new String(data), DeleteInstanceContainer.class);
    }

    public byte[] serialize() {
        return Cytosis.GSON.toJson(this).getBytes();
    }
}
