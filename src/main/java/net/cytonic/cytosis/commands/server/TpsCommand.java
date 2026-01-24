package net.cytonic.cytosis.commands.server;

import java.util.Set;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;

public class TpsCommand extends CytosisCommand {

    public static final BossBar BAR = BossBar.bossBar(Component.empty(), 1, BossBar.Color.BLUE,
        Overlay.PROGRESS, Set.of());

    public TpsCommand() {
        super("tps");
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            boolean newVal = !player.getPreference(Preferences.TPS_DEBUG);
            player.updatePreference(Preferences.TPS_DEBUG, newVal);
            if (newVal) {
                player.sendMessage(Msg.success("Showing server performance metrics!"));
                player.showBossBar(BAR);
            } else {
                player.sendMessage(Msg.success("Hiding server performance metrics!"));
                player.scheduleNextTick(_ -> player.hideBossBar(BAR));
            }
        });
    }
}
