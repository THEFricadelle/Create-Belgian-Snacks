/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import static be.thefricadelle.belgiansnacks.BelgianSnacks.REGISTRATE;

import java.util.List;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.crop.AsparagusCropBlock;
import be.thefricadelle.belgiansnacks.content.crop.BSCropBlock;
import be.thefricadelle.belgiansnacks.content.crop.BouchotBlock;
import be.thefricadelle.belgiansnacks.content.crop.ChiconCropBlock;
import be.thefricadelle.belgiansnacks.content.crop.ShortCropBlock;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.Tags;

/**
 * v2 crops and mussels (docs/13, section 5.8). Each crop block and the item that plants it are
 * registered together here, so neither class has to load the other first.
 */
public final class BSCrops {
    // ------------------------------------------------------------------ crops

    public static final BlockEntry<ChiconCropBlock> CHICON_CROP =
        crop("chicon_crop", "Chicons", p -> new ChiconCropBlock(p, BSCrops.CHICORY_ROOT));
    public static final BlockEntry<BSCropBlock> LEEK_CROP = crop("leek_crop", "Leeks", p -> new BSCropBlock(p, BSCrops.LEEK));
    public static final BlockEntry<BSCropBlock> HOPS_CROP = crop("hops_crop", "Hops", p -> new BSCropBlock(p, BSCrops.HOP_SEEDS));
    public static final BlockEntry<BSCropBlock> BRUSSELS_SPROUTS_CROP =
        crop("brussels_sprouts_crop", "Brussels Sprouts", p -> new BSCropBlock(p, BSCrops.BRUSSELS_SPROUT));
    public static final BlockEntry<AsparagusCropBlock> ASPARAGUS_CROP =
        crop("asparagus_crop", "Asparagus", p -> new AsparagusCropBlock(p, BSCrops.ASPARAGUS_CROWN));
    public static final BlockEntry<BSCropBlock> BUCKWHEAT_CROP = crop("buckwheat_crop", "Buckwheat", p -> new BSCropBlock(p, BSCrops.BUCKWHEAT));
    public static final BlockEntry<BSCropBlock> MUSTARD_CROP = crop("mustard_crop", "Mustard", p -> new BSCropBlock(p, BSCrops.MUSTARD_SEEDS));

    // ------------------------------------------------------------------ what plants them, what they give

    public static final ItemEntry<ItemNameBlockItem>
        CHICORY_ROOT = planter("chicory_root", "Chicory Root", CHICON_CROP, tag("seeds/chicory")),
        LEEK = planter("leek", "Leek", LEEK_CROP, tag("crops/leek")),
        HOP_SEEDS = planter("hop_seeds", "Hop Seeds", HOPS_CROP, tag("seeds/hops")),
        BRUSSELS_SPROUT = planter("brussels_sprout", "Brussels Sprout", BRUSSELS_SPROUTS_CROP, tag("crops/brussels_sprout")),
        ASPARAGUS_CROWN = planter("asparagus_crown", "Asparagus Crown", ASPARAGUS_CROP, tag("seeds/asparagus")),
        BUCKWHEAT = planter("buckwheat", "Buckwheat", BUCKWHEAT_CROP, tag("crops/buckwheat")),
        MUSTARD_SEEDS = planter("mustard_seeds", "Mustard Seeds", MUSTARD_CROP, tag("seeds/mustard"));

    public static final ItemEntry<Item>
        CHICON = product("chicon", "Chicon", tag("crops/chicon")),
        HOPS = product("hops", "Hops", tag("crops/hops")),
        HOP_SHOOTS = product("hop_shoots", "Hop Shoots", tag("crops/hop_shoots")),
        WHITE_ASPARAGUS = product("white_asparagus", "White Asparagus", tag("crops/asparagus")),
        GREEN_ASPARAGUS = product("green_asparagus", "Green Asparagus", tag("crops/asparagus")),
        BUCKWHEAT_FLOUR = product("buckwheat_flour", "Buckwheat Flour", tag("flours/buckwheat")),
        MUSTARD_POWDER = product("mustard_powder", "Mustard Powder", tag("dusts/mustard")),
        MUSSEL = product("mussel", "Mussel", tag("foods/raw_mussel"));

