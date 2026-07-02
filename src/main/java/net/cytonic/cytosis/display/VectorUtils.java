package net.cytonic.cytosis.display;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class VectorUtils {

    public static Vec decompose(Vector3f vector) {
        return new Vec(vector.x(), vector.y(), vector.z());
    }

    public static Vec decompose(Vector3d vector) {
        return new Vec(vector.x(), vector.y(), vector.z());
    }

    public static Vector3f composef(Point raw) {
        return new Vector3f((float) raw.x(), (float) raw.y(), (float) raw.z());
    }

    public static Vector3d composed(Point raw) {
        return new Vector3d(raw.x(), raw.y(), raw.z());
    }
}
