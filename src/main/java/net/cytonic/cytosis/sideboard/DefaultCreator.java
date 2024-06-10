package net.cytonic.cytosis.sideboard;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.net.InetAddress;
import java.util.List;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * The default implementation of {@link SideboardCreator}, creating a baseline sideboard for Cytosis.
 */
public class DefaultCreator implements SideboardCreator {
    private static final int thingy = 0;

    @Override
    public Sideboard sideboard(Player player) {
        Sideboard sideboard = new Sideboard(player);
        sideboard.updateLines(lines(player));
        return sideboard;
    }

    @Override
    public List<Component> lines(Player player) {
        try {
            return List.of(
                    MM."<gray>\{Cytosis.SERVER_ID}",
                    MM."<green>Players: \{Cytosis.getCytonicNetwork().getNetworkPlayers().size()}",
                    MM."",
                    MM."Cytosis v\{Cytosis.VERSION}",
                    MM."",
                    MM."<green>Rank: ".append(Cytosis.getRankManager().getPlayerRank(player.getUuid()).isPresent() ? Component.text(Cytosis.getRankManager().getPlayerRank(player.getUuid()).get().name(), (Cytosis.getRankManager().getPlayerRank(player.getUuid()).get().getTeamColor())) : Component.text("Dunno.")),
                    MM."<green>Chat Channel<white>: \{Cytosis.getChatManager().getChannel(player.getUuid()).name()}",
                    MM."",
                    MM."<yellow>\{InetAddress.getLocalHost().getHostAddress()}"
            );
        } catch (Exception e) {
            Logger.error("error", e);
            return List.of(MM."<red>Failed to get server information!");

        }
    }

    @Override
    public Component title(Player player) {
        return MM."<yellow><bold>Cytosis</bold></yellow>";
    }
}
