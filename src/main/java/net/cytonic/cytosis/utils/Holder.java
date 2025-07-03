package net.cytonic.cytosis.utils;

import lombok.AllArgsConstructor;

/**
 * A class that holds an object. The intention is that the {@link Holder} object is immuntable
 * (final), but its contents can change. One usecase is the {@link SplashProvider} in the
 * {@link Msg} class.
 */
@AllArgsConstructor
public class Holder<T> {
    private T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
