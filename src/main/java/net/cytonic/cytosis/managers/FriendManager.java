package net.cytonic.cytosis.managers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.containers.friends.FriendRequest;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;

/**
 * A class to manage friends
 */
@NoArgsConstructor
@CytosisComponent(dependsOn = {PreferenceManager.class})
public class FriendManager implements Bootstrappable {

    private final Map<UUID, List<UUID>> friends = new ConcurrentHashMap<>();

    private CytonicNetwork network;
    private NatsManager natsManager;
    private MysqlDatabase db;

    /**
     * Initializes the friends table and loads the online players friends
     */
    @Override
    public void init() {
        this.network = Cytosis.CONTEXT.getComponent(CytonicNetwork.class);
        this.natsManager = Cytosis.CONTEXT.getComponent(NatsManager.class);
        this.db = Cytosis.CONTEXT.getComponent(MysqlDatabase.class);

        PreparedStatement ps = db.prepare(
            "CREATE TABLE IF NOT EXISTS cytonic_friends (uuid VARCHAR(36), friends TEXT, PRIMARY KEY (uuid))");
        db.update(ps).whenComplete((r, t) -> {
            if (t != null) {
                Logger.error("An error occurred whilst creating the friends table!", t);
            }
            Cytosis.getOnlinePlayers().forEach(player -> loadFriends(player.getUuid()));
        });
    }

    /**
     * Loads a player's friends into memory
     *
     * @param uuid The player
     */
    public void loadFriends(UUID uuid) {

        PreparedStatement ps = db.prepare("SELECT * FROM cytonic_friends WHERE uuid = ?");
        try {
            ps.setString(1, uuid.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        db.query(ps).whenComplete((resultSet, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst loading friends!", throwable);
                return;
            }

            try {
                if (resultSet.next()) {
                    List<UUID> list = Cytosis.GSON.fromJson(resultSet.getString("friends"), Utils.UUID_LIST);
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.remove(uuid); // remove them if they are already their own friend
                    friends.put(uuid, list);
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst loading friends!", e);
            }
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
        List<UUID> list = friends.getOrDefault(uuid, new ArrayList<>());
        list.add(friend);
        friends.put(uuid, list);

        list = friends.getOrDefault(friend, new ArrayList<>());
        list.add(uuid);
        friends.put(friend, list);
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
        List<UUID> list = friends.getOrDefault(uuid, new ArrayList<>());
        list.add(friend);
        friends.put(uuid, list);

        PreparedStatement f1 = db.prepare(
            "INSERT INTO cytonic_friends (uuid, friends) VALUES (?, ?) ON DUPLICATE KEY UPDATE friends = ?");
        try {
            f1.setString(1, uuid.toString());
            f1.setString(2, Cytosis.GSON.toJson(list));
            f1.setString(3, Cytosis.GSON.toJson(list));
        } catch (SQLException e) {
            // big problem
            throw new RuntimeException(e);
        }
        db.update(f1)
            .whenComplete((unused, throwable) -> Logger.error("An error occurred whilst adding a friend!", throwable));

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
        natsManager.broadcastFriendRemoval(uuid, friend);
    }

    private void removeFriendRecursive(UUID uuid, UUID friend, boolean recursive) {
        List<UUID> list1 = friends.getOrDefault(uuid, new ArrayList<>());
        list1.remove(friend);
        friends.put(uuid, list1);

        PreparedStatement f1 = db.prepare(
            "INSERT INTO cytonic_friends (uuid, friends) VALUES (?,?) ON DUPLICATE KEY UPDATE friends = ?");
        try {
            f1.setString(1, uuid.toString());
            f1.setString(2, Cytosis.GSON.toJson(list1));
            f1.setString(3, Cytosis.GSON.toJson(list1));
        } catch (SQLException e) {
            // big problem
            throw new RuntimeException(e);
        }
        db.update(f1)
            .whenComplete(
                (unused, throwable) -> Logger.error("An error occurred whilst removing a friend!", throwable));

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
     * @return the list of UUIDs of the player friends
     */
    public List<UUID> getFriends(UUID uuid) {
        return friends.getOrDefault(uuid, new ArrayList<>());
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

    /**
     * Sends a friend request.
     *
     * @param sender    The sender of the request
     * @param recipient The recipient
     */
    public void sendRequest(UUID sender, UUID recipient) {
        natsManager
            .sendFriendRequest(new FriendRequest(sender, recipient, Instant.now().plus(5, ChronoUnit.MINUTES)));
    }
}