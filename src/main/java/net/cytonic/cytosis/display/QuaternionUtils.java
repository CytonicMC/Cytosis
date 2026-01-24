package net.cytonic.cytosis.display;

import net.minestom.server.coordinate.Vec;
import org.joml.Quaternionf;

public class QuaternionUtils {

    public static float[] decompose(Quaternionf quaternion) {
        return new float[]{quaternion.x, quaternion.y, quaternion.z, quaternion.w};
    }

    public static Quaternionf compose(float[] raw) {
        if (raw.length != 4) throw new IllegalArgumentException("There must be 4 elements to compose a quaternion!");
        return new Quaternionf(raw[0], raw[1], raw[2], raw[3]);
    }

    public static Quaternionf compose(Vec from, Vec to) {
        return new Quaternionf().rotationTo(VectorUtils.compose(from), VectorUtils.compose(to));
    }

}