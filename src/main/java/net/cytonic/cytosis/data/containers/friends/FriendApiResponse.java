package net.cytonic.cytosis.data.containers.friends;

import com.google.gson.Gson;

public record FriendApiResponse(boolean success, String code, String message) {
    /**
     * Deserializes this object from a string
     *
     * @param json the serialized data
     * @return the deserailized object
     */
    public static FriendApiResponse deserialize(String json) {
        return new Gson().fromJson(json, FriendApiResponse.class);
    }

    /**
     * Serializes the container into a string
     *
     * @return the serialized string
     */
    public byte[] serialize() {
        return toString().getBytes();
    }

    /**
     * Serializes the container into a string
     *
     * @return the serialized string
     */
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
