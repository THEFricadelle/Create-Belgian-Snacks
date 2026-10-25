#!/usr/bin/env python3
# Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
# SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
#
# Proprietary, closed-source software. Access to this source is restricted and
# grants no right to copy, share, reuse, redistribute, or create derivative works.
# See LICENSE and CONTRIBUTING.md at the repository root.
"""Removing the mod must never break a world (spec v2, rule R5), standard library only.

1. seed: the mod in dev fills a fresh flat world with everything of ours (UninstallSeedClient):
   items in the player, the ender chest, a chest, a barrel and a Create depot, our machines with
   their contents, our fluids in Create tanks and as placed blocks. Saves and quits.
2. without: the same world opened with Create's jars only and a probe mod, no class of this mod.
   The player must join; the probe saves and quits.
3. back: the world saved by phase 2 opened again with the release jar.
Opens three game windows, one after the other (about 6 minutes).
Usage: python tools/uninstall_smoke.py   (exit code 0 only if every phase passes)
"""

import os
import pathlib
import re
import shutil
import subprocess
import sys
import time

ROOT = pathlib.Path(__file__).resolve().parent.parent
GRADLEW = str(ROOT / ("gradlew.bat" if os.name == "nt" else "gradlew"))
RUN = ROOT / "run" / "uninstall"
DIRS = {"seed": RUN / "seed", "without": RUN / "without", "back": RUN / "back"}
TASKS = {"seed": "runUninstallSeed", "without": "runUninstallWithout", "back": "runUninstallBack"}
RUNS = {"seed": "uninstallSeed", "without": "uninstallWithout", "back": "uninstallBack"}
REPORTS = {"seed": "uninstall-seed-report.txt", "without": "uninstall-probe-report.txt", "back": "uninstall-probe-report.txt"}
WORLD = "belgian-snacks-uninstall"
CREATE_JARS = ROOT / "build" / "uninstall" / "mods"
NAMESPACE = "create_belgian_snacks"
MINUTES = 60

# Lines that mean the world or the player could not be loaded: the bug this run exists for.
FATAL = [
    "Couldn't place player",
    "Cannot encode empty ItemStack",
    "---- Minecraft Crash Report",
    "Failed to load level",
    "Failed to load player data",
]


def prepare(phase):
    game = DIRS[phase]
    game.mkdir(parents=True, exist_ok=True)
    shutil.rmtree(game / "saves" / WORLD, ignore_errors=True)
    shutil.rmtree(game / "crash-reports", ignore_errors=True)
    shutil.rmtree(game / "mods", ignore_errors=True)
    for stale in (REPORTS[phase], "logs/latest.log"):
        (game / stale).unlink(missing_ok=True)
    (game / "options.txt").write_text("\n".join([
        "onboardAccessibility:false", "pauseOnLostFocus:false", "tutorialStep:none", "joinedFirstServer:true",
        "skipMultiplayerWarning:true", "soundCategory_master:0.0", "lang:en_us", "guiScale:2", "renderDistance:6",
    ]) + "\n", encoding="utf-8")
    return game


def kill_game(run_name):
    # A client that hangs on a screen never quits by itself.
    if os.name != "nt":
        subprocess.run(["pkill", "-f", run_name])
        return
    script = ("Get-CimInstance Win32_Process -Filter \"Name='java.exe' or Name='javaw.exe'\" | "
              f"Where-Object {{ $_.CommandLine -like '*{run_name}*' }} | "
              "ForEach-Object { Stop-Process -Id $_.ProcessId -Force }")
    subprocess.run(["powershell", "-NoProfile", "-Command", script])


def result_line(report):
    if not report.is_file():
        return None
    lines = report.read_text(encoding="utf-8", errors="replace").splitlines()
    return next((line for line in reversed(lines) if line.startswith("RESULT")), None)


