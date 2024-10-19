package net.cytonic.cytosis.player;

import lombok.Getter;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.enums.ChatChannel;
import net.cytonic.enums.PlayerRank;
import net.cytonic.objects.TypedNamespace;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A wrapper class for the {@link Player} object which includes a few more useful utilities that avoids calling the managers themselves.
 */
@Getter
@SuppressWarnings("unused")
public class CytosisPlayer extends Player {
    private PlayerRank rank;

    /**
     * Creates a new instance of a player
     *
     * @param uuid             the player's UUID
     * @param username         the player's Username
     * @param playerConnection the player's connection
     */
    public CytosisPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
        super(uuid, username, playerConnection);
        rank = Cytosis.getRankManager().getPlayerRank(uuid).orElse(PlayerRank.DEFAULT); // todo: watch out for cache invalidations
    }

    /**
     * Sets the player's rank.
     * @param rank the new rank
     */
    public void setRank(PlayerRank rank) {
        this.rank = rank;
        Cytosis.getRankManager().changeRank(this, rank);
    }


    /**
     * Updates the preference
     *
     * @param namespace the namespace of the preference
     * @param value     the value to set the preference
     * @param <T>       the type of the preference value
     * @throws IllegalArgumentException if the preference has not been registered with the {@link PreferenceManager}
     * @throws IllegalArgumentException if the preference and value are not of the same type
     */
    public <T> void updatePreference(TypedNamespace<T> namespace, T value) {
        Cytosis.getPreferenceManager().updatePlayerPreference(getUuid(), namespace, value);
    }

    /**
     * Gets a preference value
     *
     * @param namespace the namespace of the preference
     * @param <T>       the Type of the preference
     * @return the value stored in the preference
     * @throws IllegalArgumentException if the preference has not been registered with the {@link PreferenceManager}
     */
    public <T> T getPreference(TypedNamespace<T> namespace) {
        return Cytosis.getPreferenceManager().getPlayerPreference(getUuid(), namespace);
    }

    /**
     * Determines if the specified cooldown is active for the player
     *
     * @param namespace the namespace of the cooldown
     * @return the boolean true if the player is on cooldown
     */
    public boolean onNetworkCooldown(NamespaceID namespace) {
        return Cytosis.getNetworkCooldownManager().isOnPersonalCooldown(getUuid(), namespace);
    }

    /**
     * Sets the player's personal cooldown
     *
     * @param namespace the namespace of the cooldown
     * @param expiry    the instant the cooldown is to expire
     */
    public void setNetworkCooldown(NamespaceID namespace, Instant expiry) {
        Cytosis.getNetworkCooldownManager().setPersonal(getUuid(), namespace, expiry);
    }

    /**
     * Gets the expiry of this player's network cooldown
     *
     * @param namespace the namespace of the cooldown
     * @return the instant the specified cooldown expires. May be null if the player isn't on cooldown
     */
    @Nullable
    public Instant getNetworkCooldown(NamespaceID namespace) {
        return Cytosis.getNetworkCooldownManager().getPersonalExpiry(getUuid(), namespace);
    }

    /**
     * Executes the given command as the player
     *
     * @param command the command to execute
     * @return the result of processing the command
     */
    @SuppressWarnings("UnusedReturnValue")
    public CommandResult dispatchCommand(String command) {
        return Cytosis.getCommandManager().getDispatcher().execute(this, command);
    }

    /**
     * Gets the player's active chat channel
     *
     * @return the player's active chat channel
     */
    public ChatChannel getChatChannel() {
        return Cytosis.getChatManager().getChannel(getUuid());
    }

    /**
     * Sets the player's active chat channel
     *
     * @param channel the channel to set the player to
     */
    public void setChatChannel(ChatChannel channel) {
        Cytosis.getChatManager().setChannel(getUuid(), channel);
    }

    /**
     * Toggles the player's vanished state
     *
     * @param vanish if they should be vanished or not
     */
    public void setVanish(boolean vanish) {
        if (vanish) {
            Cytosis.getVanishManager().enableVanish(this);
        } else Cytosis.getVanishManager().disableVanish(this);
    }

    /**
     * Gets this player's list of friends
     *
     * @return the player's friends
     */
    public List<UUID> getFriends() {
        return Cytosis.getFriendManager().getFriends(getUuid());
    }

    /**
     * Instantly removes the friend from this player's list.
     *
     * @param uuid the friend to remove
     */
    public void removeFriend(UUID uuid) {
        Cytosis.getFriendManager().removeFriend(getUuid(), uuid);
    }

    /**
     * Instantly adds the player as a friend, <strong>bypassing the request phase entirely.</strong> No messages are sent to either player.
     *
     * @param uuid the player to add as a friend
     */
    public void addFriend(UUID uuid) {
        Cytosis.getFriendManager().addFriend(getUuid(), uuid);
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        Cytosis.getActionbarManager().addToQueue(getUuid(), message);
    }

    @Override
    public void sendActionBar(@NotNull ComponentLike message) {
        this.sendActionBar(message.asComponent());
    }

    public boolean isVanished() {
        return Cytosis.getVanishManager().isVanished(getUuid());
    }

    public void setVanished(boolean vanished) {
        if (vanished) {
            Cytosis.getVanishManager().enableVanish(this);
        } else {
            Cytosis.getVanishManager().disableVanish(this);
        }
    }
}
