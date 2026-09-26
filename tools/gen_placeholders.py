#!/usr/bin/env python3
# Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
# SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
#
# Proprietary, source-available software. Public visibility of this source
# grants no right to copy, reuse, redistribute, or create derivative works.
# See LICENSE and CONTRIBUTING.md at the repository root.
"""Generate 16x16 placeholder item textures, standard library only.

Items, fluids (still, flow) and buckets. Existing files are never overwritten unless --force is given, so real textures
dropped in place are safe. Usage: python tools/gen_placeholders.py [--force]
"""

import argparse
import math
import pathlib
import struct
import zlib

SIZE = 16
ROOT = pathlib.Path(__file__).resolve().parent.parent
ITEM_DIR = ROOT / "src/main/resources/assets/create_belgian_snacks/textures/item"
FLUID_DIR = ROOT / "src/main/resources/assets/create_belgian_snacks/textures/fluid"

# id -> (shape, base colour, glyph). Digits mark the tier, letters the ingredient.
ITEMS = {
    "minced_pork": ("heap", (0xE8, 0x9A, 0x9A), "P"),
    "minced_beef": ("heap", (0xB0, 0x3A, 0x3A), "B"),
    "minced_chicken": ("heap", (0xF0, 0xC8, 0xA0), "C"),
    "beef_tallow": ("cube", (0xF4, 0xEE, 0xD8), "T"),
    "bread_crumbs": ("heap", (0xD8, 0xA8, 0x58), "R"),
    "belgian_spices": ("powder", (0xC0, 0x60, 0x20), "S"),
    "fricadelle_paste": ("ball", (0xC8, 0x80, 0x78), "1"),
    "raw_fricadelle": ("stick", (0xE8, 0x98, 0x98), "1"),
    "fricadelle": ("stick", (0x9A, 0x5A, 0x28), "1"),
    "exceptional_paste": ("ball", (0xD0, 0x90, 0x40), "2"),
    "incomplete_the_fricadelle": ("stick", (0xD8, 0xB0, 0x80), "2"),
    "raw_the_fricadelle": ("stick", (0xF0, 0xA8, 0x90), "2"),
    "the_fricadelle": ("stick", (0xB8, 0x70, 0x20), "2"),
    "absolute_paste": ("ball", (0xE8, 0xC8, 0x40), "3"),
    "raw_ultimate_fricadelle": ("stick", (0xF8, 0xC8, 0xA8), "3"),
    "ultimate_fricadelle": ("shiny_stick", (0xE0, 0xA8, 0x20), "3"),
}

# Fluid id -> colour. Each gets still/flow textures and a bucket item texture.
FLUIDS = {
    "frying_oil": (0xE8, 0xC5, 0x47),
    "melted_beef_tallow": (0xFB, 0xFA, 0xF4),
    "mayonnaise": (0xF3, 0xEB, 0xC4),
    "curry_ketchup": (0xA8, 0x32, 0x1E),
}

# 3x5 bitmap glyphs, one string per row.
GLYPHS = {
    "P": ["###", "#.#", "###", "#..", "#.."],
    "B": ["##.", "#.#", "##.", "#.#", "##."],
    "C": [".##", "#..", "#..", "#..", ".##"],
    "T": ["###", ".#.", ".#.", ".#.", ".#."],
    "R": ["##.", "#.#", "##.", "#.#", "#.#"],
    "S": [".##", "#..", ".#.", "..#", "##."],
    "1": [".#.", "##.", ".#.", ".#.", "###"],
    "2": ["##.", "..#", ".#.", "#..", "###"],
    "3": ["##.", "..#", ".#.", "..#", "##."],
}


def shade(colour, factor):
    return tuple(max(0, min(255, int(c * factor))) for c in colour)


def noise(x, y):
    # Deterministic per-pixel value in [0, 1) so reruns produce identical files.
    return ((x * 73856093) ^ (y * 19349663) ^ 0x5BD1E995) % 1000 / 1000.0


