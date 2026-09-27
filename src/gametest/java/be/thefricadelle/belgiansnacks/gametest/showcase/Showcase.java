/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest.showcase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.logistics.funnel.FunnelBlock;
import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderMode;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlockEntity;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * A showcase world with everything the mod adds, built once in a fresh flat world
 * ({@code ./gradlew runShowcase}) and kept running: the three tiers are real, automated production
 * lines whose stocks (meat, foods, sauces, spices, fat) this class tops up every second, so they
 * never stop. {@code runShowcaseCheck} builds it in a new world, waits for every tier's fried
 * fricadelle, then reports (the client takes screenshots and quits). Never in the mod jar.
 */
@EventBusSubscriber(modid = BelgianSnacks.MOD_ID)
public final class Showcase {
    private static final Logger LOGGER = LogUtils.getLogger();
    /** play, check, or empty (the class is inert). */
    static final String MODE = System.getProperty("create_belgian_snacks.showcase", "");
    static final String WORLD = "belgian-snacks-showcase";
    private static final String DATA = "create_belgian_snacks_showcase";
    private static final int SPEED = 64;
    private static final int STOCK = 64;
    private static final int SPOUT_TANK = 1000;
    private static final int UPKEEP_TICKS = 20;
    private static final int CHECK_TIMEOUT_TICKS = 20 * 60 * 8;
    // Output chests are emptied once this many slots are taken, so the fryers never block.
    private static final int OUTPUT_SLOTS_KEPT = 18;

    // Layout, relative to the spawn point (on the ground, first air block). The visitor spawns
    // facing south; the stations stand side by side along x, each line running south (+z).
    static final BlockPos WELCOME = new BlockPos(0, 0, 3);
    private static final int GALLERY_Z = 6;
    private static final int SIGN_Z = 11;
    static final BlockPos TIER1 = new BlockPos(-24, 0, 12);
    private static final int POOLS_X = -16;
    static final BlockPos NPC_CHEST = new BlockPos(-2, 0, 13);
    static final BlockPos TIER2 = new BlockPos(4, 0, 14);
    static final BlockPos TIER3 = new BlockPos(14, 0, 14);

    private enum Kind { SPICES, ONION, SAUCE, PRESS }

    private record Station(Kind kind, Supplier<Fluid> sauce) {
        static Station of(Kind kind) {
            return new Station(kind, null);
        }

        static Station sauce(Supplier<Fluid> fluid) {
            return new Station(Kind.SAUCE, fluid);
        }
    }

    /** A grinder over the start of a Sequenced Assembly line, repeated {@code passes} times, into a fryer. */
    private record Line(BlockPos column, GrinderMode mode, int passes, List<Station> sequence, Supplier<Fluid> fat, HeatLevel heat,
                        Supplier<Item> fried) {
        List<Station> stations() {
            List<Station> all = new ArrayList<>();
            for (int pass = 0; pass < passes; pass++) {
                all.addAll(sequence);
            }
            return all;
        }

        BlockPos grinder() {
            return column.above(3);
        }

        BlockPos beltStart() {
            return column.above();
        }

        BlockPos station(int index) {
            return column.offset(0, 3, 1 + index);
        }

        BlockPos fryer() {
            return column.offset(0, 1, stations().size() + 2);
        }

        BlockPos feedChest() {
            return column.offset(-1, 4, 0);
        }

        BlockPos outputChest() {
            return outputChestOf(fryer());
        }
    }

