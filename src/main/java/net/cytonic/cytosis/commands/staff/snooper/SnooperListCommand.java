package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;

import java.util.Set;

public class SnooperListCommand extends Command {
    public SnooperListCommand() {
        super("list");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((s, c) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            boolean muted = player.getPreference(CytosisNamespaces.MUTE_SNOOPER);

            Set<String> channels = player.getPreference(CytosisNamespaces.LISTENING_SNOOPS).snoops();
            Component header = Msg.mm("<b><#e829aa>SNOOPER LIST!</#e829aa></b><gray> » ").append(muted ? Msg.mm("(MUTED)") : Component.text()).appendNewline();
            if (channels.isEmpty()) {
                header = header.append(Msg.mm("<red>You arrent snooping on any channels right now."));
            } else {
                for (String channel : channels) {
                    header = header.append(Component.text("> " + channel, NamedTextColor.GRAY)).appendNewline();
                }
            }
            player.sendMessage(header);
        });
    }
}
