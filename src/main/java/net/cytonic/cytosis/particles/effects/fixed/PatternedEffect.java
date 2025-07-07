package net.cytonic.cytosis.particles.effects.fixed;

import lombok.AllArgsConstructor;
import net.cytonic.cytosis.particles.util.ParticleSupplier;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ParticlePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class PatternedEffect extends StaticEffect {
    private double particleWidth;
    private double particleHeight;
    private char[][] pattern;
    private Map<Character, ParticleSupplier> patternDictionary;
    private Pos pos;

    @Override
    public void play(PacketGroupingAudience audience) {
        if (pattern.length == 0 || pattern[0].length == 0) {
            throw new IllegalArgumentException("Pattern must be at least 1x1");
        }

        double totalHeight = pattern.length * particleHeight;


        List<ParticlePacket> packets = new ArrayList<>(); // deffer sendind until complete computation
        double y = totalHeight / 2;
        for (char[] chars : pattern) {
            y -= particleHeight;
            double totalWidth = chars.length * particleWidth;
            double x = -totalWidth / 2;
            for (char aChar : chars) {
                x += particleWidth;

                if (aChar == ' ') continue; // spaces are ignored
                if (!patternDictionary.containsKey(aChar))
                    throw new IllegalArgumentException("Pattern does not contain mapping: " + aChar);

                Pos p = pos.add(0, y, x);
                packets.add(patternDictionary.get(aChar).get().getPacket(p));
            }
        }
        packets.forEach(audience::sendGroupedPacket);
    }
}
