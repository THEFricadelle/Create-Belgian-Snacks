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

## [2026-09-26 21:13] - Farmer's Delight minced beef rejected by the fricadelle paste
**Context:** First GameTest run with Farmer's Delight loaded (`runGameTestServerCompat`).
**Error:** `farmersdelight:minced_beef not in minced_meats/beef`.
**Root cause:** The M2 tag rewrite relied on `#c:minced_beef` to reach Farmer's Delight, but FD 1.3.4 does not tag its own minced beef there; Create: Food does, which hid the gap in Arcadia.
**Fix:** `minced_meats/beef` lists `farmersdelight:minced_beef` directly (optional), besides the `c:` tags.
**Prevention:** Never assume a mod fills a convention tag with its own item: check the mod's jar. Compat is tested with the mod actually loaded, not inferred from tag files.

## [2026-09-26 21:15] - Fluid textures missing from the block atlas
**Context:** First client smoke run, `fluids.sprites` check.
**Error:** All eight `create_belgian_snacks:fluid/*_still|_flow` sprites resolved to the missing texture.
**Root cause:** Since 1.19.3 the block atlas only scans `textures/block` and `textures/item`. Textures under `textures/fluid` must be declared in `assets/minecraft/atlases/blocks.json`. Server-side GameTests and resource JUnit checks cannot see the atlas.
**Fix:** `BSSpriteSourceProvider` (datagen) declares each fluid texture as a single-file source, as Create does for its own fluids. JUnit now checks the generated atlas lists every fluid texture.
**Prevention:** Any texture outside `block/` or `item/` needs an atlas source. Rendering-side facts are covered by the client smoke run, not by the server tests.
