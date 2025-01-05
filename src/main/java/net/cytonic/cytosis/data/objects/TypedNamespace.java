package net.cytonic.cytosis.data.objects;

import net.minestom.server.utils.NamespaceID;


/**
 * A record class holding a namespace and a type, to keep type safety
 *
 * @param namespaceID the namespace
 * @param type        the type of the preference
 * @param <T>         the type of the preference
 */
public record TypedNamespace<T>(NamespaceID namespaceID, Class<T> type) {
    // records are cool


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypedNamespace<?>(NamespaceID id, Class<?> type1))) return false;
        return type1 == this.type && this.namespaceID.equals(id);
    }
}
