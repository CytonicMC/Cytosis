package net.cytonic.cytosis.replay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataDef.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockSoundType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBuffer.Type;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.EntityPositionAndRotationPacket;
import net.minestom.server.network.packet.server.play.EntityPositionSyncPacket;
import net.minestom.server.network.packet.server.play.EntityRotationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket.Action;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket.Entry;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket.Property;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket.CollisionRule;
import net.minestom.server.network.packet.server.play.TeamsPacket.CreateTeamAction;
import net.minestom.server.network.packet.server.play.TeamsPacket.NameTagVisibility;
import net.minestom.server.particle.Particle;
import net.minestom.server.registry.RegistryData.BlockEntry;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.player.OfflinePlayer;
import net.cytonic.cytosis.replay.instance.ReplayInstance;
import net.cytonic.cytosis.utils.MetadataPacketBuilder;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Players;

import static net.minestom.server.network.NetworkBuffer.BYTE;

public interface ReplayEvent {


    Type<ReplayEvent> NETWORK_TYPE = NetworkBuffer.VAR_INT.unionType(i -> switch (i) {
        case 0 -> cast(PlayerJoin.NETWORK_TYPE);
        case 1 -> cast(PlayerLeave.NETWORK_TYPE);
        case 2 -> cast(PosRot.NETWORK_TYPE);
        case 3 -> cast(PlayerIdent.NETWORK_TYPE);
        case 4 -> cast(BlockUpdate.NETWORK_TYPE);
        case 5 -> cast(Animation.NETWORK_TYPE);
        case 6 -> cast(EquipmentUpdate.NETWORK_TYPE);
        case 7 -> cast(MetaUpdate.NETWORK_TYPE);
        case 8 -> cast(ChatMessage.NETWORK_TYPE);
        case 9 -> cast(Command.NETWORK_TYPE);
        case null, default -> throw new IllegalArgumentException("Unknown replay event id: " + i);
    }, ReplayEvent::getId);

    @SuppressWarnings("unchecked")
    private static <T extends ReplayEvent> NetworkBuffer.Type<ReplayEvent> cast(NetworkBuffer.Type<T> type) {
        return (NetworkBuffer.Type<ReplayEvent>) type;
    }

    int getId();

    int tick();

    void play(ReplayInstance instance);

    @Nullable
    default ReplayEvent inverse() {
        return null;
    }

    default int transposeId(int id) {
        return id + ReplayInstance.TRANSPOSAL_OFFSET; // hopefully there won't be this many entityIDs
    }

    /**
     * An event appended to every segment to ensure every segment has a running map of entityIDs to UUIDs
     */
    record PlayerIdent(int tick, Map<Integer, UUID> idents) implements ReplayEvent {

        public static final Type<PlayerIdent> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, PlayerIdent::tick,
            NetworkBuffer.VAR_INT.mapValue(NetworkBuffer.UUID), PlayerIdent::idents,
            PlayerIdent::new
        );

        @Override
        public int getId() {
            return 3;
        }

