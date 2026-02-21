package net.cytonic.cytosis.managers;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.EnvironmentDatabase;
import net.cytonic.cytosis.data.GlobalDatabase;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.cytosis.protocol.publishers.FriendPacketsPublisher;
import net.cytonic.cytosis.utils.Msg;

/**
 * A class to manage friends
 */
@NoArgsConstructor
@CytosisComponent(dependsOn = {PreferenceManager.class, NatsManager.class, EnvironmentDatabase.class,
    CytonicNetwork.class}, priority = 10)
public class FriendManager implements Bootstrappable {

    private final Map<UUID, Set<UUID>> friends = new ConcurrentHashMap<>();
    private CytonicNetwork network;
    private GlobalDatabase db;

    /**
     * Initializes the friends table and loads the online players friends
     */
    @Override
    public void init() {
        this.network = Cytosis.get(CytonicNetwork.class);
        this.db = Cytosis.get(GlobalDatabase.class);
    }

    /**
     * Loads a player's friends into memory
     *
     * @param uuid The player
     */
    public void loadFriends(UUID uuid) {

        db.loadFriends(uuid)
            .thenAccept(uuids -> friends.put(uuid, uuids))
            .exceptionally(throwable -> {
                Logger.error("Failed to load friends!", throwable);
                return null;
            });
    }

    /**
     * Unloads the player's friends from memory
     *
     * @param uuid The player
     */
    public void unloadPlayer(UUID uuid) {
        friends.remove(uuid);
    }

    public void addCachedFriend(UUID uuid, UUID friend) {
        Set<UUID> list = friends.computeIfAbsent(uuid, u -> ConcurrentHashMap.newKeySet());
        list.add(friend);

        Set<UUID> friendList = friends.computeIfAbsent(friend, u -> ConcurrentHashMap.newKeySet());
        friendList.add(uuid);
    }

    /**
     * Adds a player as a friend
     *
     * @param uuid   The player
     * @param friend The friend
     */
    public void addFriend(UUID uuid, UUID friend) {
        if (uuid.equals(friend)) {
            return; // you can't do that!
        }
        addFriendRecursive(uuid, friend, true);
    }

    private void addFriendRecursive(UUID uuid, UUID friend, boolean recursive) {
        Set<UUID> list = friends.computeIfAbsent(uuid, u -> ConcurrentHashMap.newKeySet());
        list.add(friend);

        db.updateFriends(uuid, list);

        if (recursive) { // add the other player to their friends' list
            addFriendRecursive(friend, uuid, false);
        }
    }

    /**
     * Removes a player as a friend
     *
     * @param uuid   The player
     * @param friend The friend
     */
    public void removeFriend(UUID uuid, UUID friend) {
        if (uuid.equals(friend)) return;
        removeFriendRecursive(uuid, friend, true);
        Cytosis.get(FriendPacketsPublisher.class).sendFriendRemove(uuid, friend);
    }

    private void removeFriendRecursive(UUID uuid, UUID friend, boolean recursive) {
        Set<UUID> list = friends.computeIfAbsent(uuid, u -> ConcurrentHashMap.newKeySet());
        list.remove(friend);

        db.updateFriends(uuid, list);

        if (recursive) {
            removeFriendRecursive(friend, uuid, false);
        }
    }

    /**
     * Sends the player their list of friends
     *
     * @param player the player
     */
    public void listFriends(Player player) {
        if (getFriends(player.getUuid()).isEmpty()) {
            player.sendMessage(Msg.whoops("You have no friends :("));
            player.sendMessage(Msg.tip("Perhaps you should make some with /friend add <player>"));
            return;
        }

        player.sendMessage(
            Msg.aquaSplash("Friends List", "(" + getFriends(player.getUuid()).size() + ") <dark_gray>»</dark_gray>"));

        for (UUID friend : getFriends(player.getUuid())) {
            PlayerRank rank = network.getCachedPlayerRanks().get(friend);
            boolean online = network.getOnlinePlayers().containsKey(friend);
            String name = network.getLifetimePlayers().getByKey(friend);
            player.sendMessage(Msg.mm("<dark_gray>  > </dark_gray>")
                .append(rank.getPrefix().append(Component.text(name)).append(Component.text(" - "))
                    .append(online ? Msg.coloredBadge("ONLINE!", "green") : Msg.coloredBadge("OFFLINE :(", "red"))));
        }
    }

    /**
     * Gets a player's friends
     *
     * @param uuid The player
     * @return the set of UUIDs of the player friends
     */
    public Set<UUID> getFriends(UUID uuid) {
        return friends.getOrDefault(uuid, ConcurrentHashMap.newKeySet());
    }

    /**
     * Sends the logout message for the player
     *
     * @param uuid the player who logged out
     */
    public void sendLogoutMessage(UUID uuid) {
        PlayerRank rank = network.getCachedPlayerRanks().get(uuid);
        Component message = Msg.mm("<dark_aqua>Friend » </dark_aqua>").append(rank.getPrefix()
                .append(Component.text(network
                    .getLifetimePlayers()
                    .getByKey(uuid))))
            .append(Msg.mm("<gray> left."));
        Cytosis.getOnlinePlayers().forEach(player -> {
            if (getFriends(player.getUuid()).contains(uuid)) {
                player.sendMessage(message);
            }
        });
    }

    /**
     * Sends the login message for the player
     *
     * @param uuid the player who logged in
     */
    public void sendLoginMessage(UUID uuid) {
        PlayerRank rank = network.getCachedPlayerRanks().get(uuid);
        Component message = Msg.mm("<dark_aqua>Friend » </dark_aqua>").append(rank.getPrefix()
                .append(Component.text(network
                    .getLifetimePlayers()
                    .getByKey(uuid))))
            .append(Msg.mm("<gray> joined."));
        Cytosis.getOnlinePlayers().forEach(player -> {
            if (getFriends(player.getUuid()).contains(uuid)) {
                player.sendMessage(message);
            }
        });
    }
}