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
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import gen_placeholders as gen  # noqa: E402

MODELS = gen.ROOT / "src/main/resources/assets/create_belgian_snacks/models/block/custom"
# Hand-made models the datagen picks up when present (BSBlocks.HANDMADE).
OPTIONAL_MODELS = {
    "fryer.json": "Fryer (open vat; textures of your choice)",
    "supreme_grinder.json": "Supreme Grinder housing, must use the #side texture variable for the gauge",
    "supreme_grinder_blades.json": "Supreme Grinder blades, rotating about the vertical axis through the block centre",
}


def main():
    placeholder, handmade, missing = [], [], []
    for path, draw in sorted(gen.planned().items()):
        name = path.relative_to(gen.ROOT / "src/main/resources/assets/create_belgian_snacks/textures").as_posix()
        if not path.is_file():
            missing.append(name)
        elif path.read_bytes() == gen.png_bytes(draw()):
            placeholder.append(name)
        else:
            handmade.append(name)
    total = len(placeholder) + len(handmade) + len(missing)
    print(f"textures: {len(handmade)} hand-made, {len(placeholder)} placeholders, {len(missing)} missing, of {total}")
    for label, names in (("missing", missing), ("placeholder", placeholder), ("hand-made", handmade)):
        for name in names:
            print(f"  {label:11} {name}")
    print("hand-made block models (optional, the generated ones stand in):")
    for file, what in OPTIONAL_MODELS.items():
        state = "present" if (MODELS / file).is_file() else "not yet"
        print(f"  {state:11} models/block/custom/{file}: {what}")
    return 1 if missing else 0


if __name__ == "__main__":
    sys.exit(main())
