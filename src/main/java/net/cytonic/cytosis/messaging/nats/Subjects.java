package net.cytonic.cytosis.messaging.nats;

import net.cytonic.cytosis.Cytosis;

public class Subjects {
    public static final String SERVER_REGISTER = "servers.register";
    public static final String SERVER_SHUTDOWN = "servers.shutdown";
    public static final String SERVER_LIST = "servers.list";
    public static final String PROXY_START = "servers.proxy.startup";


    public static final String HEALTH_CHECK = "health.check." + Cytosis.getRawID();

    // friends
    public static final String FRIEND_REQUEST = "friends.request";
    public static final String FRIEND_ACCEPT = "friends.accept";
    public static final String FRIEND_DECLINE = "friends.decline";
    public static final String FRIEND_ACCEPTANCE_NOTIFY = "friends.accept.notify";
    public static final String FRIEND_DECLINATION_NOTIFY = "friends.decline.notify";
}