def mask(shape, x, y):
    cx, cy = x + 0.5, y + 0.5
    if shape == "heap":
        return cy > 7 and abs(cx - 8) < (cy - 5) * 1.1 and cy < 15
    if shape == "powder":
        return (cy > 10 and abs(cx - 8) < (cy - 9) * 2.2 and cy < 15) or (noise(x, y) > 0.93 and cy < 10)
    if shape == "ball":
        return (cx - 8) ** 2 + (cy - 9) ** 2 < 5.5 ** 2
    if shape == "cube":
        return 3 <= x <= 12 and 4 <= y <= 13
    if shape in ("stick", "shiny_stick"):
        # Distance to the segment (3,13)-(13,3), capsule-shaped.
        ax, ay, bx, by = 3.0, 13.0, 13.0, 3.0
        t = max(0.0, min(1.0, ((cx - ax) * (bx - ax) + (cy - ay) * (by - ay)) / ((bx - ax) ** 2 + (by - ay) ** 2)))
        px, py = ax + t * (bx - ax), ay + t * (by - ay)
        return math.hypot(cx - px, cy - py) < 2.4
    raise ValueError(shape)


def render(shape, colour, glyph):
    px = [[None] * SIZE for _ in range(SIZE)]
    inside = [[mask(shape, x, y) for x in range(SIZE)] for y in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            if not inside[y][x]:
                continue
            edge = any(
                not (0 <= x + dx < SIZE and 0 <= y + dy < SIZE and inside[y + dy][x + dx])
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))
            )
            if edge:
                c = (255, 240, 150) if shape == "shiny_stick" else shade(colour, 0.55)
            elif shape in ("heap", "powder"):
                c = shade(colour, 0.8 + 0.4 * noise(x, y))
            else:
                # Light from the top-left.
                c = shade(colour, 1.15 - 0.03 * (x + y))
            px[y][x] = c + (255,)
    rows = GLYPHS[glyph]
    for gy, row in enumerate(rows):
        for gx, cell in enumerate(row):
            if cell == "#":
                px[gy + 1][gx + 1] = (255, 255, 255, 255)
                if gx + 2 < SIZE and (gx + 1 >= len(row) or row[gx + 1] != "#"):
                    px[gy + 2][gx + 2] = px[gy + 2][gx + 2] or (40, 30, 20, 255)
    return px


def render_fluid(colour, frame):
    # Opaque liquid surface with soft ripples; the frame shifts them so flow textures animate.
    px = [[None] * SIZE for _ in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            ripple = math.sin((x + 2 * y + 4 * frame) * 0.7) * 0.06 + noise(x, y + frame) * 0.08
            px[y][x] = shade(colour, 0.95 + ripple) + (255,)
    return px


def render_bucket(colour):
    metal, dark = (0xB8, 0xB8, 0xC0), (0x50, 0x50, 0x58)
    px = [[None] * SIZE for _ in range(SIZE)]
    for y in range(4, 15):
        # Tapered pail: wider at the rim than at the base.
        half = 6 - (y - 4) // 4
        for x in range(8 - half, 8 + half):
            edge = x in (8 - half, 8 + half - 1) or y == 14
            px[y][x] = (dark if edge else metal) + (255,)
    for x in range(3, 13):
        px[5][x] = shade(colour, 1.0) + (255,)
        px[6][x] = shade(colour, 0.85) + (255,)
    for x in (4, 5, 10, 11):
        px[3][x] = dark + (255,)
    for x in range(6, 10):
        px[2][x] = dark + (255,)
    return px


def write_png(path, px):
    height, width = len(px), len(px[0])
    raw = b"".join(
        b"\x00" + b"".join(bytes(p if p else (0, 0, 0, 0)) for p in row) for row in px
    )

    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    path.write_bytes(png)


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--force", action="store_true", help="overwrite existing textures")
    args = parser.parse_args()

    outputs = {}
    for item_id, (shape, colour, glyph) in ITEMS.items():
        outputs[ITEM_DIR / f"{item_id}.png"] = lambda s=shape, c=colour, g=glyph: render(s, c, g)
    for fluid_id, colour in FLUIDS.items():
        outputs[FLUID_DIR / f"{fluid_id}_still.png"] = lambda c=colour: render_fluid(c, 0)
        # Two stacked frames, animated by the .mcmeta written next to it.
        outputs[FLUID_DIR / f"{fluid_id}_flow.png"] = lambda c=colour: render_fluid(c, 0) + render_fluid(c, 1)
        outputs[ITEM_DIR / f"{fluid_id}_bucket.png"] = lambda c=colour: render_bucket(c)

    written = skipped = 0
    for path, draw in outputs.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        if path.exists() and not args.force:
            skipped += 1
            continue
        write_png(path, draw())
        if path.name.endswith("_flow.png"):
            path.with_name(path.name + ".mcmeta").write_text('{"animation": {"frametime": 8}}\n', encoding="utf-8")
        written += 1
    print(f"written: {written}, kept existing: {skipped}")


if __name__ == "__main__":
    main()
