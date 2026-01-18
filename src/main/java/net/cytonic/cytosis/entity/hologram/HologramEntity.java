package net.cytonic.cytosis.entity.hologram;

import java.util.concurrent.CompletableFuture;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

@Getter
public class HologramEntity extends Entity {

    private Component text;

    public HologramEntity(Component text) {
        super(EntityType.TEXT_DISPLAY);
        this.text = text;

        TextDisplayMeta meta = (TextDisplayMeta) this.getEntityMeta();

        setInvisible(true);

        meta.setNotifyAboutChanges(false);
        meta.setCustomNameVisible(false);
        meta.setText(text);
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setHasNoGravity(true);
        meta.setNotifyAboutChanges(true);
    }

    public void setText(Component text) {
        this.text = text;

        TextDisplayMeta meta = (TextDisplayMeta) this.getEntityMeta();

        meta.setNotifyAboutChanges(false);
        meta.setText(text);
        meta.setNotifyAboutChanges(true);
    }

    @Override
    public @NotNull CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos pos) {
        pos = pos.add(0, 1.3, 0);
        return super.setInstance(instance, pos);
    }
}
