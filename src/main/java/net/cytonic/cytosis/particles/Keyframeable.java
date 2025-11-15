package net.cytonic.cytosis.particles;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface Keyframeable {

    Map<Integer, List<ParticleEffect>> getKeyframeEffects();
}
