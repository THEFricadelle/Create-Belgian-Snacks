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

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.gametest.FriterieLineGameTests.Station;
import be.thefricadelle.belgiansnacks.registry.BSCrops;
import be.thefricadelle.belgiansnacks.registry.BSDishes;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * v2 Belgian oven dishes on real Create machines (docs/13, section 5.11): a belt under deployers,
 * spouts and a press, one pass each, with the line of FriterieLineGameTests.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BelgianDishesLineGameTests {
    private static final int STOCK = 16;

    private BelgianDishesLineGameTests() {
    }

    // A cooked porkchop is a ham (#hams).
    @GameTest(template = "empty_large", timeoutTicks = 1200)
    public static void croqueMonsieurIsAssembledOnRealMachines(GameTestHelper helper) {
        FriterieLineGameTests.run(helper, List.of(new ItemStack(Items.BREAD)), List.of(BSDishes.RAW_CROQUE_MONSIEUR.get()),
            List.of(Station.deployer(new ItemStack(Items.COOKED_PORKCHOP, STOCK)), Station.deployer(BSItems.HERVE_CHEESE.asStack(STOCK)),
                Station.press()), 0);
    }

    @GameTest(template = "empty_large", timeoutTicks = 1200)
    public static void chiconsAuGratinAreAssembledOnRealMachines(GameTestHelper helper) {
        FriterieLineGameTests.run(helper, List.of(BSCrops.CHICON.asStack()), List.of(BSDishes.RAW_CHICONS_AU_GRATIN.get()),
            List.of(Station.deployer(new ItemStack(Items.COOKED_PORKCHOP, STOCK)), Station.spout(BSFluids.BECHAMEL.get().getSource()),
                Station.deployer(BSItems.HERVE_CHEESE.asStack(STOCK))), 250);
    }

    // M18 waffle toppings: powdered sugar from a deployer, Liège syrup from a spout.
    @GameTest(template = "empty_large", timeoutTicks = 1200)
    public static void wafflesAreToppedOnRealMachines(GameTestHelper helper) {
        FriterieLineGameTests.run(helper, List.of(be.thefricadelle.belgiansnacks.registry.BSWaffles.BRUSSELS_WAFFLE.asStack()),
            List.of(be.thefricadelle.belgiansnacks.registry.BSWaffles.BRUSSELS_WAFFLE_SUGARED.get()),
            List.of(Station.deployer(BSItems.POWDERED_SUGAR.asStack(STOCK))), 0);
    }

    @GameTest(template = "empty_large", timeoutTicks = 1200)
    public static void lacquemantIsFilledOnARealSpout(GameTestHelper helper) {
        FriterieLineGameTests.run(helper, List.of(be.thefricadelle.belgiansnacks.registry.BSWaffles.LACQUEMANT_WAFER.asStack()),
            List.of(be.thefricadelle.belgiansnacks.registry.BSWaffles.LACQUEMANT.get()),
            List.of(Station.spout(BSFluids.LIEGE_SYRUP.get().getSource())), 50);
    }

    // M19: the sugar tart and the mattentaart take the same transitional item as the flamiche.
    @GameTest(template = "empty_large", timeoutTicks = 1200)
    public static void sugarTartIsAssembledOnRealMachines(GameTestHelper helper) {
        FriterieLineGameTests.run(helper, List.of(BSDishes.TART_SHELL.asStack()), List.of(be.thefricadelle.belgiansnacks.registry.BSSweets.RAW_TARTE_AU_SUCRE.get()),
            List.of(Station.deployer(BSItems.CASSONADE.asStack(STOCK)), Station.deployer(new ItemStack(Items.EGG, STOCK))), 0);
    }

    @GameTest(template = "empty_large", timeoutTicks = 1200)
    public static void mattentaartIsAssembledOnRealMachines(GameTestHelper helper) {
        FriterieLineGameTests.run(helper, List.of(BSDishes.TART_SHELL.asStack()), List.of(be.thefricadelle.belgiansnacks.registry.BSSweets.RAW_MATTENTAART.get()),
            List.of(Station.deployer(BSItems.MATON.asStack(STOCK)), Station.deployer(new ItemStack(Items.EGG, STOCK))), 0);
    }

    @GameTest(template = "empty_large", timeoutTicks = 1200)
    public static void merveilleuxIsAssembledOnRealMachines(GameTestHelper helper) {
        FriterieLineGameTests.run(helper, List.of(BSItems.MERINGUE.asStack()), List.of(be.thefricadelle.belgiansnacks.registry.BSSweets.MERVEILLEUX.get()),
            List.of(Station.spout(BSFluids.CREAM.get().getSource()), Station.deployer(BSItems.MERINGUE.asStack(STOCK)),
                Station.deployer(BSItems.CHOCOLATE_SHAVINGS.asStack(STOCK))), 100);
    }

    // Chocolate on a cocoa bean at one spout, kriek at the next: no press, which would turn the chocolate into bars.
    @GameTest(template = "empty_large", timeoutTicks = 1200)
    public static void pralinesAreFilledOnRealSpouts(GameTestHelper helper) {
        FriterieLineGameTests.run(helper, List.of(new ItemStack(Items.COCOA_BEANS)), List.of(be.thefricadelle.belgiansnacks.registry.BSSweets.PRALINE_KRIEK.get()),
            List.of(Station.spout(com.simibubi.create.AllFluids.CHOCOLATE.get().getSource()), Station.spout(BSFluids.KRIEK.get().getSource())), 50);
    }

    @GameTest(template = "empty_large", timeoutTicks = 1200)
    public static void flamicheIsAssembledOnRealMachines(GameTestHelper helper) {
        FriterieLineGameTests.run(helper, List.of(BSDishes.TART_SHELL.asStack()), List.of(BSDishes.RAW_FLAMICHE.get()),
            List.of(Station.deployer(BSCrops.LEEK.asStack(STOCK)), Station.spout(BSFluids.CREAM.get().getSource()),
                Station.deployer(new ItemStack(Items.EGG, STOCK))), 250);
    }
}
