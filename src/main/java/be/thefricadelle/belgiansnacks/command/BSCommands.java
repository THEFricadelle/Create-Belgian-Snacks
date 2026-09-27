/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.command;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.content.food.FoodIndexRules;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /belgiansnacks foods count|export (op level 2): the numbers behind the grinder rate and blacklist. */
public final class BSCommands {
    private static final String KEY = BelgianSnacks.MOD_ID + ".command.foods.";
    private static final String GRINDER_KEY = BelgianSnacks.MOD_ID + ".command.grinder.";

    private BSCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("belgiansnacks")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("foods")
                .then(Commands.literal("count").executes(BSCommands::count))
                .then(Commands.literal("export").executes(BSCommands::export)))
            .then(Commands.literal("grinder")
                .then(Commands.literal("fill")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> fill(context, 100))
                        .then(Commands.argument("percent", IntegerArgumentType.integer(0, 100))
                            .executes(context -> fill(context, IntegerArgumentType.getInteger(context, "percent"))))))
                .then(Commands.literal("clear")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(BSCommands::clear)))));
    }

    // Deterministic: the first foods of the sorted index, so a test knows exactly what is in.
    private static int fill(CommandContext<CommandSourceStack> context, int percent) throws CommandSyntaxException {
        SupremeGrinderBlockEntity grinder = grinderAt(context);
        if (grinder == null) {
            return 0;
        }
        List<ResourceLocation> ids = FoodIndex.server().ids();
        int amount = (int) Math.round(ids.size() * percent / 100.0);
        grinder.setConsumed(ids.subList(0, amount));
        context.getSource().sendSuccess(() -> Component.translatable(GRINDER_KEY + "filled", amount, ids.size()), true);
        return amount;
    }

    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        SupremeGrinderBlockEntity grinder = grinderAt(context);
        if (grinder == null) {
            return 0;
        }
        grinder.setConsumed(List.of());
        context.getSource().sendSuccess(() -> Component.translatable(GRINDER_KEY + "cleared"), true);
        return 1;
    }

    @Nullable
    private static SupremeGrinderBlockEntity grinderAt(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        if (context.getSource().getLevel().getBlockEntity(pos) instanceof SupremeGrinderBlockEntity grinder) {
            return grinder;
        }
        context.getSource().sendFailure(Component.translatable(GRINDER_KEY + "not_found", pos.toShortString()));
        return null;
    }

    private static int count(CommandContext<CommandSourceStack> context) {
        FoodIndex.Snapshot index = FoodIndex.server();
        long extra = index.sources().values().stream().filter(s -> s == FoodIndexRules.Source.EXTRA).count();
        context.getSource().sendSuccess(() -> Component.translatable(KEY + "count", index.size(), extra, index.excluded(),
            String.format(java.util.Locale.ROOT, "%.1f", index.computeNanos() / 1_000_000.0)), false);
        return index.size();
    }

    private static int export(CommandContext<CommandSourceStack> context) {
        Path file = FMLPaths.CONFIGDIR.get().resolve(BelgianSnacks.MOD_ID).resolve("foods_export.csv");
        try {
            int written = exportTo(context.getSource().getServer(), file);
            context.getSource().sendSuccess(() -> Component.translatable(KEY + "export", written, file.toString()), true);
            return written;
        } catch (IOException e) {
            BelgianSnacks.LOGGER.error("Food export failed", e);
            context.getSource().sendFailure(Component.translatable(KEY + "export_failed", e.getMessage()));
            return 0;
        }
    }

    /** One line per food: id, modid, nutrition, saturation, source (food/extra), has_recipe. */
    public static int exportTo(MinecraftServer server, Path file) throws IOException {
        FoodIndex.Snapshot index = FoodIndex.server();
        Set<ResourceLocation> produced = producedByRecipes(server);
        List<String> lines = new ArrayList<>(index.size() + 1);
        lines.add("id,modid,nutrition,saturation,source,has_recipe");
        for (ResourceLocation id : index.ids()) {
            Item item = BuiltInRegistries.ITEM.get(id);
            FoodProperties food = item.components().get(DataComponents.FOOD);
            FoodIndexRules.Source source = index.sources().getOrDefault(id, FoodIndexRules.Source.FOOD);
            lines.add(String.join(",", id.toString(), id.getNamespace(),
                food == null ? "" : Integer.toString(food.nutrition()),
                food == null ? "" : String.format(java.util.Locale.ROOT, "%.2f", food.saturation()),
                source.name().toLowerCase(java.util.Locale.ROOT),
                Boolean.toString(produced.contains(id))));
        }
        Files.createDirectories(file.getParent());
        Files.write(file, lines, StandardCharsets.UTF_8);
        return index.size();
    }

    // Items some loaded recipe outputs, Create's chance outputs included. Foods with none are the
    // likeliest blacklist candidates (loot-only, creative-only or disabled by the pack).
    private static Set<ResourceLocation> producedByRecipes(MinecraftServer server) {
        Set<ResourceLocation> produced = new HashSet<>();
        var registries = server.registryAccess();
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            try {
                ItemStack result = holder.value().getResultItem(registries);
                if (!result.isEmpty()) {
                    produced.add(BuiltInRegistries.ITEM.getKey(result.getItem()));
                }
                if (holder.value() instanceof ProcessingRecipe<?, ?> processing) {
                    for (ProcessingOutput output : processing.getRollableResults()) {
                        produced.add(BuiltInRegistries.ITEM.getKey(output.getStack().getItem()));
                    }
                }
            } catch (RuntimeException e) {
                // A broken recipe from another mod must not stop the export.
                BelgianSnacks.LOGGER.debug("Skipping recipe {} in the food export", holder.id(), e);
            }
        }
        return produced;
    }
}
