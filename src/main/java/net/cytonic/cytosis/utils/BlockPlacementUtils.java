package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.logging.Logger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BlockPlacementUtils {

    public static void init() {
        for (@NotNull Block block : Block.values()) {
            String name = block.name().toUpperCase();
            List<BlockPlacementRule> rules = new ArrayList<>();

            if (block.properties().containsKey("waterlogged")) rules.add(watterLogged(block));
            if (block.properties().containsKey("axis")) rules.add(axis(block));
            if (block.properties().containsKey("facing")) rules.add(facing(block));
            if (name.contains("GLASS_PANE") || name.contains("_FENCE") || name.contains("_WALL")) {
                rules.add(glassPane(block));
            }


            for (BlockPlacementRule rule : rules) {
                MinecraftServer.getBlockManager().registerBlockPlacementRule(rule);
            }

        }
    }

    private static BlockPlacementRule axis(Block block) {
        return new BlockPlacementRule(block) {
            @Override
            public @NotNull Block blockPlace(@NotNull PlacementState placementState) {
                Block b = placementState.block();
                assert placementState.blockFace() != null : "Null blockFace when determining axis";
                b = b.withProperty("axis", toAxis(placementState.blockFace()));
                return b;
            }
        };
    }

    private static BlockPlacementRule watterLogged(Block block) {
        return new BlockPlacementRule(block) {
            @Override
            public @NotNull Block blockPlace(@NotNull PlacementState placementState) {
                Block b = placementState.block();
                Block existing = placementState.instance().getBlock(placementState.placePosition(), Block.Getter.Condition.TYPE);
                if (existing.compare(Block.WATER)) {
                    b = b.withProperty("waterlogged", "true");
                }
                return b;
            }
        };
    }

    private static BlockPlacementRule facing(Block block) {
        return new BlockPlacementRule(block) {
            @Override
            public @NotNull Block blockPlace(@NotNull PlacementState placementState) {
                assert placementState.blockFace() != null;
                assert placementState.playerPosition() != null;

                Block b = placementState.block();
                final String name = b.name().toUpperCase();
                final float yaw = placementState.playerPosition().yaw();

                if (name.contains("TRAPDOOR") || name.contains("BUTTON")) {
                    b = b.withProperty("facing", toDirectionInverted(yaw));
                } else {
                    b = b.withProperty("facing", toDirection(yaw));
                }

                if (b.name().toUpperCase().contains("BUTTON")) {
                    b = b.withProperty("face", toButtonFace(placementState.blockFace()));
                }

                return b;
            }
        };
    }

    private static BlockPlacementRule glassPane(Block block) {
        return new BlockPlacementRule(block) {
            @Override
            public Block blockPlace(@NotNull PlacementState placementState) {
                Block b = placementState.block();

                blockUpdate(new UpdateState(placementState.instance(), placementState.placePosition(), placementState.block(), placementState.blockFace()));
                return b;
            }

            @Override
            public @NotNull Block blockUpdate(UpdateState state) {
                Point pos = state.blockPosition();
                Block currentBlock = state.currentBlock();

                // Check neighboring blocks
                boolean north = state.instance().getBlock(pos.add(0, 0, -1)).isSolid();
                boolean south = state.instance().getBlock(pos.add(0, 0, 1)).isSolid();
                boolean west = state.instance().getBlock(pos.add(-1, 0, 0)).isSolid();
                boolean east = state.instance().getBlock(pos.add(1, 0, 0)).isSolid();

                Logger.info("North: " + north + " \n East: " + east + " \n South: " + south + " \n West: " + west);

                // Set block states for connection
                return currentBlock
                        .withProperty("north", north ? "true" : "false")
                        .withProperty("south", south ? "true" : "false")
                        .withProperty("west", west ? "true" : "false")
                        .withProperty("east", east ? "true" : "false");
            }
        };
    }

    private static String toAxis(BlockFace face) {
        return switch (face) {
            case EAST, WEST -> "x";
            case NORTH, SOUTH -> "z";
            case TOP, BOTTOM -> "y";
        };
    }

    private static String toDirection(float yaw) {
        if (yaw >= 135 || yaw <= -135) return "north";
        if (yaw > -135 && yaw < -45) return "east";
        if (yaw >= -45 && yaw <= 45) return "south";
        if (yaw > 45 && yaw < 135) return "west";

        // default to north
        return "north";
    }

    private static String toDirectionInverted(float yaw) {
        if (yaw >= 135 || yaw <= -135) return "south";
        if (yaw > -135 && yaw < -45) return "west";
        if (yaw >= -45 && yaw <= 45) return "north";
        if (yaw > 45 && yaw < 135) return "east";

        // default to north
        return "south";
    }

    private static String toButtonFace(BlockFace face) {
        return switch (face) {
            case EAST, WEST, NORTH, SOUTH -> "wall";
            case TOP -> "ceiling";
            case BOTTOM -> "floor";
        };
    }
}
