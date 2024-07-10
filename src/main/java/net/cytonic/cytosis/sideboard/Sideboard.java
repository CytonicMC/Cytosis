package net.cytonic.cytosis.sideboard;
/*
 * This file is part of FastBoard, licensed under the MIT License.
 *
 * Copyright (c) 2019-2023 MrMicky
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class holds the data about player sideboards (Scoreboards)
 */
@SuppressWarnings("unused")
@Getter
public class Sideboard {
    private final Player player;
    private final String id;
    private final List<Component> scores = new ArrayList<>();
    private final List<Component> lines = new ArrayList<>();
    private Component title = emptyLine();
    private boolean deleted = false;
    String[] objective = new String[]{"\u00A70", "\u00A71", "\u00A72", "\u00A73", "\u00A74", "\u00A75", "\u00A76", "\u00A77", "\u00A78", "\u00A79", "\u00A7a", "\u00A7b", "\u00A7c", "\u00A7d", "\u00A7e", "\u00A7f"};

    /**
     * Creates a new Sideboard.
     *
     * @param player the owner of the scoreboard
     */
    public Sideboard(Player player) {
        this.player = Objects.requireNonNull(player, "player");
        this.id = STR."sb-\{Integer.toHexString(ThreadLocalRandom.current().nextInt())}";

        sendObjectivePacket(ObjectiveMode.CREATE);
        sendDisplayObjectivePacket();
    }

    /**
     * Update the scoreboard title.
     *
     * @param title the new scoreboard title
     * @throws IllegalStateException if {@link #delete()} was called before
     */
    public void updateTitle(@NotNull Component title) {
        if (this.title.equals(Objects.requireNonNull(title, "title"))) {
            return;
        }

        this.title = title;
        sendObjectivePacket(ObjectiveMode.UPDATE);
    }

    /**
     * Get the scoreboard lines.
     *
     * @return the scoreboard lines
     */
    public List<Component> getLines() {
        return new ArrayList<>(this.lines);
    }

    /**
     * Get the specified scoreboard line.
     *
     * @param line the line number
     * @return the line
     * @throws IndexOutOfBoundsException if the line is higher than {@code size}
     */
    public Component getLine(int line) {
        checkLineNumber(line, true, false);
        return this.lines.get(line);
    }

    /**
     * Get how a specific line's score is displayed. On 1.20.2 or below, the value returned isn't used.
     *
     * @param line the line number
     * @return the text of how the line is displayed
     * @throws IndexOutOfBoundsException if the line is higher than {@code size}
     */
    public Optional<Component> getScore(int line) {
        checkLineNumber(line, true, false);
        return Optional.ofNullable(this.scores.get(line));
    }

    /**
     * Update a single scoreboard line.
     *
     * @param line the line number
     * @param text the new line text
     * @throws IndexOutOfBoundsException if the line is higher than {@link #size() size() + 1}
     */
    public synchronized void updateLine(int line, Component text) {
        updateLine(line, text, null);
    }

    /**
     * Update a single scoreboard line including how its score is displayed.
     * The score will only be displayed on 1.20.3 and higher.
     *
     * @param line      the line number
     * @param text      the new line text
     * @param scoreText the new line's score, if null will not change current value
     * @throws IndexOutOfBoundsException if the line is higher than {@link #size() size() + 1}
     */
    public synchronized void updateLine(int line, Component text, Component scoreText) {
        checkLineNumber(line, false, false);

        if (line < size()) {
            this.lines.set(line, text);
            this.scores.set(line, scoreText);

            sendScorePacket(getScoreByLine(line), ScoreboardAction.CHANGE);
            return;
        }

        List<Component> newLines = new ArrayList<>(this.lines);
        List<Component> newScores = new ArrayList<>(this.scores);

        if (line > size()) {
            for (int i = size(); i < line; i++) {
                newLines.add(emptyLine());
                newScores.add(null);
            }
        }

        newLines.add(text);
        newScores.add(scoreText);

        updateLines(newLines, newScores);
    }

