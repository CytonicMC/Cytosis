package net.cytonic.cytosis.utils;

import java.util.Objects;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A class that holds an object. The intention is that the {@link Holder} object is immutable (final), but its contents
 * can change. One use case is the {@link SplashProvider} in the {@link Msg} class.
 *
 * @param <T> The type of the value held within
 */
@AllArgsConstructor
public class Holder<T> {

    private T value;

    /**
     * Gets the value held within this holder
     *
     * @return the value stored, not null
     */
    @NotNull
    public T get() {
        return value;
    }

    /**
     * Set the value held within this holder
     *
     * @param value the new value, not null
     */
    @Contract(value = "null -> fail", pure = true)
    public void set(@NotNull final T value) {
        this.value = Objects.requireNonNull(value);
    }
}
