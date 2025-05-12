package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class SnooperAboutCommand extends CytosisCommand {
    public SnooperAboutCommand() {
        super("about");
        setDefaultExecutor((sender, ctx) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            player.sendMessage(
                    Msg.splash("ABOUT SNOOPER!", "e829aa", "»").appendNewline()
                            .append(Msg.mm("<gray> Snooper is a network wide system for listening to messages from various parts of the network. You need to opt into channels"))
            );
        });
    }
}