    /**
     * Remove a scoreboard line.
     *
     * @param line the line number
     */
    public synchronized void removeLine(int line) {
        checkLineNumber(line, false, false);

        if (line >= size()) {
            return;
        }

        List<Component> newLines = new ArrayList<>(this.lines);
        List<Component> newScores = new ArrayList<>(this.scores);
        newLines.remove(line);
        newScores.remove(line);
        updateLines(newLines, newScores);
    }

    /**
     * Update all the scoreboard lines.
     *
     * @param lines the new lines
     * @throws IllegalArgumentException if one line is longer than 30 chars on 1.12 or lower
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public void updateLines(Component... lines) {
        updateLines(Arrays.asList(lines));
    }

    /**
     * Update the lines of the scoreboard
     *
     * @param lines the new scoreboard lines
     * @throws IllegalArgumentException if one line is longer than 30 chars on 1.12 or lower
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public synchronized void updateLines(Collection<Component> lines) {
        updateLines(lines, null);
    }

    /**
     * Update the lines and how their score is displayed on the scoreboard.
     * The scores will only be displayed for servers on 1.20.3 and higher.
     *
     * @param lines  the new scoreboard lines
     * @param scores the set for how each line's score should be, if null will fall back to default (blank)
     * @throws IllegalArgumentException if one line is longer than 30 chars on 1.12 or lower
     * @throws IllegalArgumentException if lines and scores are not the same size
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public synchronized void updateLines(Collection<Component> lines, Collection<Component> scores) {
        Objects.requireNonNull(lines, "lines");
        checkLineNumber(lines.size(), false, true);

        if (scores != null && scores.size() != lines.size()) {
            throw new IllegalArgumentException("The size of the scores must match the size of the board");
        }

        List<Component> oldLines = new ArrayList<>(this.lines);
        this.lines.clear();
        this.lines.addAll(lines);

        List<Component> oldScores = new ArrayList<>(this.scores);
        this.scores.clear();
        this.scores.addAll(scores != null ? scores : Collections.nCopies(lines.size(), null));

        int linesSize = this.lines.size();
        if (oldLines.size() != linesSize) {
            List<Component> oldLinesCopy = new ArrayList<>(oldLines);

            if (oldLines.size() > linesSize) {
                for (int i = oldLinesCopy.size(); i > linesSize; i--) {
                    sendTeamPacket(i - 1, TeamMode.REMOVE);
                    sendScorePacket(i - 1, ScoreboardAction.REMOVE);
                    oldLines.removeFirst();
                }
            } else {
                for (int i = oldLinesCopy.size(); i < linesSize; i++) {
                    sendScorePacket(i, ScoreboardAction.CHANGE);
                    sendTeamPacket(i, TeamMode.CREATE, Component.empty(), Component.empty());
                }
            }
        }

        for (int i = 0; i < linesSize; i++) {
            if (!Objects.equals(getLineByScore(oldLines, i), getLineByScore(i))) {
                sendLineChange(i);
            }
            if (!Objects.equals(getLineByScore(oldScores, i), getLineByScore(this.scores, i))) {
                sendScorePacket(i, ScoreboardAction.CHANGE);
            }
        }
    }

    /**
     * Update how a specified line's score is displayed on the scoreboard. A null value will reset the displayed
     * text back to default. The scores will only be displayed for servers on 1.20.3 and higher.
     *
     * @param line the line number
     * @param text the text to be displayed as the score. if null, no score will be displayed
     * @throws IllegalArgumentException if the line number is not in range
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public synchronized void updateScore(int line, Component text) {
        checkLineNumber(line, true, false);
        this.scores.set(line, text);
        sendScorePacket(getScoreByLine(line), ScoreboardAction.CHANGE);
    }

    /**
     * Reset a line's score back to default (blank). The score will only be displayed for servers on 1.20.3 and higher.
     *
     * @param line the line number
     * @throws IllegalArgumentException if the line number is not in range
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public synchronized void removeScore(int line) {
        updateScore(line, null);
    }

    /**
     * Update how all lines' scores are displayed. A value of null will reset the displayed text back to default.
     * The scores will only be displayed for servers on 1.20.3 and higher.
     *
     * @param texts the set of texts to be displayed as the scores
     * @throws IllegalArgumentException if the size of the texts does not match the current size of the board
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public synchronized void updateScores(Component... texts) {
        updateScores(Arrays.asList(texts));
    }

    /**
     * Update how all lines' scores are displayed.  A null value will reset the displayed
     * text back to default (blank). Only available on 1.20.3+ servers.
     *
     * @param texts the set of texts to be displayed as the scores
     * @throws IllegalArgumentException if the size of the texts does not match the current size of the board
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public synchronized void updateScores(Collection<Component> texts) {
        Objects.requireNonNull(texts, "texts");

        if (this.scores.size() != this.lines.size()) {
            throw new IllegalArgumentException("The size of the scores must match the size of the board");
        }

        List<Component> newScores = new ArrayList<>(texts);
        for (int i = 0; i < this.scores.size(); i++) {
            if (Objects.equals(this.scores.get(i), newScores.get(i))) {
                continue;
            }

            this.scores.set(i, newScores.get(i));
            sendScorePacket(getScoreByLine(i), ScoreboardAction.CHANGE);
        }
    }

    /**
     * Get the scoreboard size (the number of lines).
     *
     * @return the size
     */
    public int size() {
        return this.lines.size();
    }

