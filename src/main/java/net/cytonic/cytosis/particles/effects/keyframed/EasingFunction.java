package net.cytonic.cytosis.particles.effects.keyframed;

/**
 * Represents different types of easing functions that can be applied to create smooth transitions or visual motion
 * effects. These easing types determine how keyframes transition with regard to time.
 * <p>
 * Api Note: All effects take the same amount of time to play regardless of easing function
 */
public enum EasingFunction {
    /**
     * Effectively no easing
     */
    LINEAR,
    /**
     * An even more gentle {@link #CUBIC}, but harsh compared to {@link #SINE}.
     */
    QUADRATIC,
    /**
     * Similar to {@link #EXPONENTIAL}, but a little more gentle. Still quite a harsh ease out though.
     */
    CUBIC,
    /**
     * A smooth ease, light in and out
     */
    SINE,
    /**
     * A gentle ease in and rather sharp ease out.
     */
    CIRCULAR,
    /**
     * Very gentle ease in extreme ease out. A base 2 exponent
     */
    EXPONENTIAL
}
