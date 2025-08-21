package net.cytonic.cytosis.data.objects.preferences;

import lombok.Getter;
import net.cytonic.cytosis.data.objects.TypedNamespace;

/**
 * A class that represents a preference that is created by something else.
 *
 * @param <T> the type of this fallback
 */
@Getter
public class FallbackPreference<T> extends NamespacedPreference<T> {
    private final String rawValue;

    public FallbackPreference(TypedNamespace<T> namespace, String rawValue) {
        super(namespace, null);
        this.rawValue = rawValue;
    }
}
