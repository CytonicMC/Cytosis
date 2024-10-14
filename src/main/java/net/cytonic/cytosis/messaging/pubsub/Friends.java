package net.cytonic.cytosis.messaging.pubsub;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.enums.PlayerRank;
import net.cytonic.objects.Tuple;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A pub sub that handles player friend requests
 */
@NoArgsConstructor
public class Friends extends JedisPubSub {

    private static final Component line = MM."<st><dark_aqua>                                                                                 ";

    @Override
    public void onMessage(String channel, String message) {
        Tuple<UUID, UUID> tuple = Tuple.deserialize(message, Tuple.UUID_TYPE); // <sender, target>
        if (Cytosis.getCytonicNetwork() == null) {
            Logger.warn("Recieved friend request but cytonic network is not loaded!");
            return; // friend request system disabled
        }


        String targetName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(tuple.getSecond());
        String senderName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(tuple.getFirst());
        PlayerRank targetRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(tuple.getSecond());
        PlayerRank senderRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(tuple.getFirst());


        Component target = targetRank.getPrefix().append(Component.text(targetName));
        Component sender = senderRank.getPrefix().append(Component.text(senderName));

        switch (channel) {
            case RedisDatabase.FRIEND_REQUEST_SENT -> {
                for (Player player : Cytosis.getOnlinePlayers()) {
                    if (player.getUuid().equals(tuple.getSecond())) {
                        player.sendMessage(line);
                        player.sendMessage(sender.append(MM."<aqua> sent you a friend request! You have 5 minutes to accept it! ").append(getButtons(tuple.getFirst())));
                        player.sendMessage(line);
                    } else if (player.getUuid().equals(tuple.getFirst())) {
                        player.sendMessage(line);
                        player.sendMessage(MM."<aqua>You send a friend request to ".append(target).append(MM."<aqua>! They have 5 minutes to accept it!"));
                        player.sendMessage(line);
                    }
                }
            }
            case RedisDatabase.FRIEND_REQUEST_ACCEPTED -> {
                for (Player player : Cytosis.getOnlinePlayers()) {
                    if (player.getUuid().equals(tuple.getSecond())) {
                        player.sendMessage(line);
                        player.sendMessage(MM."<aqua>You accepted ".append(sender).append(MM."<aqua>'s friend request!"));
                        player.sendMessage(line);
                    } else if (player.getUuid().equals(tuple.getFirst())) {
                        player.sendMessage(line);
                        player.sendMessage(target.append(MM."<aqua> accepted your friend request!"));
                        player.sendMessage(line);
                    }
                }
            }
            case RedisDatabase.FRIEND_REQUEST_DECLINED -> {
                for (Player player : Cytosis.getOnlinePlayers()) {
                    if (player.getUuid().equals(tuple.getSecond())) {
                        player.sendMessage(line);
                        player.sendMessage(MM."<red>You declined ".append(sender).append(MM."<red>'s friend request!"));
                        player.sendMessage(line);
                    } else if (player.getUuid().equals(tuple.getFirst())) {
                        player.sendMessage(line);
                        player.sendMessage(target.append(MM."<red> declined your friend request!"));
                        player.sendMessage(line);
                    }
                }
            }
            case RedisDatabase.FRIEND_REQUEST_EXPIRED -> {
                for (Player player : Cytosis.getOnlinePlayers()) {
                    if (player.getUuid().equals(tuple.getSecond())) {
                        player.sendMessage(line);
                        player.sendMessage(MM."<aqua>Your friend request from ".append(sender).append(MM."<aqua> has expired!"));
                        player.sendMessage(line);
                    } else if (player.getUuid().equals(tuple.getFirst())) {
                        player.sendMessage(line);
                        player.sendMessage(MM."<aqua>Your friend request to ".append(target).append(MM."<aqua> has expired!"));
                        player.sendMessage(line);
                    }
                }
            }
            case RedisDatabase.FRIEND_REMOVED -> {
                for (Player player : Cytosis.getOnlinePlayers()) {
                    if (player.getUuid().equals(tuple.getSecond())) {
                        player.sendMessage(line);
                        player.sendMessage(sender.append(MM."<aqua> removed you from their friend list!"));
                        player.sendMessage(line);
                    } else if (player.getUuid().equals(tuple.getFirst())) {
                        player.sendMessage(line);
                        player.sendMessage(MM."<aqua>You removed ".append(target).append(MM." from your friend list!"));
                        player.sendMessage(line);
                    }
                }
            }
        }
    }

    private Component getButtons(UUID uuid) {
        return MM."<green><b><click:run_command:/friend accept \{uuid}>[Accept]</click></b></green> <red><b><click:run_command:/friend decline \{uuid}>[Decline]</click></b></red>";
    }
}
