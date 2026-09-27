#!/usr/bin/env python3
# Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
# SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
#
# Proprietary, source-available software. Public visibility of this source
# grants no right to copy, reuse, redistribute, or create derivative works.
# See LICENSE and CONTRIBUTING.md at the repository root.
"""M6.5: the mod in the real Arcadia pack, standard library only.

1. Two dev clients with the whole pack: A creates a world and runs ArcadiaSmokeRun (recipes, KubeJS,
   conflicts, Create Heat JS, grinder, JEI), opens it to LAN; B joins and both feed one grinder.
2. The release jar, as a player installs it: every jar of the pack plus build/libs, no class of this
   project, loading A's world, whose datapack runs `belgiansnacks foods export` at load.
The CurseForge instance is only read; a KubeJS test script goes into the copies under run/ only.
Expect 30 to 40 minutes, two game windows and about 16 GB of memory.
Usage: python tools/arcadia_smoke.py [path to the instance]
"""

import os
import pathlib
import re
import shutil
import subprocess
import sys
import time

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import arcadia_export as pack  # noqa: E402

ROOT = pack.ROOT
GAME_A = ROOT / "run" / "arcadia"
GAME_B = ROOT / "run" / "arcadia-b"
GAME_JAR = ROOT / "run" / "arcadia-jar"
TOOLS = ROOT / "tools" / "arcadia"
WORLD = "belgian-snacks-arcadia"
REPORT = "arcadia-smoke-report.txt"
LAN_MARKER = "arcadia-lan-open.txt"
M5_CSV = ROOT / "docs" / "data" / f"arcadia-{pack.PACK_VERSION}-foods.csv"
MINUTES = 60


def gradle(task, log_path):
    log = open(log_path, "w", encoding="utf-8")
    return subprocess.Popen([pack.GRADLEW, task, "--console=plain"], cwd=ROOT, stdout=log, stderr=subprocess.STDOUT), log


def kill_game(run_name):
    # A crashed client waits on Crash Assistant's window, and a done jar run never quits by itself.
    if os.name != "nt":
        subprocess.run(["pkill", "-f", run_name])
        return
    script = ("Get-CimInstance Win32_Process -Filter \"Name='java.exe' or Name='javaw.exe'\" | "
              f"Where-Object {{ $_.CommandLine -like '*{run_name}*' }} | "
              "ForEach-Object { Stop-Process -Id $_.ProcessId -Force }")
    subprocess.run(["powershell", "-NoProfile", "-Command", script])


def wait_for(condition, process, minutes):
    deadline = time.time() + minutes * MINUTES
    while time.time() < deadline:
        if condition():
            return True
        if process is not None and process.poll() is not None:
            return condition()
        time.sleep(5)
    return False


def read_report(game):
    path = game / REPORT
    return path.read_text(encoding="utf-8").splitlines() if path.is_file() else []


def fingerprint(lines):
    for line in lines:
        match = re.match(r"PASS foods\.index - (\d+) foods.*fingerprint (-?\d+)", line)
        if match:
            return match.group(1), match.group(2)
    return None


def prepare_all(instance):
    print("== preparing the pack copies")
    pack.prepare(instance, GAME_A)
    shutil.copy2(TOOLS / "zz_belgian_snacks_m65_test.js", GAME_A / "kubejs" / "server_scripts")
    # B: the same pack in its own folder, so no two games share a config file lock.
    pack.prepare(instance, GAME_B)
    shutil.copy2(TOOLS / "zz_belgian_snacks_m65_test.js", GAME_B / "kubejs" / "server_scripts")
    for game in (GAME_A, GAME_B):
        for stale in (REPORT, LAN_MARKER):
            (game / stale).unlink(missing_ok=True)


def lan_phase():
    print("== client A: world, checks, LAN (the pack takes minutes to load)")
    a, log_a = gradle("runArcadiaSmokeA", GAME_A / "gradle-smoke.log")
    if not wait_for(lambda: (GAME_A / LAN_MARKER).is_file(), a, 40):
        print("   A never opened the LAN world, see", GAME_A / "logs" / "latest.log")
        kill_game("arcadiaSmokeA")
        a.wait()
        log_a.close()
        return False
    print("== client B: joining")
    b, log_b = gradle("runArcadiaSmokeB", GAME_B / "gradle-smoke.log")
    for name, process, game in (("A", a, GAME_A), ("B", b, GAME_B)):
        if not wait_for(lambda g=game: (g / REPORT).is_file(), process, 30):
            print(f"   no report from {name}, stopping it")
        # Let it quit by itself: A saves the world the jar run loads next.
        if not wait_for(lambda p=process: p.poll() is not None, None, 3):
            kill_game("arcadiaSmoke" + name)
        process.wait()
    log_a.close()
    log_b.close()
    ok = True
    for name, game in (("A", GAME_A), ("B", GAME_B)):
        lines = read_report(game)
        print(f"== client {name}")
        for line in lines or ["no report: " + str(game / REPORT)]:
            print("   " + line)
        ok &= bool(lines) and lines[-1].startswith("RESULT PASS")
    host, guest = fingerprint(read_report(GAME_A)), fingerprint(read_report(GAME_B))
    same = host is not None and host == guest
    print(("== food index identical on both clients: " if same else "== FOOD INDEX DIFFERS: ") + f"A {host}, B {guest}")
    return ok and same


