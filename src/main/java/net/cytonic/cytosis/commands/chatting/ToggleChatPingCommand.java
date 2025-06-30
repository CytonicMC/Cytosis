package net.cytonic.cytosis.commands.chatting;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.Msg;

public class ToggleChatPingCommand extends CytosisCommand {
    public ToggleChatPingCommand() {
        super("togglechatping");

        setDefaultExecutor((sender, ignored) -> {
            if (sender instanceof CytosisPlayer player) {
                if (player.getPreference(CytosisPreferences.CHAT_MESSAGE_PING)) {
                    player.updatePreference(CytosisPreferences.CHAT_MESSAGE_PING, false);
                    player.sendMessage(Msg.success("Chat message pings are now <red>disabled<grey>!"));
                } else {
                    player.updatePreference(CytosisPreferences.CHAT_MESSAGE_PING, true);
                    player.sendMessage(Msg.success("Chat message pings are now <green>enabled<grey>!"));
                }
            }
        });
    }
}
