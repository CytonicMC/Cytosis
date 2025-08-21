package net.cytonic.cytosis.npcs.dialogs;

import lombok.Getter;
import net.cytonic.cytosis.data.objects.Tuple;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Utils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unused")
public class Dialog {

    @Getter
    @Nullable
    private final Key id;

    private final List<Tuple<Component, Integer>> lines;
    @Getter
    private final boolean isStatic;

    @Getter
    private final boolean clickingAdvances;

    @Nullable
    private final Function<CytosisPlayer, List<Tuple<Component, Integer>>> generator;

    public Dialog(@Nullable Key id, List<Tuple<Component, Integer>> lines, boolean clickingAdvances) {
        this.id = id;
        this.lines = lines;
        this.clickingAdvances = clickingAdvances;
        this.generator = null;
        isStatic = true;
    }

    public Dialog(@Nullable Key id, @NotNull Function<CytosisPlayer, List<Tuple<Component, Integer>>> generator, boolean clickingAdvances) {
        this.id = id;
        this.lines = new ArrayList<>();
        this.clickingAdvances = clickingAdvances;
        this.generator = generator;
        isStatic = false;
    }


    public static Dialog evenDelay(@Nullable Key id, int ticks, boolean clickingAdvances, Collection<Component> lines) {
        List<Tuple<Component, Integer>> tuples = new ArrayList<>(lines.size());
        for (Component line : lines) {
            tuples.add(Tuple.of(line, ticks));
        }
        return new Dialog(id, tuples, clickingAdvances);
    }

    public static Dialog evenDelay(@Nullable Key id, int ticks, boolean clickingAdvances, Component... lines) {
        return evenDelay(id, ticks, clickingAdvances, Utils.list(lines));
    }

    public static Dialog evenDelay(@Nullable Key id, int ticks, boolean clickingAdvances, Function<CytosisPlayer, List<Component>> generator) {
        return new Dialog(id, generator.andThen(lines -> {
            List<Tuple<Component, Integer>> m = new ArrayList<>(lines.size());
            for (Component line : lines) {
                m.add(Tuple.of(line, ticks));
            }
            return m;
        }), clickingAdvances);
    }

    public List<Tuple<Component, Integer>> getLines(CytosisPlayer player) {
        if (generator != null) return generator.apply(player);
        return lines;
    }
}