    private static final Line THE_LINE = new Line(TIER2, GrinderMode.THE_, 3,
        List.of(Station.of(Kind.SPICES), Station.sauce(() -> BSFluids.MAYONNAISE.get().getSource()),
            Station.sauce(() -> BSFluids.CURRY_KETCHUP.get().getSource()), Station.of(Kind.ONION), Station.of(Kind.PRESS)),
        () -> BSFluids.FRYING_OIL.get().getSource(), HeatLevel.KINDLED, BSItems.THE_FRICADELLE::get);
    private static final Line ULTIMATE_LINE = new Line(TIER3, GrinderMode.ULTIMATE, 5,
        List.of(Station.sauce(() -> BSFluids.MELTED_BEEF_TALLOW.get().getSource()), Station.of(Kind.SPICES),
            Station.sauce(() -> BSFluids.MAYONNAISE.get().getSource()), Station.sauce(() -> BSFluids.CURRY_KETCHUP.get().getSource()),
            Station.of(Kind.ONION), Station.of(Kind.PRESS)),
        () -> BSFluids.MELTED_BEEF_TALLOW.get().getSource(), HeatLevel.SEETHING, BSItems.ULTIMATE_FRICADELLE::get);

    // The tier 1 line of ProductionLineGameTests, same offsets (its y = 1 is the ground here).
    private static final BlockPos T1_WHEEL_WEST = new BlockPos(1, 3, 1);
    private static final BlockPos T1_WHEEL_EAST = new BlockPos(3, 3, 1);
    private static final BlockPos T1_CHEST = new BlockPos(2, 5, 1);
    private static final BlockPos T1_FEEDER = new BlockPos(2, 4, 1);
    private static final BlockPos T1_HOPPER = new BlockPos(2, 2, 1);
    private static final BlockPos T1_BASIN = new BlockPos(2, 2, 2);
    private static final BlockPos T1_BELT_START = new BlockPos(2, 1, 3);
    private static final BlockPos T1_BELT_END = new BlockPos(2, 1, 7);
    private static final BlockPos T1_PRESS = new BlockPos(2, 3, 6);
    private static final BlockPos T1_FRYER = new BlockPos(2, 1, 8);

    private static BlockPos origin;
    private static int checkTicks;
    private static final Map<String, Integer> reached = new LinkedHashMap<>();
    /** Set once by the check: the report, which the client reads to take its screenshots and quit. */
    static volatile String checkReport;

    private Showcase() {
    }

