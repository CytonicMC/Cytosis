package net.cytonic.cytosis.commands.moderation;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

/**
 * The class representing the clearchat command
 */
public class ClearchatCommand extends CytosisCommand {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public ClearchatCommand() {
        super("clearchat", "cc");
        setCondition(CommandUtils.IS_MODERATOR);
        setDefaultExecutor((sender, ignored) -> {
            if (sender instanceof CytosisPlayer player) {
                for (CytosisPlayer online : Cytosis.getOnlinePlayers()) {
                    if (online.isStaff()) {
                        online.sendMessage(Msg.green("Chat has been cleared by ").append(player.formattedName())
                            .append(Msg.green("!")));
                    } else {
                        for (int i = 0; i < 250; i++) {
                            online.sendMessage("");
                        }
                    }
                }
                Component snoop = player.formattedName()
                    .append(Msg.mm("<gray> cleared the chat in server " + Cytosis.CONTEXT.SERVER_ID + "."));
                Cytosis.get(SnooperManager.class)
                    .sendSnoop(CytosisSnoops.CHAT_CLEAR, Msg.snoop(snoop));
            } else {
                sender.sendMessage(Msg.mm("<red>Only players may execute this command :("));
            }
        });
    }
}