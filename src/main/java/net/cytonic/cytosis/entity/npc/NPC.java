package net.cytonic.cytosis.entity.npc;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

import net.cytonic.cytosis.entity.npc.configuration.NPCConfiguration;
import net.cytonic.cytosis.entity.npc.dialogs.Dialog;
import net.cytonic.cytosis.events.npcs.NPCInteractEvent;
import net.cytonic.cytosis.player.CytosisPlayer;

public abstract class NPC {

    public static final int SPAWN_DISTANCE = 48;
    public static final int LOOK_DISTANCE = 5;

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

    @OverrideOnly
    public void onClick(NPCInteractEvent event) {
    }

    protected Dialog startDialog(CytosisPlayer player) {
        return new Dialog(this, player);
    }
}