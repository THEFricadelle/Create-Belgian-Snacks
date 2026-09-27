#!/usr/bin/env python3
# Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
# SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
#
# Proprietary, source-available software. Public visibility of this source
# grants no right to copy, reuse, redistribute, or create derivative works.
# See LICENSE and CONTRIBUTING.md at the repository root.
"""Which textures and models are still placeholders, standard library only.

Every texture the mod needs is listed by tools/gen_placeholders.py; a file whose bytes are exactly
what the generator would draw is a placeholder, anything else is hand-made. The optional
hand-made block models (docs/06) are listed apart. Usage: python tools/asset_status.py
"""

import pathlib
import struct
import sys
import zlib

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import gen_placeholders as gen  # noqa: E402

MODELS = gen.ROOT / "src/main/resources/assets/create_belgian_snacks/models/block/custom"
# Hand-made models the datagen picks up when present (BSBlocks.HANDMADE).
OPTIONAL_MODELS = {
    "fryer.json": "Fryer (open vat; textures of your choice)",
    "supreme_grinder.json": "Supreme Grinder housing, must use the #side texture variable for the gauge",
    "supreme_grinder_blades.json": "Supreme Grinder blades, rotating about the vertical axis through the block centre",
}

SOUNDS = gen.ROOT / "src/main/resources/assets/create_belgian_snacks/sounds"
# Recordings the datagen picks up when present (BSSoundDefinitionsProvider.recordingOr); vanilla
# sounds stand in until then. The visitor only says line 4 (TheFricadelleNpc.SPOKEN_PHRASE), whose
# text is in BSLang (npc.phrase.4).
SOUNDS_USED = {
    "fryer/sizzle.ogg": "Fryer sizzling, played every 2 s while frying (about 2 s long)",
    "grinder/grind.ogg": "Supreme Grinder taking a food (short, under 1 s)",
    "grinder/complete.ogg": "Supreme Grinder finishing a paste (1 to 2 s)",
    "grinder/running.ogg": "Supreme Grinder turning, played every 2 s while fast enough (about 2 s long)",
    "npc/phrase_4.ogg": "THEFricadelle's voice, line 4 (the visitor always says it)",
}


MAX_COLOURS = 48


def ogg_problem(data):
    """None for a mono Ogg Vorbis file, the reason otherwise (a stereo sound is not positional in game)."""
    if data[:4] != b"OggS":
        if data[:3] == b"ID3" or data[:2] in (b"\xff\xfb", b"\xff\xf3", b"\xff\xf2"):
            return "an MP3 renamed to .ogg; convert it: ffmpeg -i in.mp3 -ac 1 -c:a libvorbis -q:a 5 out.ogg"
        return "not an Ogg file"
    segments = data[26]
    packet = data[27 + segments:]
    if packet[:7] != b"\x01vorbis":
        return "not Vorbis (export as Ogg Vorbis)"
    channels = packet[11]
    return None if channels == 1 else f"{channels} channels, must be mono"


def decode_png(data):
    """Width, height and RGBA rows of an 8-bit RGBA, non-interlaced PNG; a reason string otherwise."""
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        return "not a PNG"
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
    if depth != 8 or colour != 6 or interlace:
        return f"must be 8-bit RGBA, non-interlaced (bit depth {depth}, colour type {colour})"
    raw, stride, rows, prev = zlib.decompress(idat), width * 4, [], bytearray(width * 4)
    for y in range(height):
        kind, line = raw[y * (stride + 1)], bytearray(raw[y * (stride + 1) + 1:(y + 1) * (stride + 1)])
        for i in range(stride):
            a = line[i - 4] if i >= 4 else 0
            b = prev[i]
            c = prev[i - 4] if i >= 4 else 0
            if kind == 1:
                line[i] = (line[i] + a) & 255
            elif kind == 2:
                line[i] = (line[i] + b) & 255
            elif kind == 3:
                line[i] = (line[i] + (a + b) // 2) & 255
            elif kind == 4:
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                line[i] = (line[i] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 255
        rows.append(bytes(line))
        prev = line
    return width, height, rows


def problems(path, expected):
    """What makes a hand-made texture unusable: size, format, soft edges, too many colours."""
    decoded = decode_png(path.read_bytes())
    if isinstance(decoded, str):
        return [decoded]
    width, height, rows = decoded
    found = []
    if (width, height) != expected:
        found.append(f"size {width}x{height}, expected {expected[0]}x{expected[1]}")
    pixels = [row[i:i + 4] for row in rows for i in range(0, len(row), 4)]
    alphas = {p[3] for p in pixels}
    if not alphas <= {0, 255}:
        found.append("half-transparent pixels (pixel art needs each pixel fully opaque or fully clear)")
    colours = {p for p in pixels if p[3] == 255}
    if len(colours) > MAX_COLOURS:
        found.append(f"{len(colours)} colours, over {MAX_COLOURS}: blurred or not pixel art")
    return found


def main():
    placeholder, handmade, missing, broken = [], [], [], []
    for path, draw in sorted(gen.planned().items()):
        name = path.relative_to(gen.ROOT / "src/main/resources/assets/create_belgian_snacks/textures").as_posix()
        if not path.is_file():
            missing.append(name)
            continue
        pixels = draw()
        if path.read_bytes() == gen.png_bytes(pixels):
            placeholder.append(name)
            continue
        handmade.append(name)
        for problem in problems(path, (len(pixels[0]), len(pixels))):
            broken.append(f"{name}: {problem}")
    total = len(placeholder) + len(handmade) + len(missing)
    print(f"textures: {len(handmade)} hand-made, {len(placeholder)} placeholders, {len(missing)} missing, of {total}")
    for label, names in (("missing", missing), ("placeholder", placeholder), ("hand-made", handmade)):
        for name in names:
            print(f"  {label:11} {name}")
    if broken:
        print("hand-made textures to fix:")
        for line in broken:
            print("  " + line)
    print("hand-made block models (optional, the generated ones stand in):")
    for file, what in OPTIONAL_MODELS.items():
        state = "present" if (MODELS / file).is_file() else "not yet"
        print(f"  {state:11} models/block/custom/{file}: {what}")
    print("recorded sounds (optional, vanilla sounds stand in):")
    for file, what in SOUNDS_USED.items():
        path = SOUNDS / file
        if not path.is_file():
            state = "not yet"
        else:
            problem = ogg_problem(path.read_bytes())
            state = "present" if problem is None else "to fix"
            if problem:
                broken.append(f"sounds/{file}: {problem}")
                what += f" ({problem})"
        print(f"  {state:11} sounds/{file}: {what}")
    for path in sorted(p for p in SOUNDS.rglob("*") if p.is_file()) if SOUNDS.is_dir() else []:
        name = path.relative_to(SOUNDS).as_posix()
        if name not in SOUNDS_USED:
            reason = "no sound uses this name (see the list above)"
            problem = ogg_problem(path.read_bytes())
            if problem:
                reason += f"; {problem}"
            broken.append(f"sounds/{name}: {reason}")
            print(f"  {'unused':11} sounds/{name}: {reason}")
    return 1 if missing or broken else 0


if __name__ == "__main__":
    sys.exit(main())
