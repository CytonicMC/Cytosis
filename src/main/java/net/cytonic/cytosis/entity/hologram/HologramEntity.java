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

        editEntityMeta(TextDisplayMeta.class, meta -> {
            meta.setText(text);
            meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
            meta.setHasNoGravity(true);
            meta.setUseDefaultBackground(true);
            meta.setLineWidth(1000);
        });
    }

    public void setText(Component text) {
        this.text = text;

        editEntityMeta(TextDisplayMeta.class, meta -> {
            meta.setText(text);
            meta.setUseDefaultBackground(true);
            meta.setLineWidth(1000);
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos pos) {
        return super.setInstance(instance, pos);
    }
}
