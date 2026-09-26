# Error Log

## [2026-09-26 19:12] - JEI 19.57 runtime dependency blocked by repository filter
**Context:** First `runData` after scoping every third-party repository with `exclusiveContent`.
**Error:** `Could not resolve all files for configuration ':runtimeClasspath'`, `net.mezzdev.config:mezz_config-1.21.1-config-api:0.5.12 FAILED`.
**Root cause:** JEI 19.5x depends on MezzConfig, published under the `net.mezzdev.config` group on maven.blamejared.com. The BlameJared filter only allowed `mezz.jei`.
**Fix:** Added `includeGroup 'net.mezzdev.config'` to the BlameJared `exclusiveContent` filter.
**Prevention:** When scoping a repository, list the groups of the transitive dependencies too (`./gradlew dependencies --configuration runtimeClasspath` shows the FAILED ones).

## [2026-09-26 19:13] - Registrate lang entry added too late for datagen
**Context:** `BSDatagen.gatherData` called `REGISTRATE.addRawLang(...)` inside the `GatherDataEvent` listener.
**Error:** `IllegalStateException: Cannot add data generator after construction of root generator`.
**Root cause:** `addRawLang` lazily registers a Registrate lang provider. Registrate builds its root provider in its own `GatherDataEvent` listener, which runs before ours.
**Fix:** Call `addRawLang` during mod construction (`BSCreativeTabs.register`). It is a no-op outside datagen.
**Prevention:** Any `REGISTRATE.addRawLang` / `addDataGenerator` call belongs to mod construction, never to a `GatherDataEvent` listener.
