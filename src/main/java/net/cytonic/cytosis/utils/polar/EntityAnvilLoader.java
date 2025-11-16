package net.cytonic.cytosis.utils.polar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.entity.metadata.other.PaintingVariant;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Rotation;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.logging.Logger;

public class EntityAnvilLoader extends AnvilLoader {
    Path entitiesPath;

    public EntityAnvilLoader(@NotNull Path path) {
        super(path);
        this.entitiesPath = path.resolve("entities");
    }

    public EntityAnvilLoader(@NotNull String path) {
        this(Path.of(path));
    }

    @Override
    public @NotNull Chunk loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        Chunk future = super.loadChunk(instance, chunkX, chunkZ);

        final int regionX = CoordConversion.chunkToRegion(chunkX);
        final int regionZ = CoordConversion.chunkToRegion(chunkZ);

        File entityFile = entitiesPath.resolve("r." + regionX + "." + regionZ + ".mca").toFile();
        if (!entityFile.exists()) return future;
        try (AccessibleRegionFile regionFile = new AccessibleRegionFile(entityFile.toPath())) {
            CompoundBinaryTag data = regionFile.readChunkData(chunkX, chunkZ);
            if (data != null) {
                ListBinaryTag entitiesList = data.getList("Entities");
                entitiesList.forEach(entityTag -> {
                    Pos pos;
                    CompoundBinaryTag binaryTag = (CompoundBinaryTag) entityTag;
                    // read the entities namespace id from the binarytag
                    String id = binaryTag.getString("id");
                    EntityType entityType = EntityType.fromKey(id);
                    if (entityType == null) throw new RuntimeException("Unknown entity type from id " + id);


                    Entity entity = new Entity(entityType);

                    if (entityType == EntityType.PAINTING) {
                        pos = parseBlockPos(binaryTag.getIntArray("block_pos"), binaryTag.getList("Rotation"));
                        StringBinaryTag variantTag = (StringBinaryTag) binaryTag.get("variant");
                        RegistryKey<PaintingVariant> ref = MinecraftServer.getPaintingVariantRegistry()
                                .getKey(Key.key(variantTag.value()));
                        if (ref == null) {
                            Logger.error("Unknown painting variant: " + variantTag.value() + " - skipping entity");
                            return;
                        }
                        entity.set(DataComponents.PAINTING_VARIANT, ref);

                        ByteBinaryTag facingTag = (ByteBinaryTag) binaryTag.get("facing");
                        Direction dir;
                        byte facing = facingTag.value();
                        if (facing == 0) {
                            dir = Direction.SOUTH;
                        } else if (facing == 1) {
                            dir = Direction.WEST;
                        } else if (facing == 2) {
                            dir = Direction.NORTH;
                        } else if (facing == 3) {
                            dir = Direction.EAST;
                        } else {
                            Logger.warn("Unknown facing value: " + facing + " - skipping entity");
                            return;
                        }
                        PaintingMeta meta = (PaintingMeta) entity.getEntityMeta();
                        meta.setDirection(dir);
                        meta.setHasNoGravity(true);
                    } else if (entityType == EntityType.ITEM_FRAME || entityType == EntityType.GLOW_ITEM_FRAME) {
                        pos = parseBlockPos(binaryTag.getIntArray("block_pos"), binaryTag.getList("Rotation"));
                        ByteBinaryTag facingTag = (ByteBinaryTag) binaryTag.get("Facing");
                        Direction dir;
                        byte facing = facingTag.value();
                        if (facing == 0) {
                            dir = Direction.DOWN;
                        } else if (facing == 1) {
                            dir = Direction.UP;
                        } else if (facing == 2) {
                            dir = Direction.NORTH;
                        } else if (facing == 3) {
                            dir = Direction.SOUTH;
                        } else if (facing == 4) {
                            dir = Direction.WEST;
                        } else if (facing == 5) {
                            dir = Direction.EAST;
                        } else {
                            Logger.warn("Unknown facing value: " + facing + " - skipping entity");
                            return;
                        }
                        ItemFrameMeta meta = (ItemFrameMeta) entity.getEntityMeta();

                        boolean invisible;
                        ByteBinaryTag invisibleTag = (ByteBinaryTag) binaryTag.get("Invisible");
                        invisible = invisibleTag != null && invisibleTag.value() == 1;

                        ByteBinaryTag rotationTag = (ByteBinaryTag) binaryTag.get("ItemRotation");
                        if (rotationTag == null) {
                            Logger.warn("Missing item rotation value - skipping entity");
                            return;
                        }

                        CompoundBinaryTag itemTag = binaryTag.getCompound("Item");
                        if (!itemTag.isEmpty()) {
                            ItemStack i = ItemStack.fromItemNBT(itemTag);
                            meta.setItem(i);
                        }
                        meta.setRotation(Rotation.values()[rotationTag.value()]);
                        meta.setDirection(dir);
                        meta.setInvisible(invisible);
                        meta.setHasNoGravity(true);

                    } else {
                        Logger.warn("Unsupported entity type: " + id + " - skipping entity");
                        return;
                    }

                    entity.setInstance(instance, pos).thenAccept(unused -> {
                        entity.teleport(pos);
                    });
                });


            }

        } catch (IOException e) {
            Logger.error("Failed to read chunk file", e);
        }

        return future;
    }

    private Pos parsePos(ListBinaryTag posTag, ListBinaryTag rotationTag) {
        // convert the pos to a double[]
        double[] posList = new double[]{0, 0, 0};
        if (posTag != null) {
            for (int i = 0; i < posTag.size(); i++) {
                DoubleBinaryTag tag = (DoubleBinaryTag) posTag.get(i);
                posList[i] = tag.value();
            }
        }
        // convert the rotation to a float[]
        float[] rotationList = new float[]{0, 0};
        if (rotationTag != null) {
            for (int i = 0; i < rotationTag.size(); i++) {
                FloatBinaryTag tag = (FloatBinaryTag) rotationTag.get(i);
                rotationList[i] = tag.value();
            }
        }

        // convert the pos and rotation arrays into a Pos
        return new Pos(
                posList[0],
                posList[1],
                posList[2],
                rotationList[0],
                rotationList[1]
        );
    }

    private Pos parseBlockPos(int[] posTag, ListBinaryTag rotationTag) {
        // convert the pos to a double[]

        // convert the rotation to a float[]
        float[] rotationList = new float[]{0, 0};
        if (rotationTag != null) {
            for (int i = 0; i < rotationTag.size(); i++) {
                FloatBinaryTag tag = (FloatBinaryTag) rotationTag.get(i);
                rotationList[i] = tag.value();
            }
        }

        // convert the pos and rotation arrays into a Pos
        return new Pos(
                posTag[0],
                posTag[1],
                posTag[2],
                rotationList[0],
                rotationList[1]
        );
    }
}
