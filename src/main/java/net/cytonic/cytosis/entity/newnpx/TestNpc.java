package net.cytonic.cytosis.entity.newnpx;

import java.util.List;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;

import net.cytonic.cytosis.events.npcs.NPCInteractEvent;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class TestNpc extends NPC {

    protected TestNpc() {
        super(new HumanConfiguration() {
            @Override
            public String texture(CytosisPlayer player) {
                return null;
            }

            @Override
            public String signature(CytosisPlayer player) {
                return null;
            }

            @Override
            public List<Component> holograms(CytosisPlayer player) {
                return List.of(
                    Msg.mm("Line 1"),
                    Msg.mm("Line 2"),
                    Msg.mm("Line 3"),
                    Msg.mm("Line 4"),
                    Msg.mm("Line 5")
                );
            }

            @Override
            public Pos position(CytosisPlayer player) {
                return new Pos(0, 200, 0);
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().sendMessage(Msg.mm("On click!!!"));
        Logger.debug("Hello world!");
    }
}
