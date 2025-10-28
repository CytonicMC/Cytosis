package net.cytonic.cytosis.data.containers;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.cytonic.cytosis.Cytosis;

public record SnoopsContainer(Set<String> snoops) {

    public static SnoopsContainer fromJson(String json) {
        return Cytosis.GSON.fromJson(json, SnoopsContainer.class);
    }

    public SnoopsContainer without(String snoop) {
        return new SnoopsContainer(snoops.stream().filter(s -> !s.equals(snoop)).collect(Collectors.toSet()));
    }

    public SnoopsContainer with(String snoop) {
        HashSet<String> newSnoops = new HashSet<>(snoops);
        newSnoops.add(snoop);
        return new SnoopsContainer(newSnoops);
    }

    @Override
    public String toString() {
        return toJson();
    }

    public String toJson() {
        return Cytosis.GSON.toJson(this);
    }
}
