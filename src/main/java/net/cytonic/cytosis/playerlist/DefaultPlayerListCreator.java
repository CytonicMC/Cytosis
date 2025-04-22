package net.cytonic.cytosis.playerlist;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;
import net.kyori.adventure.text.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A class providing the default player list for Cytosis
 */
@NoArgsConstructor
public class DefaultPlayerListCreator implements PlayerlistCreator {

    private final Instant start = Instant.now();

    /**
     * Creates the default player list
     *
     * @param player the player for personalization
     * @return the list of columns whose size is {@code colCount}
     */
    @Override
    public List<Column> createColumns(CytosisPlayer player) {
        List<Column> columns = new ArrayList<>();
        columns.add(PlayerlistCreator.PLAYER_COLUMN.apply(player));

        columns.add(new Column(Msg.mm("<dark_aqua><b>     Server Info"), PlayerListFavicon.BLUE,
                Utils.list(new PlayerListEntry(Msg.mm("<dark_aqua>Uptime: " + DurationParser.unparse(start, " ")), 0),
                        new PlayerListEntry(Component.empty(), 1),
                        new PlayerListEntry(Msg.mm("<dark_aqua>Players: " + Cytosis.getOnlinePlayers().size()), 2),
                        new PlayerListEntry(Msg.mm("<dark_aqua>Version: " + Cytosis.VERSION), 3),
                        new PlayerListEntry(Msg.mm("<dark_aqua>ID: " + Cytosis.getRawID()), 4),
                        new PlayerListEntry(Msg.mm("<darK_aqua>Network Players: " + Cytosis.getCytonicNetwork().getOnlinePlayers().size()), 5)
                )));
        columns.add(new Column(Msg.mm("<yellow><b>     Player Info"), PlayerListFavicon.YELLOW, Utils.list(
                new PlayerListEntry(Msg.mm("<yellow>Rank: " + player.getRank().name()), 0),
                new PlayerListEntry(Msg.mm("<yellow>Ping: " + player.getLatency() + "ms"), 1),
                new PlayerListEntry(Msg.mm("<yellow>Locale: " + player.getLocale()), 2)
        )));

        return columns;
    }

    /**
     * Creates the header for the player
     *
     * @param player the player for personalization
     * @return the component to be displayed as the header
     */
    @Override
    public Component header(CytosisPlayer player) {
        return Msg.mm("<aqua><b>CytonicMC");
    }

    /**
     * Creates the footer for the player
     *
     * @param player the player for personalization
     * @return the component to be displayed as the footer
     */
    @Override
    public Component footer(CytosisPlayer player) {
        return Msg.mm("<aqua>mc.cytonic.net").appendNewline().append(Msg.mm("<gold>forums.cytonic.net"));
    }

    /**
     * Gets the column count
     *
     * @return the number of columns, between 1 and 4 inclusive
     */
    @Override
    public int getColumnCount() {
        return 3;
    }
}
