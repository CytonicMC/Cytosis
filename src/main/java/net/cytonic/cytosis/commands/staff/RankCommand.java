package net.cytonic.cytosis.commands.staff;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.util.CommandUtils;
import net.cytonic.cytosis.commands.util.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.SnoopUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.util.Locale;
import java.util.Optional;

/**
 * A command that allows players to change another player's rank
 */
public class RankCommand extends CytosisCommand {

    /**
     * A command that allows authorized users to change player ranks
     */
    public RankCommand() {
        super("rank");
        setCondition(CommandUtils.withRank(PlayerRank.OWNER));

        var rankArg = ArgumentType.Enum("rank", PlayerRank.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
        rankArg.setCallback((sender, exception) -> sender.sendMessage("The rank " + exception.getInput() + " is invalid!"));
        rankArg.setSuggestionCallback((cmds, cmdc, suggestion) -> {
            for (PlayerRank rank : PlayerRank.values()) {
                suggestion.addEntry(new SuggestionEntry(rank.name().toLowerCase(Locale.ROOT), rank.getPrefix()));
            }
        });

        var playerArg = ArgumentType.Word("player");
        playerArg.setSuggestionCallback((sender, cmdc, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(Msg.mm("<green>Fetching online players..."));
            }
            Cytosis.getCytonicNetwork().getOnlinePlayers().forEach(player ->
                    suggestion.addEntry(new SuggestionEntry(player.getValue())));
        });


        addSyntax((sender, context) -> {
            String name = context.get(playerArg);
            if (!Cytosis.getCytonicNetwork().getOnlinePlayers().containsValue(name)) {
                sender.sendMessage(Msg.mm("<red>The player " + context.get("player") + " doesn't exist!"));
                return;
            }
            Optional<CytosisPlayer> optionalPlayer = Cytosis.getPlayer(name);
            if (optionalPlayer.isEmpty()) {
                sender.sendMessage(Msg.mm("<red>You must be on the same server to set someone's rank! Use the /find command to find and go to their server."));
                return;
            }

            final CytosisPlayer player = optionalPlayer.get();
            final PlayerRank newRank = context.get(rankArg);

            if (player == sender) {
                sender.sendMessage(Msg.mm("<red>You cannot change your own rank!"));
                return;
            }
            Cytosis.getDatabaseManager().getMysqlDatabase().getPlayerRank(player.getUuid()).whenComplete((rank, throwable) -> {
                if (throwable != null) {
                    sender.sendMessage("An error occurred whilst fetching the old rank!");
                    return;
                }

                // if it's a console we don't care (There isn't a console impl)
                if (sender instanceof CytosisPlayer s) {
                    PlayerRank senderRank = Cytosis.getRankManager().getPlayerRank(s.getUuid()).orElseThrow();
                    if (!PlayerRank.canChangeRank(senderRank, rank, newRank)) {
                        sender.sendMessage(Msg.mm("<red>You cannot do this!"));
                        return;
                    }
                }

                setRank(player, newRank, sender);
            });
        }, playerArg, rankArg);
    }

    private void setRank(CytosisPlayer player, PlayerRank rank, CommandSender sender) {
        Component actor;
        if (sender instanceof CytosisPlayer p) {
            actor = p.formattedName();
        } else {
            actor = Msg.mm("<red>UNKNOWN");
        }
        Component snoop = actor.append(Msg.mm("<gray> changed ")).append(player.formattedName()).append(Msg.mm("<gray>'s rank to ")
                .append(rank.getPrefix().replaceText(builder -> builder.match(" ").replacement("")))).append(Msg.mm("<gray>."));
        Cytosis.getSnooperManager().sendSnoop(CytosisSnoops.CHANGE_RANK, SnoopUtils.toSnoop(snoop));
        Cytosis.getDatabaseManager().getMysqlDatabase().setPlayerRank(player.getUuid(), rank).whenComplete((v, t) -> {
            if (t != null) {
                sender.sendMessage(Msg.mm("<red>An error occurred whilst setting " + player.getUsername() + "'s rank! Check the console for more details."));
                Logger.error("An error occurred whilst setting " + player.getUsername() + "'s rank! Check the console for more details.", t);
                return;
            }
            Cytosis.getRankManager().changeRank(player, rank);
            sender.sendMessage(Msg.mm("<green>Successfully updated " + player.getUsername() + "'s rank!"));
        });
    }
}
