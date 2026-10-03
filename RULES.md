# Project Rules & AI/IDE Instructions

## 1. Project Identity

| Field | Value |
|---|---|
| Project name | Create: Belgian Snacks |
| ID | create_belgian_snacks |
| Package | be.thefricadelle.belgiansnacks |
| Tech stack | Java 21, Minecraft 1.21.1, NeoForge, Gradle; Python asset tools |
| Author | THEFricadelle |
| License | All Rights Reserved; see LICENSE |
| Dependencies | Create, Registrate, Flywheel, Ponder; optional JEI, Jade, Farmer's Delight |

## 2. Git Workflow

Use the existing `dev` branch conventions; use `feat/`, `fix/` or `hotfix/` branches for isolated work. Do not invent staging or release branches. Conventional commits, no co-author trailers. Commit freely, but push only when the author asks, in grouped batches (decided on 2026-09-27; this replaces the global batch-push thresholds for this project). Keep version 1.0.0 until a version change is requested. Preserve unrelated local changes.

Issues live in the public `THEFricadelle/mc-mods-issues` repository, not in this one. A commit that fixes one carries `Fixes THEFricadelle/mc-mods-issues#<number>` in its body; the issue closes when the commit reaches `main`, that is at release. Until then, label the issue `fixed next release`. Use `Refs` instead of `Fixes` for a commit that only advances it.

## 3. Code Conventions

English code, comments and logs; French user communication. Java PascalCase classes and camelCase members, Python snake_case. Preserve author and license headers. README and changelog are bilingual, English first. Edit source providers rather than generated resources when changing datagen. No copied Minecraft/Create artwork. Textures use at most 16 opaque colours, binary alpha, 16x16 pixels (flow: 16x32). Preserve flow metadata and atlas registration.

## 4. Project Structure

```text
./
  .github/                 Existing CI workflows
  .git/, .gradle/           Local version/build state
  .vscode/                 Local IDE settings
  src/main/                Mod Java code, resources and metadata
  src/generated/resources/ Datagen output
  src/                     Additional test and development source sets
  docs/                    Design, specifications and texture prompts
  tools/                   Python asset and smoke-test utilities
  tools/arcadia/           Pack test support
  gradle/                  Gradle wrapper
  build/                   Ignored build outputs and local previews
  run/                     Ignored development worlds, logs and screenshots
  build.gradle             Dependencies, source sets, run and test tasks
  gradle.properties        Version, identity and dependency pins
  settings.gradle          Gradle project setup
  gradlew, gradlew.bat      Build entry points
  .gitignore               Local data exclusions
  .gitattributes           Git text conventions
  README.md                Bilingual installation and usage
  CHANGELOG.md             Bilingual change history
  RULES.md                 Project working rules
  ERROR_LOG.md             Failures, fixes and prevention
  LICENSE, NOTICE.md       License terms and summary
  CONTRIBUTING.md          Contribution policy
  CONTRIBUTORS.md          Existing credits
  CLAUDE.md                Ignored local configuration
```

## 5. Adding a New Feature (Step by Step)

1. Read these rules, ERROR_LOG.md and relevant docs; inspect Git state.
2. Plan affected files, risks and rollback; choose an appropriate existing or feature branch.
3. Make minimal changes, preserving authors and the current version.
4. Update bilingual documentation and run relevant validation.
5. Review the diff and prepare a conventional local commit.
6. Request explicit push confirmation before publishing a branch or PR.

## 6. Testing Checklist

- [ ] Run `python tools/asset_status.py` after texture edits.
- [ ] Run `gradlew.bat build` for resources or Java changes.
- [ ] Check texture previews and `gradlew.bat runClientSmoke` for rendering changes.
- [ ] Use GameTests for gameplay changes, compat tests for integrations.
- [ ] Keep generated resources current where providers change.
- [ ] Inspect actual reports; process exit alone is insufficient for mutation tests.
- [ ] No local QA artifacts, secrets or ignored configuration in commits.

## 7. Environment Setup

Clone the repository, install JDK 21 and import it as a Gradle project in the IDE. Run `gradlew.bat build` on Windows (`./gradlew build` on Unix). Use `runData` for resources, `runClient` for the development game, and `runClientSmoke` for scripted rendering checks. Python 3 is required by tools; core texture generation uses the standard library only. See README for dependency links.

## 8. AI Assistant Instructions

1. Author: THEFricadelle. No generated-by attribution in code or commits.
2. Communicate in French. Ask whether French localization is required after new English code/UI.
3. Plan before edits; delegate broad exploration. Never create workflow subdirectories without approval.
4. Log errors in ERROR_LOG.md. Do not call a failed external image service successful.
5. Prefer exact pixel artwork for tiny sprites when authorized. Never overwrite future manual art implicitly.
6. Existing placeholder tools must not overwrite final assets without an explicit flag.
7. Never call client-only tooltip/font APIs in server tests. Preserve fluid atlas declarations.
8. Check external account identities exactly; THE_Fricadelle is the author's game account.
9. Report unavailable tests honestly. Do not push or bump versions automatically.
