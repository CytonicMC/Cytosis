package net.cytonic.cytosis.display;

import net.minestom.server.coordinate.Vec;
import org.joml.Vector3f;

public class VectorUtils {

    public static Vec decompose(Vector3f vector) {
        return new Vec(vector.x(), vector.y(), vector.z());
    }

    public static Vector3f compose(Vec raw) {
        return new Vector3f((float) raw.x(), (float) raw.y(), (float) raw.z());
    }
}
