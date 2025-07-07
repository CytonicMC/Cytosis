package net.cytonic.cytosis.particles;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public interface Keyframeable {
    Map<Integer, List<ParticleEffect>> getKeyframeEffects();
}
