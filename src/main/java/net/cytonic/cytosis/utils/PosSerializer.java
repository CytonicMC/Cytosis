package net.cytonic.cytosis.utils;

import net.minestom.server.coordinate.Pos;

/**
 * A class that provides utilities for serializing and deserializing {@link Pos} objects.
 */
public final class PosSerializer {
    /**
     * Default constructor
     */
    private PosSerializer() {
    }

    /**
     * Serializes a {@link Pos} object into a human-readable string format.
     *
     * @param pos The position to be serialized.
     * @return A string representation of the position in the format: "Pos{x=%.2f, y=%.2f, z=%.2f, yaw=%.2f, pitch=%.2f}"
     */
    public static String serialize(Pos pos) {
        return String.format("Pos{x=%.2f, y=%.2f, z=%.2f, yaw=%.2f, pitch=%.2f}", pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
    }

    /**
     * Deserializes a human-readable string format into a {@link Pos} object.
     *
     * @param serializedPos The string representation of the position to be deserialized.
     *                      The string should be in the format: "Pos{x=%.2f, y=%.2f, z=%.2f, yaw=%.2f, pitch=%.2f}"
     * @return A {@link Pos} object representing the deserialized position.
     * If the input string is empty, a new {@link Pos} object with all coordinates set to 0 is returned.
     */
    public static Pos deserialize(String serializedPos) {
        if (!serializedPos.isEmpty()) {
            serializedPos = serializedPos.replace("}", "");
            String[] parts = serializedPos.split("[=,\\s]+");
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[3]);
            double z = Double.parseDouble(parts[5]);
            float yaw = Float.parseFloat(parts[7]);
            float pitch = Float.parseFloat(parts[9]);
            return new Pos(x, y, z, yaw, pitch);
        } else {
            return new Pos(0, 0, 0, 180, 0);
        }
    }
}