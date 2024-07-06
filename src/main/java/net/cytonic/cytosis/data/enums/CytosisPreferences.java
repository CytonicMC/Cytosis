package net.cytonic.cytosis.data.enums;

import net.cytonic.objects.NamespacedPreference;
import net.minestom.server.utils.NamespaceID;

public class CytosisPreferences {
    public static final NamespacedPreference<Boolean> ACCEPT_FRIEND_REQUESTS = new NamespacedPreference<>(NamespaceID.from("cytosis", "accept_friend_request"), true);
}
