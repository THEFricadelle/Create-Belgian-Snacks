/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest.ponder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.content.logistics.funnel.FunnelBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Builds the Ponder scenes' schematics with real blocks (belts, deployers holding their items,
 * filled spouts) and saves them as structure files, then stops the server. Run with
 * {@code ./gradlew runPonderSchematics}; the files land in src/main/resources and are committed.
 * Layer 0 of each schematic is the Ponder base plate.
 */
@EventBusSubscriber(modid = BelgianSnacks.MOD_ID)
public final class PonderSchematicsRun {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String OUTPUT = System.getProperty("create_belgian_snacks.ponderSchematics", "");
    private static final int BUILD_Y = 150;

    private record Schematic(String name, Vec3i size, Consumer<Builder> build) {
    }

    private PonderSchematicsRun() {
    }

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent event) {
        if (OUTPUT.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        List<Schematic> schematics = List.of(
            new Schematic("fryer", new Vec3i(5, 4, 5), PonderSchematicsRun::fryer),
            new Schematic("supreme_grinder", new Vec3i(5, 5, 5), PonderSchematicsRun::grinder),
            new Schematic("the_fricadelle_line", new Vec3i(7, 5, 7), b -> line(b, 5)),
            new Schematic("ultimate_fricadelle_line", new Vec3i(8, 5, 8), b -> line(b, 6)));
        int x = 0;
        for (Schematic schematic : schematics) {
            BlockPos origin = new BlockPos(x, BUILD_Y, 0);
            x += 32;
            Builder builder = new Builder(level, origin);
            builder.clear(schematic.size());
            builder.plate(schematic.size());
            schematic.build().accept(builder);
            save(level, origin, schematic);
        }
        server.halt(false);
    }

    private static void save(ServerLevel level, BlockPos origin, Schematic schematic) {
        StructureTemplate template = new StructureTemplate();
        template.fillFromWorld(level, origin, schematic.size(), false, Blocks.STRUCTURE_VOID);
        CompoundTag tag = template.save(new CompoundTag());
        // Belt segments name their controller by world position: make it relative to the schematic,
        // where the Ponder scene places it.
        for (Tag block : tag.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag nbt = ((CompoundTag) block).getCompound("nbt");
            NbtUtils.readBlockPos(nbt, "Controller")
                .ifPresent(controller -> nbt.put("Controller", NbtUtils.writeBlockPos(controller.subtract(origin))));
        }
        Path file = Path.of(OUTPUT).resolve(schematic.name() + ".nbt");
        try {
            Files.createDirectories(file.getParent());
            NbtIo.writeCompressed(tag, file);
            LOGGER.info("[ponder-schematics] wrote {}", file);
        } catch (IOException e) {
            LOGGER.error("[ponder-schematics] could not write {}", file, e);
        }
    }

    // A fryer on a burner, fed by a short belt from the west, emptied by a funnel into a chest east.
    private static void fryer(Builder b) {
        b.set(2, 1, 2, AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED));
        b.set(2, 2, 2, BSBlocks.FRYER.getDefaultState());
        b.set(0, 1, 2, AllBlocks.ANDESITE_CASING.getDefaultState());
        b.set(1, 1, 2, AllBlocks.ANDESITE_CASING.getDefaultState());
        b.belt(new BlockPos(0, 2, 2), new BlockPos(1, 2, 2));
        b.set(0, 2, 3, AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.Z));
        b.set(3, 2, 2, AllBlocks.ANDESITE_FUNNEL.getDefaultState().setValue(BlockStateProperties.FACING, Direction.EAST)
            .setValue(FunnelBlock.EXTRACTING, true));
        b.set(4, 2, 2, Blocks.CHEST.defaultBlockState());
        b.fluid(2, 2, 2, BSFluids.FRYING_OIL.get().getSource(), 2000);
    }

    // A grinder under a motor, fed by a belt from the west, emptied by a hopper into a chest.
    private static void grinder(Builder b) {
        b.set(2, 1, 2, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.SOUTH));
        b.set(2, 1, 3, Blocks.CHEST.defaultBlockState());
        b.set(2, 2, 2, BSBlocks.SUPREME_GRINDER.getDefaultState());
        b.set(2, 3, 2, AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.Y));
        b.set(2, 4, 2, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.DOWN));
        b.set(0, 1, 2, AllBlocks.ANDESITE_CASING.getDefaultState());
        b.set(1, 1, 2, AllBlocks.ANDESITE_CASING.getDefaultState());
        b.belt(new BlockPos(0, 2, 2), new BlockPos(1, 2, 2));
        b.set(0, 2, 3, AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.Z));
    }

    // A belt along x at z = 3 with the stations two blocks above it: 5 for tier 2, 6 for tier 3
    // (beef tallow spout first). The belt runs one block past the last station.
    private static void line(Builder b, int stations) {
        int z = 3;
        b.belt(new BlockPos(0, 1, z), new BlockPos(stations + 1, 1, z));
        b.set(0, 1, z + 1, AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.Z));
        int x = 1;
        if (stations == 6) {
            b.spout(x++, 3, z, BSFluids.MELTED_BEEF_TALLOW.get().getSource());
        }
        b.deployer(x++, 3, z, BSItems.BELGIAN_SPICES.asStack(64));
        b.spout(x++, 3, z, BSFluids.MAYONNAISE.get().getSource());
        b.spout(x++, 3, z, BSFluids.CURRY_KETCHUP.get().getSource());
        b.deployer(x++, 3, z, new ItemStack(Items.BEETROOT, 64));
        b.set(x, 3, z, AllBlocks.MECHANICAL_PRESS.getDefaultState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
    }

    private static final class Builder {
        private final ServerLevel level;
        private final BlockPos origin;

        Builder(ServerLevel level, BlockPos origin) {
            this.level = level;
            this.origin = origin;
        }

        void clear(Vec3i size) {
            for (BlockPos pos : BlockPos.betweenClosed(origin, origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1))) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        void plate(Vec3i size) {
            for (int x = 0; x < size.getX(); x++) {
                for (int z = 0; z < size.getZ(); z++) {
                    set(x, 0, z, Blocks.SMOOTH_STONE.defaultBlockState());
                }
            }
        }

        void set(int x, int y, int z, BlockState state) {
            level.setBlock(origin.offset(x, y, z), state, 3);
        }

        void belt(BlockPos from, BlockPos to) {
            BeltConnectorItem.createBelts(level, origin.offset(from), origin.offset(to));
            // A belt links to its controller on its first server tick, which the Ponder world (client
            // side) never runs: link it now, so the schematic carries the chain.
            BeltBlock.initBelt(level, origin.offset(from));
        }

        void fluid(int x, int y, int z, Fluid fluid, int amount) {
            IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, origin.offset(x, y, z), null);
            tank.fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
        }

        void spout(int x, int y, int z, Fluid fluid) {
            set(x, y, z, AllBlocks.SPOUT.getDefaultState());
            fluid(x, y, z, fluid, 1000);
        }

        void deployer(int x, int y, int z, ItemStack held) {
            set(x, y, z, AllBlocks.DEPLOYER.getDefaultState().setValue(BlockStateProperties.FACING, Direction.DOWN)
                .setValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE, false));
            level.getCapability(Capabilities.ItemHandler.BLOCK, origin.offset(x, y, z), null).insertItem(0, held, false);
        }
    }
}
