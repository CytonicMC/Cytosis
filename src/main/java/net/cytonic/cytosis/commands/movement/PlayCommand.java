package net.cytonic.cytosis.commands.movement;

import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.arguments.ArgumentWord;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.impl.objects.games.PlayProtocolObject;

public class PlayCommand extends CytosisCommand {

    public PlayCommand() {
        super("play");
        if (!Cytosis.getServer().serverType().asString().equals("lobby:lobby")) {
            unavailable(Msg.whoops("This command is only available in lobbies!"));
            return;
        }

        ArgumentWord word = new ArgumentWord("word").from("gg", "bw", "gilded", "bedwars", "gilded_gorge");
        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("Invalid Syntax! Usage: /play <game>")));
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;

            switch (context.get(word).toLowerCase()) {
                case "gg", "gilded", "gilded_gorge" -> {
                    new PlayProtocolObject.Packet(player.getUuid()).request("gorge.player.spawn_server", (val, t) -> {
                        if (t != null) {
                            player.sendMessage(
                                Msg.serverError("Failed to send you to Gilded Gorge! Please try again later."));
                            Logger.error("Failed to send player " + player.getUuid() + " to Gilded Gorge!", t);
                            return;
                        }
                        if (val.error() != null) {
                            player.sendMessage(
                                Msg.serverError("Failed to send you to Gilded Gorge! Please try again later."));
                            Logger.error("Failed to send player %s to Gilded Gorge. API Response: %s", player.getUuid(),
                                val.error());
                        }
                        player.sendToGenericServer(val.serverType(), "Gilded Gorge");
                    });
                }
                case "bw", "bedwars" -> //todo: Some sort of logic for game queuing
                    player.sendToGenericServer(Key.key("bedwars:lobby"), "Bedwars");
                default -> sender.sendMessage(Msg.whoops("Unknown game '%s'!", context.get(word)));
            }
        }, word);
    }
}