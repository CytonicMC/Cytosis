package net.cytonic.cytosis.data.containers;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

public record SnoopsContainer(Set<String> snoops) {

    public static final Codec<SnoopsContainer> CODEC = StructCodec.struct(
        "snoops", Codec.STRING.set(), SnoopsContainer::snoops,
        SnoopsContainer::new
    );

    public SnoopsContainer without(String snoop) {
        return new SnoopsContainer(snoops.stream().filter(s -> !s.equals(snoop)).collect(Collectors.toSet()));
    }

    public SnoopsContainer with(String snoop) {
        HashSet<String> newSnoops = new HashSet<>(snoops);
        newSnoops.add(snoop);
        return new SnoopsContainer(newSnoops);
    }
}
