package net.cytonic.cytosis.sideboard;

import java.util.List;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.CytosisContext;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

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
            return List.of(Msg.mm("<gray>" + CytosisContext.SERVER_ID),
                Msg.mm("<green>Players: " + Cytosis.CONTEXT.getComponent(CytonicNetwork.class)
                    .getOnlinePlayers()
                    .size()), Msg.mm(""), Msg.mm("Cytosis v" + Cytosis.VERSION), Msg.mm(""), Msg.mm("<green>Rank: ")
                    .append(Component.text(player.getRank()
                        .name(), (player.getRank()
                        .getTeamColor()))), Msg.mm("<green>Chat Channel<white>: " + player.getChatChannel()
                    .name()), Msg.mm(""), Msg.mm("<yellow>mc.cytonic.net"));
        } catch (Exception e) {
            Logger.error("error", e);
            return List.of(Msg.mm("<red>Failed to get server information!"));
        }
    }

    @Override
    public Component title(CytosisPlayer player) {
        return Msg.mm("<yellow><bold>Cytosis</bold></yellow>");
    }
}