package net.cytonic.cytosis.utils;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockPlacementUtils {

    private static final Block[] PLANTS = new Block[]{
            Block.ACACIA_SAPLING,
            Block.ALLIUM,
            Block.AZALEA,
            Block.AZURE_BLUET,
            Block.BAMBOO,
            Block.BIRCH_SAPLING,
            Block.BROWN_MUSHROOM,
            Block.CHERRY_SAPLING,
//            Material.CLOSED_EYEBLOSSOM,
            Block.CORNFLOWER,
            Block.CRIMSON_FUNGUS,
            Block.CRIMSON_ROOTS,
            Block.DANDELION,
            Block.DARK_OAK_SAPLING,
            Block.FERN,
            Block.FLOWERING_AZALEA,
            Block.JUNGLE_SAPLING,
            Block.LARGE_FERN,
            Block.LILAC,
            Block.LILY_OF_THE_VALLEY,
            Block.MANGROVE_PROPAGULE,
            Block.NETHER_SPROUTS,
            Block.OAK_SAPLING,
            // Material.OPEN_EYEBLOSSOM,
            Block.ORANGE_TULIP,
            Block.OXEYE_DAISY,
            // Material.PALE_OAK_SAPLING,
            Block.PEONY,
            Block.PINK_TULIP,
            Block.PITCHER_PLANT,
            Block.POPPY,
            Block.RED_MUSHROOM,
            Block.RED_TULIP,
            Block.ROSE_BUSH,
            Block.SPRUCE_SAPLING,
            Block.SUGAR_CANE,
            Block.SUNFLOWER,
            Block.SWEET_BERRY_BUSH,
            Block.TALL_GRASS,
            Block.TORCHFLOWER,
            Block.WARPED_ROOTS,
            Block.WHITE_TULIP,
            Block.WITHER_ROSE,
    };

    private static final Block[] AMETHYST = new Block[]{
            Block.AMETHYST_CLUSTER,
            Block.LARGE_AMETHYST_BUD,
            Block.MEDIUM_AMETHYST_BUD,
            Block.SMALL_AMETHYST_BUD
    };

    private static final Block[] WALLS = new Block[]{
            Block.ANDESITE_WALL,
            Block.BLACKSTONE_WALL,
            Block.BRICK_WALL,
            Block.COBBLED_DEEPSLATE_WALL,
            Block.COBBLESTONE_WALL,
            Block.DEEPSLATE_BRICK_WALL,
            Block.DEEPSLATE_TILE_WALL,
            Block.DIORITE_WALL,
            Block.END_STONE_BRICK_WALL,
            Block.GRANITE_WALL,
            Block.MOSSY_COBBLESTONE_WALL,
            Block.MOSSY_STONE_BRICK_WALL,
            Block.MUD_BRICK_WALL,
            Block.NETHER_BRICK_WALL,
            Block.POLISHED_BLACKSTONE_BRICK_WALL,
            Block.POLISHED_BLACKSTONE_WALL,
            Block.POLISHED_DEEPSLATE_WALL,
            Block.POLISHED_TUFF_WALL,
            Block.PRISMARINE_WALL,
            Block.RED_NETHER_BRICK_WALL,
            Block.RED_SANDSTONE_WALL,
            //Block.RESIN_BRICK_WALL,
            Block.SANDSTONE_WALL,
            Block.STONE_BRICK_WALL,
            Block.TUFF_BRICK_WALL,
            Block.TUFF_WALL,
    };

    private static final Block[] GLASS_PANES = new Block[]{
            Block.BLACK_STAINED_GLASS_PANE,
            Block.BLUE_STAINED_GLASS_PANE,
            Block.BROWN_STAINED_GLASS_PANE,
            Block.CYAN_STAINED_GLASS_PANE,
            Block.GLASS_PANE,
            Block.GRAY_STAINED_GLASS_PANE,
            Block.GREEN_STAINED_GLASS_PANE,
            Block.IRON_BARS, // not glass but acts the same
            Block.LIGHT_BLUE_STAINED_GLASS_PANE,
            Block.LIGHT_GRAY_STAINED_GLASS_PANE,
            Block.LIME_STAINED_GLASS_PANE,
            Block.MAGENTA_STAINED_GLASS_PANE,
            Block.ORANGE_STAINED_GLASS_PANE,
            Block.PINK_STAINED_GLASS_PANE,
            Block.PURPLE_STAINED_GLASS_PANE,
            Block.RED_STAINED_GLASS_PANE,
            Block.WHITE_STAINED_GLASS_PANE,
            Block.YELLOW_STAINED_GLASS_PANE
    };

    private static final Block[] STAIRS = new Block[]{
            Block.ACACIA_STAIRS,
            Block.ANDESITE_STAIRS,
            Block.BAMBOO_MOSAIC_STAIRS,
            Block.BAMBOO_STAIRS,
            Block.BIRCH_STAIRS,
            Block.BLACKSTONE_STAIRS,
            Block.BRICK_STAIRS,
            Block.CHERRY_STAIRS,
            Block.COBBLED_DEEPSLATE_STAIRS,
            Block.COBBLESTONE_STAIRS,
            Block.CRIMSON_STAIRS,
            Block.CUT_COPPER_STAIRS,
            Block.DARK_OAK_STAIRS,
            Block.DARK_PRISMARINE_STAIRS,
            Block.DEEPSLATE_BRICK_STAIRS,
            Block.DEEPSLATE_TILE_STAIRS,
            Block.DIORITE_STAIRS,
            Block.END_STONE_BRICK_STAIRS,
            Block.EXPOSED_CUT_COPPER_STAIRS,
            Block.GRANITE_STAIRS,
            Block.JUNGLE_STAIRS,
            Block.MANGROVE_STAIRS,
            Block.MOSSY_COBBLESTONE_STAIRS,
            Block.MOSSY_STONE_BRICK_STAIRS,
            Block.MUD_BRICK_STAIRS,
            Block.NETHER_BRICK_STAIRS,
            Block.OAK_STAIRS,
            Block.OXIDIZED_CUT_COPPER_STAIRS,
            //Block.PALE_OAK_STAIRS,
            Block.POLISHED_ANDESITE_STAIRS,
            Block.POLISHED_BLACKSTONE_BRICK_STAIRS,
            Block.POLISHED_BLACKSTONE_STAIRS,
            Block.POLISHED_DIORITE_STAIRS,
            Block.POLISHED_GRANITE_STAIRS,
            Block.POLISHED_TUFF_STAIRS,
            Block.PRISMARINE_BRICK_STAIRS,
            Block.PRISMARINE_STAIRS,
            Block.PURPUR_STAIRS,
            Block.QUARTZ_STAIRS,
            Block.RED_NETHER_BRICK_STAIRS,
            Block.RED_SANDSTONE_STAIRS,
            //Block.RESIN_BRICK_STAIRS,
            Block.SANDSTONE_STAIRS,
            Block.SMOOTH_QUARTZ_STAIRS,
            Block.SMOOTH_RED_SANDSTONE_STAIRS,
            Block.SMOOTH_SANDSTONE_STAIRS,
            Block.SPRUCE_STAIRS,
            Block.STONE_BRICK_STAIRS,
            Block.STONE_STAIRS,
            Block.TUFF_BRICK_STAIRS,
            Block.TUFF_STAIRS,
            Block.WARPED_STAIRS,
            Block.WAXED_CUT_COPPER_STAIRS,
            Block.WAXED_EXPOSED_CUT_COPPER_STAIRS,
            Block.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
            Block.WAXED_WEATHERED_CUT_COPPER_STAIRS,
            Block.WEATHERED_CUT_COPPER_STAIRS
    };

    public static void init() {
        for (@NotNull Block block : Block.values()) {
            String name = block.name().toUpperCase();
            List<BlockPlacementRule> rules = new ArrayList<>();

            if (block.properties().containsKey("waterlogged")) rules.add(watterLogged(block));
            if (block.properties().containsKey("axis")) rules.add(axis(block));
            if (block.properties().containsKey("facing")) rules.add(facing(block, false));
            if (name.contains("GLASS_PANE") || name.endsWith("_FENCE")) rules.add(glassPane(block));
            if (name.contains("HANGING") || name.contains("WEEPING_VINES") || name.contains("CAVE_VINES"))
                rules.add(hanging(block));
            if (name.contains("_PLATE")) rules.add(solidBelow(block));
            if (name.contains("CARPET") || name.contains("CANDLE") || name.contains("TORCH") || name.contains("TURTLE") || name.contains("FUNGUS") || name.contains("RAIL") || name.contains("SEA_PICKLE"))
                rules.add(anythingBelow(block));
            if (Arrays.stream(WALLS).anyMatch(block::compare)) rules.add(walls(block));
            if (Arrays.stream(PLANTS).anyMatch(block::compare)) rules.add(plant(block));
            if (Arrays.stream(STAIRS).anyMatch(block::compare)) rules.add(stairs(block));
            if (Arrays.stream(AMETHYST).anyMatch(block::compare)) {
                rules.add(solid(block));
                rules.add(facing(block, true));
            }
            //todo: things that can be placed on walls and on the ground
            //todo: end rods
            //todo: frogspawn
            // amethysts
            // banners

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

    private static BlockPlacementRule facing(Block block, boolean backwards) {
        return new BlockPlacementRule(block) {
            @Override
            public Block blockPlace(@NotNull PlacementState placementState) {
                assert placementState.blockFace() != null;
                assert placementState.playerPosition() != null;

                Block b = placementState.block();
                final String name = b.name().toUpperCase();
                final float yaw = placementState.playerPosition().yaw();

                if (backwards) {
                    b = b.withProperty("facing", toDirectionInverted(yaw));
                } else {
                    b = b.withProperty("facing", toDirection(yaw));
                }

                if (b.name().toUpperCase().contains("BUTTON")) {
                    if (!placementState.instance().getBlock(placementState.placePosition().relative(placementState.blockFace().getOppositeFace())).isSolid()) {
                        return null;
                    }

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
                return blockUpdate(new UpdateState(placementState.instance(), placementState.placePosition(), placementState.block(), placementState.blockFace()));
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

                // Set block states for connection
                return currentBlock
                        .withProperty("north", north ? "true" : "false")
                        .withProperty("south", south ? "true" : "false")
                        .withProperty("west", west ? "true" : "false")
                        .withProperty("east", east ? "true" : "false");
            }
        };
    }

    private static BlockPlacementRule hanging(Block block) {
        return new BlockPlacementRule(block) {
            @Override
            public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
                if (placementState.instance().getBlock(placementState.placePosition().add(0, 1, 0), Block.Getter.Condition.TYPE).isSolid()) {
                    return block;
                }
                return null;
            }
        };
    }

    private static BlockPlacementRule solidBelow(Block block) {
        return new BlockPlacementRule(block) {
            @Override
            public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
                if (placementState.instance().getBlock(placementState.placePosition().sub(0, 1, 0), Block.Getter.Condition.TYPE).isSolid()) {
                    return block;
                }
                return null;
            }
        };
    }

    private static BlockPlacementRule anythingBelow(Block block) {
        return new BlockPlacementRule(block) {
            @Override
            public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
                if (!placementState.instance().getBlock(placementState.placePosition().sub(0, 1, 0), Block.Getter.Condition.TYPE).isAir()) {
                    return block;
                }
                return null;
            }
        };
    }

    private static BlockPlacementRule walls(Block block) {
        return new BlockPlacementRule(block) {
            @Override
            public Block blockPlace(@NotNull PlacementState placementState) {
                return blockUpdate(new UpdateState(placementState.instance(), placementState.placePosition(), placementState.block(), placementState.blockFace()));
            }

            @Override
            public @NotNull Block blockUpdate(UpdateState state) {

                // the directions have 3 options: none, low, tall
                // up is only not true if nothing above and in a row
                Block.Getter i = state.instance();
                Point p = state.blockPosition();


                boolean up;
                String east, west, north, south;

                Block n = i.getBlock(p.relative(BlockFace.NORTH));
                Block e = i.getBlock(p.relative(BlockFace.EAST));
                Block s = i.getBlock(p.relative(BlockFace.SOUTH));
                Block w = i.getBlock(p.relative(BlockFace.WEST));

                up = (n.isSolid() ^ s.isSolid()) || (e.isSolid() ^ w.isSolid()) || !(n.isSolid() || e.isSolid() || s.isSolid() || w.isSolid());

                east = e.isSolid() ? (i.getBlock(p.relative(BlockFace.EAST).add(0, 1, 0)).isSolid() ? "tall" : "low") : "none";
                west = w.isSolid() ? (i.getBlock(p.relative(BlockFace.WEST).add(0, 1, 0)).isSolid() ? "tall" : "low") : "none";
                north = n.isSolid() ? (i.getBlock(p.relative(BlockFace.NORTH).add(0, 1, 0)).isSolid() ? "tall" : "low") : "none";
                south = s.isSolid() ? (i.getBlock(p.relative(BlockFace.SOUTH).add(0, 1, 0)).isSolid() ? "tall" : "low") : "none";

                return state.currentBlock()
                        .withProperty("east", east)
                        .withProperty("west", west)
                        .withProperty("north", north)
                        .withProperty("south", south)
                        .withProperty("up", String.valueOf(up));
            }
        };
    }

    private static BlockPlacementRule plant(Block block) {
        Block[] canPlaceOn = new Block[]{
                Block.CLAY,
                Block.DIRT,
                Block.COARSE_DIRT,
                Block.GRASS_BLOCK,
                Block.MYCELIUM,
                Block.PODZOL,
                Block.ROOTED_DIRT,
                Block.MUD
        };
        return new BlockPlacementRule(block) {
            @Override
            public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
                Block b = placementState.instance().getBlock(placementState.placePosition().relative(BlockFace.BOTTOM));
                if (Arrays.stream(canPlaceOn).noneMatch(b::compare)) {
                    return null;
                }
                return placementState.block();
            }
        };
    }

    private static BlockPlacementRule solid(Block block) {
        return new BlockPlacementRule(block) {
            @Override
            public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
                Block b = placementState.instance().getBlock(placementState.placePosition().relative(placementState.blockFace().getOppositeFace()));
                if (b.isSolid()) {
                    return placementState.block();
                } else return null;
            }
        };
    }

    private static BlockPlacementRule stairs(Block block) {
        return new BlockPlacementRule(block) {

            @Override
            public @NotNull Block blockPlace(@NotNull PlacementState state) {
                double y = state.cursorPosition().y();
//                boolean up = y == 0.0 ^ !(y == 1.0) || y > 0.5;
                boolean up = (y == 0.0 || y > 0.5) && y != 1.0;

                return blockUpdate(new UpdateState(
                        state.instance(),
                        state.placePosition(),
                        state.block().withProperty("facing", toDirection(state.playerPosition().yaw()))
                                .withProperty("half", up ? "top" : "bottom"),
                        state.blockFace()
                ));
            }


            @Override
            public @NotNull Block blockUpdate(@NotNull UpdateState updateState) {
                Block b = updateState.currentBlock();
                Block.Getter i = updateState.instance();

                Block n = i.getBlock(updateState.blockPosition().relative(BlockFace.NORTH));
                Block e = i.getBlock(updateState.blockPosition().relative(BlockFace.EAST));
                Block s = i.getBlock(updateState.blockPosition().relative(BlockFace.SOUTH));
                Block w = i.getBlock(updateState.blockPosition().relative(BlockFace.WEST));

                boolean nStair = Arrays.stream(STAIRS).anyMatch(n::compare);
                boolean eStair = Arrays.stream(STAIRS).anyMatch(e::compare);
                boolean sStair = Arrays.stream(STAIRS).anyMatch(s::compare);
                boolean wStair = Arrays.stream(STAIRS).anyMatch(w::compare);
                // we don't really care if it's not a stair

                BlockFace original = BlockFace.valueOf(b.getProperty("facing").toUpperCase());

                switch (original) {
                    case NORTH -> {
                        if (!(nStair || sStair)) return b;
                        if (nStair) {
                            if (!n.getProperty("half").equals(b.getProperty("half"))) return b;
                            if (n.getProperty("facing").equals("east")) {
                                return b.withProperty("shape", "outer_right");
                            } else if (n.getProperty("facing").equals("west")) {
                                return b.withProperty("shape", "outer_left");
                            }
                        }
                        if (sStair) {
                            if (!s.getProperty("half").equals(b.getProperty("half"))) return b;
                            if (s.getProperty("facing").equals("east")) {
                                return b.withProperty("shape", "inner_right");
                            } else if (s.getProperty("facing").equals("west")) {
                                return b.withProperty("shape", "inner_left");
                            }
                        }

                    }
                    case EAST -> {
                        if (!(eStair || wStair)) return b;
                        if (eStair) {
                            if (!e.getProperty("half").equals(b.getProperty("half"))) return b;
                            if (e.getProperty("facing").equals("south")) {
                                return b.withProperty("shape", "outer_left");
                            } else if (e.getProperty("facing").equals("north")) {
                                return b.withProperty("shape", "outer_right");
                            }
                        }
                        if (wStair) {
                            if (!w.getProperty("half").equals(b.getProperty("half"))) return b;
                            if (w.getProperty("facing").equals("north")) {
                                return b.withProperty("shape", "inner_left");
                            } else if (w.getProperty("facing").equals("south")) {
                                return b.withProperty("shape", "inner_right");
                            }
                        }

                    }
                    case SOUTH -> {
                        if (!(nStair || sStair)) return b;
                        if (nStair) {
                            if (!n.getProperty("half").equals(b.getProperty("half"))) return b;
                            if (n.getProperty("facing").equals("east")) {
                                return b.withProperty("shape", "inner_left");
                            } else if (n.getProperty("facing").equals("west")) {
                                return b.withProperty("shape", "inner_right");
                            }
                        }
                        if (sStair) {
                            if (!s.getProperty("half").equals(b.getProperty("half"))) return b;
                            if (s.getProperty("facing").equals("east")) {
                                return b.withProperty("shape", "outer_left");
                            } else if (s.getProperty("facing").equals("west")) {
                                return b.withProperty("shape", "outer_right");
                            }
                        }

                    }
                    case WEST -> {
                        if (!(eStair || wStair)) return b;
                        if (eStair) {
                            if (!e.getProperty("half").equals(b.getProperty("half"))) return b;
                            if (e.getProperty("facing").equals("south")) {
                                return b.withProperty("shape", "inner_left");
                            } else if (e.getProperty("facing").equals("north")) {
                                return b.withProperty("shape", "inner_right");
                            }
                        }
                        if (wStair) {
                            if (!w.getProperty("half").equals(b.getProperty("half"))) return b;
                            if (w.getProperty("facing").equals("north")) {
                                return b.withProperty("shape", "outer_left");
                            } else if (w.getProperty("facing").equals("south")) {
                                return b.withProperty("shape", "outer_right");
                            }
                        }

                    }
                }
                return b;
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
            case BOTTOM -> "ceiling";
            case TOP -> "floor";
        };
    }
}
