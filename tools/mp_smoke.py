#!/usr/bin/env python3
# Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
# SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
#
# Proprietary, source-available software. Public visibility of this source
# grants no right to copy, reuse, redistribute, or create derivative works.
# See LICENSE and CONTRIBUTING.md at the repository root.
"""Two real clients on a real dedicated server, standard library only.

Starts the serverSmoke run, waits for it, then starts clientSmokeA and clientSmokeB, which join it
with quick play. The mod's own harnesses (gametest/multiplayer) do the checking and write reports;
this script only launches, waits and reads them. Opens two game windows.
Usage: python tools/mp_smoke.py   (exit code 0 only if every report says RESULT PASS)
"""

import os
import pathlib
import shutil
import subprocess
import sys
import time

ROOT = pathlib.Path(__file__).resolve().parent.parent
GRADLEW = str(ROOT / ("gradlew.bat" if os.name == "nt" else "gradlew"))
RUN = ROOT / "run" / "mpsmoke"
SERVER_DIR = RUN / "server"
CLIENT_DIRS = {"A": RUN / "client-a", "B": RUN / "client-b"}
PORT = 25599
TIMEOUT_SECONDS = 600

FLAT = ('{"biome"\\:"minecraft\\:plains","layers"\\:[{"block"\\:"minecraft\\:bedrock","height"\\:1},'
        '{"block"\\:"minecraft\\:dirt","height"\\:2},{"block"\\:"minecraft\\:grass_block","height"\\:1}]}')


def prepare():
    SERVER_DIR.mkdir(parents=True, exist_ok=True)
    shutil.rmtree(SERVER_DIR / "world", ignore_errors=True)
    for stale in ("smoke-mp-server.txt", "logs/latest.log"):
        (SERVER_DIR / stale).unlink(missing_ok=True)
    (SERVER_DIR / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    (SERVER_DIR / "server.properties").write_text("\n".join([
        "online-mode=false", f"server-port={PORT}", "level-type=minecraft\\:flat", f"generator-settings={FLAT}",
        "difficulty=peaceful", "spawn-protection=0", "spawn-monsters=false", "allow-flight=true", "gamemode=survival",
        "max-players=4", "view-distance=6", "simulation-distance=6",
    ]) + "\n", encoding="utf-8")
    for client in CLIENT_DIRS.values():
        client.mkdir(parents=True, exist_ok=True)
        shutil.rmtree(client / "screenshots", ignore_errors=True)
        for stale in ("smoke-mp-report.txt", "logs/latest.log"):
            (client / stale).unlink(missing_ok=True)
        # No accessibility onboarding, no pause on focus loss (two windows fight for focus), no sound.
        (client / "options.txt").write_text("\n".join([
            "onboardAccessibility:false", "pauseOnLostFocus:false", "tutorialStep:none", "joinedFirstServer:true",
            "skipMultiplayerWarning:true", "soundCategory_master:0.0", "lang:en_us", "guiScale:2", "renderDistance:6",
        ]) + "\n", encoding="utf-8")


def gradle(*tasks, log):
    return subprocess.Popen([GRADLEW, *tasks, "--console=plain"], cwd=ROOT, stdout=log, stderr=subprocess.STDOUT)


def kill(process):
    if process.poll() is None:
        if os.name == "nt":
            subprocess.run(["taskkill", "/PID", str(process.pid), "/T", "/F"], capture_output=True)
        else:
            process.kill()


def wait_for(predicate, seconds):
    deadline = time.time() + seconds
    while time.time() < deadline:
        if predicate():
            return True
        time.sleep(1)
    return False


def main():
    # Compile once up front: the three builds below then only find up-to-date classes.
    if subprocess.run([GRADLEW, "classes", "gametestClasses", "--console=plain", "-q"], cwd=ROOT).returncode != 0:
        print("compilation failed")
        return 1
    prepare()
    logs = {name: open(RUN / f"gradle-{name}.log", "w", encoding="utf-8") for name in ("server", "A", "B")}
    server = gradle("runServerSmoke", log=logs["server"])
    processes = [server]
    try:
        server_log = SERVER_DIR / "logs" / "latest.log"
        if not wait_for(lambda: server_log.is_file() and "Done (" in server_log.read_text(encoding="utf-8", errors="replace"), 300):
            print("the server did not start, see", server_log)
            return 1
        for role in ("A", "B"):
            processes.append(gradle(f"runClientSmoke{role}", log=logs[role]))
            time.sleep(5)
        if not wait_for(lambda: all(p.poll() is not None for p in processes), TIMEOUT_SECONDS):
            print("timeout: stopping every process")
    finally:
        for process in processes:
            kill(process)
        for log in logs.values():
            log.close()

    ok = True
    reports = {"server": SERVER_DIR / "smoke-mp-server.txt"}
    reports.update({f"client {role}": path / "smoke-mp-report.txt" for role, path in CLIENT_DIRS.items()})
    for name, path in reports.items():
        print(f"== {name}")
        if not path.is_file():
            print("   no report:", path)
            ok = False
            continue
        lines = path.read_text(encoding="utf-8").splitlines()
        for line in lines:
            print("  ", line)
        ok &= bool(lines) and lines[-1].startswith("RESULT PASS")
    # Every side must hold the very same food index: compare what each report says.
    fingerprints = {}
    for name, path in reports.items():
        if path.is_file():
            for line in path.read_text(encoding="utf-8").splitlines():
                if "foodIndex - " in line:
                    fingerprints[name] = line.split("foodIndex - ", 1)[1]
    if len(set(fingerprints.values())) != 1 or len(fingerprints) != len(reports):
        print("== food index differs between sides:", fingerprints)
        ok = False
    else:
        print("== food index identical on every side:", next(iter(fingerprints.values())))
    print("Screenshots:", ", ".join(str(p / "screenshots") for p in CLIENT_DIRS.values()))
    print("MULTIPLAYER SMOKE", "PASS" if ok else "FAIL")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