    // ------------------------------------------------------------------ the mussel pole

    public static final BlockEntry<BouchotBlock> BOUCHOT = REGISTRATE.block("bouchot", p -> new BouchotBlock(p, BSCrops.MUSSEL))
        .initialProperties(() -> Blocks.BAMBOO_SAPLING)
        .properties(p -> p.mapColor(MapColor.WOOD).sound(SoundType.BAMBOO).noOcclusion().noCollission().randomTicks().strength(0.5f)
            .pushReaction(PushReaction.DESTROY).dynamicShape().offsetType(BlockBehaviour.OffsetType.NONE))
        .lang("Bouchot")
        .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.get()).forAllStatesExcept(state -> ConfiguredModel.builder()
            .modelFile(prov.models().cross("block/bouchot/stage" + state.getValue(BouchotBlock.AGE),
                prov.modLoc("block/bouchot/stage" + state.getValue(BouchotBlock.AGE))).renderType("cutout"))
            .build(), BouchotBlock.WATERLOGGED))
        // Ripe mussels for anyone; the pole itself only for a player, so a harvester never duplicates it.
        .loot((tables, block) -> tables.add(block, LootTable.lootTable()
            .withPool(LootPool.lootPool().add(LootItem.lootTableItem(block))
                .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                    EntityPredicate.Builder.entity().of(EntityType.PLAYER))))
            .withPool(LootPool.lootPool().add(LootItem.lootTableItem(BSCrops.MUSSEL.get())
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))))
                .when(ripe(block, BouchotBlock.AGE, BouchotBlock.MAX_AGE)))))
        .item()
        .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/bouchot")))
        .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
            .pattern(" T ").pattern("SMS").pattern(" S ")
            .define('S', Items.STICK).define('T', Items.STRING).define('M', BSCrops.MUSSEL.get())
            .unlockedBy("has_mussel", RegistrateRecipeProvider.has(BSCrops.MUSSEL.get()))
            .save(prov, BelgianSnacks.asResource("crafting/bouchot")))
        .build()
        .register();

    /** Every crop block, for tests and the Botany Pots compat. */
    public static final List<BlockEntry<? extends CropBlock>> CROPS =
        List.of(CHICON_CROP, LEEK_CROP, HOPS_CROP, BRUSSELS_SPROUTS_CROP, ASPARAGUS_CROP, BUCKWHEAT_CROP, MUSTARD_CROP);

    private BSCrops() {
    }

    public static void register() {
        // Loading the class registers the entries above with Registrate.
    }

    private static TagKey<Item> tag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }

    private static <B extends CropBlock> BlockEntry<B> crop(String name, String english,
                                                        NonNullFunction<BlockBehaviour.Properties, B> factory) {
        return REGISTRATE.block(name, factory)
            .initialProperties(() -> Blocks.WHEAT)
            .lang(english)
            .blockstate(BSCrops::cropModels)
            .loot((tables, block) -> tables.add(block, cropDrops(tables, block)))
            .register();
    }

    // Four drawings per crop: ages 0-7 share them two by two, ages 0-3 one each. Ripe asparagus
    // shows white or green.
    private static void cropModels(DataGenContext<Block, ? extends CropBlock> ctx,
                                   RegistrateBlockstateProvider prov) {
        CropBlock crop = ctx.get();
        String name = ctx.getName().replace("_crop", "");
        prov.getVariantBuilder(crop).forAllStates(state -> {
            int age = crop.getAge(state);
            int stage = stage(crop, state);
            String texture = name + "_stage" + stage;
            if (crop instanceof AsparagusCropBlock && age == 3) {
                texture += state.getValue(AsparagusCropBlock.BLANCHED) ? "_white" : "_green";
            }
            return ConfiguredModel.builder()
                .modelFile(prov.models().crop("block/crop/" + texture, prov.modLoc("block/crop/" + texture)).renderType("cutout"))
                .build();
        });
    }

    private static LootTable.Builder cropDrops(RegistrateBlockLootTables tables, CropBlock block) {
        IntegerProperty age = block instanceof ShortCropBlock ? ShortCropBlock.AGE : CropBlock.AGE;
        LootItemCondition.Builder ripe = ripe(block, age, block.getMaxAge());
        LootTable.Builder table = LootTable.lootTable();
        if (block == CHICON_CROP.get()) {
            table.withPool(count(CHICON.get(), 2, 3).when(ripe)).withPool(chance(CHICORY_ROOT.get(), 0.5f).when(ripe));
            seedUnlessRipe(table, CHICORY_ROOT.get(), ripe);
        } else if (block == HOPS_CROP.get()) {
            table.withPool(count(HOPS.get(), 1, 2).when(ripe)).withPool(count(HOP_SEEDS.get(), 1, 1).when(ripe))
                .withPool(chance(HOP_SHOOTS.get(), 0.5f).when(ripe));
            seedUnlessRipe(table, HOP_SEEDS.get(), ripe);
        } else if (block == ASPARAGUS_CROP.get()) {
            LootItemCondition.Builder white = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(age, 3).hasProperty(AsparagusCropBlock.BLANCHED, true));
            LootItemCondition.Builder green = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(age, 3).hasProperty(AsparagusCropBlock.BLANCHED, false));
            table.withPool(count(WHITE_ASPARAGUS.get(), 1, 2).when(white)).withPool(count(GREEN_ASPARAGUS.get(), 1, 2).when(green))
                .withPool(chance(ASPARAGUS_CROWN.get(), 0.25f).when(ripe));
            seedUnlessRipe(table, ASPARAGUS_CROWN.get(), ripe);
        } else {
            // Leek, sprouts, buckwheat and mustard plant themselves: more of them once ripe.
            ItemLike self = block == LEEK_CROP.get() ? LEEK.get() : block == BRUSSELS_SPROUTS_CROP.get() ? BRUSSELS_SPROUT.get()
                : block == BUCKWHEAT_CROP.get() ? BUCKWHEAT.get() : MUSTARD_SEEDS.get();
            int[] range = block == BRUSSELS_SPROUTS_CROP.get() || block == MUSTARD_CROP.get() ? new int[] {2, 4}
                : block == BUCKWHEAT_CROP.get() ? new int[] {2, 3} : new int[] {1, 3};
            table.withPool(count(self, range[0], range[1]).when(ripe));
            seedUnlessRipe(table, self, ripe);
        }
        return tables.applyExplosionDecay(block, table);
    }

    private static LootItemCondition.Builder ripe(Block block, IntegerProperty age, int max) {
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(age, max));
    }

    private static LootPool.Builder count(ItemLike item, int min, int max) {
        return LootPool.lootPool().add(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))));
    }

    private static LootPool.Builder chance(ItemLike item, float chance) {
        return LootPool.lootPool().add(LootItem.lootTableItem(item)).when(LootItemRandomChanceCondition.randomChance(chance));
    }

    private static void seedUnlessRipe(LootTable.Builder table, ItemLike seed, LootItemCondition.Builder ripe) {
        table.withPool(LootPool.lootPool().add(LootItem.lootTableItem(seed)).when(InvertedLootItemCondition.invert(ripe)));
    }

    private static ItemEntry<ItemNameBlockItem> planter(String name, String english, BlockEntry<? extends Block> crop, TagKey<Item> tag) {
        TagKey<Item> parent = tag.location().getPath().startsWith("seeds/") ? Tags.Items.SEEDS : Tags.Items.CROPS;
        return REGISTRATE.item(name, p -> new ItemNameBlockItem(crop.get(), p)).lang(english).tag(tag, parent).register();
    }

    private static ItemEntry<Item> product(String name, String english, TagKey<Item> tag) {
        return REGISTRATE.item(name, Item::new).lang(english).tag(tag).register();
    }

    /** The growth stage a state shows, 0 to 3. */
    public static int stage(CropBlock crop, BlockState state) {
        return crop.getMaxAge() == 3 ? crop.getAge(state) : Math.min(3, crop.getAge(state) / 2);
    }
}
