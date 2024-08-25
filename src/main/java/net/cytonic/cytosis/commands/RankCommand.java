package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.enums.PlayerRank;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.Locale;
import java.util.Optional;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A command that allows players to change another player's rank
 */
public class RankCommand extends Command {

    /**
     * A command that allows authorized users to change player ranks
     */
    public RankCommand() {
        super("rank");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.rank"));

        var rankArg = ArgumentType.Enum("rank", PlayerRank.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
        rankArg.setCallback((sender, exception) -> sender.sendMessage(STR."The rank \{exception.getInput()} is invalid!"));
        rankArg.setSuggestionCallback((_, _, suggestion) -> {
            for (PlayerRank rank : PlayerRank.values()) {
                suggestion.addEntry(new SuggestionEntry(rank.name().toLowerCase(Locale.ROOT), rank.getPrefix()));
            }
        });

        var playerArg = ArgumentType.Word("player");
        playerArg.setSuggestionCallback((sender, _, suggestion) -> {
            if (sender instanceof Player player) {
                player.sendActionBar(MM."<green>Fetching online players...");
            }
            Cytosis.getCytonicNetwork().getOnlinePlayers().forEach(player ->
                    suggestion.addEntry(new SuggestionEntry(player.getValue())));
        });


        addSyntax((sender, context) -> {
            String name = context.get(playerArg);
            if (!Cytosis.getCytonicNetwork().getOnlinePlayers().containsValue(name)) {
                sender.sendMessage(MM."<red>The player \{context.get("player")} doesn't exist!");
                return;
            }
            Optional<Player> optionalPlayer = Cytosis.getPlayer(name);
            if (optionalPlayer.isEmpty()) {
                sender.sendMessage(MM."<red>You must be on the same server to set someone's rank! Use the /find command to find and go to their server.");
                return;
            }

            final Player player = optionalPlayer.get();
            final PlayerRank newRank = context.get(rankArg);

            if (player == sender) {
                sender.sendMessage(MM."<red>You cannot change your own rank!");
                return;
            }
            Cytosis.getDatabaseManager().getMysqlDatabase().getPlayerRank(player.getUuid()).whenComplete((rank, throwable) -> {
                if (throwable != null) {
                    sender.sendMessage("An error occurred whilst fetching the old rank!");
                    return;
                }

                // if it's a console we don't care (There isn't a console impl)
                if (sender instanceof Player s) {
                    PlayerRank senderRank = Cytosis.getRankManager().getPlayerRank(s.getUuid()).orElseThrow();
                    if (!PlayerRank.canChangeRank(senderRank, rank, newRank)) {
                        sender.sendMessage(MM."<red>You cannot do this!");
                        return;
                    }
                }

                setRank(player, newRank, sender);
            });
        }, playerArg, rankArg);
    }

    private void setRank(Player player, PlayerRank rank, CommandSender sender) {
        Cytosis.getDatabaseManager().getMysqlDatabase().setPlayerRank(player.getUuid(), rank).whenComplete((_, t) -> {
            if (t != null) {
                sender.sendMessage(MM."<red>An error occurred whilst setting \{player.getUsername()}'s rank! Check the console for more details.");
                Logger.error(STR."An error occurred whilst setting \{player.getUsername()}'s rank! Check the console for more details.", t);
                return;
            }
            Cytosis.getRankManager().changeRank(player, rank);
            sender.sendMessage(MM."<green>Successfully updated \{player.getUsername()}'s rank!");
        });
    }
}
