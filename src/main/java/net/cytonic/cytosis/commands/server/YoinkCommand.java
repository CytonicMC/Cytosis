package net.cytonic.cytosis.commands.server;

import java.util.List;
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
import net.cytonic.cytosis.data.objects.JsonComponent;
import net.cytonic.cytosis.data.packet.packets.ChatMessagePacket;
import net.cytonic.cytosis.data.packet.publishers.SendPlayerToServerPacketPublisher;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class YoinkCommand extends CytosisCommand {

    public YoinkCommand() {
        super("yoink", "pullhere", "warpplayertothiserver", "pullplayertothisserver");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, context) -> sender.sendMessage(Msg.whoops("You need to specify a player!")));

        var playerArgument = ArgumentType.Word("player");
        playerArgument.setSuggestionCallback(
            (cmds, cmdc, suggestion) -> Cytosis.get(CytonicNetwork.class).getOnlinePlayers()
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
                sender.sendMessage(Msg.whoops("The player " + playerName + " doesn't exist!"));
                return;
            }

            if (uuid.equals(player.getUuid())) {
                player.sendMessage(Msg.whoops("You cannot do this to yourself!"));
                return;
            }

            if (!network.getOnlinePlayers().containsKey(uuid)) {
                player.sendMessage(Component.text("The player " + playerName + " is not online!", NamedTextColor.RED));
                return;
            }

            Cytosis.get(SendPlayerToServerPacketPublisher.class)
                .sendPlayerToServer(uuid, Cytosis.CONTEXT.currentServer());
            player.sendMessage(Msg.goldSplash("YOINK!", "Successfully warped to your server!"));
            Component component = Msg.splash("YOINKED!", "be9e25", "").append(player.formattedName())
                .append(Msg.mm("<gray> pulled you to their server!"));
            new ChatMessagePacket(List.of(uuid), ChatChannel.INTERNAL_MESSAGE, new JsonComponent(component),
                null).publish();
        }), playerArgument);
    }
}