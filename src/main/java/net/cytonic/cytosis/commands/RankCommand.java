package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.ranks.PlayerRank;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import java.util.Locale;
import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class RankCommand extends Command {

    public RankCommand() {
        super("rank");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.rank"));
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<red>You must specify a valid player and rank!"));

        var rankArg = ArgumentType.Enum("rank", PlayerRank.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
        rankArg.setCallback((sender, exception) -> sender.sendMessage(STR."The rank \{exception.getInput()} is invalid!"));
        rankArg.setSuggestionCallback((_, _, suggestion) -> {
            for (PlayerRank rank : PlayerRank.values()) {
                suggestion.addEntry(new SuggestionEntry(rank.name().toLowerCase(Locale.ROOT), rank.getPrefix()));
            }
        });

        var playerArg = ArgumentType.Entity("player").onlyPlayers(true);
        playerArg.setCallback((sender, exception) -> sender.sendMessage(STR."The player \{exception.getInput()} doesn't exist!"));


        var group = ArgumentType.Group("rank-group", playerArg, rankArg);

        addSyntax((sender, context) -> {
            final Player player = context.get(group).get(playerArg).findFirstPlayer(sender);
            final PlayerRank newRank = context.get(group).get(rankArg);
            if (player == null) {
                sender.sendMessage(MM."<red>The player \{context.get(group).getRaw("player")} doesn't exist!");
                return;
            }
            if (player == sender) {
                sender.sendMessage(MM."<red>You cannot change your own rank!");
                return;
            }
            Cytosis.getDatabaseManager().getDatabase().getPlayerRank(player.getUuid()).whenComplete((rank, throwable) -> {
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
        }, group);
    }

    private void setRank(Player player, PlayerRank rank, CommandSender sender) {
        Cytosis.getDatabaseManager().getDatabase().setPlayerRank(player.getUuid(), rank).whenComplete((_, t) -> {
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