        @Override
        public void play(ReplayInstance instance) {
            // does nothing (only used by normalizer, removed there)
        }
    }

    record PlayerJoin(int tick, int entityId) implements ReplayEvent {

        public static final Type<PlayerJoin> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, PlayerJoin::tick,
            NetworkBuffer.VAR_INT, PlayerJoin::entityId,
            PlayerJoin::new
        );

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public void play(ReplayInstance i) {
            UUID packet = i.getTag(ReplayInstance.UUID_MAP).get(entityId);
            UUID uuid = i.getTag(ReplayInstance.IDENTS_MAP).get(entityId);
            OfflinePlayer op = Players.offline(uuid);

            SpawnEntityPacket spawn = new SpawnEntityPacket(transposeId(entityId), packet, EntityType.PLAYER, Pos.ZERO,
                0, 0, Vec.ZERO);

            PlayerSkin skin = PlayerSkin.fromUuid(uuid.toString());
            Property props = new Property("textures", skin.textures(), skin.signature());
            Entry e = new Entry(packet, getName(i), List.of(props),
                true, 0, GameMode.ADVENTURE, Msg.grey("⌜\uD83D\uDCFA⌟</gray>%s", op.username()), null, 1, true);
            PlayerInfoUpdatePacket info = new PlayerInfoUpdatePacket(Action.ADD_PLAYER, e);

            TeamsPacket createTeam = new TeamsPacket(packet.toString(),
                new CreateTeamAction(Component.empty(), (byte) 0, NameTagVisibility.ALWAYS, CollisionRule.ALWAYS,
                    NamedTextColor.GRAY, Msg.grey("⌜\uD83D\uDCFA⌟ "), getSuffix(i), List.of(getName(i))));

            EntityMetaDataPacket meta = MetadataPacketBuilder.empty(transposeId(entityId))
                .setByte(Player.DISPLAYED_MODEL_PARTS_FLAGS.index(), (byte) 127).build();
            i.sendPackets(info, spawn, meta, createTeam);
        }

        @Override
        @NonNull
        public ReplayEvent inverse() {
            return new PlayerLeave(tick, entityId);
        }

        private String getName(ReplayInstance i) {
            UUID uuid = i.getTag(ReplayInstance.IDENTS_MAP).get(entityId);
            OfflinePlayer op = Players.offline(uuid);
            if (op.username().length() > 14) {
                return op.username().substring(0, 14) + "§r";
            }
            return op.username() + "§r";
        }

        private Component getSuffix(ReplayInstance i) {
            UUID uuid = i.getTag(ReplayInstance.IDENTS_MAP).get(entityId);
            OfflinePlayer op = Players.offline(uuid);
            if (op.username().length() > 14) {
                return Msg.grey(op.username().substring(14));
            }
            return Component.empty();
        }
    }

    record PlayerLeave(int tick, int entityId) implements ReplayEvent {

        public static final Type<PlayerLeave> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, PlayerLeave::tick,
            NetworkBuffer.VAR_INT, PlayerLeave::entityId,
            PlayerLeave::new
        );

        @Override
        public int getId() {
            return 1;
        }

        @Override
        public void play(ReplayInstance i) {
            DestroyEntitiesPacket remove = new DestroyEntitiesPacket(transposeId(entityId));
            UUID player = i.getTag(ReplayInstance.UUID_MAP).get(entityId);
            PlayerInfoRemovePacket info = new PlayerInfoRemovePacket(player);
            i.sendPackets(info, remove);
        }

        @Override
        @NonNull
        public ReplayEvent inverse() {
            return new PlayerJoin(tick, entityId);
        }
    }

    record PosRot(int tick, Map<Integer, Snapshot> entries) implements ReplayEvent {

        public static final Type<PosRot> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, PosRot::tick,
            NetworkBuffer.VAR_INT.mapValue(Snapshot.NETWORK_TYPE, Integer.MAX_VALUE), PosRot::entries,
            PosRot::new
        );

        public PosRot(Map<Integer, Pos> deltas, Set<Integer> isAbsolute, int tick) {
            Map<Integer, Snapshot> entries = new HashMap<>(deltas.size());
            deltas.forEach((i, pos) -> entries.put(i, Snapshot.fromPos(pos, isAbsolute.contains(i))));
            this(tick, entries);
        }

        @Override
        public int getId() {
            return 2;
        }

        @Override
        public void play(ReplayInstance i) {
            entries.forEach((id, snapshot) -> {
                if (snapshot.isAbsolute()) {
                    Pos p = snapshot.toPos();
                    i.sendPackets(
                        new EntityPositionSyncPacket(transposeId(id), p, Pos.ZERO, p.yaw(), p.pitch(), true),
                        new EntityHeadLookPacket(transposeId(id), p.yaw())
                    );
                    return;
                }
                Pos p = snapshot.toShortPos();
                if (p.samePoint(Pos.ZERO)) {
                    i.sendPackets(
                        new EntityRotationPacket(transposeId(id), p.yaw(), p.pitch(), true),
                        new EntityHeadLookPacket(transposeId(id), p.yaw())
                    );
                    return;
                }

                i.sendPackets(
                    new EntityPositionAndRotationPacket(transposeId(id),
                        (short) p.x(),
                        (short) p.y(),
                        (short) p.z(),
                        p.yaw(),
                        p.pitch(),
                        true),
                    new EntityHeadLookPacket(transposeId(id), p.yaw())
                );
            });
        }

        @Override
        public @NotNull ReplayEvent inverse() {
            Map<Integer, Snapshot> inverted = new HashMap<>();
            entries.forEach((integer, snapshot) -> {
                inverted.put(integer, snapshot.invert());
            });
            return new PosRot(tick, inverted);
        }

        // coordinates are in 10,000ths of blocks
        // angles are in 100ths of degrees
        public record Snapshot(boolean isAbsolute, int dx, int dy, int dz, float yaw, float pitch) {

            public static final NetworkBuffer.Type<Snapshot> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.BOOLEAN, Snapshot::isAbsolute,
                NetworkBuffer.VAR_INT, (Snapshot s) -> zigzag(s.dx()),
                NetworkBuffer.VAR_INT, (Snapshot s) -> zigzag(s.dy()),
                NetworkBuffer.VAR_INT, (Snapshot s) -> zigzag(s.dz()),
                NetworkBuffer.BYTE, (Snapshot s) -> angleToByte(s.yaw()),
                NetworkBuffer.BYTE, (Snapshot s) -> angleToByte(s.pitch()),
                (isAbsolute, zx, zy, zz, yawB, pitchB) -> new Snapshot(
                    isAbsolute, unzigzag(zx), unzigzag(zy), unzigzag(zz),
                    byteToAngle(yawB), byteToAngle(pitchB)
                )
            );

            /**
             * Zigzagging reduces the encoded size of negative integers
             */
            private static int zigzag(int n) {
                return (n << 1) ^ (n >> 31);
            }

            private static int unzigzag(int n) {
                return (n >>> 1) ^ -(n & 1);
            }

            private static byte angleToByte(float degrees) {
                return (byte) (degrees * 256.0f / 360.0f);
            }

            private static float byteToAngle(byte b) {
                return (b & 0xFF) * 360.0f / 256.0f;
            }

            public static Snapshot fromPos(Pos delta, boolean isAbsolute) {
                int factor = isAbsolute ? 100 : 10_000;
                return new Snapshot(
                    isAbsolute,
                    (int) (delta.x() * factor),
                    (int) (delta.y() * factor),
                    (int) (delta.z() * factor),
                    (int) (delta.yaw()),
                    (int) (delta.pitch())
                );
            }

            public Pos toPos() {
                double factor = isAbsolute ? 100.0 : 10_000.0;
                return new Pos(dx / factor, dy / factor, dz / factor, yaw, pitch);
            }

            // only used with relatives
            public Pos toShortPos() {
                double factor = 0.4096;
                return new Pos(dx * factor, dy * factor, dz * factor, yaw, pitch);
            }

            private Snapshot invert() {
                if (isAbsolute) return this;
                return new Snapshot(false, -dx, -dy, -dz, yaw, pitch);
            }
        }
    }

    record BlockUpdate(int tick, Point pos, Block block, @Nullable Block existing) implements ReplayEvent {

        public static final NetworkBuffer.Type<BlockUpdate> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, ReplayEvent::tick,
            NetworkBuffer.BLOCK_POSITION, BlockUpdate::pos,
            NetworkBuffer.VAR_INT.transform(Block::fromStateId, Block::stateId), BlockUpdate::block,
            NetworkBuffer.VAR_INT.transform(Block::fromStateId, Block::stateId).optional(), BlockUpdate::existing,
            BlockUpdate::new
        );


        @Override
        public int getId() {
            return 4;
        }

        @Override
        public void play(ReplayInstance instance) {
            Block existing = instance.getBlock(pos);
            instance.setBlock(pos, block, false);
            if (block.isAir()) {
                BlockEntry e = existing.registry();
                ServerPacket p = new ParticlePacket(Particle.BLOCK.withBlock(existing), false, false,
                    pos.add(new Vec(0.5)), new Vec(0.25), 0, 30);
                instance.sendPackets(p);
                BlockSoundType type = e.getBlockSoundType();
                if (type == null) return;
                SoundEvent event = type.breakSound();
                if (event == null) return;
                instance.playSound(Sound.sound(event.key(), Sound.Source.BLOCK, type.volume(), type.pitch()), pos);
            } else {
                BlockEntry e = block.registry();
                BlockSoundType type = e.getBlockSoundType();
                instance.playSound(
                    Sound.sound(type.placeSound().key(), Sound.Source.BLOCK, type.volume(), type.pitch()), pos);
            }
        }

        @Override
        public @NonNull ReplayEvent inverse() {
            return new BlockUpdate(tick, pos, existing == null ? Block.AIR : existing, block);
        }
    }

    record Animation(int tick, int entityId, EntityAnimationPacket.Animation animation) implements ReplayEvent {

        public static final NetworkBuffer.Type<Animation> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, ReplayEvent::tick,
            NetworkBuffer.VAR_INT, Animation::entityId,
            NetworkBuffer.Enum(EntityAnimationPacket.Animation.class), Animation::animation,
            Animation::new
        );

        @Override
        public int getId() {
            return 5;
        }

        @Override
        public void play(ReplayInstance instance) {
            instance.sendPackets(new EntityAnimationPacket(transposeId(entityId), animation));
        }
    }

    //todo: serialize model data too
    //todo: figure out inverse. (needs to be state aware?)
    record EquipmentUpdate(int tick, int entityId, Map<EquipmentSlot, ItemStack> slots) implements ReplayEvent {

        public static final NetworkBuffer.Type<EquipmentUpdate> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, ReplayEvent::tick,
            NetworkBuffer.VAR_INT, EquipmentUpdate::entityId,
            NetworkBuffer.Enum(EquipmentSlot.class)
                .mapValue(Material.NETWORK_TYPE.transform(ItemStack::of, ItemStack::material)), EquipmentUpdate::slots,
            EquipmentUpdate::new
        );

        @Override
        public int getId() {
            return 6;
        }

        @Override
        public void play(ReplayInstance instance) {
            instance.sendPackets(new EntityEquipmentPacket(transposeId(entityId), slots));
        }
    }

    record MetaUpdate(int tick, int entityId, Map<Integer, Metadata.Entry<?>> entries) implements ReplayEvent {

        /// stolen from {@link EntityMetaDataPacket#SERIALIZER}
        private static final NetworkBuffer.Type<Map<Integer, Metadata.Entry<?>>> MAP_TYPE = new NetworkBuffer.Type<>() {
            @Override
            public void write(@NonNull NetworkBuffer buffer, Map<Integer, Metadata.Entry<?>> value) {
                for (Map.Entry<Integer, Metadata.Entry<?>> entry : value.entrySet()) {
                    buffer.write(BYTE, entry.getKey().byteValue());
                    buffer.write(Metadata.Entry.SERIALIZER, entry.getValue());
                }
                buffer.write(BYTE, (byte) 0xFF); // End
            }

            @Override
            public Map<Integer, Metadata.Entry<?>> read(NetworkBuffer buffer) {
                Map<Integer, Metadata.Entry<?>> entries = new HashMap<>();
                while (true) {
                    final byte index = buffer.read(BYTE);
                    if (index == (byte) 0xFF) { // reached the end
                        break;
                    }
                    Metadata.Entry<?> entry = Metadata.Entry.SERIALIZER.read(buffer);
                    entries.put((int) index, entry);
                }
                return entries;
            }
        };

        public static final NetworkBuffer.Type<MetaUpdate> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, MetaUpdate::tick,
            NetworkBuffer.VAR_INT, MetaUpdate::entityId,
            MAP_TYPE, MetaUpdate::entries,
            MetaUpdate::new
        );

        @Override
        public int getId() {
            return 7;
        }

        @Override
        public void play(ReplayInstance instance) {
            instance.sendPackets(new EntityMetaDataPacket(transposeId(entityId), entries));
        }
    }

    record ChatMessage(int tick, int entityId, Component message, ChatChannel channel) implements ReplayEvent {

        public static final NetworkBuffer.Type<ChatMessage> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, ReplayEvent::tick,
            NetworkBuffer.VAR_INT, ChatMessage::entityId,
            NetworkBuffer.COMPONENT, ChatMessage::message,
            NetworkBuffer.Enum(ChatChannel.class), ChatMessage::channel,
            ChatMessage::new
        );

        @Override
        public int getId() {
            return 8;
        }

        @Override
        public void play(ReplayInstance i) {
            if (i.getTag(ReplayInstance.CHAT_MESSAGS)) {
                for (net.minestom.server.entity.Player player : i.getPlayers()) {
                    if (!(player instanceof CytosisPlayer p)) continue;
                    if (p.canAuditChannel(channel)) {
                        p.sendMessage(Msg.grey("「REPLAY」</gray>").append(message));
                    }
                }
            }
        }
    }

    record Command(int tick, int entityId, String command) implements ReplayEvent {

        public static final NetworkBuffer.Type<Command> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, ReplayEvent::tick,
            NetworkBuffer.VAR_INT, Command::entityId,
            NetworkBuffer.STRING, Command::command,
            Command::new
        );

        @Override
        public int getId() {
            return 9;
        }

        @Override
        public void play(ReplayInstance i) {
            if (i.getTag(ReplayInstance.COMMANDS)) {
                i.sendPackets(new SystemChatPacket(Msg.grey("「COMMAND」%s » /%s", i.mini(entityId), command), false));
            }
        }
    }
}
