package net.cytonic.cytosis.data.objects.preferences;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public abstract class Preference<T> {

    private final Class<T> type;
    private T value;

    public Preference(Class<T> clazz, T value) {
        this.type = Objects.requireNonNull(clazz);
        this.value = value;
    }

    public Class<T> type() {
        return type;
    }

    public T value() {
        return value;
    }
}
