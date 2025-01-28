package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.Command;

public class SnooperAboutCommand extends Command {
    public SnooperAboutCommand() {
        super("about");
        setDefaultExecutor((sender, ctx) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            player.sendMessage(
                    Msg.mm("<b><#e829aa>ABOUT SNOOPER!</#e829aa></b><gray> »").appendNewline()
                            .append(Msg.mm("<gray> Snooper is a network wide system for listening to messages from various parts of the network. You need to opt into channels"))
            );
        });
    }
}
