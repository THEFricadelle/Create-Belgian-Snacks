#!/usr/bin/env python3
# Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
# SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
#
# Proprietary, closed-source software. Access to this source is restricted and
# grants no right to copy, share, reuse, redistribute, or create derivative works.
# See LICENSE and CONTRIBUTING.md at the repository root.
"""Project icon from the THE_FRICADELLE item texture, standard library only.

Scales the 16x16 texture by whole pixels (no smoothing) onto an opaque square background and writes
the mod list logo (src/main/resources/create_belgian_snacks.png, logoFile in neoforge.mods.toml)
and the CurseForge / Modrinth icon (docs/publication/icon.png). Re-run after changing the texture.
Usage: python tools/make_icon.py [--size 512] [--background 2b1d14]
"""

import argparse
import sys

import asset_status
import gen_placeholders as gen

TEXTURE = gen.ROOT / "src/main/resources/assets/create_belgian_snacks/textures/item/ultimate_fricadelle.png"
OUTPUTS = (
    gen.ROOT / "src/main/resources/create_belgian_snacks.png",
    gen.ROOT / "docs/publication/icon.png",
)


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--size", type=int, default=512)
    parser.add_argument("--background", default="2b1d14", help="hex RGB behind transparent pixels")
    args = parser.parse_args()

    decoded = asset_status.decode_png(TEXTURE.read_bytes())
    if isinstance(decoded, str):
        sys.exit(f"{TEXTURE.name}: {decoded}")
    width, height, rows = decoded
    background = tuple(bytes.fromhex(args.background)) + (255,)
    scale = args.size * 7 // 8 // max(width, height)
    left = (args.size - width * scale) // 2
    top = (args.size - height * scale) // 2

    px = [[background] * args.size for _ in range(args.size)]
    for y in range(height):
        for x in range(width):
            r, g, b, a = rows[y][x * 4:x * 4 + 4]
            if a == 0:
                continue
            for dy in range(scale):
                row = px[top + y * scale + dy]
                for dx in range(scale):
                    row[left + x * scale + dx] = (r, g, b, 255)

    data = gen.png_bytes(px)
    for out in OUTPUTS:
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_bytes(data)
        print(f"wrote {out.relative_to(gen.ROOT)} ({args.size}x{args.size}, texture x{scale})")


if __name__ == "__main__":
    main()
