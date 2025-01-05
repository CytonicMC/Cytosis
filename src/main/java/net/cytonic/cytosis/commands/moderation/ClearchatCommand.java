package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.configuration.ResetChatPacket;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * The class representing the clearchat command
 */
public class ClearchatCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public ClearchatCommand() {
        super("clearchat", "cc");
        setCondition(CommandUtils.IS_MODERATOR);
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof CytosisPlayer player) {
                ResetChatPacket packet = new ResetChatPacket();
                for (CytosisPlayer online : Cytosis.getOnlinePlayers()) {
                    if (online.isStaff()) {
                        // don't actually clear the chat
                        online.sendMessage(MM."<green>Chat has been cleared by ".append(player.formattedName()).append(MM."<green>!"));
                    } else {
                        //todo: TEST THIS :)
                        online.getPlayerConnection().setConnectionState(ConnectionState.CONFIGURATION);
                        online.sendPacket(packet);
                        online.getPlayerConnection().setConnectionState(ConnectionState.PLAY);
                    }
                }
            } else {
                sender.sendMessage(MM."<red>Only players may execute this command :(");
            }
        });
    }
}