package net.cytonic.cytosis.playerlist;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration.State;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH;

/**
 * A creator for the playerlist, for use with {@link net.cytonic.cytosis.managers.PlayerListManager}
 */
public interface PlayerlistCreator {

    Function<CytosisPlayer, Column> PLAYER_COLUMN = (player) -> {
        List<PlayerListEntry> players = new ArrayList<>();
        for (CytosisPlayer p : Cytosis.getOnlinePlayers()) {
            if (p.isVanished()) {
                if (!player.isStaff()) {
                    continue;
                }
                Component nicked = p.isNicked() ? Msg.darkAqua(" \uD83C\uDFAD") : Component.empty();
                nicked = nicked.decoration(STRIKETHROUGH, State.FALSE).decoration(ITALIC, State.FALSE);
                players.add(new PlayerListEntry(
                    p.getRank().getPrefix().color(NamedTextColor.GRAY).decorate(STRIKETHROUGH, ITALIC)
                        .append(p.getTrueName().color(NamedTextColor.GRAY).decorate(STRIKETHROUGH, ITALIC))
                        .append(nicked),
                    p.getRank().ordinal(), new PlayerInfoUpdatePacket.Property("textures", p.getSkin()
                    .textures(), p.getTrueSkin().signature())));
                continue;
            }
            if (p.isNicked()) {
                PlayerRank rank = p.getTrueRank();
                if (player.getUuid().equals(p.getUuid())) {
                    players.add(new PlayerListEntry(rank.getPrefix()
                        .append(p.getTrueName().color(rank.getTeamColor())), rank.ordinal(),
                        new PlayerInfoUpdatePacket.Property("textures", p.getTrueSkin()
                            .textures(), p.getTrueSkin()
                            .signature())));
                    continue;
                }

                if (player.isStaff()) {
                    players.add(new PlayerListEntry(rank.getPrefix().append(p.getTrueName().color(rank.getTeamColor()))
                        .append(Msg.darkAqua(" \uD83C\uDFAD")), p.getRank()
                        .ordinal(), new PlayerInfoUpdatePacket.Property("textures", p.getTrueSkin()
                        .textures(), p.getTrueSkin()
                        .signature())));
                    continue;
                }
            }
            PlayerRank rank = p.getRank();
            players.add(new PlayerListEntry(p.formattedName(), rank.ordinal(),
                new PlayerInfoUpdatePacket.Property("textures", p.getSkin()
                    .textures(), p.getSkin()
                    .signature())));
        }

        Column playerCol = new Column(Msg.mm("<dark_purple><b>        Players    "), PlayerListFavicon.PURPLE);
        if (players.size() >= 19) {
            int extra = players.size() - 19;
            players = new ArrayList<>(players.subList(0, 18));
            players.add(new PlayerListEntry(Msg.mm("<italic> + " + extra + " more"), 100));
        } else {
            playerCol.setEntries(new ArrayList<>(players));
            players.clear();
        }
        return playerCol;
    };

    /**
     * Creates all the categories for the playerlist
     *
     * @param player The player to create {@link Column}s for
     * @return The list of categories
     */
    List<Column> createColumns(CytosisPlayer player);

    /**
     * Creates the header for the playerlist
     *
     * @param player The player to create the header for
     * @return The header in Component form
     */
    Component header(CytosisPlayer player);

    /**
     * creates the footer for the playerlist
     *
     * @param player The player to create the footer for
     * @return The footer in Component form
     */
    Component footer(CytosisPlayer player);

    /**
     * Gets the number of columns, between one and 4, inclusive.
     *
     * @return A number between 1 and 4  [1, 4]
     */
    int getColumnCount();
}
