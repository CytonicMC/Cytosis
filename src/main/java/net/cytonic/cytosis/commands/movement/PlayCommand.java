package net.cytonic.cytosis.commands.movement;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentWord;

public class PlayCommand extends Command {
    public PlayCommand() {
        super("play");
        ArgumentWord word = new ArgumentWord("word").from("gg", "bw", "gilded", "bedwars", "gilded_gorge");
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(Msg.whoops("Invalid Syntax! Usage: /play <game>"));
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            switch (context.get(word).toLowerCase()) {
                case "gg", "gilded", "gilded_gorge" ->
                        Cytosis.getNatsManager().sendPlayerToServer(player.getUuid(), "gilded_gorge", "hub", "Gilded Gorge");
                case "bw", "bedwars" ->//todo: Some sort of logic for game queuing
                        Cytosis.getNatsManager().sendPlayerToServer(player.getUuid(), "bedwars", "game_server", "BedWars");

            }
        }, word);
    }
}
