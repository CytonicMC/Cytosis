package net.cytonic.cytosis.utils;

import java.util.List;
import java.util.function.UnaryOperator;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Result.Ok;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.codec.Transcoder.MapLike;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Codec} wrapper that adds versioning support to any existing codec.
 *
 * <p>When decoding, the stored version number is read first and the value is
 * migrated forward through each registered transformer until it reaches the current (latest) version before being
 * handed off to the underlying codec. When encoding, the value is always written at the latest version.</p>
 *
 * <p>Each transformer passed to the constructor represents a single version
 * upgrade step, in order. With {@code n} transformers there are {@code n + 1} versions (1 through {@code n + 1}).  A
 * value stored at version {@code k} will have transformers {@code k-1 … n-1} applied to it (0-indexed) before being
 * decoded by the underlying codec.</p>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * // STRING codec that knows about 3 versions.
 * // Transformer 0 : v1 → v2  (e.g. prepend "v2:")
 * // Transformer 1 : v2 → v3  (e.g. upper-case)
 * Codec<String> VERSIONED = new VersionedCodec<>(
 *         Codec.STRING,
 *         s -> "v2:" + s,          // version 1 → 2
 *         s -> s.toUpperCase()     // version 2 → 3
 * );
 * }</pre>
 *
 * @param <T> the type handled by the underlying codec
 */
@SuppressWarnings("unused")
public final class VersionedCodec<T> implements Codec<T> {

    /**
     * The version written when encoding – always {@code transformers.size() + 1}.
     */
    private final int latestVersion;

    /**
     * The codec used for the actual value (de)serialization at latest version.
     */
    private final Codec<T> delegate;

    /**
     * Ordered list of upgrade steps.  Index {@code i} upgrades version {@code i + 1} to version {@code i + 2}.
     */
    private final List<UnaryOperator<T>> transformers;


    /**
     * Creates a {@link VersionedCodec}.
     *
     * @param delegate     the underlying codec for the current (latest) type
     * @param transformers ordered migration functions, one per version boundary. The first element migrates v1 → v2,
     *                     the second v2 → v3, etc.
     */
    @SafeVarargs
    public VersionedCodec(@NotNull Codec<T> delegate, @NotNull UnaryOperator<T>... transformers) {
        this.delegate = delegate;
        this.transformers = List.of(transformers);    // immutable copy
        this.latestVersion = transformers.length + 1; // versions are 1-based
    }


    /**
     * Decodes a versioned value.
     *
     * <p>The encoded form is expected to be a map with two keys:
     * <ul>
     *   <li>{@code "$v"} - an integer</li>
     *   <li>{@code "val"} - the payload understood by the delegate codec</li>
     * </ul>
     *
     * <p> If that format is not followed, the payload is assumed to be of version 1, and every transformer is run
     * on the payload.
     */
    @Override
    public <D> @NotNull Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D input) {

        final Result<MapLike<D>> mapResult = coder.getMap(input);

        if (!(mapResult instanceof Ok<MapLike<D>>(Transcoder.MapLike<D> value1))) {
            return decodeValue(coder, input, 1);
        }

        // --- read version ---------------------------------------------------
        final Result<D> versionEntryResult = value1.getValue("$v");
        if (!(versionEntryResult instanceof Ok<D>(D value))) {
            return versionEntryResult.cast();
        }

        final Result<Integer> versionResult = coder.getInt(value);
        if (!(versionResult instanceof Ok<Integer>(Integer version))) {
            return versionResult.cast();
        }

        if (version < 1 || version > latestVersion) {
            return new Result.Error<>("Unknown codec version " + version + " (expected 1–" + latestVersion + ")");
        }

        // --- read raw value -------------------------------------------------
        final Result<D> valueEntryResult = value1.getValue("val");
        if (!(valueEntryResult instanceof Ok<D>(D value2))) {
            return valueEntryResult.cast();
        }

        return decodeValue(coder, value2, version);
    }

    private <D> Result<T> decodeValue(Transcoder<D> coder, D rawValue, int version) {
        // --- decode with delegate -------------------------------------------
        Result<T> decoded = delegate.decode(coder, rawValue);
        if (!(decoded instanceof Ok<T>(T value))) {
            return decoded;
        }

        // --- apply upgrade transformers (version → latestVersion) -----------
        for (int i = version - 1; i < transformers.size(); i++) {
            try {
                value = transformers.get(i).apply(value);
            } catch (Exception e) {
                return new Result.Error<>(
                    "Failed to migrate codec value from version "
                        + (i + 1) + " to " + (i + 2) + ": " + e.getMessage());
            }
        }
        return new Result.Ok<>(value);
    }

    /**
     * Encodes a value at the latest version.
     *
     * <p>Produces a map:
     * <pre>{@code
     * {
     *   "$v":  "latestVersion",
     *   "val": "delegate-encoded value";
     * }
     * }</pre>
     */
    @Override
    public <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder, T value) {
        // Encode the value with the delegate first
        final Result<D> encodedValue = delegate.encode(coder, value);
        if (!(encodedValue instanceof Ok<D>(D value1))) {
            return encodedValue;
        }

        // Build the versioned wrapper map
        final Transcoder.MapBuilder<D> builder = coder.createMap();
        builder.put("$v", coder.createInt(latestVersion));
        builder.put("val", value1);
        return new Result.Ok<>(builder.build());
    }

    /**
     * Returns the version number that will be written on the next {@link #encode}.
     */
    public int latestVersion() {
        return latestVersion;
    }

    /**
     * Returns the number of registered migration steps.
     */
    public int migrationCount() {
        return transformers.size();
    }
}
