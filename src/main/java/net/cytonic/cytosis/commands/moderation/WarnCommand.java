package net.cytonic.cytosis.commands.moderation;

import java.util.List;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.ChatMessage;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;
import net.cytonic.cytosis.utils.SnoopUtils;

public class WarnCommand extends CytosisCommand {

    public WarnCommand() {
        super("warn");
        setCondition(CommandUtils.IS_HELPER);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.red("Usage: /warn <player> [reason]")));
        ArgumentStringArray reasonArg = ArgumentType.StringArray("reason");
        reasonArg.setDefaultValue(new String[]{""});

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer actor) {
                final String player = context.get(CommandUtils.NETWORK_PLAYERS);
                final String reason = String.join(" ", context.get(reasonArg));
                UUID uuid = PlayerUtils.resolveUuid(player);
                if (uuid == null) {
                    sender.sendMessage(Msg.red("The player %s doesn't exist or is not online!",
                        context.get(CommandUtils.NETWORK_PLAYERS)));
                    return;
                }

                PlayerRank playerRank = Cytosis.CONTEXT.getComponent(CytonicNetwork.class).getCachedPlayerRanks()
                    .get(uuid);

                if (playerRank.isStaff()) {
                    sender.sendMessage(Msg.mm("<red>" + player + " cannot be warned!"));
                    return;
                }
                Component string = Component.empty();
                if (!reason.isEmpty()) {
                    string = Msg.mm("\n<aqua>Reason: " + reason);
                }
                actor.sendMessage(Msg.mm("<green>Warned " + player + ".").append(string));

                Component component = Msg.mm("<red>You have been warned.").append(string);
                Component snoop = actor.formattedName().append(Msg.grey(" warned "))
                    .append(SnoopUtils.toTarget(uuid)).append(Msg.grey(" for <yellow>" + reason + "</yellow>."));
                Cytosis.CONTEXT.getComponent(SnooperManager.class)
                    .sendSnoop(CytosisSnoops.PLAYER_WARN, Msg.snoop(snoop));
                Cytosis.CONTEXT.getComponent(NatsManager.class).sendChatMessage(
                    new ChatMessage(List.of(uuid), ChatChannel.INTERNAL_MESSAGE, Msg.toJson(component),
                        actor.getUuid()));
            }
        }, CommandUtils.NETWORK_PLAYERS, reasonArg);
    }
}