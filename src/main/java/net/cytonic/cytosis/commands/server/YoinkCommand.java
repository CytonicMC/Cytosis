package net.cytonic.cytosis.commands.server;

import java.util.Set;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.protocol.publishers.SendPlayerToServerPacketPublisher;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.impl.notify.ChatMessageNotifyPacket;

public class YoinkCommand extends CytosisCommand {

    public YoinkCommand() {
        super("yoink", "pullhere", "warpplayertothiserver", "pullplayertothisserver");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("You need to specify a player!")));

        var playerArgument = ArgumentType.Word("player");
        playerArgument.setSuggestionCallback(
            (_, _, suggestion) -> Cytosis.get(CytonicNetwork.class).getOnlinePlayers()
                .getValues()
                .forEach(v -> suggestion.addEntry(new SuggestionEntry(v))));
        playerArgument.setCallback((sender, exception) -> sender.sendMessage(
            Component.text("The player " + exception.getInput() + " is invalid!", NamedTextColor.RED)));

        addSyntax(((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;

            String playerName = context.get(playerArgument);
            CytonicNetwork network = Cytosis.get(CytonicNetwork.class);
            UUID uuid = network.getLifetimeFlattened().getByValue(playerName.toLowerCase());
            if (uuid == null) {
                player.whoops("The player %s doesn't exist!", playerName);
                return;
            }

            if (uuid.equals(player.getUuid())) {
                player.whoops("You cannot do this to yourself!");
                return;
            }

            if (!network.getOnlinePlayers().containsKey(uuid)) {
                player.sendMessage(Msg.red("The player %s is not online!", playerName));
                return;
            }

            if (Cytosis.getPlayer(uuid).isPresent()) {
                player.whoops("The player %s is already on your server!", playerName);
                return;
            }

            Cytosis.get(SendPlayerToServerPacketPublisher.class)
                .sendPlayerToServer(uuid, Cytosis.CONTEXT.currentServer());
            player.sendMessage(Msg.goldSplash("YOINK!", "Successfully warped to your server!"));
            Component component = Msg.splash("YOINKED!", "be9e25", "").append(player.formattedName())
                .append(Msg.grey(" pulled you to their server!"));
            new ChatMessageNotifyPacket.Packet(Set.of(uuid), ChatChannel.INTERNAL_MESSAGE, component, null).publish();
        }), playerArgument);
    }
}