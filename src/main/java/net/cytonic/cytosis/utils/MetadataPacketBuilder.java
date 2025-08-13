package net.cytonic.cytosis.utils;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.animal.*;
import net.minestom.server.entity.metadata.animal.tameable.CatVariant;
import net.minestom.server.entity.metadata.animal.tameable.WolfSoundVariant;
import net.minestom.server.entity.metadata.animal.tameable.WolfVariant;
import net.minestom.server.entity.metadata.other.PaintingVariant;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@SuppressWarnings({"unchecked", "unused"})
public class MetadataPacketBuilder {
    private final int entityID;
    private final Map<Integer, Metadata.Entry<?>> entries;

    private MetadataPacketBuilder(EntityMetaDataPacket packet) {
        this.entityID = packet.entityId();
        this.entries = new HashMap<>(packet.entries());
    }

    @SuppressWarnings("unused")
    private MetadataPacketBuilder() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public static MetadataPacketBuilder empty(int entityID) {
        return new MetadataPacketBuilder(new EntityMetaDataPacket(entityID, new HashMap<>()));
    }

    public static MetadataPacketBuilder builder(EntityMetaDataPacket packet) {
        return new MetadataPacketBuilder(packet);
    }

    public MetadataPacketBuilder setOnFire(boolean enable) {
        return setBitmask(enable, (byte) 0x01);
    }

    public MetadataPacketBuilder setSneaking(boolean enable) {
        return setBitmask(enable, (byte) 0x02);
    }

    public MetadataPacketBuilder setSprinting(boolean enable) {
        return setBitmask(enable, (byte) 0x08);
    }

    public MetadataPacketBuilder setSwimming(boolean enable) {
        return setBitmask(enable, (byte) 0x10);
    }

    public MetadataPacketBuilder setInvisible(boolean enable) {
        return setBitmask(enable, (byte) 0x20);
    }

    public MetadataPacketBuilder setGlowing(boolean enable) {
        return setBitmask(enable, (byte) 0x40);
    }

    public MetadataPacketBuilder setFlyingWithElytra(boolean enable) {
        return setBitmask(enable, (byte) 0x01);
    }

    public MetadataPacketBuilder setByte(int index, byte value) {
        entries.put(index, Metadata.Byte(value));
        return this;
    }

    public MetadataPacketBuilder setVarInt(int index, int value) {
        entries.put(index, Metadata.VarInt(value));
        return this;
    }

    public MetadataPacketBuilder setVarLong(int index, long value) {
        entries.put(index, Metadata.VarLong(value));
        return this;
    }

    public MetadataPacketBuilder setFloat(int index, float value) {
        entries.put(index, Metadata.Float(value));
        return this;
    }

    public MetadataPacketBuilder setString(int index, String value) {
        entries.put(index, Metadata.String(value));
        return this;
    }

    public MetadataPacketBuilder setTextComponent(int index, Component value) {
        entries.put(index, Metadata.Chat(value));
        return this;
    }

    public MetadataPacketBuilder setOptionalTextComponent(int index, Component value) {
        entries.put(index, Metadata.OptChat(value));
        return this;
    }

    /**
     * This is also known as an ItemStack!
     */
    public MetadataPacketBuilder setSlot(int index, ItemStack value) {
        entries.put(index, Metadata.ItemStack(value));
        return this;
    }

    public MetadataPacketBuilder setBoolean(int index, boolean value) {
        entries.put(index, Metadata.Boolean(value));
        return this;
    }

    public MetadataPacketBuilder setRotation(int index, Point value) {
        entries.put(index, Metadata.Rotation(value));
        return this;
    }

    /**
     * Also known as a Block Position!
     */
    public MetadataPacketBuilder setPosition(int index, Point value) {
        entries.put(index, Metadata.BlockPosition(value));
        return this;
    }

    /**
     * Also known as an Optional Block Position!
     */
    public MetadataPacketBuilder setOptionalPosition(int index, Point value) {
        entries.put(index, Metadata.OptBlockPosition(value));
        return this;
    }

    public MetadataPacketBuilder setDirection(int index, Direction value) {
        entries.put(index, Metadata.Direction(value));
        return this;
    }

    /**
     * Also known as an Optional UUID!
     */
    public MetadataPacketBuilder setOptionalLivingEntityReference(int index, UUID value) {
        entries.put(index, Metadata.OptUUID(value));
        return this;
    }

