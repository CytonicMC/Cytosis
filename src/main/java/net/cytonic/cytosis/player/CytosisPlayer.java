package net.cytonic.cytosis.player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.github.togar2.pvp.player.CombatPlayerImpl;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.CloseWindowPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.OpenWindowPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.ActionbarManager;
import net.cytonic.cytosis.managers.ChatManager;
import net.cytonic.cytosis.managers.FriendManager;
import net.cytonic.cytosis.managers.LocalCooldownManager;
import net.cytonic.cytosis.managers.NetworkCooldownManager;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.managers.RankManager;
import net.cytonic.cytosis.managers.VanishManager;
import net.cytonic.cytosis.nicknames.NicknameManager;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.cytosis.protocol.publishers.FriendPacketsPublisher;
import net.cytonic.cytosis.protocol.publishers.SendPlayerToServerPacketPublisher;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;
import net.cytonic.protocol.data.enums.KickReason;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.data.objects.Party;
import net.cytonic.protocol.impl.notify.PlayerKickNotifyPacket;
import net.cytonic.protocol.impl.objects.FriendApiProtocolObject;

/**
 * A wrapper class for the {@link Player} object which includes a few more useful utilities that avoids calling the
 * managers themselves.
 */
@SuppressWarnings("unused")
public class CytosisPlayer extends CombatPlayerImpl {

    private PlayerRank rank;

