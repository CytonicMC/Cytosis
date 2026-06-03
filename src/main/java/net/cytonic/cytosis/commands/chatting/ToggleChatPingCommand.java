package net.cytonic.cytosis.commands.chatting;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;

public class ToggleChatPingCommand extends CytosisCommand {

    public ToggleChatPingCommand() {
        super("togglechatping");

        setDefaultExecutor((sender, ignored) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            player.togglePreference(Preferences.CHAT_MESSAGE_PING, () ->
                    player.sendMessage(Msg.success("Chat message pings are now <green>enabled<grey>!")),
                () -> player.sendMessage(Msg.success("Chat message pings are now <red>disabled<grey>!")));
        });
    }
}
