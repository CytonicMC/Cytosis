/*
    THIS CODE WAS WRITTEN BY THE CONTRIBUTORS OF 'Minestom/VanillaReimplementaion'
    https://github.com/Minestom/VanillaReimplementation
    ** THIS FILE MAY HAVE BEEN EDITED BY THE CYTONIC DEVELOPMENT TEAM **
 */
package net.cytonic.cytosis.logging;

/**
 * Represents different colors that can be used for text formatting.
 */
public enum Color {
    /**
     * Color end string, color reset
     */
    RESET("\033[0m"),

    /**
     * Regular Colors. Normal color, no bold, background color etc.
     */
    BLACK("\033[0;30m"),    // BLACK
    /**
     * Regular Colors. Normal color, no bold, background color etc.
     */
    RED("\033[0;31m"),      // RED
    /**
     * Regular Colors. Normal color, no bold, background color etc.
     */
    GREEN("\033[0;32m"),    // GREEN
    /**
     * Regular Colors. Normal color, no bold, background color etc.
     */
    YELLOW("\033[0;33m"),   // YELLOW
    /**
     * Regular Colors. Normal color, no bold, background color etc.
     */
    BLUE("\033[0;34m"),     // BLUE
    /**
     * Regular Colors. Normal color, no bold, background color etc.
     */
    MAGENTA("\033[0;35m"),  // MAGENTA
    /**
     * Regular Colors. Normal color, no bold, background color etc.
     */
    CYAN("\033[0;36m"),     // CYAN
    /**
     * Regular Colors. Normal color, no bold, background color etc.
     */
    WHITE("\033[0;37m"),    // WHITE

    /**
     * Bold Colors
     */
    BLACK_BOLD("\033[1;30m"),   // BLACK
    /**
     * Bold Colors
     */
    /**
     * Bold Colors
     */
    RED_BOLD("\033[1;31m"),     // RED
    /**
     * Bold Colors
     */
    GREEN_BOLD("\033[1;32m"),   // GREEN
    /**
     * Bold Colors
     */
    YELLOW_BOLD("\033[1;33m"),  // YELLOW
    /**
     * Bold Colors
     */
    BLUE_BOLD("\033[1;34m"),    // BLUE
    /**
     * Bold Colors
     */
    MAGENTA_BOLD("\033[1;35m"), // MAGENTA
    /**
     * Bold Colors
     */
    CYAN_BOLD("\033[1;36m"),    // CYAN
    /**
     * Bold Colors
     */
    WHITE_BOLD("\033[1;37m"),   // WHITE

    /**
     * Underlined Colors
     */
    BLACK_UNDERLINED("\033[4;30m"),     // BLACK
    /**
     * Underlined Colors
     */
    RED_UNDERLINED("\033[4;31m"),       // RED
    /**
     * Underlined Colors
     */
    GREEN_UNDERLINED("\033[4;32m"),     // GREEN
    /**
     * Underlined Colors
     */
    YELLOW_UNDERLINED("\033[4;33m"),    // YELLOW
    /**
     * Underlined Colors
     */
    BLUE_UNDERLINED("\033[4;34m"),      // BLUE
    /**
     * Underlined Colors
     */
    MAGENTA_UNDERLINED("\033[4;35m"),   // MAGENTA
    /**
     * Underlined Colors
     */
    CYAN_UNDERLINED("\033[4;36m"),      // CYAN
    /**
     * Underlined Colors
     */
    WHITE_UNDERLINED("\033[4;37m"),     // WHITE

