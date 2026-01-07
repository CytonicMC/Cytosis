package net.cytonic.cytosis.commands.moderation;

import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.enums.KickReason;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.packet.packets.PlayerKickPacket;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;
import net.cytonic.cytosis.utils.SnoopUtils;
import net.cytonic.protocol.data.objects.JsonComponent;

public class KickCommand extends CytosisCommand {

    public KickCommand() {
        super("kick");
        setCondition(CommandUtils.IS_MODERATOR);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.red("Usage: /kick <player> [reason]")));
        ArgumentStringArray reasonArg = ArgumentType.StringArray("reason");
        reasonArg.setDefaultValue(new String[]{"No", "reason", "specified."});

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                if (!actor.isModerator()) {
                    actor.sendMessage(Msg.red("You don't have permission to use this command!"));
                    return;
                }
                final String player = context.get(CommandUtils.NETWORK_PLAYERS);
                final String reason = String.join(" ", context.get(reasonArg));

                UUID uuid = PlayerUtils.resolveUuid(player);
                if (uuid == null) {
                    sender.sendMessage(Msg.whoops("The player '%s' either doesn't exist or is not online!", player));
                    return;
                }

                PlayerRank rank = Cytosis.get(CytonicNetwork.class).getCachedPlayerRanks().get(uuid);
                if (rank == null) {
                    sender.sendMessage(Msg.whoops("Failed to fine %s's rank!", player));
                    return;
                }

                if (rank.isStaffNotHelper()) {
                    sender.sendMessage(Msg.mm("<red>" + player + " cannot be kicked!"));
                    return;
                }

                Component snoop = actor.formattedName().append(Msg.grey("kicked "))
                    .append(SnoopUtils.toTarget(uuid))
                    .append(Msg.grey("for <yellow>" + reason + "</yellow>."));
                Cytosis.get(SnooperManager.class)
                    .sendSnoop(CytosisSnoops.PLAYER_KICK, Msg.snoop(snoop));
                new PlayerKickPacket(uuid, KickReason.COMMAND,
                    new JsonComponent(Msg.red("\nYou have been kicked. \n<aqua>Reason: " + reason))).publish();
            }
        }, CommandUtils.NETWORK_PLAYERS, reasonArg);
    }
}