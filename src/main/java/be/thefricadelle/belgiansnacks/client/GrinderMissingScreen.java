/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.client;

import java.util.ArrayList;
import java.util.List;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.network.GrinderMissingResponsePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Read-only list of the foods a supreme grinder still misses, as item icons. Only the visible rows
 * are drawn, so a list of a few thousand costs the same as a short one.
 */
public class GrinderMissingScreen extends Screen {
    private static final int CELL = 18;
    private static final int MAX_COLUMNS = 16;
    private static final int PADDING = 8;
    private static final int HEADER = 22;

    private final BlockPos pos;
    private final List<ItemStack> stacks;
    private final int total;
    private int columns;
    private int visibleRows;
    private int left;
    private int top;
    private int scrollRow;

    public GrinderMissingScreen(GrinderMissingResponsePayload payload) {
        super(Component.translatable(BelgianSnacks.MOD_ID + ".grinder.screen.title"));
        this.pos = payload.pos();
        this.total = payload.total();
        this.stacks = new ArrayList<>(payload.missing().size());
        payload.missing().forEach(id -> stacks.add(new ItemStack(BuiltInRegistries.ITEM.get(id))));
    }

    @Override
    protected void init() {
        columns = Mth.clamp((width - 2 * PADDING - 16) / CELL, 1, MAX_COLUMNS);
        visibleRows = Math.max(1, (height - 2 * PADDING - HEADER - 16) / CELL);
        int panelWidth = columns * CELL + 2 * PADDING;
        int panelHeight = Math.min(rows(), visibleRows) * CELL + 2 * PADDING + HEADER;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        scrollRow = Mth.clamp(scrollRow, 0, maxScroll());
    }

    private int rows() {
        return Math.max(1, (stacks.size() + columns - 1) / columns);
    }

    private int maxScroll() {
        return Math.max(0, rows() - visibleRows);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int panelWidth = columns * CELL + 2 * PADDING;
        int shownRows = Math.min(rows(), visibleRows);
        int panelHeight = shownRows * CELL + 2 * PADDING + HEADER;
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xE0101010);
        graphics.renderOutline(left, top, panelWidth, panelHeight, 0xFFB08830);

        Component header = stacks.isEmpty()
            ? Component.translatable(BelgianSnacks.MOD_ID + ".grinder.screen.none")
            : Component.translatable(BelgianSnacks.MOD_ID + ".grinder.screen.count", stacks.size(), total);
        graphics.drawString(font, title, left + PADDING, top + PADDING, 0xFFE0B040, false);
        graphics.drawString(font, header, left + PADDING, top + PADDING + 10, 0xFFA0A0A0, false);

        int gridTop = top + PADDING + HEADER;
        ItemStack hovered = ItemStack.EMPTY;
        int first = scrollRow * columns;
        int last = Math.min(stacks.size(), first + shownRows * columns);
        for (int i = first; i < last; i++) {
            int slot = i - first;
            int x = left + PADDING + (slot % columns) * CELL;
            int y = gridTop + (slot / columns) * CELL;
            graphics.fill(x, y, x + 16, y + 16, 0x40FFFFFF);
            graphics.renderItem(stacks.get(i), x, y);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                hovered = stacks.get(i);
            }
        }
        if (maxScroll() > 0) {
            int trackX = left + panelWidth - 4;
            int trackHeight = shownRows * CELL;
            int thumb = Math.max(8, trackHeight * visibleRows / rows());
            int thumbY = gridTop + (trackHeight - thumb) * scrollRow / maxScroll();
            graphics.fill(trackX, gridTop, trackX + 2, gridTop + trackHeight, 0x40FFFFFF);
            graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumb, 0xFFB08830);
        }
        if (!hovered.isEmpty()) {
            graphics.renderTooltip(font, hovered, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollRow = Mth.clamp(scrollRow - (int) Math.signum(scrollY), 0, maxScroll());
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int missingCount() {
        return stacks.size();
    }

    /** Icons drawn per frame at most: the visible rows, not the whole list. */
    public int drawnPerFrame() {
        return Math.min(stacks.size() - scrollRow * columns, Math.min(rows(), visibleRows) * columns);
    }
}
