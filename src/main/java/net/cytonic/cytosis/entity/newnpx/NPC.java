package net.cytonic.cytosis.entity.newnpx;

import lombok.Getter;
import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.entity.newnpx.configuration.NPCConfiguration;
import net.cytonic.cytosis.events.npcs.NPCInteractEvent;

public abstract class NPC {

    public static final int SPAWN_DISTANCE = 48;
    public static final int LOOK_DISTANCE = 5;
    public static final float HOLOGRAM_DISTANCE = 1.1f;

    @Getter
    private final NPCConfiguration config;
    @Getter
    private final Component name;

    public NPC(NPCConfiguration config) {
        this.config = config;
        String className = getClass().getSimpleName().replaceAll("NPC", "");
        this.name =
            config.chatName() != null ? config.chatName()
                : Component.text(className.replaceAll("(?<=.)(?=\\p{Lu})", " "));
    }

    public abstract void onClick(NPCInteractEvent event);
}