def run_phase(phase, minutes):
    game = DIRS[phase]
    report = game / REPORTS[phase]
    log_path = game / "gradle.log"
    print(f"== {phase}")
    with open(log_path, "w", encoding="utf-8") as log:
        process = subprocess.Popen([GRADLEW, TASKS[phase], "--console=plain"], cwd=ROOT, stdout=log, stderr=subprocess.STDOUT)
        deadline = time.time() + minutes * MINUTES
        while time.time() < deadline and process.poll() is None and result_line(report) is None:
            time.sleep(2)
        # The client stops itself once its report is written; give it time to save the world.
        settle = time.time() + 90
        while process.poll() is None and time.time() < settle:
            time.sleep(2)
        if process.poll() is None:
            kill_game(RUNS[phase])
            process.wait()
    return check(phase)


def check(phase):
    game = DIRS[phase]
    result = result_line(game / REPORTS[phase])
    log = game / "logs" / "latest.log"
    text = log.read_text(encoding="utf-8", errors="replace") if log.is_file() else ""
    problems = [marker for marker in FATAL if marker in text]
    crashes = list((game / "crash-reports").glob("*.txt")) if (game / "crash-reports").is_dir() else []
    problems += [f"crash report {crash.name}" for crash in crashes]
    # What the game says about our missing content: expected without the mod, listed for review.
    removal = [line for line in text.splitlines() if NAMESPACE in line and re.search(r"/(WARN|ERROR)\]", line)]
    if phase != "without":
        problems += [line for line in removal if "/ERROR]" in line]
    ok = result is not None and result.startswith("RESULT PASS") and not problems
    print(f"   report: {result or 'none'}")
    for problem in problems[:20]:
        print("   PROBLEM:", problem[:300])
    if phase == "without":
        print(f"   {len(removal)} warning or error lines about the missing mod (expected), e.g.:")
        for line in removal[:8]:
            print("     ", line[:220])
    print("   RESULT", "PASS" if ok else "FAIL", "-", log)
    return ok


def copy_world(source, target):
    shutil.copytree(DIRS[source] / "saves" / WORLD, DIRS[target] / "saves" / WORLD)


def main():
    print("== building: release jar, test sources, probe, Create jars")
    build = subprocess.run([GRADLEW, "jar", "gametestClasses", "uninstallProbeClasses", "prepareUninstallMods",
                            "--console=plain", "-q"], cwd=ROOT)
    if build.returncode != 0:
        return 1
    # build/libs may keep older releases: take the one of the current version.
    props = (ROOT / "gradle.properties").read_text(encoding="utf-8")
    version = re.search(r"^mod_version=(.+)$", props, re.M).group(1).strip()
    jars = [ROOT / "build" / "libs" / f"create-belgian-snacks-{version}.jar"]
    if not jars[0].is_file():
        print("no release jar", jars[0])
        return 1

    results = {}
    prepare("seed")
    results["seed"] = run_phase("seed", 8)
    if not (DIRS["seed"] / "saves" / WORLD / "level.dat").is_file():
        print("   no saved world: the next phases cannot run")
        results["seed"] = False

    if results["seed"]:
        game = prepare("without")
        shutil.copytree(CREATE_JARS, game / "mods")
        copy_world("seed", "without")
        results["without"] = run_phase("without", 6)

        game = prepare("back")
        shutil.copytree(CREATE_JARS, game / "mods")
        shutil.copy2(jars[0], game / "mods" / jars[0].name)
        copy_world("without", "back")
        results["back"] = run_phase("back", 6)

    summary = [f"{phase}: {'PASS' if ok else 'FAIL'}" for phase, ok in results.items()]
    passed = len(results) == 3 and all(results.values())
    summary.append("RESULT PASS" if passed else "RESULT FAIL")
    (RUN / "uninstall-smoke-report.txt").write_text("\n".join(summary) + "\n", encoding="utf-8")
    print("==", " | ".join(summary))
    return 0 if passed else 1


if __name__ == "__main__":
    sys.exit(main())
