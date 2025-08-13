package net.cytonic.cytosis.particles.effects.looping;

/**
 * A utility class holding commonly offset angles.
 * <p></p>
 * NOTE: This class only supports angles between 0 and 180. To get an offset greater than that (or less than),
 * use the opposite phase. ({@link Phase#ONE} -> {@link Phase#THREE} and {@link Phase#TWO} -> {@link Phase#FOUR})
 *
 * @see <a href="https://www.mathsisfun.com/geometry/images/circle-unit-304560.svg">Unit Circle Diagram</a>
 */
public interface Angles {
    /**
     * Represents an angle of 30 degrees in radians.
     */
    double THIRTY = Math.PI / 6;

    /**
     * Represents an angle of 45 degrees in radians.
     */
    double FOURTY_FIVE = Math.PI / 4;

    /**
     * Represents an angle of 60 degrees in radians.
     */
    double SIXTY = 2 * THIRTY;

    /**
     * Represents an angle of 90 degrees in radians.
     */
    double NINETY = 3 * THIRTY;

    /**
     * Represents an angle of 120 degrees in radians.
     */
    double ONE_TWENTY = 4 * THIRTY;

    /**
     * Represents an angle of 135 degrees in radians.
     */
    double ONE_THRITY_FIVE = 3 * FOURTY_FIVE;

    /**
     * Represents an angle of 150 degrees in radians.
     */
    double ONE_FIFTY = 5 * THIRTY;


    /**
     * Calculate a custom angle to start at
     *
     * @param degrees The angle, in DEGREES!
     * @return the custom angle, in radians.
     */
    static double custom(double degrees) {
        return Math.toRadians(degrees);
    }
}
