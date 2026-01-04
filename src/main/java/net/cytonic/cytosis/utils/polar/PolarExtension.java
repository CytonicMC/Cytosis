package net.cytonic.cytosis.utils.polar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import net.hollowcube.polar.PolarWorldAccess;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.entity.metadata.other.PaintingVariant;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Holder;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Rotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.PosSerializer;

public class PolarExtension implements PolarWorldAccess {

    /*
     * FORMAT:
     * INT -> Number of entity entries
     * ------------------------------
     * (repeating)
     * INT -> size of the following array
     * BINARY -> The bytes of the serialized nbt
     * _____________________________
     *
     */

    private static void writeTag(@NotNull NetworkBuffer userData, CompoundBinaryTag binaryTag) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            BinaryTagIO.writer().write(binaryTag, out);
        } catch (IOException e) {
            throw new RuntimeException(e); // bad juju
        }
        byte[] serialized = out.toByteArray();
        userData.write(NetworkBuffer.INT, serialized.length);
        userData.write(NetworkBuffer.BYTE_ARRAY, serialized);
    }

    @Override
    public void loadWorldData(@NotNull Instance instance, @Nullable NetworkBuffer userData) {
        if (userData == null) return;

        int entityCount = userData.read(NetworkBuffer.INT);
        for (int i = 0; i < entityCount; i++) {
            int bytesToRead = userData.read(NetworkBuffer.INT);
            byte[] serialized = userData.read(NetworkBuffer.BYTE_ARRAY);
            if (serialized.length != bytesToRead) throw new RuntimeException("Invalid entity data length");
            CompoundBinaryTag binaryTag;
            try {
                binaryTag = BinaryTagIO.reader().read(new ByteArrayInputStream(serialized));
            } catch (IOException e) {
                throw new RuntimeException(e); // much badness
            }
            String type = binaryTag.getString("type");
            Pos pos = PosSerializer.deserializeFromTag(binaryTag.getCompound("pos"));
            if (type.equals("painting")) {
                String dir = binaryTag.getString("direction");
                String variant = binaryTag.getString("variant");
                Holder<PaintingVariant> var = MinecraftServer.getPaintingVariantRegistry().get(Key.key(variant));
                if (var == null) {
                    Logger.error("Unknown painting variant: " + variant + " - skipping entity");
                    return;
                }
                Entity entity = new Entity(EntityType.PAINTING);
                PaintingMeta meta = (PaintingMeta) entity.getEntityMeta();
                meta.setDirection(Direction.valueOf(dir));
                meta.setHasNoGravity(true);
                entity.set(DataComponents.PAINTING_VARIANT, var);
                MinecraftServer.getSchedulerManager().scheduleNextTick(() -> entity.setInstance(instance, pos));
            } else if (type.equals("item_frame") || type.equals("glow_item_frame")) {
                boolean isglowing = type.equals("glow_item_frame");
                String dir = binaryTag.getString("direction");
                String rotation = binaryTag.getString("rotation");
                ItemStack itemStack = ItemStack.fromItemNBT(binaryTag.getCompound("item"));

                Entity entity = new Entity(isglowing ? EntityType.GLOW_ITEM_FRAME : EntityType.ITEM_FRAME);
                ItemFrameMeta meta = (ItemFrameMeta) entity.getEntityMeta();
                meta.setHasNoGravity(true);
                meta.setRotation(Rotation.valueOf(rotation));
                meta.setDirection(Direction.valueOf(dir));
                meta.setItem(itemStack);
                MinecraftServer.getSchedulerManager().scheduleNextTick(() -> entity.setInstance(instance, pos));
            }
        }
    }

    @Override
    public void saveWorldData(@NotNull Instance instance, @NotNull NetworkBuffer userData) {
        final Set<EntityType> types = Set.of(EntityType.PAINTING, EntityType.ITEM_FRAME, EntityType.GLOW_ITEM_FRAME);
        final List<Entity> entities = instance.getEntities().stream()
            .filter(entity -> types.contains(entity.getEntityType())).toList();

        userData.write(NetworkBuffer.INT, entities.size());
        for (Entity entity : entities) {
            if (entity.getEntityType() == EntityType.PAINTING) {
                PaintingMeta meta = (PaintingMeta) entity.getEntityMeta();
                Holder<PaintingVariant> var = entity.get(DataComponents.PAINTING_VARIANT,
                    PaintingVariant.ALBAN); // default to something
                CompoundBinaryTag binaryTag = CompoundBinaryTag.builder()
                    .putString("type", "painting")
                    .put("pos", PosSerializer.serializeAsTag(entity.getPosition()))
                    .putString("direction", meta.getDirection().name())
                    .putString("variant", var.asKey().key().asString())
                    .build();
                writeTag(userData, binaryTag);
            } else if (entity.getEntityType() == EntityType.ITEM_FRAME
                || entity.getEntityType() == EntityType.GLOW_ITEM_FRAME) {
                boolean isglowing = entity.getEntityType() == EntityType.GLOW_ITEM_FRAME;
                ItemFrameMeta meta = (ItemFrameMeta) entity.getEntityMeta();
                CompoundBinaryTag binaryTag = CompoundBinaryTag.builder()
                    .putString("type", isglowing ? "glow_item_frame" : "item_frame")
                    .put("pos", PosSerializer.serializeAsTag(entity.getPosition()))
                    .putString("direction", meta.getDirection().name())
                    .put("item", meta.getItem().toItemNBT())
                    .putString("rotation", meta.getRotation().name())
                    .build();
                writeTag(userData, binaryTag);
            }
        }
    }
}
