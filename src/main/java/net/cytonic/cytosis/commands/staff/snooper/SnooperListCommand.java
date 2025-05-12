package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Set;

public class SnooperListCommand extends CytosisCommand {
    public SnooperListCommand() {
        super("list");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((s, c) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            boolean muted = player.getPreference(CytosisNamespaces.MUTE_SNOOPER);

            Set<String> channels = player.getPreference(CytosisNamespaces.LISTENING_SNOOPS).snoops();
            Component header = Msg.splash("SNOOPER LIST!", "e829aa", "» ").append(muted ? Msg.mm("(MUTED)") : Component.text()).appendNewline();
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
