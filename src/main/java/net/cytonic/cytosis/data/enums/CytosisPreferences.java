package net.cytonic.cytosis.data.enums;

import net.cytonic.objects.NamespacedPreference;
import net.minestom.server.utils.NamespaceID;

/**
 * A list of Cytosis supplied preferences
 */
public class CytosisPreferences {
    /**
     * A private constructor to prevent instantiation
     */
    private CytosisPreferences() {
        // do nothing
    }

    /**
     * A preference to accept or decline friend requests, type of BOOLEAN
     */
    public static final NamespacedPreference<Boolean> ACCEPT_FRIEND_REQUESTS = new NamespacedPreference<>(NamespaceID.from("cytosis", "accept_friend_request"), true);
}
