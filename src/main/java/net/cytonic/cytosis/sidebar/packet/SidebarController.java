package net.cytonic.cytosis.sidebar.packet;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ResetScorePacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class SidebarController<P extends CytosisPlayer> {

    public static final String SIDEBAR_ID = "cytosis__sidebar";
    public static final String SIDEBAR_ABSOLUTE_ID_PREFIX = "L_";

    private final P player;
    private final HashMap<Integer, Integer> absoluteIDCache;

    public SidebarController(P player) {
        this.player = player;
        this.absoluteIDCache = new HashMap<>();
    }

    public void createSidebar(Component text) {
        ScoreboardObjectivePacket objectivePacket = new ScoreboardObjectivePacket(SIDEBAR_ID, (byte) 0, text,
            ScoreboardObjectivePacket.Type.INTEGER, Sidebar.NumberFormat.blank());

        DisplayScoreboardPacket displayPacket = new DisplayScoreboardPacket((byte) 1, SIDEBAR_ID);

        this.player.sendPacket(objectivePacket);
        this.player.sendPacket(displayPacket);
    }

    public void deleteSidebar() {
        ScoreboardObjectivePacket packet = new ScoreboardObjectivePacket(SIDEBAR_ID, (byte) 1, null, null, null);

        this.player.sendPacket(packet);

        this.absoluteIDCache.clear();
    }

    public void renameSidebar(Component text) {
        ScoreboardObjectivePacket packet = new ScoreboardObjectivePacket(SIDEBAR_ID, (byte) 2, text, null, null);

        this.player.sendPacket(packet);
    }

    public void moveComponent(int startAbsoluteID, int componentLength, int scoreStart) {
        for(int i = 0; i < componentLength; ++i) {
            this.moveLine(startAbsoluteID + i, scoreStart + i);
        }
    }

    public void removeComponent(int startAbsoluteID, int componentLength) {
        for(int i = 0; i < componentLength; ++i) {
            this.removeLine(startAbsoluteID + i);
        }
    }

    public void addComponent(int startAbsoluteID, int scoreStart, @NotNull Collection<Component> component) {
       int i = 0;

       for(Component line : component) {
           this.addLine(startAbsoluteID + i, scoreStart + i, line);

           ++i;
       }
    }

    public void addLine(int absoluteID, int score, Component text) {
        UpdateScorePacket packet = new UpdateScorePacket(SIDEBAR_ABSOLUTE_ID_PREFIX + absoluteID, SIDEBAR_ID, score,
            text, null);

        this.absoluteIDCache.put(absoluteID, score);

        this.player.sendPacket(packet);
    }

    public void changeLineText(int absoluteID, Component text) {
        UpdateScorePacket packet = new UpdateScorePacket(SIDEBAR_ABSOLUTE_ID_PREFIX + absoluteID, SIDEBAR_ID,
            this.absoluteIDCache.get(absoluteID), text, null);

        this.player.sendPacket(packet);
    }

    public void moveLine(int absoluteID, int toScore) {
        UpdateScorePacket packet = new UpdateScorePacket(SIDEBAR_ABSOLUTE_ID_PREFIX + absoluteID, SIDEBAR_ID, toScore,
            null, null);

        this.absoluteIDCache.put(absoluteID, toScore);

        this.player.sendPacket(packet);
    }

    public void removeLine(int absoluteID) {
        ResetScorePacket packet = new ResetScorePacket(SIDEBAR_ABSOLUTE_ID_PREFIX + absoluteID, SIDEBAR_ID);

        this.absoluteIDCache.remove(absoluteID);

        this.player.sendPacket(packet);
    }


}
