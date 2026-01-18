package net.cytonic.cytosis.commands.staff;

import java.util.Locale;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.GlobalDatabase;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;
import net.cytonic.protocol.notifyPackets.PlayerRankUpdateNotifyPacket;

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
        rankArg.setCallback(
            (sender, exception) -> sender.sendMessage("The rank " + exception.getInput() + " is invalid!"));
        rankArg.setSuggestionCallback((cmds, cmdc, suggestion) -> {
            for (PlayerRank rank : PlayerRank.values()) {
                suggestion.addEntry(new SuggestionEntry(rank.name().toLowerCase(Locale.ROOT), rank.getPrefix()));
            }
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            String name = context.get(CommandUtils.LIFETIME_PLAYERS).toLowerCase(Locale.ROOT);
            PlayerRank newRank = context.get(rankArg);
            UUID target = PlayerUtils.resolveUuid(name);
            if (target == null) {
                sender.sendMessage(
                    Msg.red("The player " + context.get(CommandUtils.LIFETIME_PLAYERS) + " doesn't exist!"));
                return;
            }

            if (player.getUuid().equals(target)) {
                sender.sendMessage(Msg.red("You cannot change your own rank!"));
                return;
            }

            Cytosis.get(GlobalDatabase.class).getPlayerRank(target).thenAccept(rank -> {
                if (!PlayerRank.canChangeRank(player.getRank(), rank, newRank)) {
                    sender.sendMessage(Msg.whoops("You cannot do this!"));
                    return;
                }
                setRank(target, rank, newRank, sender);
            }).exceptionally(throwable -> {
                sender.sendMessage("An error occurred whilst fetching the old rank!");
                return null;
            });
        }, CommandUtils.LIFETIME_PLAYERS, rankArg);
    }

    private void setRank(UUID uuid, PlayerRank oldRank, PlayerRank rank, CommandSender sender) {
        Component actor;
        if (!(sender instanceof CytosisPlayer player)) return;
        actor = player.trueFormattedName();

        String usr = Cytosis.get(CytonicNetwork.class).getLifetimePlayers().getByKey(uuid);
        Component usrComp = oldRank.getPrefix().append(Component.text(usr, oldRank.getTeamColor()));

        Component snoop = actor.append(Msg.mm("<gray> changed ")).append(usrComp).append(Msg.mm("<gray>'s rank to "))
            .append(rank.getPrefix().replaceText(builder -> builder.match(" ").replacement("")))
            .append(Msg.mm("<gray>."));

        Cytosis.get(SnooperManager.class).sendSnoop(CytosisSnoops.CHANGE_RANK, Msg.snoop(snoop));
        Cytosis.get(GlobalDatabase.class).setPlayerRank(uuid, rank).thenAccept(_ -> {
            new PlayerRankUpdateNotifyPacket.Packet(uuid, rank.name()).publish();
            sender.sendMessage(Msg.mm("<green>Successfully updated " + usr + "'s rank!"));
        }).exceptionally(throwable -> {
            sender.sendMessage(Msg.mm(
                "<red>An error occurred whilst setting " + uuid + "'s rank! Check the console for more details."));
            Logger.error("An error occurred whilst setting " + uuid + "'s rank! Check the console for more details.",
                throwable);
            return null;
        });
    }
}