    /**
     * Delete this FastBoard, and will remove the scoreboard for the associated player if he is online.
     * After this, all uses of {@link #updateLines} and {@link #updateTitle} will throw an {@link IllegalStateException}
     *
     * @throws IllegalStateException if this was already called before
     */
    public void delete() {

        for (int i = 0; i < this.lines.size(); i++) {
            sendTeamPacket(i, TeamMode.REMOVE);
        }

        sendObjectivePacket(ObjectiveMode.REMOVE);
        this.deleted = true;
    }

    /**
     * Send a line change packet
     *
     * @param score the score to change
     */
    public void sendLineChange(int score) {
        Component line = getLineByScore(score);
        sendTeamPacket(score, TeamMode.UPDATE, line, Component.empty());
    }

    /**
     * Get an empty line
     * @return an empty line
     */
    public Component emptyLine() {
        return Component.empty();
    }

    private void checkLineNumber(int line, boolean checkInRange, boolean checkMax) {
        if (line < 0) {
            throw new IllegalArgumentException("Line number must be positive");
        }

        if (checkInRange && line >= this.lines.size()) {
            throw new IllegalArgumentException(STR."Line number must be under \{this.lines.size()}");
        }

        if (checkMax && line >= 16) {
            throw new IllegalArgumentException(STR."Line number is too high: \{line}");
        }
    }

    /**
     * Gets a score from a line by number
     * @param line the line
     * @return the score
     */
    public int getScoreByLine(int line) {
        return this.lines.size() - line - 1;
    }

    /**
     * Gets a line by the score number
     * @param score The score
     * @return The line by the score
     */
    public Component getLineByScore(int score) {
        return getLineByScore(this.lines, score);
    }

    /**
     * gets a line by score
     * @param lines The existing lines
     * @param score The score
     * @return The Line
     */
    public Component getLineByScore(List<Component> lines, int score) {
        return score < lines.size() ? lines.get(lines.size() - score - 1) : null;
    }

    /**
     * Sends the objective packet
     * @param mode with the mode
     */
    public void sendObjectivePacket(ObjectiveMode mode) {

        ScoreboardObjectivePacket packet;
        switch (mode.ordinal()) {
            case 0 ->
                    packet = new ScoreboardObjectivePacket(this.id, (byte) 0, this.title, ScoreboardObjectivePacket.Type.INTEGER, Sidebar.NumberFormat.blank());
            case 1 ->
                    packet = new ScoreboardObjectivePacket(this.id, (byte) 1, this.title, ScoreboardObjectivePacket.Type.INTEGER, Sidebar.NumberFormat.blank());
            case 2 ->
                    packet = new ScoreboardObjectivePacket(this.id, (byte) 2, this.title, ScoreboardObjectivePacket.Type.INTEGER, Sidebar.NumberFormat.blank());
            default -> throw new IllegalArgumentException(STR."Invalid mode: \{mode}");
        }

        sendPacket(packet);
    }