def compare_with_m5():
    csv = GAME_A / "arcadia-foods.csv"
    if not csv.is_file() or not M5_CSV.is_file():
        print("== export comparison skipped: missing", csv if not csv.is_file() else M5_CSV)
        return False
    ids = lambda path: {line.split(",")[0] for line in path.read_text(encoding="utf-8").splitlines()[1:]}
    now, before = ids(csv), ids(M5_CSV)
    gone, new = sorted(before - now), sorted(now - before)
    # The test script blacklists the apple; anything else means the pack or the mod changed.
    ok = gone == ["minecraft:apple"] and not new
    print(f"== export vs M5: {len(now)} foods now, {len(before)} at M5, gone {gone}, new {new}" + ("" if ok else "  UNEXPECTED"))
    return ok


def jar_phase(instance):
    print("== release jar: the pack's own jars plus build/libs")
    jars = [j for j in (ROOT / "build" / "libs").glob("*.jar") if not j.name.endswith(("-sources.jar", "-javadoc.jar"))]
    if len(jars) != 1:
        print("   expected one release jar in build/libs, found", [j.name for j in jars])
        return False
    pack.prepare(instance, GAME_JAR, left_out=set())
    shutil.copy2(jars[0], GAME_JAR / "mods" / jars[0].name)
    shutil.copy2(TOOLS / "zz_belgian_snacks_m65_test.js", GAME_JAR / "kubejs" / "server_scripts")
    world = GAME_JAR / "saves" / WORLD
    shutil.copytree(GAME_A / "saves" / WORLD, world)
    shutil.copytree(TOOLS / "m65_export", world / "datapacks" / "m65_export")
    export = GAME_JAR / "config" / "create_belgian_snacks" / "foods_export.csv"
    export.unlink(missing_ok=True)
    log = GAME_JAR / "logs" / "latest.log"
    process, gradle_log = gradle("runArcadiaJar", GAME_JAR / "gradle-jar.log")
    indexed = lambda: log.is_file() and "Food index:" in log.read_text(encoding="utf-8", errors="replace")
    done = wait_for(lambda: export.is_file() and indexed(), process, 30)
    time.sleep(10)
    kill_game("arcadiaJar")
    process.wait()
    gradle_log.close()
    if not done:
        print("   no export from the jar run, see", log)
        return False
    text = log.read_text(encoding="utf-8", errors="replace")
    counts = re.findall(r"Food index: (\d+) foods", text)
    rows = len(export.read_text(encoding="utf-8").splitlines()) - 1
    # No class of this project is on that run's classpath: a food index line can only come from the jar.
    loaded = bool(counts)
    problems = [line for line in text.splitlines()
                if "create_belgian_snacks" in line and re.search(r"/ERROR\]|Couldn't parse|Parsing error|Failed to load", line)]
    print(f"   jar {jars[0].name} loaded: {loaded}; food index {counts[-1] if counts else '?'}; export rows {rows}")
    for line in problems[:20]:
        print("   ERROR LINE:", line[:300])
    ok = loaded and bool(counts) and int(counts[-1]) == rows and not problems
    print("   RESULT", "PASS" if ok else "FAIL")
    return ok


def main():
    instance = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pack.DEFAULT_INSTANCE
    if not (instance / "mods").is_dir():
        print("no mods folder in", instance)
        return 1
    print("== building the release jar")
    if subprocess.run([pack.GRADLEW, "jar", "--console=plain", "-q"], cwd=ROOT).returncode != 0:
        return 1
    prepare_all(instance)
    results = {"lan": lan_phase()}
    results["export"] = compare_with_m5()
    results["jar"] = jar_phase(instance)
    print("== SUMMARY", " ".join(f"{k}={'PASS' if v else 'FAIL'}" for k, v in results.items()))
    return 0 if all(results.values()) else 1


if __name__ == "__main__":
    sys.exit(main())
