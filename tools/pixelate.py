#!/usr/bin/env python3
# Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
# SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
#
# Proprietary, source-available software. Public visibility of this source
# grants no right to copy, reuse, redistribute, or create derivative works.
# See LICENSE and CONTRIBUTING.md at the repository root.
"""Turn a large generated image into a game texture, standard library only.

Samples the centre of each cell (no smoothing), makes the magenta key colour (#FF00FF, and close
shades) fully transparent, every other pixel fully opaque, and writes an 8-bit RGBA PNG.
Usage: python tools/pixelate.py <input.png> <output.png> [--size 16x16] [--opaque]
--opaque keeps no transparency (block faces, fluids).
"""

import argparse
import pathlib
import struct
import sys
import zlib

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from gen_placeholders import png_bytes  # noqa: E402

CHANNELS = {2: 3, 6: 4}


def read_png(path):
    data = path.read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        sys.exit(f"{path} is not a PNG")
    pos, idat, header = 8, b"", None
    while pos < len(data):
        length, kind = struct.unpack(">I4s", data[pos:pos + 8])
        body = data[pos + 8:pos + 8 + length]
        if kind == b"IHDR":
            header = struct.unpack(">IIBBBBB", body)
        elif kind == b"IDAT":
            idat += body
        pos += 12 + length
    width, height, depth, colour, _, _, interlace = header
    if depth != 8 or colour not in CHANNELS or interlace:
        sys.exit(f"{path}: only 8-bit RGB or RGBA, non-interlaced PNGs (got depth {depth}, colour type {colour}); re-save it as such")
    bpp = CHANNELS[colour]
    stride, raw, rows, prev = width * bpp, zlib.decompress(idat), [], bytearray(width * bpp)
    for y in range(height):
        kind, line = raw[y * (stride + 1)], bytearray(raw[y * (stride + 1) + 1:(y + 1) * (stride + 1)])
        for i in range(stride):
            a = line[i - bpp] if i >= bpp else 0
            b = prev[i]
            c = prev[i - bpp] if i >= bpp else 0
            if kind == 1:
                line[i] = (line[i] + a) & 255
            elif kind == 2:
                line[i] = (line[i] + b) & 255
            elif kind == 3:
                line[i] = (line[i] + (a + b) // 2) & 255
            elif kind == 4:
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                line[i] = (line[i] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 255
        rows.append([tuple(line[i:i + bpp]) + ((255,) if bpp == 3 else ()) for i in range(0, stride, bpp)])
        prev = line
    return width, height, rows


def is_key(pixel):
    r, g, b, a = pixel
    return a < 128 or (r > 200 and b > 200 and g < 80)


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("input", type=pathlib.Path)
    parser.add_argument("output", type=pathlib.Path)
    parser.add_argument("--size", default="16x16", help="target WIDTHxHEIGHT, 16x32 for fluid flow textures")
    parser.add_argument("--opaque", action="store_true", help="no transparency (block faces, fluids)")
    args = parser.parse_args()
    target_w, target_h = (int(v) for v in args.size.lower().split("x"))
    width, height, rows = read_png(args.input)
    out = []
    for ty in range(target_h):
        row = []
        for tx in range(target_w):
            pixel = rows[int((ty + 0.5) * height / target_h)][int((tx + 0.5) * width / target_w)]
            if not args.opaque and is_key(pixel):
                row.append(None)
            else:
                row.append(pixel[:3] + (255,))
        out.append(row)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_bytes(png_bytes(out))
    print(f"{args.output}: {target_w}x{target_h}")


if __name__ == "__main__":
    main()
