package net.cytonic.cytosis.commands.movement;

import net.minestom.server.command.builder.arguments.ArgumentWord;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.packet.publishers.SendPlayerToServerPacketPublisher;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class PlayCommand extends CytosisCommand {

    public PlayCommand() {
        super("play");
        ArgumentWord word = new ArgumentWord("word").from("gg", "bw", "gilded", "bedwars", "gilded_gorge");
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(Msg.whoops("Invalid Syntax! Usage: /play <game>"));
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            SendPlayerToServerPacketPublisher natsManager = Cytosis.get(SendPlayerToServerPacketPublisher.class);
            switch (context.get(word).toLowerCase()) {
                case "gg", "gilded", "gilded_gorge" -> natsManager
                    .sendPlayerToGenericServer(player.getUuid(), "gilded_gorge", "hub", "Gilded Gorge");
                case "bw", "bedwars" -> //todo: Some sort of logic for game queuing
                    natsManager.sendPlayerToGenericServer(player.getUuid(), "bedwars", "solos", "BedWars");
                default -> sender.sendMessage(Msg.whoops("Unknown game '%s'!", context.get(word)));
            }
        }, word);
    }
}