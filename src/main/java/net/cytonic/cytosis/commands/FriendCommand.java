package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.CytosisPreferences;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A command to handle friends
 */
public class FriendCommand extends Command {

    /**
     * Creates the command
     */
    public FriendCommand() {
        super("friend", "f");
        setCondition(Conditions::playerOnly);

        var action = ArgumentType.Word("action").from("add", "remove", "list", "accept", "decline");

        var playerArg = ArgumentType.Word("player").setDefaultValue("");
        playerArg.setSuggestionCallback((sender, context, suggestion) -> {
            if (sender instanceof Player player) {
                if (context.get(action).equalsIgnoreCase("add") ||
                        context.get(action).equalsIgnoreCase("decline") ||
                        context.get(action).equalsIgnoreCase("accept")) {

                    List<UUID> friends = Cytosis.getFriendManager().getFriends(player.getUuid());

                    for (String networkPlayer : Cytosis.getCytonicNetwork().getOnlinePlayers().getValues()) {
                        if (networkPlayer.equalsIgnoreCase(player.getUsername())) continue;
                        if (friends.contains(Cytosis.getCytonicNetwork().getOnlinePlayers().getByValue(networkPlayer)))
                            continue;
                        suggestion.addEntry(new SuggestionEntry(networkPlayer));
                    }
                }

                if (context.get(action).equalsIgnoreCase("remove")) {
                    for (UUID friend : Cytosis.getFriendManager().getFriends(player.getUuid())) {
                        suggestion.addEntry(new SuggestionEntry(Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(friend)));
                    }
                }
            }
        });


        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MM."<red><b>ERROR!</b></red> <gray>You must be a player to use this command!");
                return;
            }
            // actions that don't require a UUID:
            String actionStr = context.get(action).toLowerCase(Locale.ROOT);
            if (actionStr.equals("list")) {
                Cytosis.getFriendManager().listFriends(player);
                return;
            }

            if (context.get(playerArg).isEmpty()) {
                player.sendMessage(MM."<red><b>ERROR!</b></red> <gray>Please specify a player to use the subcommand '\{context.get(action)}'!");
                return;
            }

            UUID target;
            String name;
            try {
                target = UUID.fromString(context.get(playerArg));
                name = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(target);
            } catch (IllegalArgumentException e) {
                target = Cytosis.getCytonicNetwork().getLifetimeFlattened().getByValue(context.get(playerArg).toLowerCase());
                name = context.get(playerArg);
            }

            if (target == null) {
                player.sendMessage(MM."<red><b>ERROR!</b></red> <gray>The player '\{context.get(playerArg)}' does not exist!");
                return;
            }

            if (target.equals(player.getUuid())) {
                player.sendMessage(MM."<red><b>ERROR!</b></red> <gray>You cannot \{context.get(action)} yourself!");
                return;
            }

            Component targetComp = Cytosis.getCytonicNetwork().getPlayerRanks().get(target).getPrefix().append(Component.text(name));

            UUID finalTarget = target;
            switch (context.get(action).toLowerCase(Locale.ROOT)) {
                case "add" -> {
                    // check to see if they are online
                    if (!Cytosis.getCytonicNetwork().getOnlinePlayers().containsKey(target)) {
                        player.sendMessage(MM."<red><b>ERROR!</b></red> <gray>The player ".append(targetComp).append(MM."<gray> is not online!"));
                        return;
                    }

                    if (!Cytosis.getPreferenceManager().getPlayerPreference(target, CytosisPreferences.ACCEPT_FRIEND_REQUESTS)) {
                        player.sendMessage(MM."<red><b>ERROR!</b></red> ".append(targetComp).append(MM."<gray> is not accepting friend requests!"));
                        return;
                    }

                    if (Cytosis.getFriendManager().getFriends(player.getUuid()).contains(target)) {
                        player.sendMessage(MM."<red><b>ERROR!</b></red> <gray>You are already friends with ".append(targetComp).append(MM."<gray>!"));
                        return;
                    }

                    Cytosis.getCynwaveWrapper().sendFriendRequest(target, player.getUuid()).whenComplete((s, throwable) -> {
                        if (throwable != null) {
                            player.sendMessage(MM."<red><b>SERVER ERROR!</b></red> <gray>\{throwable.getMessage()}");
                        }
                        if (s.equalsIgnoreCase("ALREADY_SENT")) {
                            player.sendMessage(MM."<red><b>ERROR!</b></red> <gray>You have already sent a friend request to ".append(targetComp).append(MM."<gray>!"));
                        } else if (s.equalsIgnoreCase("INVALID_TOKEN")) {
                            player.sendMessage(MM."<red><b>SERVER ERROR!</b></red> <gray>\{s}");
                        }
                    });
                }
                case "remove" -> {
                    if (Cytosis.getFriendManager().getFriends(player.getUuid()).contains(target)) {
                        Cytosis.getFriendManager().removeFriend(player.getUuid(), target);
                    } else {
                        player.sendMessage(MM."<red><b>ERROR!</b></red> <gray>You are not friends with ".append(targetComp).append(MM."<gray>!"));
                    }
                }
                case "accept" ->
                        Cytosis.getCynwaveWrapper().acceptFriendRequest(player.getUuid(), target).whenComplete((s, throwable) -> {
                            if (throwable != null) {
                                player.sendMessage(MM."<red><b>SERVER ERROR!</b></red> <gray>Please contact an administrator. SERVER: \{Cytosis.SERVER_ID} TIME: \{Instant.now().toString()} \{throwable.getMessage()}");
                            }
                            switch (s) {
                                case "NOT_FOUND" ->
                                        player.sendMessage(MM."<red><b>ERROR!</b></red> <gray>You don't have an active friend request from ".append(targetComp).append(MM."<gray>!"));
                                case "UNAUTHORIZED" ->
                                        player.sendMessage(MM."<red><b>ERROR!</b></red> <gray>For some reason, you don't have permission to accept a friend request from ".append(targetComp).append(MM."<gray>!"));
                                case "OK" -> Cytosis.getFriendManager().addFriend(player.getUuid(), finalTarget);
                                case "INVALID_TOKEN" ->
                                        player.sendMessage(MM."<red><b>SERVER ERROR!</b></red> <gray>\{s}");
                            }
                        });
                case "decline" ->
                        Cytosis.getCynwaveWrapper().declineFriendRequest(player.getUuid(), target).whenComplete((s, throwable) -> {
                            if (throwable != null) {
                                player.sendMessage(MM."<red><b>SERVER ERROR!</b></red> <gray>\{throwable.getMessage()}");
                            }
                            if (s.equalsIgnoreCase("NOT_FOUND")) {
                                player.sendMessage(MM."<red><b>ERROR!</b></red> <gray>You don't have an active friend request from ".append(targetComp).append(MM."<gray>!"));
                            } else if (s.equalsIgnoreCase("INVALID_TOKEN")) {
                                player.sendMessage(MM."<red><b>SERVER ERROR!</b></red> <gray>\{s}");
                            }
                        });
            }

        }, action, playerArg);
    }
}
