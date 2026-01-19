package net.cytonic.cytosis.entity.newnpx;

import java.util.List;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;

import net.cytonic.cytosis.entity.newnpx.configuration.NPCConfiguration;
import net.cytonic.cytosis.events.npcs.NPCInteractEvent;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class TestNpc extends NPC {

    protected TestNpc() {
        super(new NPCConfiguration() {
            @Override
            public String texture(CytosisPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY3OTc0NTExNjU0MCwKICAicHJvZmlsZUlkIiA6ICJjYmFkZmRmNTRkZTM0N2UwODQ3MjUyMDIyYTFkNGRkZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJvRml3aSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kYzU2NWU5MTBjODA3ZGU5OWM0ZjlmMzhlMjUxZDVjYjE4NTI3OWViNTgzMGVjMjI3OTA3ZmQ5NjZkMmQ3YzAxIgogICAgfQogIH0KfQ==";
            }

            @Override
            public String signature(CytosisPlayer player) {
                return "OSpSjIuF5zmJP9h0DCp/K6hFqaLYL3ABhnXaDwft6IKVZUNQgXAM4yvKXzhiFpZtKXlQ87y7bdjadaeGymKvmwtX7+m9M4SDujPADxCXw8EoGs7xNDfMD0j0ACyPG/Dh2dC29wYoq+0RvGvj0Dkc6KBzfU/anCHq82PJ5VEvnmLuH83yA1uN3Q9miuQuS6AKtd/XjO8QbWV1Ba5WMkqtWOBcmij6p2zIcldg9/wwKaBc3uZi3NVw0oqTuNAvVOJ6KLZERb9sMMcQvjiwRy+2WqrWUyMVAglueviKoLMBe1n52OkpH8JMZUSB6usX4h5MeER+KSaQzfagKv21jer/BSyc5IPX4KtBONGJ0SvnQYnXvXtz8RJiM20+zgxlrHCihxIrhFSZ4//fwowgmVhfqhd8fPRFDMK6xv1z92YetVGVSWlIczNp83NduqR2EEm+oyh+u4wUhbPztge/gLvVGLcELXcwICz0oMx8HzrixpFJJawCTf9YrR3upnnsOujTfqnUSZlMMzUk/DSW+2hvw0DExckJwF2OYy7EORcnYqruGTCWy9s7jEct7hAsC/ukMTXXtAinCYCKYgbnzvxKCsLqa2xMoytr9Wp/BuN2xGyPAq3b2hjVls1ROtSu0PwiZROeE58v9wC+5yQai5sa/5LQ+JEjhQLYzj+tRtWmvvI=";
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