    // ------------------------------------------------------------------ build

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent event) {
        if (MODE.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        Data data = level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Data::new, Data::load, null), DATA);
        if (data.origin == null) {
            data.origin = new BlockPos(0, level.getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0), 0);
            build(server, level, data.origin);
            data.setDirty();
            LOGGER.info("[showcase] built at {}", data.origin);
        }
        origin = data.origin;
    }

    private static void build(MinecraftServer server, ServerLevel level, BlockPos o) {
        GameRules rules = level.getGameRules();
        rules.getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        level.setDayTime(6000);
        level.setDefaultSpawnPos(o, 0);

        // A path in front of the stations.
        for (int x = -26; x <= 18; x++) {
            for (int z = SIGN_Z - 1; z <= SIGN_Z; z++) {
                set(level, o.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
            }
        }

        sign(level, o.offset(WELCOME), lit("Create:"), lit("Belgian Snacks"), lit("by / par"), lit("THEFricadelle"));
        sign(level, o.offset(WELCOME).east(2), lit("Hold [W] over"), lit("an item: Ponder"), lit("Maintiens [W]"), lit("sur un item"));
        gallery(level, o);
        pools(level, o);
        tier1(level, o.offset(TIER1));
        sign(level, o.offset(TIER1.getX() + 2, 0, SIGN_Z), lit("Tier 1 · Palier 1"), name(BSItems.FRICADELLE.get()),
            lit("meat > fryer"), lit("viande > friteuse"));
        set(level, o.offset(NPC_CHEST), Blocks.CHEST.defaultBlockState());
        sign(level, o.offset(NPC_CHEST.getX(), 0, SIGN_Z), lit("Eat one to meet"), lit("the author"), lit("Manges-en une"),
            lit("pour le voir"));
        line(level, o, THE_LINE);
        line(level, o, ULTIMATE_LINE);
        sign(level, o.offset(TIER2.getX(), 0, SIGN_Z), lit("Tier 2 · Palier 2"), name(BSItems.THE_FRICADELLE.get()),
            lit("3 loops · tours"), name(BSItems.EXCEPTIONAL_PASTE.get()));
        sign(level, o.offset(TIER3.getX(), 0, SIGN_Z), lit("Tier 3 · Palier 3"), name(BSItems.ULTIMATE_FRICADELLE.get()),
            lit("5 loops · tours"), name(BSItems.ABSOLUTE_PASTE.get()));
        upkeep(level, o);
    }

    // Every item of the mod in a frame lying on a low counter, so the stations behind stay in view.
    private static void gallery(ServerLevel level, BlockPos o) {
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(BelgianSnacks.MOD_ID)) {
                items.add(item);
            }
        }
        int perRow = (items.size() + 1) / 2;
        int left = -perRow / 2;
        for (int i = 0; i < items.size(); i++) {
            BlockPos counter = o.offset(left + i % perRow, 0, GALLERY_Z + i / perRow);
            set(level, counter, AllBlocks.ANDESITE_CASING.getDefaultState());
            ItemFrame frame = new ItemFrame(level, counter.above(), Direction.UP);
            frame.setItem(new ItemStack(items.get(i)), false);
            CompoundTag tag = frame.saveWithoutId(new CompoundTag());
            tag.putBoolean("Fixed", true);
            frame.load(tag);
            level.addFreshEntity(frame);
        }
        sign(level, o.offset(left - 2, 0, GALLERY_Z), lit("Every item"), lit("of the mod"), lit("Tous les items"), lit("du mod"));
    }

    // The four fluids, a 2 x 2 pool each, sunk into the ground.
    private static void pools(ServerLevel level, BlockPos o) {
        List<Fluid> fluids = List.of(BSFluids.FRYING_OIL.get().getSource(), BSFluids.MELTED_BEEF_TALLOW.get().getSource(),
            BSFluids.MAYONNAISE.get().getSource(), BSFluids.CURRY_KETCHUP.get().getSource());
        for (int i = 0; i < fluids.size(); i++) {
            int x = POOLS_X + 3 * i;
            for (int dx = 0; dx < 2; dx++) {
                for (int dz = 0; dz < 2; dz++) {
                    set(level, o.offset(x + dx, -1, SIGN_Z + 2 + dz), fluids.get(i).defaultFluidState().createLegacyBlock());
                }
            }
            sign(level, o.offset(x, 0, SIGN_Z), lit(""), fluids.get(i).getFluidType().getDescription(), lit(""), lit(""));
        }
    }

    private static void tier1(ServerLevel level, BlockPos t) {
        set(level, t.offset(T1_WHEEL_WEST), AllBlocks.CRUSHING_WHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.Z));
        set(level, t.offset(T1_WHEEL_EAST), AllBlocks.CRUSHING_WHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.Z));
        motor(level, t.offset(T1_WHEEL_WEST).north(), Direction.SOUTH, -SPEED);
        motor(level, t.offset(T1_WHEEL_EAST).north(), Direction.SOUTH, SPEED);
        set(level, t.offset(T1_CHEST), Blocks.CHEST.defaultBlockState());
        set(level, t.offset(T1_FEEDER), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        set(level, t.offset(T1_HOPPER), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.SOUTH));
        set(level, t.offset(T1_BASIN).below(), Blocks.STONE.defaultBlockState());
        set(level, t.offset(T1_BASIN), AllBlocks.BASIN.getDefaultState().setValue(BasinBlock.FACING, Direction.SOUTH));
        BlockPos mixer = t.offset(T1_BASIN).above(2);
        set(level, mixer, AllBlocks.MECHANICAL_MIXER.getDefaultState());
        set(level, mixer.west(), AllBlocks.COGWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.Y));
        motor(level, mixer.west().above(), Direction.DOWN, SPEED);
        BeltConnectorItem.createBelts(level, t.offset(T1_BELT_START), t.offset(T1_BELT_END));
        motor(level, t.offset(T1_BELT_START).west(), Direction.EAST, SPEED);
        set(level, t.offset(T1_PRESS), AllBlocks.MECHANICAL_PRESS.getDefaultState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        motor(level, t.offset(T1_PRESS).west(), Direction.EAST, SPEED);
        fryer(level, t.offset(T1_FRYER), HeatLevel.KINDLED);
    }

    private static void line(ServerLevel level, BlockPos o, Line line) {
        // Foods go from a double chest through a hopper into the grinder; its paste drops through a
        // hopper onto the belt, which carries it under every station and into the fryer.
        BlockPos grinder = o.offset(line.grinder());
        set(level, grinder, BSBlocks.SUPREME_GRINDER.getDefaultState());
        set(level, grinder.above(), AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.Y));
        motor(level, grinder.above(2), Direction.DOWN, SPEED);
        if (level.getBlockEntity(grinder) instanceof SupremeGrinderBlockEntity be) {
            be.setMode(line.mode());
        }
        set(level, grinder.below(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        set(level, grinder.west(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.EAST));
        BlockPos chest = o.offset(line.feedChest());
        set(level, chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST).setValue(ChestBlock.TYPE, ChestType.RIGHT));
        set(level, chest.north(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST).setValue(ChestBlock.TYPE, ChestType.LEFT));

        List<Station> stations = line.stations();
        BlockPos beltStart = o.offset(line.beltStart());
        BeltConnectorItem.createBelts(level, beltStart, beltStart.south(stations.size() + 1));
        motor(level, beltStart.west(), Direction.EAST, SPEED);
        // One motor for every station: a row of meshing cogwheels along the line, each turning the
        // deployer or press east of it through its axle.
        motor(level, o.offset(line.station(0)).west(2), Direction.EAST, SPEED);
        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            BlockPos pos = o.offset(line.station(i));
            set(level, pos.west(), AllBlocks.COGWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.X));
            switch (station.kind()) {
                case SPICES, ONION -> set(level, pos, AllBlocks.DEPLOYER.getDefaultState().setValue(BlockStateProperties.FACING, Direction.DOWN)
                    .setValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE, true));
                case SAUCE -> set(level, pos, AllBlocks.SPOUT.getDefaultState());
                case PRESS -> set(level, pos, AllBlocks.MECHANICAL_PRESS.getDefaultState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
            }
        }
        fryer(level, o.offset(line.fryer()), line.heat());
    }

    // A funnel on the fryer's east side drops what it takes just below itself (it never inserts
    // into a block in front), where a hopper catches it for the chest.
    private static BlockPos outputChestOf(BlockPos fryer) {
        return fryer.offset(2, -1, 0);
    }

    // A fryer on a creative Blaze Burner, emptied by a funnel, a hopper and a chest on its east side.
    private static void fryer(ServerLevel level, BlockPos pos, HeatLevel heat) {
        set(level, pos.below(), AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, heat));
        if (level.getBlockEntity(pos.below()) instanceof BlazeBurnerBlockEntity burner) {
            burner.isCreative = true;
        }
        set(level, pos, BSBlocks.FRYER.getDefaultState());
        set(level, pos.east(), AllBlocks.ANDESITE_FUNNEL.getDefaultState().setValue(BlockStateProperties.FACING, Direction.EAST)
            .setValue(FunnelBlock.EXTRACTING, true));
        set(level, pos.east().below(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.EAST));
        set(level, outputChestOf(pos), Blocks.CHEST.defaultBlockState());
    }

    // ------------------------------------------------------------------ upkeep

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        if (MODE.isEmpty() || origin == null) {
            return;
        }
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        if (server.getTickCount() % UPKEEP_TICKS == 0) {
            upkeep(level, origin);
        }
        if (MODE.equals("check") && checkReport == null) {
            check(level, origin);
        }
    }

    private static void upkeep(ServerLevel level, BlockPos o) {
        if (!level.isLoaded(o.offset(TIER3))) {
            return;
        }
        // Tier 1: the same four raw foods in turn, so the basin always gets a full set.
        IItemHandler meat = items(level, o.offset(TIER1).offset(T1_CHEST));
        if (meat != null && isEmpty(meat)) {
            int slot = 0;
            for (int round = 0; round < 4; round++) {
                for (Item raw : new Item[] {Items.PORKCHOP, Items.BEEF, Items.CHICKEN, Items.BREAD}) {
                    meat.insertItem(slot++, new ItemStack(raw, 4), false);
                }
            }
        }
        topUpFryer(level, o.offset(TIER1).offset(T1_FRYER), BSFluids.FRYING_OIL.get().getSource());
        trim(level, outputChestOf(o.offset(TIER1).offset(T1_FRYER)));

        for (Line line : List.of(THE_LINE, ULTIMATE_LINE)) {
            // One of every food: the grinder takes each once, the rest wait in the chest.
            IItemHandler foods = items(level, o.offset(line.feedChest()));
            if (foods != null && isEmpty(foods)) {
                int slot = 0;
                for (ResourceLocation id : FoodIndex.server().ids()) {
                    if (slot >= foods.getSlots()) {
                        break;
                    }
                    foods.insertItem(slot++, new ItemStack(BuiltInRegistries.ITEM.get(id)), false);
                }
            }
            List<Station> stations = line.stations();
            for (int i = 0; i < stations.size(); i++) {
                Station station = stations.get(i);
                BlockPos pos = o.offset(line.station(i));
                switch (station.kind()) {
                    case SPICES -> topUpHand(level, pos, BSItems.BELGIAN_SPICES.get());
                    case ONION -> topUpHand(level, pos, onion());
                    case SAUCE -> {
                        IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
                        if (tank != null) {
                            tank.fill(new FluidStack(station.sauce().get(), SPOUT_TANK - tank.getFluidInTank(0).getAmount()),
                                IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                    default -> {
                    }
                }
            }
            topUpFryer(level, o.offset(line.fryer()), line.fat().get());
            trim(level, o.offset(line.outputChest()));
        }

        IItemHandler treats = items(level, o.offset(NPC_CHEST));
        if (treats != null && treats.getStackInSlot(0).getCount() < 4) {
            treats.extractItem(0, 64, false);
            treats.insertItem(0, BSItems.ULTIMATE_FRICADELLE.asStack(16), false);
        }
    }

    private static void topUpHand(ServerLevel level, BlockPos deployer, Item item) {
        IItemHandler hand = items(level, deployer);
        if (hand != null && hand.getStackInSlot(0).getCount() < STOCK / 2) {
            hand.insertItem(0, new ItemStack(item, STOCK - hand.getStackInSlot(0).getCount()), false);
        }
    }

    private static void topUpFryer(ServerLevel level, BlockPos pos, Fluid fat) {
        if (level.getBlockEntity(pos) instanceof FryerBlockEntity fryer) {
            var tank = fryer.getTank().getPrimaryHandler();
            fryer.getTank().getCapability().fill(new FluidStack(fat, tank.getCapacity() - tank.getFluidAmount()), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    // Empties an output chest that is getting full, so the funnel and the fryer never block.
    private static void trim(ServerLevel level, BlockPos chest) {
        IItemHandler handler = items(level, chest);
        if (handler == null) {
            return;
        }
        int taken = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            taken += handler.getStackInSlot(slot).isEmpty() ? 0 : 1;
        }
        if (taken >= OUTPUT_SLOTS_KEPT) {
            for (int slot = 1; slot < handler.getSlots(); slot++) {
                handler.extractItem(slot, 64, false);
            }
        }
    }

    // ------------------------------------------------------------------ check

    private static void check(ServerLevel level, BlockPos o) {
        checkTicks++;
        probe(level, "tier 1 fricadelle", outputChestOf(o.offset(TIER1).offset(T1_FRYER)), BSItems.FRICADELLE.get());
        probe(level, "tier 2 THE_Fricadelle", o.offset(THE_LINE.outputChest()), THE_LINE.fried().get());
        probe(level, "tier 3 THE_FRICADELLE", o.offset(ULTIMATE_LINE.outputChest()), ULTIMATE_LINE.fried().get());
        if (checkTicks % 600 == 0) {
            LOGGER.info("[showcase] {} s: reached {}; {}", checkTicks / 20, reached.keySet(), stages(level, o));
        }
        boolean all = reached.size() == 3;
        if (all || checkTicks >= CHECK_TIMEOUT_TICKS) {
            StringBuilder report = new StringBuilder();
            reached.forEach((what, tick) -> report.append("PASS ").append(what).append(" - after ").append(tick / 20).append(" s\n"));
            for (String what : List.of("tier 1 fricadelle", "tier 2 THE_Fricadelle", "tier 3 THE_FRICADELLE")) {
                if (!reached.containsKey(what)) {
                    report.append("FAIL ").append(what).append(" - not in its output chest after ").append(checkTicks / 20).append(" s; ")
                        .append(stages(level, o)).append('\n');
                }
            }
            report.append(all ? "RESULT PASS" : "RESULT FAIL");
            checkReport = report.toString();
            LOGGER.info("[showcase] check:\n{}", checkReport);
        }
    }

    private static void probe(ServerLevel level, String what, BlockPos chest, Item item) {
        IItemHandler handler = items(level, chest);
        for (int slot = 0; handler != null && slot < handler.getSlots(); slot++) {
            if (handler.getStackInSlot(slot).is(item)) {
                reached.putIfAbsent(what, checkTicks);
            }
        }
    }

    // Where each line stands, to name the stage a failing line is stuck before.
    private static String stages(ServerLevel level, BlockPos o) {
        StringBuilder text = new StringBuilder();
        if (level.getBlockEntity(o.offset(TIER1).offset(T1_FRYER)) instanceof FryerBlockEntity fryer) {
            text.append("tier 1 fryer ").append(fryer.status()).append(" basket ").append(fryer.getBasket()).append("; ");
        }
        for (Line line : List.of(THE_LINE, ULTIMATE_LINE)) {
            if (level.getBlockEntity(o.offset(line.grinder())) instanceof SupremeGrinderBlockEntity grinder) {
                text.append(line.mode()).append(" grinder ").append(grinder.getCount()).append('/').append(grinder.getGoal()).append(", ");
            }
            if (level.getBlockEntity(o.offset(line.fryer())) instanceof FryerBlockEntity fryer) {
                text.append("fryer ").append(fryer.status()).append(" basket ").append(fryer.getBasket()).append("; ");
            }
        }
        return text.toString();
    }

    // ------------------------------------------------------------------ helpers

    private static void set(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 3);
    }

    private static void motor(ServerLevel level, BlockPos pos, Direction facing, int speed) {
        set(level, pos, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, facing));
        if (level.getBlockEntity(pos) instanceof CreativeMotorBlockEntity motor) {
            motor.generatedSpeed.setValue(speed);
        }
    }

    // A standing sign facing north (towards the spawn), waxed so a click does not open the editor.
    private static void sign(ServerLevel level, BlockPos pos, Component... lines) {
        set(level, pos, Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, 8));
        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            SignText text = new SignText();
            for (int i = 0; i < lines.length; i++) {
                text = text.setMessage(i, lines[i]);
            }
            sign.setText(text, true);
            sign.setWaxed(true);
        }
    }

    private static Component lit(String text) {
        return Component.literal(text);
    }

    // Item names follow the reader's language.
    private static Component name(Item item) {
        return Component.translatable(item.getDescriptionId());
    }

    private static Item onion() {
        var onions = BuiltInRegistries.ITEM.getTag(BSTags.C_ONIONS);
        return onions.isPresent() && onions.get().size() > 0 ? onions.get().get(0).value() : Items.BEETROOT;
    }

    private static IItemHandler items(ServerLevel level, BlockPos pos) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
    }

    private static boolean isEmpty(IItemHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (!handler.getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Where the showcase stands in its world, saved with it so it is built only once. */
    private static final class Data extends SavedData {
        private BlockPos origin;

        static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data data = new Data();
            data.origin = NbtUtils.readBlockPos(tag, "Origin").orElse(null);
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            if (origin != null) {
                tag.put("Origin", NbtUtils.writeBlockPos(origin));
            }
            return tag;
        }
    }
}
