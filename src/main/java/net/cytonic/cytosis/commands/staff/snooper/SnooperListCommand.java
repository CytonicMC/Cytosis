package net.cytonic.cytosis.commands.staff.snooper;

import java.util.Set;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;

public class SnooperListCommand extends CytosisCommand {

    public SnooperListCommand() {
        super("list");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((s, _) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            boolean muted = player.getPreference(Preferences.MUTE_SNOOPER);

            Set<String> channels = player.getPreference(Preferences.LISTENING_SNOOPS).snoops();
            Component header = Msg.splash("SNOOPER LIST!", "e829aa", "» ")
                .append(muted ? Msg.mm("(MUTED)") : Component.text()).appendNewline();
            if (channels.isEmpty()) {
                header = header.append(Msg.red("You are not snooping on any channels right now."));
            } else {
                for (String channel : channels) {
                    header = header.append(Component.text("> " + channel, NamedTextColor.GRAY)).appendNewline();
                }
            }
            player.sendMessage(header);
        });
    }
}
