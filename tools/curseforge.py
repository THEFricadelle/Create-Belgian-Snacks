#!/usr/bin/env python3
# Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
# SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
#
# Proprietary, source-available software. Public visibility of this source
# grants no right to copy, reuse, redistribute, or create derivative works.
# See LICENSE and CONTRIBUTING.md at the repository root.
"""CurseForge uploads through the official Upload API, standard library only.

The API token is read from the CF_API_TOKEN environment variable, else from curseforge.token at
the repository root (git-ignored by *.token). It is never printed or written anywhere else.

  python tools/curseforge.py upload             upload build/libs/<jar> with its dependencies
  python tools/curseforge.py upload --dry-run   print the request without sending it

Metadata: version from gradle.properties, release notes from the English block of
docs/publication/release-<version>.md, game versions Minecraft 1.21.1 + NeoForge + Java 21, client and
server, and the dependencies below. Dependencies can only be set here, at upload: on 2026-09-28 the
update-file endpoint answered HTTP 500 to any relations or gameVersions change on an existing file.
"""

import argparse
import json
import os
import pathlib
import sys
import urllib.error
import urllib.request
import uuid

ROOT = pathlib.Path(__file__).resolve().parent.parent
TOKEN_FILE = ROOT / "curseforge.token"
API = "https://minecraft.curseforge.com/api"
# Public id of the project page (curseforge.com/minecraft/mc-mods/create-belgian-snacks).
PROJECT_ID = 1715566

RELATIONS = [
    {"slug": "create", "type": "requiredDependency"},
    {"slug": "jei", "type": "optionalDependency"},
    {"slug": "jade", "type": "optionalDependency"},
    {"slug": "kubejs", "type": "optionalDependency"},
    {"slug": "farmers-delight", "type": "optionalDependency"},
]
# (version type slug prefix, version name) for every file.
GAME_VERSIONS = [("minecraft-1-21", "1.21.1"), ("modloader", "NeoForge"), ("java", "Java 21"),
                 ("environment", "Client"), ("environment", "Server")]


def token():
    value = os.environ.get("CF_API_TOKEN", "").strip()
    if not value and TOKEN_FILE.is_file():
        value = TOKEN_FILE.read_text(encoding="utf-8").strip()
    if not value:
        sys.exit(f"No API token: paste it into {TOKEN_FILE.name} at the repository root "
                 "(authors.curseforge.com > API Tokens), or set CF_API_TOKEN.")
    return value


def properties():
    values = {}
    for line in (ROOT / "gradle.properties").read_text(encoding="utf-8").splitlines():
        if "=" in line and not line.lstrip().startswith("#"):
            key, value = line.split("=", 1)
            values[key.strip()] = value.strip()
    return values


def release_notes(version):
    path = ROOT / f"docs/publication/release-{version}.md"
    if not path.is_file():
        sys.exit(f"No release notes: {path.relative_to(ROOT)}")
    text = path.read_text(encoding="utf-8")
    start = text.index("## English")
    end = text.find("\n## ", start + 1)
    return text[start + len("## English"):end if end != -1 else None].strip()


def request(method, path, api_token, body=None, content_type=None):
    req = urllib.request.Request(f"{API}{path}", data=body, method=method)
    req.add_header("X-Api-Token", api_token)
    req.add_header("User-Agent", "create-belgian-snacks-publisher")
    if content_type:
        req.add_header("Content-Type", content_type)
    try:
        with urllib.request.urlopen(req, timeout=120) as response:
            return response.status, response.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as error:
        return error.code, error.read().decode("utf-8", "replace")


def multipart(fields, file_field=None, file_path=None):
    boundary = uuid.uuid4().hex
    parts = []
    for name, value in fields.items():
        parts.append(f'--{boundary}\r\nContent-Disposition: form-data; name="{name}"\r\n\r\n{value}\r\n'.encode())
    if file_path:
        parts.append((f'--{boundary}\r\nContent-Disposition: form-data; name="{file_field}"; '
                      f'filename="{file_path.name}"\r\nContent-Type: application/java-archive\r\n\r\n').encode())
        parts.append(file_path.read_bytes() + b"\r\n")
    parts.append(f"--{boundary}--\r\n".encode())
    return b"".join(parts), f"multipart/form-data; boundary={boundary}"


def game_version_ids(api_token):
    status, body = request("GET", "/game/version-types", api_token)
    if status != 200:
        sys.exit(f"Version types: HTTP {status} {body[:300]}")
    types = json.loads(body)
    status, body = request("GET", "/game/versions", api_token)
    if status != 200:
        sys.exit(f"Game versions: HTTP {status} {body[:300]}")
    versions = json.loads(body)
    ids = []
    for type_prefix, name in GAME_VERSIONS:
        type_ids = {t["id"] for t in types if t["slug"].startswith(type_prefix)}
        match = [v["id"] for v in versions if v["gameVersionTypeID"] in type_ids and v["name"] == name]
        if len(match) != 1:
            sys.exit(f"Game version {name!r} ({type_prefix}): {len(match)} matches, expected 1")
        ids.append(match[0])
    return ids


def file_metadata(api_token, release_type):
    props = properties()
    version = props["mod_version"]
    return props, version, {
        "changelog": release_notes(version),
        "changelogType": "markdown",
        "displayName": f"{props['mod_name']} {version}",
        "gameVersions": game_version_ids(api_token),
        "releaseType": release_type,
        "relations": {"projects": RELATIONS},
    }


def send(label, path, metadata, api_token, dry_run, file_path=None):
    shown = json.dumps(metadata, indent=2, ensure_ascii=False)
    print(f"{label}: POST {API}{path}" + (f" with {file_path.relative_to(ROOT)}" if file_path else ""))
    print(shown)
    if dry_run:
        print("dry run: nothing sent")
        return
    body, content_type = multipart({"metadata": json.dumps(metadata)}, "file", file_path)
    status, reply = request("POST", path, api_token, body, content_type)
    if 200 <= status < 300:
        print(f"OK (HTTP {status}) {reply[:300]}")
    else:
        sys.exit(f"Failed: HTTP {status} {reply[:500]}")


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    commands = parser.add_subparsers(dest="command", required=True)
    upload = commands.add_parser("upload", help="upload the built jar with its dependencies")
    upload.add_argument("--release-type", choices=("release", "beta", "alpha"), default="release")
    upload.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    api_token = token()
    props, version, metadata = file_metadata(api_token, args.release_type)
    jar = ROOT / f"build/libs/{props['mod_slug']}-{version}.jar"
    if not jar.is_file():
        sys.exit(f"No {jar.relative_to(ROOT)}: run ./gradlew build first")
    send("Upload", f"/projects/{PROJECT_ID}/upload-file", metadata, api_token, args.dry_run, jar)


if __name__ == "__main__":
    main()
