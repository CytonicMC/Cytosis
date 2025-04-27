package net.cytonic.cytosis.managers;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.containers.friends.FriendRequest;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class to manage friends
 */
@NoArgsConstructor
public class FriendManager {
    private final ConcurrentHashMap<UUID, List<UUID>> friends = new ConcurrentHashMap<>();
    private final MysqlDatabase db = Cytosis.getDatabaseManager().getMysqlDatabase();

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
     * Initializes the friends table and loads the online players friends
     */
    public void init() {
        PreparedStatement ps = db.prepare("CREATE TABLE IF NOT EXISTS cytonic_friends (uuid VARCHAR(36), friends TEXT, PRIMARY KEY (uuid))");
        db.update(ps).whenComplete((r, t) -> {
            if (t != null) Logger.error("An error occurred whilst creating the friends table!", t);
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

    /**
     * Adds a player as a friend
     *
     * @param uuid   The player
     * @param friend The friend
     */
    public void addFriend(UUID uuid, UUID friend) {
        List<UUID> list = friends.getOrDefault(uuid, new ArrayList<>());
        list.add(friend);
        friends.put(uuid, list);

        PreparedStatement f1 = db.prepare("INSERT INTO cytonic_friends (uuid, friends) VALUES (?, ?) ON DUPLICATE KEY UPDATE friends = ?");
        try {
            f1.setString(1, uuid.toString());
            f1.setString(2, Cytosis.GSON.toJson(list));
            f1.setString(3, Cytosis.GSON.toJson(list));
        } catch (SQLException e) {
            // big problem
            throw new RuntimeException(e);
        }
        db.update(f1).whenComplete((unused, throwable) -> Logger.error("An error occurred whilst adding a friend!", throwable));

        // add the friend to the other player too
        List<UUID> list2 = friends.getOrDefault(friend, new ArrayList<>());
        list2.add(uuid);
        friends.put(friend, list2);

        PreparedStatement f2 = db.prepare("INSERT INTO cytonic_friends (uuid, friends) VALUES (?, ?) ON DUPLICATE KEY UPDATE friends = ?");
        try {
            f2.setString(1, friend.toString());
            f2.setString(2, Cytosis.GSON.toJson(list));
            f2.setString(3, Cytosis.GSON.toJson(list));
        } catch (SQLException e) {
            // big problem
            throw new RuntimeException(e);
        }
        db.update(f2).whenComplete((unused, throwable) -> Logger.error("An error occurred whilst adding a friend!", throwable));
    }

    /**
     * Removes a player as a friend
     *
     * @param uuid   The player
     * @param friend The friend
     */
    public void removeFriend(UUID uuid, UUID friend) {
        List<UUID> list1 = friends.getOrDefault(uuid, new ArrayList<>());
        list1.remove(friend);
        friends.put(uuid, list1);

        PreparedStatement f1 = db.prepare("INSERT INTO cytonic_friends (uuid, friends) VALUES (?,?) ON DUPLICATE KEY UPDATE friends = ?");
        try {
            f1.setString(1, uuid.toString());
            f1.setString(2, Cytosis.GSON.toJson(list1));
            f1.setString(3, Cytosis.GSON.toJson(list1));
        } catch (SQLException e) {
            // big problem
            throw new RuntimeException(e);
        }
        db.update(f1).whenComplete((unused, throwable) -> Logger.error("An error occurred whilst removing a friend!", throwable));

        List<UUID> list2 = friends.getOrDefault(friend, new ArrayList<>());
        list2.remove(uuid);
        friends.put(friend, list2);

        PreparedStatement f2 = db.prepare("INSERT INTO cytonic_friends (uuid, friends) VALUES (?,?) ON DUPLICATE KEY UPDATE friends = ?");
        try {
            f2.setString(1, friend.toString());
            f2.setString(2, Cytosis.GSON.toJson(list2));
            f2.setString(3, Cytosis.GSON.toJson(list2));
        } catch (SQLException e) {
            // big problem
            throw new RuntimeException(e);
        }
        db.update(f2).whenComplete((unused, throwable) -> Logger.error("An error occurred whilst removing a friend!", throwable));

        Cytosis.getNatsManager().broadcastFriendRemoval(uuid, friend);
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

        player.sendMessage(Msg.aquaSplash("Friends List", "(" + getFriends(player.getUuid()).size() + ") <dark_gray>»</dark_gray>"));

        for (UUID friend : getFriends(player.getUuid())) {
            PlayerRank rank = Cytosis.getCytonicNetwork().getPlayerRanks().get(friend);
            boolean online = Cytosis.getCytonicNetwork().getOnlinePlayers().containsKey(friend);
            String name = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(friend);
            player.sendMessage(Msg.mm("<dark_gray>  > </dark_gray>").append(rank.getPrefix().append(Component.text(name)).append(Component.text(" - ")).append(online ? Msg.coloredBadge("ONLINE!", "green") : Msg.coloredBadge("OFFLINE :(", "red"))));
        }
    }

    /**
     * Sends the logout message for the player
     *
     * @param uuid the player who logged out
     */
    public void sendLogoutMessage(UUID uuid) {
        PlayerRank rank = Cytosis.getCytonicNetwork().getPlayerRanks().get(uuid);
        Component message = Msg.mm("<dark_aqua>Friend » </dark_aqua>").append(rank.getPrefix().append(Component.text(Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid)))).append(Msg.mm("<gray> left."));
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
        PlayerRank rank = Cytosis.getCytonicNetwork().getPlayerRanks().get(uuid);
        Component message = Msg.mm("<dark_aqua>Friend » </dark_aqua>").append(rank.getPrefix().append(Component.text(Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid)))).append(Msg.mm("<gray> joined."));
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
        Cytosis.getNatsManager().sendFriendRequest(new FriendRequest(sender, recipient, Instant.now().plus(5, ChronoUnit.MINUTES)));
    }
}
