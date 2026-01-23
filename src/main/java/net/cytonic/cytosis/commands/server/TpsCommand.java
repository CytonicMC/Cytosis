package net.cytonic.cytosis.commands.server;

import java.util.Set;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.events.ServerEventListeners;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisPreferences;

public class TpsCommand extends CytosisCommand {

    public static final BossBar BAR = BossBar.bossBar(Component.empty(), 1, BossBar.Color.BLUE,
        BossBar.Overlay.NOTCHED_20, Set.of());

    public TpsCommand() {
        super("tps");
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            boolean newVal = player.getPreference(CytosisPreferences.TPS_DEBUG);
            player.updatePreference(CytosisPreferences.TPS_DEBUG, !newVal);
            if (newVal) {
                ServerEventListeners.TPS_CACHE.add(player.getUuid());
                player.showBossBar(BAR);
            } else {
                ServerEventListeners.TPS_CACHE.remove(player.getUuid());
                player.scheduleNextTick(_ -> player.hideBossBar(BAR));
            }
        });
    }
}
