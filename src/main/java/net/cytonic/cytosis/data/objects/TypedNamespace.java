package net.cytonic.cytosis.data.objects;

import net.kyori.adventure.key.Key;


/**
 * A record class holding a namespace and a type, to keep type safety
 *
 * @param namespaceID the namespace
 * @param type        the type of the preference
 * @param <T>         the type of the preference
 */
public record TypedNamespace<T>(Key namespaceID, Class<T> type) {
    // records are cool


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypedNamespace<?>(Key id, Class<?> type1))) return false;
        return type1 == this.type && this.namespaceID.equals(id);
    }
}