    /**
     * Creates a new instance of a player
     *
     * @param uuid             the player's UUID
     * @param username         the player's Username
     * @param playerConnection the player's connection
     */
    public CytosisPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
        this(playerConnection, new GameProfile(uuid, username));
        rank = Cytosis.get(RankManager.class).getPlayerRank(uuid).orElseGet(() -> {
            Logger.warn("The rank manager does not have a rank for " + uuid + ". Using default rank instead.");
            return PlayerRank.DEFAULT;
        });
    }

    public CytosisPlayer(@NotNull PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
        rank = Cytosis.get(RankManager.class).getPlayerRank(gameProfile.uuid()).orElseGet(() -> {
            Logger.warn(
                "The rank manager does not have a rank for " + gameProfile.name() + ". Using default rank instead.");
            return PlayerRank.DEFAULT;
        });
    }

    public PlayerRank getRank() {
        if (isNicked()) {
            return Cytosis.get(NicknameManager.class).getData(getUuid()).rank();
        }

        return getTrueRank();
    }

    /**
     * Sets the player's rank.
     *
     * @param rank the new rank
     */
    public void setRank(PlayerRank rank) {
        this.rank = rank;
        Cytosis.get(RankManager.class).changeRank(this, rank);
    }

    /**
     * Sets the player's rank. This is unsafe as it doesn't actually affect the database.
     *
     * @param rank the new rank
     */
    @Internal
    public void setRankUnsafe(PlayerRank rank) {
        this.rank = rank;
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
    public <T> void updatePreference(Preference<T> namespace, T value) {
        Cytosis.get(PreferenceManager.class).updatePlayerPreference(getUuid(), namespace, value);
    }

    /**
     * Gets all the possible preferences this player has. Some may not be registered in the {@link PreferenceManager}.
     *
     * @return the value stored in the preference
     */
    public Set<Key> getPreferenceKeys() {
        return Cytosis.get(PreferenceManager.class).getPlayerKeys(getUuid());
    }

    /**
     * Gets a preference value
     *
     * @param namespace the namespace of the preference
     * @param <T>       the Type of the preference
     * @return the value stored in the preference
     * @throws IllegalArgumentException if the preference has not been registered with the {@link PreferenceManager}
     */
    public <T> T getPreference(Preference<T> namespace) {
        return Cytosis.get(PreferenceManager.class).getPlayerPreference(getUuid(), namespace);
    }

    /**
     * Determines if the specified cooldown is active for the player
     *
     * @param namespace the namespace of the cooldown
     * @return the boolean true if the player is on cooldown
     */
    public boolean onNetworkCooldown(Key namespace) {
        return Cytosis.get(NetworkCooldownManager.class).isOnPersonalCooldown(getUuid(), namespace);
    }

    /**
     * Determines if the specified cooldown is active for the player, on this server
     *
     * @param namespace the namespace of the local cooldown
     * @return the boolean true if the player is on cooldown on this server
     */
    public boolean onLocalCooldown(Key namespace) {
        return Cytosis.get(LocalCooldownManager.class).isOnPersonalCooldown(getUuid(), namespace);
    }

    /**
     * Sets the player's personal cooldown
     *
     * @param namespace the namespace of the cooldown
     * @param expiry    the instant the cooldown is to expire
     */
    public void setNetworkCooldown(Key namespace, Instant expiry) {
        Cytosis.get(NetworkCooldownManager.class).setPersonal(getUuid(), namespace, expiry);
    }

    /**
     * Sets the player's personal local cooldown
     *
     * @param namespace the namespace of the local cooldown
     * @param expiry    the instant the cooldown is to expire
     */
    public void setLocalCooldown(Key namespace, Instant expiry) {
        Cytosis.get(LocalCooldownManager.class).setPersonalCooldown(getUuid(), namespace, expiry);
    }

    /**
     * Gets the expiry of this player's network cooldown
     *
     * @param namespace the namespace of the cooldown
     * @return the instant the specified cooldown expires. May be null if the player isn't on cooldown
     */
    @Nullable
    public Instant getNetworkCooldown(Key namespace) {
        return Cytosis.get(NetworkCooldownManager.class).getPersonalExpiry(getUuid(), namespace);
    }

    /**
     * Gets the expiry of this player's local cooldown
     *
     * @param namespace the namespace of the cooldown
     * @return the instant the specified cooldown expires. May be null if the player isn't on cooldown
     */
    @Nullable
    public Instant getLocalCooldown(Key namespace) {
        return Cytosis.get(LocalCooldownManager.class).getPersonalExpiry(getUuid(), namespace);
    }

    /**
     * Executes the given command as the player
     *
     * @param command the command to execute
     * @return the result of processing the command
     */
    @SuppressWarnings("UnusedReturnValue")
    public CommandResult dispatchCommand(String command) {
        return Cytosis.get(CommandManager.class).getDispatcher().execute(this, command);
    }

    /**
     * Gets the player's active chat channel
     *
     * @return the player's active chat channel
     */
    public ChatChannel getChatChannel() {
        return Cytosis.get(ChatManager.class).getChannel(getUuid());
    }

    /**
     * Sets the player's active chat channel
     *
     * @param channel the channel to set the player to
     */
    public void setChatChannel(ChatChannel channel) {
        Cytosis.get(ChatManager.class).setChannel(getUuid(), channel);
    }

    /**
     * Toggles the player's vanished state
     *
     * @param vanish if they should be vanished or not
     */
    public void setVanish(boolean vanish) {
        VanishManager vanishManager = Cytosis.get(VanishManager.class);
        if (vanish) {
            vanishManager.enableVanish(this);
        } else vanishManager.disableVanish(this);
    }

    /**
     * Gets this player's list of friends
     *
     * @return the player's friends
     */
    public Set<UUID> getFriends() {
        return Cytosis.get(FriendManager.class).getFriends(getUuid());
    }

    /**
     * Instantly removes the friend from this player's list.
     *
     * @param uuid the friend to remove
     */
    public void removeFriend(UUID uuid) {
        Cytosis.get(FriendManager.class).removeFriend(getUuid(), uuid);
    }

    /**
     * Instantly adds the player as a friend, <strong>bypassing the request phase entirely.</strong> No messages are
     * sent to either player.
     *
     * @param uuid the player to add as a friend
     */
    public void addFriend(UUID uuid) {
        Cytosis.get(FriendManager.class).addFriend(getUuid(), uuid);
    }

    @Override
    public void sendActionBar(@NotNull ComponentLike message) {
        this.sendActionBar(message.asComponent());
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        Cytosis.get(ActionbarManager.class).addToQueue(getUuid(), message);
    }

    /**
     * Gets the player skin.
     *
     * @return the player skin object, null means that the player has his {@link #getUuid()} default skin
     */
    @Override
    public @Nullable PlayerSkin getSkin() {
        if (isNicked()) {
            NicknameManager.NicknameData data = Cytosis.get(NicknameManager.class).getData(getUuid());
            return new PlayerSkin(data.value(), data.signature());
        }
        return super.getSkin();
    }

    /**
     * Gets the player's name as a component. This will either return the display name (if set) or a component holding
     * the username.
     *
     * @return the name
     */
    @Override
    public @NotNull Component getName() {
        return Component.text(getUsername());
    }

    @Override
    public @NotNull String getUsername() {
        if (isNicked()) {
            return Cytosis.get(NicknameManager.class).getData(getUuid()).nickname();
        }
        return getTrueUsername();
    }

    @Override
    public void kick(@NonNull Component message) {
        kick(KickReason.UNKNOWN, message);
    }

    /**
     * Returns if this player has helper (or higher) permissions
     *
     * @return If this player has helping permissions
     */
    public boolean isHelper() {
        return EnumSet.of(PlayerRank.OWNER, PlayerRank.MODERATOR, PlayerRank.HELPER).contains(getTrueRank());
    }

    public PlayerRank getTrueRank() {
        return rank;
    }

    /**
     * Returns this player's rank prefix followed by their name in the appropriate color.
     * <br>
     * Example: {@code [OWNER] Foxikle}
     *
     * @return The formatted name, including their rank prefix
     */
    public Component formattedName() {
        return getRank().getPrefix().append(Component.text(getUsername(), getRank().getTeamColor()));
    }

    /**
     * Returns this player's rank prefix followed by their name in the appropriate color.
     * <br>
     * Example: {@code [OWNER] Foxikle}
     *
     * @return The formatted name, including their rank prefix
     */
    public Component trueFormattedName() {
        return getTrueRank().getPrefix().append(Component.text(getTrueUsername(), getTrueRank().getTeamColor()));
    }

    public boolean canSendToChannel(ChatChannel channel) {
        return switch (channel) {
            case STAFF -> isStaff();
            case MOD -> isModerator();
            case ADMIN -> isAdmin();
            case PARTY -> isInParty() && (isStaff() || !getParty().isMuted() || getParty().hasAuthority(getUuid()));
            default -> true;
        };
    }

    public boolean canReceiveFromChannel(ChatChannel channel) {
        return switch (channel) {
            case STAFF -> isStaff();
            case MOD -> isModerator();
            case ADMIN -> isAdmin();
            case PARTY -> isInParty();
            default -> true;
        };
    }

    public boolean isStaff() {
        return EnumSet.of(PlayerRank.OWNER, PlayerRank.ADMIN, PlayerRank.MODERATOR, PlayerRank.HELPER)
            .contains(getTrueRank());
    }

    /**
     * Returns if this player has moderator (or higher) permissions
     *
     * @return If this player has moderation permissions
     */
    public boolean isModerator() {
        return EnumSet.of(PlayerRank.OWNER, PlayerRank.MODERATOR).contains(getTrueRank());
    }

    /**
     * Returns if this player has admin (or higher) permissions
     *
     * @return If this player has administrative permissions
     */
    public boolean isAdmin() {
        return EnumSet.of(PlayerRank.OWNER, PlayerRank.ADMIN).contains(getTrueRank());
    }

    public void sendFriendRequest(UUID recipient) {
        Cytosis.get(FriendPacketsPublisher.class)
            .sendFriendRequest(
                new FriendApiProtocolObject.Packet(getUuid(), recipient, Instant.now().plus(5, ChronoUnit.MINUTES)));
    }

    public void acceptFriendRequest(UUID sender) {
        Cytosis.get(FriendPacketsPublisher.class).sendAcceptFriendRequest(sender, getUuid());
    }

    public void acceptFriendRequestById(UUID requestId) {
        Cytosis.get(FriendPacketsPublisher.class).sendAcceptFriendRequest(requestId);
    }

    public void declineFriendRequest(UUID sender) {
        Cytosis.get(FriendPacketsPublisher.class).sendDeclineFriendRequest(sender, getUuid());
    }

    public void declineFriendRequestById(UUID requestId) {
        Cytosis.get(FriendPacketsPublisher.class).sendDeclineFriendRequest(requestId);
    }

    public boolean isVanished() {
        return Cytosis.get(VanishManager.class).isVanished(getUuid());
    }

    public void setVanished(boolean vanished) {
        VanishManager vanishManager = Cytosis.get(VanishManager.class);
        if (vanished) {
            vanishManager.enableVanish(this);
        } else {
            vanishManager.disableVanish(this);
        }
    }

    public boolean canReceiveSnoop(byte flags) {
        if ((flags & 0x01) != 0 && rank == PlayerRank.OWNER) {
            return true;
        }
        if ((flags & 0x02) != 0 && rank == PlayerRank.ADMIN) {
            return true;
        }
        if ((flags & 0x04) != 0 && rank == PlayerRank.MODERATOR) {
            return true;
        }
        return (flags & 0x08) != 0 && rank == PlayerRank.HELPER;
    }

    public boolean hasPlayedBefore() {
        return Cytosis.get(CytonicNetwork.class).hasPlayedBefore(getUuid());
    }

    public @NotNull Component getTrueName() {
        return Component.text(getTrueUsername());
    }

    public @Nullable PlayerSkin getTrueSkin() {
        return super.getSkin();
    }

    public @NotNull String getTrueUsername() {
        return super.getUsername();
    }

    public boolean isNicked() {
        return Cytosis.get(NicknameManager.class).isNicked(getUuid());
    }

    @Nullable
    public Party getParty() {
        return Cytosis.get(PartyManager.class).getPlayerParty(getUuid());
    }

    public boolean isInParty() {
        return getParty() != null;
    }

    @Override
    public void updateNewViewer(@NotNull Player p) {
        if (!(p instanceof CytosisPlayer player)) return;
        if (!isNicked()) {
            super.updateNewViewer(player);
        } else {
            Cytosis.get(NicknameManager.class)
                .sendNicknamePacketsToPlayer(this, player, getPreference(Preferences.NICKED_UUID), false);
        }
    }

    /**
     * Removes the player from this server, and possibly the entire network.
     *
     * @param reason  The reason to kick the player. If {@link KickReason#isRescuable()} is true, the proxy will try to
     *                reroute the player to a different server. Otherwise, the player's connection is terminated and the
     *                supplied message is shown.
     * @param message The message to send to the client. This may or may not be displayed to the client.
     */
    public void kick(KickReason reason, @Nullable Component message) {
        if (message == null) {
            message = Component.text("No reason specified.");
        }
        if (reason == null) {
            reason = KickReason.UNKNOWN;
        }
        new PlayerKickNotifyPacket.Packet(getUuid(), reason, new JsonComponent(message)).publish();
    }

    public void kickInternal(Component msg) {
        super.kick(msg);
    }

    public void sendToServer(String id) {
        CytonicServer server = Cytosis.get(CytonicNetwork.class).getServers().get(id);
        if (server == null) {
            sendMessage(Msg.whoops("Tried to send you to a non-existent server!"));
            return;
        }
        Cytosis.get(SendPlayerToServerPacketPublisher.class).sendPlayerToServer(getUuid(), server);
    }

    public void sendToGenericServer(Key key, @Nullable String name) {
        if (name == null) {
            name = "A server";
        }

        Cytosis.get(SendPlayerToServerPacketPublisher.class)
            .sendPlayerToGenericServer(getUuid(), key.namespace(), key.value(), name);
    }

    public void closeBook() {
        sendPacket(new OpenWindowPacket(100, 0, Component.empty()));
        sendPacket(new CloseWindowPacket(100));
    }
}
