package net.cytonic.cytosis.playerlist;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.ranks.PlayerRank;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Utils;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class DefaultPlayerListCreator implements PlayerlistCreator {
    private final int colCount = 4;
    private final Instant start = Instant.now();

    @Override
    public List<Column> createColumns(Player player) {
        List<Column> columns = new ArrayList<>();

        List<PlayerListEntry> players = new ArrayList<>();

        for (Player p : Cytosis.getOnlinePlayers()) {
            PlayerRank rank = Cytosis.getRankManager().getPlayerRank(p.getUuid()).orElse(PlayerRank.DEFAULT);
            players.add(new PlayerListEntry(rank.getPrefix().append(p.getName()), rank.ordinal(),
                    new PlayerInfoUpdatePacket.Property("textures", p.getSkin().textures(), p.getSkin().signature())));
        }

        Column players1 = new Column(MM."<dark_purple><b>        Players    ", PlayerListFavicon.PURPLE);
        if (players.size() >= 19) {
            players1.setEntries(new ArrayList<>(players.subList(0, 19)));
            players = new ArrayList<>(players.subList(19, players.size()));
        } else {
            players1.setEntries(new ArrayList<>(players));
            players.clear();
        }


        Column players2 = new Column(MM."<dark_purple><b>        Players    ", PlayerListFavicon.PURPLE);
        if (players.size() >= 19) {
            int extra = players.size() - 19;
            players = new ArrayList<>(players.subList(0, 18));
            players.add(new PlayerListEntry(MM."<italic> + \{extra} more", 100));
        }
        players2.setEntries(players);
        columns.add(players1);
        columns.add(players2);

        columns.add(new Column(MM."<dark_aqua><b>     Server Info", PlayerListFavicon.BLUE,
                Utils.list(new PlayerListEntry(MM."<dark_aqua>Uptime: \{DurationParser.unparse(start, " ")}", 0),
                        new PlayerListEntry(Component.empty(), 1),
                        new PlayerListEntry(MM."<dark_aqua>Players: \{Cytosis.getOnlinePlayers().size()}", 2),
                        new PlayerListEntry(MM."<dark_aqua>Version: \{Cytosis.VERSION}", 3),
                        new PlayerListEntry(MM."<dark_aqua>ID: \{Cytosis.getRawID()}", 4),
                        new PlayerListEntry(MM."<darK_aqua>Network Players: \{Cytosis.getCytonicNetwork().getNetworkPlayers().size()}", 5)
                )));
        columns.add(new Column(MM."<yellow><b>     Player Info", PlayerListFavicon.YELLOW, Utils.list(
                new PlayerListEntry(MM."<yellow>Rank: \{Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElse(PlayerRank.DEFAULT).name()}", 0),
                new PlayerListEntry(MM."<yellow>Ping: \{player.getLatency()}ms", 1),
                new PlayerListEntry(MM."<yellow>Locale: \{player.getLocale()}", 2)
        )));

        return columns;
    }

    @Override
    public Component header(Player player) {
        return MM."<aqua><b>CytonicMC";
    }

    @Override
    public Component footer(Player player) {
        return MM."<aqua>mc.cytonic.net".appendNewline().append(MM."<gold>forums.cytonic.net");
    }

    @Override
    public int getColumnCount() {
        return colCount;
    }
}