    public MetadataPacketBuilder setBlockState(int index, Block value) {
        entries.put(index, Metadata.BlockState(value));
        return this;
    }

    /**
     * @param value The value of the state in the block state registry!
     */
    public MetadataPacketBuilder setOptionalBlockState(int index, Integer value) {
        entries.put(index, Metadata.OptBlockState(value));
        return this;
    }

    public MetadataPacketBuilder setNBT(int index, BinaryTag value) {
        entries.put(index, Metadata.NBT(value));
        return this;
    }

    public MetadataPacketBuilder setParticle(int index, Particle value) {
        entries.put(index, Metadata.Particle(value));
        return this;
    }

    public MetadataPacketBuilder setParticles(int index, Particle... value) {
        entries.put(index, Metadata.ParticleList(Utils.list(value)));
        return this;
    }

    public MetadataPacketBuilder setParticles(int index, List<Particle> value) {
        entries.put(index, Metadata.ParticleList(value));
        return this;
    }

    public MetadataPacketBuilder setVillagerData(int index, VillagerMeta.VillagerData value) {
        entries.put(index, Metadata.VillagerData(value));
        return this;
    }

    public MetadataPacketBuilder setOptionalVarInt(int index, int value) {
        entries.put(index, Metadata.OptVarInt(value));
        return this;
    }

    public MetadataPacketBuilder setPose(int index, EntityPose value) {
        entries.put(index, Metadata.Pose(value));
        return this;
    }

    public MetadataPacketBuilder setCatVariant(int index, RegistryKey<@NotNull CatVariant> value) {
        entries.put(index, Metadata.CatVariant(value));
        return this;
    }

    public MetadataPacketBuilder setCowVariant(int index, RegistryKey<@NotNull CowVariant> value) {
        entries.put(index, Metadata.CowVariant(value));
        return this;
    }

    public MetadataPacketBuilder setWolfVariant(int index, RegistryKey<@NotNull WolfVariant> value) {
        entries.put(index, Metadata.WolfVariant(value));
        return this;
    }

    public MetadataPacketBuilder setWolfSoundVariant(int index, RegistryKey<@NotNull WolfSoundVariant> value) {
        entries.put(index, Metadata.WolfSoundVariant(value));
        return this;
    }

    public MetadataPacketBuilder SetFrogVariant(int index, RegistryKey<@NotNull FrogVariant> value) {
        entries.put(index, Metadata.FrogVariant(value));
        return this;
    }

    public MetadataPacketBuilder setPigVariant(int index, RegistryKey<@NotNull PigVariant> value) {
        entries.put(index, Metadata.PigVariant(value));
        return this;
    }

    public MetadataPacketBuilder setChickenVariant(int index, RegistryKey<@NotNull ChickenVariant> value) {
        entries.put(index, Metadata.ChickenVariant(value));
        return this;
    }

    public MetadataPacketBuilder setPaintingVariant(int index, RegistryKey<@NotNull PaintingVariant> value) {
        entries.put(index, Metadata.PaintingVariant(value));
        return this;
    }

    public MetadataPacketBuilder setSnifferState(int index, SnifferMeta.State value) {
        entries.put(index, Metadata.SnifferState(value));
        return this;
    }

    public MetadataPacketBuilder setArmadilloState(int index, ArmadilloMeta.State value) {
        entries.put(index, Metadata.ArmadilloState(value));
        return this;
    }

    /**
     * Used for things like Scale and Translation for display entities
     */
    public MetadataPacketBuilder setVector3(int index, Vec value) {
        entries.put(index, Metadata.Vector3(value));
        return this;
    }

    /**
     * Used for things like Scale for display entities
     *
     * @throws IllegalArgumentException if {@code value.length} doesn't equal 4
     */
    public MetadataPacketBuilder setQuaternion(int index, float[] value) {
        if (value.length != 4) throw new IllegalArgumentException();
        entries.put(index, Metadata.Quaternion(value));
        return this;
    }

    private MetadataPacketBuilder setBitmask(boolean enable, byte bit) {
        Metadata.Entry<@NotNull Byte> entry = (Metadata.Entry<@NotNull Byte>) entries.get(0);
        if (entry == null) {
            entry = Metadata.Byte(bit);
        }
        byte flags = entry.value();
        if (enable) {
            flags |= bit;
        } else {
            flags &= (byte) ~bit;
        }
        return setByte(0, flags);
    }

    public EntityMetaDataPacket build() {
        return new EntityMetaDataPacket(entityID, entries);
    }
}
