package net.cytonic.cytosis.managers;

import lombok.NoArgsConstructor;
import net.cytonic.containers.friends.FriendRequest;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.Utils;
import net.cytonic.enums.PlayerRank;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.cytonic.cytosis.data.DatabaseTemplate.QUERY;
import static net.cytonic.cytosis.data.DatabaseTemplate.UPDATE;
import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A class to manage friends
 */
@NoArgsConstructor
public class FriendManager {
    private final ConcurrentHashMap<UUID, List<UUID>> friends = new ConcurrentHashMap<>();

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
        UPDATE."CREATE TABLE IF NOT EXISTS cytonic_friends (uuid VARCHAR(36), friends TEXT, PRIMARY KEY (uuid))".whenComplete((_, throwable) -> {
            if (throwable != null) Logger.error("An error occurred whilst creating the friends table!", throwable);
        });
        Cytosis.getOnlinePlayers().forEach(player -> loadFriends(player.getUuid()));
    }

    /**
     * Loads a player's friends into memory
     *
     * @param uuid The player
     */
    public void loadFriends(UUID uuid) {
        QUERY."SELECT friends FROM cytonic_friends WHERE uuid = '\{uuid.toString()}'".whenComplete((resultSet, throwable) -> {
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


        UPDATE."INSERT INTO cytonic_friends (uuid, friends) VALUES ('\{uuid}', '\{Cytosis.GSON.toJson(list)}') ON DUPLICATE KEY UPDATE friends = '\{Cytosis.GSON.toJson(list)}'".whenComplete((_, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst adding a friend!", throwable);
            }
        });

        // add the friend to the other player too
        List<UUID> list2 = friends.getOrDefault(friend, new ArrayList<>());
        list2.add(uuid);
        friends.put(friend, list2);

        UPDATE."INSERT INTO cytonic_friends (uuid, friends) VALUES ('\{friend}', '\{Cytosis.GSON.toJson(list2)}') ON DUPLICATE KEY UPDATE friends = '\{Cytosis.GSON.toJson(list2)}'".whenComplete((_, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst adding a friend!", throwable);
            }
        });
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

        UPDATE."INSERT INTO cytonic_friends (uuid, friends) VALUES ('\{uuid}', '\{Cytosis.GSON.toJson(list1)}') ON DUPLICATE KEY UPDATE friends = '\{Cytosis.GSON.toJson(list1)}'".whenComplete((_, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst removing a friend!", throwable);
            }
        });

        List<UUID> list2 = friends.getOrDefault(friend, new ArrayList<>());
        list2.remove(uuid);
        friends.put(friend, list2);

        UPDATE."INSERT INTO cytonic_friends (uuid, friends) VALUES ('\{friend}', '\{Cytosis.GSON.toJson(list2)}') ON DUPLICATE KEY UPDATE friends = '\{Cytosis.GSON.toJson(list2)}'".whenComplete((_, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst removing a friend!", throwable);
            }
        });

        Cytosis.getNatsManager().broadcastFriendRemoval(uuid, friend);
    }

    /**
     * Sends the player their list of friends
     *
     * @param player the player
     */
    public void listFriends(Player player) {
        if (getFriends(player.getUuid()).isEmpty()) {
            player.sendMessage(MM."<red><b>ERROR!</b></red> <gray>You have no friends :(");
            player.sendMessage(MM."<green><b>TIP!<b/></green> <gray>Perhaps you should invite some with /friend add <player>");
            return;
        }

        player.sendMessage(MM."<aqua><b>Friends List</b> <gray>(\{getFriends(player.getUuid()).size()}) <dark_gray>»</dark_gray>");

        for (UUID friend : getFriends(player.getUuid())) {
            PlayerRank rank = Cytosis.getCytonicNetwork().getPlayerRanks().get(friend);
            boolean online = Cytosis.getCytonicNetwork().getOnlinePlayers().containsKey(friend);
            String name = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(friend);
            player.sendMessage(MM."<dark_gray>  > </dark_gray>".append(rank.getPrefix().append(Component.text(name)).append(Component.text(" - ")).append(online ? MM."<green><b>ONLINE!" : MM." <red><b>OFFLINE :(")));
        }
    }

    /**
     * Sends the logout message for the player
     * @param uuid the player who logged out
     */
    public void sendLogoutMessage(UUID uuid) {
        PlayerRank rank = Cytosis.getCytonicNetwork().getPlayerRanks().get(uuid);
        Component message = MM."<dark_aqua>Friend » </dark_aqua>".append(rank.getPrefix().append(Component.text(Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid)))).append(MM."<gray> left.");
        Cytosis.getOnlinePlayers().forEach(player -> {
            if (getFriends(player.getUuid()).contains(uuid)) {
                player.sendMessage(message);
            }
        });
    }

    /**
     * Sends the login message for the player
     * @param uuid the player who logged in
     */
    public void sendLoginMessage(UUID uuid) {
        PlayerRank rank = Cytosis.getCytonicNetwork().getPlayerRanks().get(uuid);
        Component message = MM."<dark_aqua>Friend » </dark_aqua>".append(rank.getPrefix().append(Component.text(Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid)))).append(MM."<gray> joined.");
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
