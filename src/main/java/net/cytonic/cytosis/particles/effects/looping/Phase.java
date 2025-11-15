package net.cytonic.cytosis.particles.effects.looping;

/**
 * The phases associated with circular loops. To get points 180 degrees apart, the phases must both be even or odd.
 * Phases {@link #ONE} and {@link #THREE} spin counterclockwise. Phases {@link #TWO} and {@link #FOUR} spin clockwise.
 */
public enum Phase {
    /**
     * Phase 1 (linked with {@link #THREE})
     */
    ONE, // sin
    /**
     * Phase 2 (linked with {@link #FOUR})
     */
    TWO, // cos
    /**
     * Phase 3 (linked with {@link #ONE})
     */
    THREE, //-sin
    /**
     * Phase 4 (linked with {@link #TWO})
     */
    FOUR, // -cos
}
