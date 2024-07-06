package net.cytonic.cytosis.managers;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.cytonic.cytosis.data.DatabaseTemplate.QUERY;
import static net.cytonic.cytosis.data.DatabaseTemplate.UPDATE;

/**
 * A class to manage friends
 */
public class FriendManager {

    /**
     * The default constructor
     */
    public FriendManager() {
        // do nothing
    }

    private final ConcurrentHashMap<UUID, List<UUID>> friends = new ConcurrentHashMap<>();

    /**
     * Gets a player's friends
     *
     * @param uuid The player
     * @return the list of UUIDs of the player friends
     */
    @Nullable
    public List<UUID> getFriends(UUID uuid) {
        return friends.get(uuid);
    }

    /**
     * Initializes the friends table and loads the online players friends
     */
    public void init() {
        UPDATE."CREATE TABLE IF NOT EXISTS cytonic_friends (uuid VARCHAR(36), friends TEXT)".whenComplete((_, throwable) -> {
            if (throwable != null) Logger.error("An error occurred whilst creating the friends table!", throwable);
        });
        Cytosis.getOnlinePlayers().forEach(player -> loadFriends(player.getUuid()));
    }

    /**
     * Loads a player's friends into memory
     * @param uuid The player
     */
    public void loadFriends(UUID uuid) {
        QUERY."SELECT friends FROM friends WHERE uuid = '\{uuid.toString()}'".whenComplete((resultSet, throwable) -> {
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
     * Adds a player as a friend
     * @param uuid The player
     * @param friend The friend
     */
    public void addFriend(UUID uuid, UUID friend) {
        List<UUID> list = friends.getOrDefault(uuid, new ArrayList<>());
        list.add(friend);
        friends.put(uuid, list);

        UPDATE."INSERT INTO cytonic_friends (uuid, friends) VALUES ('\{uuid}', '\{Cytosis.GSON.toJson(list)}') ON DUPLICATE KEY UPDATE friends = '\{Cytosis.GSON.toJson(list)}')".whenComplete((_, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst adding a friend!", throwable);
            }
        });
    }

    /**
     * Adds a player as a friend
     *
     * @param uuid   The player
     * @param friend The friend
     */
    public void removeFriend(UUID uuid, UUID friend) {
        List<UUID> list = friends.getOrDefault(uuid, new ArrayList<>());
        list.remove(friend);
        friends.put(uuid, list);

        UPDATE."INSERT INTO cytonic_friends (uuid, friends) VALUES ('\{uuid}', '\{Cytosis.GSON.toJson(list)}') ON DUPLICATE KEY UPDATE friends = '\{Cytosis.GSON.toJson(list)}')".whenComplete((_, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst removing a friend!", throwable);
            }
        });
    }
}
