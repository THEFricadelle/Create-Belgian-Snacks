/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.crop.AsparagusCropBlock;
import be.thefricadelle.belgiansnacks.content.crop.BouchotBlock;
import be.thefricadelle.belgiansnacks.registry.BSCrops;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * v2 crops and mussels (docs/13, section 5.8): growth, the light rules of the chicon and the
 * asparagus, what each gives, and Create's harvester taking them.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CropGameTests {
    private static final String TEMPLATE = "empty";
    private static final BlockPos CROP = new BlockPos(1, 1, 1);
    private static final int TICKS = 3000;

    private CropGameTests() {
    }

    // Every crop but the chicon grows in daylight, up to its last age, on plain farmland.
    @GameTest(template = TEMPLATE)
    public static void cropsGrowInDaylight(GameTestHelper helper) {
        List<CropBlock> crops = BSCrops.CROPS.stream().map(entry -> (CropBlock) entry.get())
            .filter(crop -> crop != BSCrops.CHICON_CROP.get()).toList();
        for (int i = 0; i < crops.size(); i++) {
            BlockPos pos = new BlockPos(i % 3, 1, i / 3);
            helper.setBlock(pos.below(), Blocks.FARMLAND);
            helper.setBlock(pos, crops.get(i).defaultBlockState());
            grow(helper, pos);
            BlockState grown = helper.getBlockState(pos);
            helper.assertTrue(crops.get(i).isMaxAge(grown), BlockPosText.of(grown) + " did not ripen");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void eachCropGivesItsHarvest(GameTestHelper helper) {
        expectDrops(helper, BSCrops.CHICON_CROP.get(), BSCrops.CHICON.get());
        expectDrops(helper, BSCrops.LEEK_CROP.get(), BSCrops.LEEK.get());
        expectDrops(helper, BSCrops.HOPS_CROP.get(), BSCrops.HOPS.get());
        expectDrops(helper, BSCrops.BRUSSELS_SPROUTS_CROP.get(), BSCrops.BRUSSELS_SPROUT.get());
        expectDrops(helper, BSCrops.BUCKWHEAT_CROP.get(), BSCrops.BUCKWHEAT.get());
        expectDrops(helper, BSCrops.MUSTARD_CROP.get(), BSCrops.MUSTARD_SEEDS.get());
        // Unripe, a crop gives back what planted it.
        List<ItemStack> young = Block.getDrops(BSCrops.HOPS_CROP.get().defaultBlockState(), helper.getLevel(), helper.absolutePos(CROP), null);
        helper.assertTrue(young.size() == 1 && young.get(0).is(BSCrops.HOP_SEEDS.get()), "young hops give " + young);
        helper.succeed();
    }

    // The chicon is forced in the dark: shut in stone it ripens, in daylight it stays a root but lives.
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void chiconGrowsOnlyInTheDark(GameTestHelper helper) {
        helper.setBlock(CROP.below(), Blocks.FARMLAND);
        helper.setBlock(CROP, BSCrops.CHICON_CROP.get().defaultBlockState());
        grow(helper, CROP);
        helper.assertValueEqual(BSCrops.CHICON_CROP.get().getAge(helper.getBlockState(CROP)), 0, "chicon age in daylight");
        cellar(helper);
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(helper.getBlockState(CROP).is(BSCrops.CHICON_CROP.get()), "the chicon did not survive");
            helper.assertTrue(light(helper) <= 4, "the cellar lets in light " + light(helper));
            grow(helper, CROP);
            helper.assertTrue(BSCrops.CHICON_CROP.get().isMaxAge(helper.getBlockState(CROP)), "the chicon did not ripen in the dark");
            helper.succeed();
        });
    }

    // Grown in the dark, asparagus comes out white; in daylight, green.
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void asparagusIsWhiteInTheDarkAndGreenInDaylight(GameTestHelper helper) {
        helper.setBlock(CROP.below(), Blocks.FARMLAND);
        helper.setBlock(CROP, BSCrops.ASPARAGUS_CROP.get().defaultBlockState());
        grow(helper, CROP);
        BlockState daylight = helper.getBlockState(CROP);
        helper.assertTrue(!daylight.getValue(AsparagusCropBlock.BLANCHED) && drops(helper, daylight).stream()
            .anyMatch(stack -> stack.is(BSCrops.GREEN_ASPARAGUS.get())), "daylight asparagus: " + daylight);
        helper.setBlock(CROP, BSCrops.ASPARAGUS_CROP.get().defaultBlockState());
        cellar(helper);
        helper.runAfterDelay(20, () -> {
            grow(helper, CROP);
            BlockState dark = helper.getBlockState(CROP);
            helper.assertTrue(dark.getValue(AsparagusCropBlock.BLANCHED) && drops(helper, dark).stream()
                .anyMatch(stack -> stack.is(BSCrops.WHITE_ASPARAGUS.get())), "asparagus of the dark: " + dark);
            helper.succeed();
        });
    }

    // Mussels grow on a pole under water only; a player picks them and the pole starts again.
    @GameTest(template = TEMPLATE)
    public static void musselsGrowUnderwaterAndArePicked(GameTestHelper helper) {
        BlockPos dry = new BlockPos(0, 1, 0);
        helper.setBlock(dry.below(), Blocks.STONE);
        helper.setBlock(dry, BSCrops.BOUCHOT.get().defaultBlockState());
        helper.setBlock(CROP.below(), Blocks.STONE);
        helper.setBlock(CROP, BSCrops.BOUCHOT.get().defaultBlockState().setValue(BouchotBlock.WATERLOGGED, true));
        grow(helper, dry);
        grow(helper, CROP);
        helper.assertValueEqual(helper.getBlockState(dry).getValue(BouchotBlock.AGE), 0, "mussels on a dry pole");
        helper.assertValueEqual(helper.getBlockState(CROP).getValue(BouchotBlock.AGE), BouchotBlock.MAX_AGE, "mussels on a pole under water");
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "picker"));
        BlockPos at = helper.absolutePos(CROP);
        helper.getBlockState(CROP).useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(at), Direction.UP, at, false));
        helper.assertValueEqual(helper.getBlockState(CROP).getValue(BouchotBlock.AGE), 0, "pole after picking");
        int mussels = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(2)).stream()
            .filter(entity -> entity.getItem().is(BSCrops.MUSSEL.get())).mapToInt(entity -> entity.getItem().getCount()).sum();
        helper.assertTrue(mussels >= 2 && mussels <= 4, mussels + " mussels picked");
        helper.succeed();
    }

    // Create's harvester takes ripe crops and ripe mussel poles, never young ones; harvesting a pole
    // gives mussels, never the pole itself (it would duplicate it).
    @GameTest(template = TEMPLATE)
    public static void createsHarvesterTakesThemRipe(GameTestHelper helper) {
        HarvesterMovementBehaviour harvester = new HarvesterMovementBehaviour();
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(CROP);
        for (var entry : BSCrops.CROPS) {
            CropBlock crop = (CropBlock) entry.get();
            helper.assertTrue(harvester.isValidCrop(level, pos, crop.getStateForAge(crop.getMaxAge())), entry.getId() + " ripe refused");
            helper.assertFalse(harvester.isValidCrop(level, pos, crop.getStateForAge(0)), entry.getId() + " taken young");
        }
        BlockState ripePole = BSCrops.BOUCHOT.get().defaultBlockState().setValue(BouchotBlock.AGE, BouchotBlock.MAX_AGE)
            .setValue(BouchotBlock.WATERLOGGED, true);
        helper.assertTrue(harvester.isValidCrop(level, pos, ripePole), "ripe pole refused");
        helper.assertFalse(harvester.isValidCrop(level, pos, BSCrops.BOUCHOT.get().defaultBlockState()), "empty pole taken");
        List<ItemStack> harvested = Block.getDrops(ripePole, level, pos, null);
        helper.assertTrue(harvested.stream().noneMatch(stack -> stack.is(BSCrops.BOUCHOT.get().asItem())), "the harvester got the pole: " + harvested);
        helper.assertTrue(harvested.stream().anyMatch(stack -> stack.is(BSCrops.MUSSEL.get())), "no mussels harvested: " + harvested);
        helper.succeed();
    }

    // ------------------------------------------------------------------ helpers

    private static void grow(GameTestHelper helper, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        ServerLevel level = helper.getLevel();
        for (int i = 0; i < TICKS; i++) {
            BlockState state = level.getBlockState(pos);
            if (state.isRandomlyTicking()) {
                state.randomTick(level, pos, level.random);
            }
        }
    }

    // Stone all around the crop and its farmland, above and below: farmland is not a full block, so
    // sky light would come in through its sides.
    private static void cellar(GameTestHelper helper) {
        for (int x = 0; x <= 2; x++) {
            for (int z = 0; z <= 2; z++) {
                helper.setBlock(new BlockPos(x, 2, z), Blocks.STONE);
                helper.setBlock(new BlockPos(x, -1, z), Blocks.STONE);
                if (x != 1 || z != 1) {
                    helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
                    helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                }
            }
        }
    }

    private static int light(GameTestHelper helper) {
        return helper.getLevel().getRawBrightness(helper.absolutePos(CROP), 0);
    }

    private static List<ItemStack> drops(GameTestHelper helper, BlockState state) {
        return Block.getDrops(state, helper.getLevel(), helper.absolutePos(CROP), null);
    }

    private static void expectDrops(GameTestHelper helper, CropBlock crop, Item product) {
        List<ItemStack> ripe = drops(helper, crop.getStateForAge(crop.getMaxAge()));
        helper.assertTrue(ripe.stream().anyMatch(stack -> stack.is(product)), BlockPosText.of(crop.defaultBlockState()) + " gives " + ripe);
    }

    private static final class BlockPosText {
        static String of(BlockState state) {
            return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()) + " " + state.getValues();
        }
    }
}
