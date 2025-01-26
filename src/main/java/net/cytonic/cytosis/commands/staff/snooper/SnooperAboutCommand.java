package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.command.builder.Command;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class SnooperAboutCommand extends Command {
    public SnooperAboutCommand() {
        super("about");
        setDefaultExecutor((sender, ctx) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            player.sendMessage(
                    MM."<b><#e829aa>AOUT SNOOPER!</#e829aa></b><gray> »".appendNewline()
                            .append(MM."<gray> Snooper is a network wide system for listening to messages from various parts of the network. You need to opt into channels")
            );
        });
    }
}