    /**
     * Sends a packet to display the objective
     */
    public void sendDisplayObjectivePacket() {
        sendPacket(new DisplayScoreboardPacket((byte) 1, this.id));
    }

    /**
     * Sends a score packet
     * @param score The score
     * @param action the action
     */
    public void sendScorePacket(int score, ScoreboardAction action) {
        sendModernScorePacket(score, action);
    }

    /**
     * Sends a score packet
     * @param score The score
     * @param action the action
     */
    private void sendModernScorePacket(int score, ScoreboardAction action) {
        String objName = objective[score];

        ServerPacket packet;

        if (action == ScoreboardAction.REMOVE) {
            packet = new ResetScorePacket(objName, this.id);
        } else {
            packet = new UpdateScorePacket(objName, this.id, score, null, Sidebar.NumberFormat.blank());
        }
        sendPacket(packet);
    }

    /**
     * Sends a team packet
     * @param score The score
     * @param mode the mode
     */
    public void sendTeamPacket(int score, TeamMode mode) {
        sendTeamPacket(score, mode, Component.empty(), Component.empty());
    }

    /**
     * Sends a team packet
     * @param score the score
     * @param mode the mode
     * @param prefix the prefix
     * @param suffix the suffix
     */
    public void sendTeamPacket(int score, TeamMode mode, Component prefix, Component suffix) {
        if (mode == TeamMode.ADD_PLAYERS || mode == TeamMode.REMOVE_PLAYERS) throw new UnsupportedOperationException();
        TeamsPacket.Action action;

        switch (mode) {
            case CREATE ->
                    action = new TeamsPacket.CreateTeamAction(Component.empty(), (byte) 2, TeamsPacket.NameTagVisibility.ALWAYS, TeamsPacket.CollisionRule.ALWAYS, NamedTextColor.WHITE, prefix, suffix, List.of(objective[score]));
            case UPDATE ->
                    action = new TeamsPacket.UpdateTeamAction(Component.empty(), (byte) 2, TeamsPacket.NameTagVisibility.ALWAYS, TeamsPacket.CollisionRule.ALWAYS, NamedTextColor.WHITE, prefix, suffix);
            case REMOVE -> action = new TeamsPacket.RemoveTeamAction();
            case null, default -> throw new UnsupportedOperationException();
        }

        sendPacket(new TeamsPacket(this.id + ':' + score, action));
    }

    @SuppressWarnings("UnstableApiUsage")
    private void sendPacket(ServerPacket packet) {
        if (this.deleted) throw new IllegalStateException("This Sideboard has been deleted");
        if (this.player.isOnline()) player.sendPacket(packet);
    }

    /**
     * Allowed modes for a scoreboard
     */
    public enum ObjectiveMode {
        /**
         * Create a new objective
         */
        CREATE,
        /**
         * Remove the objective
         */
        REMOVE,
        /**
         * Update the objective
         */
        UPDATE
    }

    /**
     * actions for team packets
     */
    public enum TeamMode {
        /**
         * Create a new team
         */
        CREATE,
        /**
         * Remove a team
         */
        REMOVE,
        /**
         * Update a team
         */
        UPDATE,
        /**
         * Add a player to a team
         */
        ADD_PLAYERS,
        /**
         * Remove a player from a team
         */
        REMOVE_PLAYERS
    }

    /**
     * Actions for scoreboards
     */
    public enum ScoreboardAction {
        /**
         * Change the scoreboard
         */
        CHANGE,
        /**
         * Remove the scoreboard
         */
        REMOVE
    }
}