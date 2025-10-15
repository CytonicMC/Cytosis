package net.cytonic.cytosis.data.objects;

import java.util.Objects;
import java.util.UUID;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;

/**
 * A class that holds a tuple (Two things)
 *
 * @param <F> The first object
 * @param <S> The second object
 * @author Foxikle
 */
public class Tuple<F, S> {

    /**
     * A {@link TypeToken} for {@link Tuple} holding two {@link UUID}s
     */
    public static final TypeToken<Tuple<UUID, UUID>> UUID_TYPE = new TypeToken<>() {
    };
    /**
     * A {@link TypeToken} for {@link Tuple} holding two {@link String}s
     */
    public static final TypeToken<Tuple<String, String>> STRING_TYPE = new TypeToken<>() {
    };
    private final F first;
    private final S second;

    /**
     * Creates a new touple
     *
     * @param first  the first object
     * @param second the second object
     * @throws AssertionError if {@code first} or {@code second} is {@literal null}.
     */
    private Tuple(F first, S second) {
        assert first != null;
        assert second != null;

        this.first = first;
        this.second = second;
    }

    /**
     * Creates a new {@link Tuple} for the given elements.
     *
     * @param first  must not be {@literal null}.
     * @param second must not be {@literal null}.
     * @param <F>    the type of the first element
     * @param <S>    the type of the second element
     * @return the new {@link Tuple}
     * @throws AssertionError if {@code first} or {@code second} is {@literal null}.
     */
    public static <F, S> Tuple<F, S> of(@NotNull F first, @NotNull S second) {
        return new Tuple<>(first, second);
    }

    /**
     * Deserializes the given json into a {@link Tuple}.
     *
     * @param json           Raw json string
     * @param tupleTypeToken A {@link TypeToken} to define the types of the {@link Tuple} values
     * @param <F>            The type of the first element
     * @param <S>            The type of the second element
     * @return the deserialized {@link Tuple}
     */
    public static <F, S> Tuple<F, S> deserialize(@NotNull String json, TypeToken<Tuple<F, S>> tupleTypeToken) {
        return Cytosis.GSON.fromJson(json, tupleTypeToken);
    }

    /**
     * Hashes this object
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int result = Objects.hashCode(first);
        result = 31 * result + Objects.hashCode(second);
        return result;
    }

    /**
     * Tests if this touple is equal to another
     *
     * @param o the other object
     * @return if the objects are equal
     */
    @Override
    public boolean equals(@Nullable Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Tuple<?, ?> pair)) {
            return false;
        }

        if (!Objects.equals(first, pair.first)) {
            return false;
        }

        return Objects.equals(second, pair.second);
    }

    /**
     * Converts this touple to a json string
     *
     * @return the json string
     */
    @Override
    public String toString() {
        return Cytosis.GSON.toJson(this);
    }

    /**
     * Makes a copy of this tuple
     *
     * @return the copy
     */
    public Tuple<F, S> copy() {
        return new Tuple<>(first, second);
    }

    /**
     * Gets the first object
     *
     * @return the first object
     */
    public F getFirst() {
        return first;
    }

    /**
     * Gets the second object
     *
     * @return the second object
     */
    public S getSecond() {
        return second;
    }
}
