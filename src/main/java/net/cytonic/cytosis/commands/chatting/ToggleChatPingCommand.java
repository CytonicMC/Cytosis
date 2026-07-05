package net.cytonic.cytosis.commands.chatting;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Preferences;

public class ToggleChatPingCommand extends CytosisCommand {

    public ToggleChatPingCommand() {
        super("togglechatping");

        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            player.togglePreference(Preferences.CHAT_MESSAGE_PING,
                () -> player.enabledPlural("Chat message pings"),
                () -> player.disbledPlural("Chat message pings")
            );
        });
    }
}
