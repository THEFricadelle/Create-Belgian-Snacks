#!/usr/bin/env python3
# Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
# SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
#
# Proprietary, closed-source software. Access to this source is restricted and
# grants no right to copy, share, reuse, redistribute, or create derivative works.
# See LICENSE and CONTRIBUTING.md at the repository root.
"""Export the food index of the real Arcadia pack, standard library only.

Copies the pack's mods, KubeJS scripts and configs from the CurseForge instance (read only) into
run/arcadia, leaving out every mod the dev runtime already provides, then runs arcadiaExport: a
client that creates a world, exports the food index and quits. The CSV and a per-mod summary are
copied into docs/data/. Opens a game window; expect several minutes and about 8 GB of memory.
Usage: python tools/arcadia_export.py [path to the instance]
"""

import os
import pathlib
import re
import shutil
import subprocess
import sys
import zipfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
GRADLEW = str(ROOT / ("gradlew.bat" if os.name == "nt" else "gradlew"))
DEFAULT_INSTANCE = pathlib.Path.home() / "curseforge" / "minecraft" / "Instances" / "Arcadia Echoes Of Power V2 (2)"
GAME = ROOT / "run" / "arcadia"
PACK_VERSION = "2.0.32"
# Already in the dev runtime (build.gradle); a second copy would stop the game at load.
DEV_MOD_IDS = {"create", "ponder", "flywheel", "jei", "jade", "mezz_config", "create_belgian_snacks"}


def mod_ids(jar):
    try:
        with zipfile.ZipFile(jar) as z:
            toml = z.read("META-INF/neoforge.mods.toml").decode("utf-8", errors="replace")
    except (KeyError, zipfile.BadZipFile):
        return set()
    # Only the mods the jar declares: [[dependencies.x]] blocks also carry modId lines.
    ids, section = set(), ""
    for line in toml.splitlines():
        header = re.match(r"^\s*\[\[?([^\]]+)\]\]?", line)
        if header:
            section = header.group(1).strip()
            continue
        declared = re.match(r'^\s*modId\s*=\s*"([^"]+)"', line)
        if declared and section == "mods":
            ids.add(declared.group(1))
    return ids


def prepare(instance, game=GAME, left_out=DEV_MOD_IDS):
    """Copies the pack into game, without the jars declaring one of the left_out mod ids."""
    mods = game / "mods"
    shutil.rmtree(mods, ignore_errors=True)
    shutil.rmtree(game / "saves", ignore_errors=True)
    mods.mkdir(parents=True)
    kept = skipped = 0
    for jar in sorted((instance / "mods").glob("*.jar")):
        ids = mod_ids(jar)
        if ids & left_out:
            print("  left out (already in dev):", jar.name)
            skipped += 1
            continue
        link_or_copy(jar, mods / jar.name)
        kept += 1
    # KubeJS edits tags and recipes; the pack also ships global datapacks and its own resource pack.
    for folder in ("kubejs", "config", "defaultconfigs", "datapacks", "resourcepacks", "moonlight-global-datapacks"):
        shutil.rmtree(game / folder, ignore_errors=True)
        if (instance / folder).is_dir():
            shutil.copytree(instance / folder, game / folder)
    for stale in ("arcadia-foods.csv", "arcadia-foods-summary.txt"):
        (game / stale).unlink(missing_ok=True)
    # The pack's own options (resource pack order included), with the automation settings on top.
    overrides = {"onboardAccessibility": "false", "pauseOnLostFocus": "false", "tutorialStep": "none",
                 "soundCategory_master": "0.0", "lang": "en_us", "renderDistance": "4"}
    lines = []
    source = instance / "options.txt"
    if source.is_file():
        for line in source.read_text(encoding="utf-8", errors="replace").splitlines():
            if line.split(":", 1)[0] not in overrides:
                lines.append(line)
    lines += [f"{key}:{value}" for key, value in overrides.items()]
    (game / "options.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"{kept} mods copied, {skipped} left out")


def link_or_copy(source, target):
    # A hard link costs no disk for the second and third copy of a 1 GB pack; copy across volumes.
    try:
        os.link(source, target)
    except OSError:
        shutil.copy2(source, target)


def main():
    instance = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_INSTANCE
    if not (instance / "mods").is_dir():
        print("no mods folder in", instance)
        return 1
    prepare(instance)
    log = open(GAME / "gradle-export.log", "w", encoding="utf-8")
    result = subprocess.run([GRADLEW, "runArcadiaExport", "--console=plain"], cwd=ROOT, stdout=log, stderr=subprocess.STDOUT)
    log.close()
    csv = GAME / "arcadia-foods.csv"
    summary = GAME / "arcadia-foods-summary.txt"
    if not csv.is_file() or not summary.is_file():
        print("no export produced (gradle exit", result.returncode, "), see", GAME / "logs" / "latest.log")
        return 1
    data = ROOT / "docs" / "data"
    data.mkdir(parents=True, exist_ok=True)
    shutil.copy2(csv, data / f"arcadia-{PACK_VERSION}-foods.csv")
    shutil.copy2(summary, data / f"arcadia-{PACK_VERSION}-foods-summary.txt")
    print(summary.read_text(encoding="utf-8"))
    return 0


if __name__ == "__main__":
    sys.exit(main())
