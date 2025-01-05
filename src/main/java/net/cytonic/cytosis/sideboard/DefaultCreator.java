package net.cytonic.cytosis.sideboard;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.Component;

import java.net.InetAddress;
import java.util.List;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;


/**
 * The default implementation of {@link SideboardCreator}; creating a baseline sideboard for Cytosis.
 */
public class DefaultCreator implements SideboardCreator {

    /**
     * The default constructor
     */
    public DefaultCreator() {
    }

    @Override
    public Sideboard sideboard(CytosisPlayer player) {
        Sideboard sideboard = new Sideboard(player);
        sideboard.updateLines(lines(player));
        return sideboard;
    }

    @Override
    public List<Component> lines(CytosisPlayer player) {
        try {
            return List.of(
                    MM."<gray>\{Cytosis.SERVER_ID}",
                    MM."<green>Players: \{Cytosis.getCytonicNetwork().getOnlinePlayers().size()}",
                    MM."",
                    MM."Cytosis v\{Cytosis.VERSION}",
                    MM."",
                    MM."<green>Rank: ".append(Component.text(player.getRank().name(), (player.getRank().getTeamColor()))),
                    MM."<green>Chat Channel<white>: \{player.getChatChannel().name()}",
                    MM."",
                    MM."<yellow>\{InetAddress.getLocalHost().getHostAddress()}"
            );
        } catch (Exception e) {
            Logger.error("error", e);
            return List.of(MM."<red>Failed to get server information!");
        }
    }

    @Override
    public Component title(CytosisPlayer player) {
        return MM."<yellow><bold>Cytosis</bold></yellow>";
    }
}
