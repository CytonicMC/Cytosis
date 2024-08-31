package net.cytonic.cytosis.messaging.containers;

/**
 * A skeleton class providing default methods to redis containers
 */
public interface Container {
    /**
     * Deserializes the serialized container. The format of a serialized container is: {@code ID-JSON}, so an example
     * might be {@code UPDATE_COOLDOWN-{"id":"UPDATE_COOLDOWN", "target":"GLOBAL", "namespace":"cytosis:", "expiry":"<Insert Instant Here>"}}
     *
     * @param serializedContainer the serialized data
     * @return an instance of {@link Container}, using data from the specified string
     */
    static Container deserialize(String serializedContainer) {
        String[] parts = serializedContainer.split("-");
        return switch (parts[0]) {
            case "UPDATE_COOLDOWN" -> CooldownUpdateContainer.create().parse(parts[1]);
            default -> throw new IllegalStateException(STR."Unexpected value: \{parts[0]}");
        };
    }

    /**
     * Converts the container to a string
     *
     * @return the serialized format
     */
    String serialize();

    String id();

    Container parse(String json);
}
