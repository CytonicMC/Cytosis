package net.cytonic.cytosis.utils;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class handling parsing of duration strings
 */
public final class DurationParser {
    private static final Pattern PATTERN = Pattern.compile("(\\d+)([ydhms])");

    /**
     * The default constructor
     */
    private DurationParser() {
    }

    /**
     * Parses a duration from a string akin to "1y5d6h23m12s"
     * `-1` provides a null, representing a permanant value
     *
     * @param input The string to parse the value from
     * @return null if the duration is permanant, returns {@link Instant#now()} if the input doesn't contain any parsable data, or an {@link Instant} with the specified duration from now.
     */
    @Nullable
    public static Instant parse(String input) {
        if (input.equalsIgnoreCase("-1")) return null;

        Matcher matcher = PATTERN.matcher(input);
        Duration duration = Duration.ZERO;

        while (matcher.find()) {
            long amount = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            duration = switch (unit) {
                case "y" -> duration.plusDays(amount * 365);
                case "d" -> duration.plusDays(amount);
                case "h" -> duration.plusHours(amount);
                case "m" -> duration.plusMinutes(amount);
                case "s" -> duration.plusSeconds(amount);
                default -> duration;
            };
        }

        return Instant.now().plus(duration);
    }

    /**
     * Converts an Instant to a duration string
     *
     * @param instant The instant to convert to duration string
     * @param spacing The spacing between tokens; a spacing of " " would result a string similar to "1y 29d 4h 10m 3s"
     * @return The duration string representing the duration from now to the given instant
     */
    public static String unparse(@Nullable Instant instant, String spacing) {
        if (instant == null) return null;
        Duration duration = Duration.between(Instant.now(), instant);

        long years = Math.abs(duration.toDays() / 365);
        long days = Math.abs(duration.toDays() % 365);
        long hours = Math.abs(duration.toHours() % 24);
        long minutes = Math.abs(duration.toMinutes() % 60);
        long seconds = Math.abs(duration.getSeconds() % 60);

        StringBuilder builder = new StringBuilder();

        if (years > 0) {
            builder.append(years).append("y").append(spacing);
        }
        if (days > 0) {
            builder.append(days).append("d").append(spacing);
        }
        if (hours > 0) {
            builder.append(hours).append("h").append(spacing);
        }
        if (minutes > 0) {
            builder.append(minutes).append("m").append(spacing);
        }
        if (seconds > 0) {
            builder.append(seconds).append("s").append(spacing);
        }

        return builder.toString();
    }

    /**
     * Converts an Instant to a duration string with full timeunits written out
     *
     * @param instant The instant to convert to duration string
     * @return The duration string representing the duration from now to the given instant
     */
    public static String unparseFull(@Nullable Instant instant) {
        if (instant == null) return null;
        Duration duration = Duration.between(Instant.now(), instant);

        long years = duration.toDays() / 365;
        long days = duration.toDays() % 365;
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        StringBuilder builder = new StringBuilder();

        if (years > 1) {
            builder.append(years).append(" Years ");
        } else if (years == 1) {
            builder.append(years).append(" Year ");
        }
        if (days > 1) {
            builder.append(days).append(" Days ");
        } else if (days == 1) {
            builder.append(days).append(" Day ");
        }
        if (hours > 1) {
            builder.append(hours).append(" Hours ");
        } else if (hours == 1) {
            builder.append(hours).append(" Hour ");
        }
        if (minutes > 1) {
            builder.append(minutes).append(" Minutes ");
        } else if (minutes == 1) {
            builder.append(minutes).append(" Minute ");
        }
        if (seconds > 1) {
            builder.append(seconds).append(" Seconds");
        } else if (seconds == 1) {
            builder.append(seconds).append(" Second");
        }


        return builder.toString();
    }
}