    /**
     * Background colors
     */
    BLACK_BACKGROUND("\033[40m"),   // BLACK
    /**
     * Background colors
     */
    RED_BACKGROUND("\033[41m"),     // RED
    /**
     * Background colors
     */
    GREEN_BACKGROUND("\033[42m"),   // GREEN
    /**
     * Background colors
     */
    YELLOW_BACKGROUND("\033[43m"),  // YELLOW
    /**
     * Background colors
     */
    BLUE_BACKGROUND("\033[44m"),    // BLUE
    /**
     * Background colors
     */
    MAGENTA_BACKGROUND("\033[45m"), // MAGENTA
    /**
     * Background colors
     */
    CYAN_BACKGROUND("\033[46m"),    // CYAN
    /**
     * Background colors
     */
    WHITE_BACKGROUND("\033[47m"),   // WHITE

    /**
     * High Intensity Colors
     */
    BLACK_BRIGHT("\033[0;90m"),     // BLACK
    /**
     * High Intensity Colors
     */
    RED_BRIGHT("\033[0;91m"),       // RED
    /**
     * High Intensity Colors
     */
    GREEN_BRIGHT("\033[0;92m"),     // GREEN
    /**
     * High Intensity Colors
     */
    YELLOW_BRIGHT("\033[0;93m"),    // YELLOW
    /**
     * High Intensity Colors
     */
    BLUE_BRIGHT("\033[0;94m"),      // BLUE
    /**
     * High Intensity Colors
     */
    MAGENTA_BRIGHT("\033[0;95m"),   // MAGENTA
    /**
     * High Intensity Colors
     */
    CYAN_BRIGHT("\033[0;96m"),      // CYAN
    /**
     * High Intensity Colors
     */
    WHITE_BRIGHT("\033[0;97m"),     // WHITE

    /**
     * Bold and High Intensity
     */
    BLACK_BOLD_BRIGHT("\033[1;90m"),    // BLACK
    /**
     * Bold and High Intensity
     */
    RED_BOLD_BRIGHT("\033[1;91m"),      // RED
    /**
     * Bold and High Intensity
     */
    GREEN_BOLD_BRIGHT("\033[1;92m"),    // GREEN
    /**
     * Bold and High Intensity
     */
    YELLOW_BOLD_BRIGHT("\033[1;93m"),   // YELLOW
    /**
     * Bold and High Intensity
     */
    BLUE_BOLD_BRIGHT("\033[1;94m"),     // BLUE
    /**
     * Bold and High Intensity
     */
    MAGENTA_BOLD_BRIGHT("\033[1;95m"),  // MAGENTA
    /**
     * Bold and High Intensity
     */
    CYAN_BOLD_BRIGHT("\033[1;96m"),     // CYAN
    /**
     * Bold and High Intensity
     */
    WHITE_BOLD_BRIGHT("\033[1;97m"),    // WHITE

    /**
     * High Intensity backgrounds
     */
    BLACK_BACKGROUND_BRIGHT("\033[0;100m"),     // BLACK
    /**
     * High Intensity backgrounds
     */
    RED_BACKGROUND_BRIGHT("\033[0;101m"),       // RED
    /**
     * High Intensity backgrounds
     */
    GREEN_BACKGROUND_BRIGHT("\033[0;102m"),     // GREEN
    /**
     * High Intensity backgrounds
     */
    YELLOW_BACKGROUND_BRIGHT("\033[0;103m"),    // YELLOW
    /**
     * High Intensity backgrounds
     */
    BLUE_BACKGROUND_BRIGHT("\033[0;104m"),      // BLUE
    /**
     * High Intensity backgrounds
     */
    MAGENTA_BACKGROUND_BRIGHT("\033[0;105m"),   // MAGENTA
    /**
     * High Intensity backgrounds
     */
    CYAN_BACKGROUND_BRIGHT("\033[0;106m"),      // CYAN
    /**
     * High Intensity backgrounds
     */
    WHITE_BACKGROUND_BRIGHT("\033[0;107m");     // WHITE

    private final String code;

    /**
     * Creates a new color
     *
     * @param code The color code
     */
    Color(String code) {
        this.code = code;
    }

    /**
     * Returns the code of the color
     * @return The color code
     */
    @Override
    public String toString() {
        return code;
    }
}