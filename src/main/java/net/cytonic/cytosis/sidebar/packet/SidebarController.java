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

public class SidebarController<P extends CytosisPlayer> {

    public static final String SIDEBAR_ID = "cytosis__sidebar";
    public static final String SIDEBAR_ABSOLUTE_ID_PREFIX = "L_";
    public static final int SIDEBAR_LINE_SIZE = 15;

    private final P player;
    private final HashMap<Integer, Integer> absoluteIDCache;

    private final byte[] claimCache;

    public SidebarController(P player) {
        this.player = player;
        this.absoluteIDCache = new HashMap<>();
        this.claimCache = new byte[3];
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

        for(int i = 0; i < 3; ++i) {
            this.claimCache[i] = 0;
        }
    }

    public void renameSidebar(Component text) {
        ScoreboardObjectivePacket packet = new ScoreboardObjectivePacket(SIDEBAR_ID, (byte) 2, text, null, null);

        this.player.sendPacket(packet);
    }

    public void moveComponent(int startAbsoluteID, int componentLength, int moveByScore) {
        int startScore = this.absoluteIDCache.get(startAbsoluteID);

        for(int i = 0; i < componentLength; ++i) {
            this.moveLine(startAbsoluteID + i, startScore + moveByScore + i);
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

    public void displaceAllBelow(int startAbsoluteID, int byOffset) {
        for(int absoluteID = startAbsoluteID; absoluteID < this.player.getCurrentSidebar().getTotalAbsoluteID(); ++absoluteID) {
            if(!this.absoluteIDCache.containsKey(absoluteID)) continue;

            int currentLine = this.absoluteIDCache.get(absoluteID);

            this.moveLine(absoluteID, currentLine + byOffset);
        }
    }

    public int getPreviousAbsoluteID(int absoluteID) {
        for(int i = absoluteID; i >= 0; --i) {
            if(this.absoluteIDCache.containsKey(i)) return i;
        }

        return 0;
    }

    public int getPreviousAbsoluteIDOffset(int absoluteID) {
        return this.absoluteIDCache.getOrDefault(this.getPreviousAbsoluteID(absoluteID), 0);
    }

    public void addLine(int absoluteID, int score, Component text) {
        UpdateScorePacket packet = new UpdateScorePacket(SIDEBAR_ABSOLUTE_ID_PREFIX + absoluteID, SIDEBAR_ID, score,
            text, null);

        this.markClaimed(score);
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

        this.markUnclaimed(this.absoluteIDCache.get(absoluteID));
        this.markClaimed(toScore);

        this.absoluteIDCache.put(absoluteID, toScore);

        this.player.sendPacket(packet);
    }

    public void removeLine(int absoluteID) {
        ResetScorePacket packet = new ResetScorePacket(SIDEBAR_ABSOLUTE_ID_PREFIX + absoluteID, SIDEBAR_ID);

        this.markUnclaimed(this.absoluteIDCache.get(absoluteID));
        this.absoluteIDCache.remove(absoluteID);

        this.player.sendPacket(packet);
    }

    public int getAmountOfTakenLines() {
        return this.absoluteIDCache.size();
    }

    public int getAmountOfFreeLines() {
        if(this.getAmountOfTakenLines() >= 15) {
            return 0;
        }

        return SIDEBAR_LINE_SIZE - this.getAmountOfTakenLines();
    }

    public int getFurthestLine() {
        for(int i = 0; i < SIDEBAR_LINE_SIZE; ++i) {
            if(!this.isLineClaimed(i)) {
                return i;
            }
        }

        return SIDEBAR_LINE_SIZE;
    }

    private void markClaimed(int line) {
        this.claimCache[line / 8] |= (byte) (1 << line % 8);
    }

    private void markUnclaimed(int line) {
        this.claimCache[line / 8] &= (byte) (1 << line % 8);
    }

    private boolean isLineClaimed(int line) {
        return (this.claimCache[line / 8] & (1 << line % 8)) == 0;
    }


}
