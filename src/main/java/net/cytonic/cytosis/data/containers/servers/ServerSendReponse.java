package net.cytonic.cytosis.data.containers.servers;

import net.cytonic.cytosis.Cytosis;

public record ServerSendReponse(boolean success, String message) {

    public static ServerSendReponse parse(byte[] data) {
        return Cytosis.GSON.fromJson(new String(data), ServerSendReponse.class);
    }
}
