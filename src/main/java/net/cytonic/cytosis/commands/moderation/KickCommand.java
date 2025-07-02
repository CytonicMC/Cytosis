package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.enums.KickReason;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;
import net.cytonic.cytosis.utils.SnoopUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;

import java.util.UUID;


public class KickCommand extends CytosisCommand {

    public KickCommand() {
        super("kick");
        setCondition(CommandUtils.IS_MODERATOR);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.mm("<RED>Usage: /kick <player> [reason]")));
        var reasonArg = ArgumentType.StringArray("reason");
        reasonArg.setDefaultValue(new String[]{"No", "reason", "specified."});


        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                if (!actor.isModerator()) {
                    actor.sendMessage(Msg.mm("<red>You don't have permission to use this command!"));
                    return;
                }
                final String player = context.get(CommandUtils.NETWORK_PLAYERS);
                final String reason = String.join(" ", context.get(reasonArg));

                UUID uuid = PlayerUtils.resolveUuid(player);
                if (uuid == null) {
                    sender.sendMessage(Msg.whoops("The player '%s' either doesn't exist or is not online!", player));
                    return;
                }

                PlayerRank rank = Cytosis.getCytonicNetwork().getCachedPlayerRanks().get(uuid);
                if (rank == null) {
                    sender.sendMessage(Msg.whoops("Failed to fine %s's rank!", player));
                    return;
                }

                if (rank.isStaffNotHelper()) {
                    sender.sendMessage(Msg.mm("<red>" + player + " cannot be kicked!"));
                    return;
                }

                Component snoop = actor.formattedName().append(Msg.mm("<gray> kicked ")).append(SnoopUtils.toTarget(uuid)).append(Msg.mm("<gray> for <yellow>" + reason + "</yellow>."));
                Cytosis.getSnooperManager().sendSnoop(CytosisSnoops.PLAYER_KICK, Msg.snoop(snoop));
                Cytosis.getNatsManager().kickPlayer(uuid, KickReason.COMMAND, Msg.mm("\n<red>You have been kicked. \n<aqua>Reason: " + reason));

            }
        }, CommandUtils.NETWORK_PLAYERS, reasonArg);
    }
}
