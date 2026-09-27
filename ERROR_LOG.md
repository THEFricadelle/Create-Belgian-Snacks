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

## [2026-09-27 01:25] - Two-client smoke: client B stuck on the players check
**Context:** `testAll` during M5.
**Error:** Client B never passed `mp.twoPlayers`, waited 5 minutes, then crashed with an NPE on `Minecraft.getConnection()` when the server stopped. No report for B.
**Root cause:** Race. Client A finished its steps and disconnected about 2 s after B joined, before B reached its players check. The fixed 100-tick delay before A's take assumed both clients joined at the same time. The step suppliers also assumed a live connection.
**Fix:** Network handshake: B crouches once it has seen the batch, and A takes the output only after it sees that crouch through the server. The runner now fails with `runner.disconnected` and writes its report when the connection drops.
**Prevention:** Never coordinate two clients with a timer. Use a signal that travels through the server.

## [2026-09-27 02:07] - Goggle tooltip of a kinetic block entity crashes a dedicated server
**Context:** M6, a GameTest called `SupremeGrinderBlockEntity.addToGoggleTooltip` to check the lines.
**Error:** `RuntimeException: Attempted to load class net/minecraft/client/Minecraft for invalid dist DEDICATED_SERVER`, from `LangBuilder.forGoggles` inside `KineticBlockEntity.addToGoggleTooltip`.
**Root cause:** Create's goggle lines measure text with the client font. The method sits on a common class but is client-only in practice.
**Fix:** The GameTest checks the synced figures (count, goal, total, examples); the goggle lines are checked by `ClientSmokeTest`.
**Prevention:** Never call `addToGoggleTooltip` (or anything using Create's `LangBuilder.forGoggles`) from server code or a server GameTest.

## [2026-09-27 02:12] - Mutation checks reported every mutation as caught, without running a test
**Context:** M6, a Python script injected bugs into the grinder and ran the GameTests through `subprocess.run("./gradlew ...", shell=True)`.
**Error:** Every run exited 1 and no test name was found in the output.
**Root cause:** On Windows `shell=True` uses `cmd`, where `./gradlew` does not exist: the "failure" was the shell, not a test. Re-running with `gradlew.bat` also failed to report, so the check moved to a bash loop.
**Fix:** A bash loop (`perl -0pi` per mutation, `./gradlew runGameTestServer`, grep of `<test> failed at`), which also reports mutations that did not apply and compile errors separately. 8 of 8 mutations caught after one missing test was added.
**Prevention:** A mutation run only counts when it names the test that failed; an exit code alone proves nothing.

## [2026-09-27 03:45] - Fryer stalls when two frying recipes take the same item
**Context:** M6.5 in Arcadia. A KubeJS script added a heated potato recipe; a superheated potato recipe also existed.
**Error:** Fryer on a kindled Blaze Burner with oil and potatoes: status `NO_HEAT` forever, nothing fried.
**Root cause:** `findRecipe` took the first recipe matching the item (`getRecipeFor`), whatever the heat below. The first was the superheated one, which could never run on that burner.
**Fix:** `findRecipe` takes, among every recipe for the item, the first the heat below allows, as Create's basin does; the recipe cache went too (a recipe added by `/reload` or KubeJS could stay hidden behind a cached "none"). GameTest `ofTwoRecipesTheOneTheHeatAllowsRuns`, with the runnable recipe sorting second, checked by mutation.
**Prevention:** A machine never assumes one recipe per input: packs add variants.

## [2026-09-27 03:41] - Create Heat JS rewrites Create's heat rules
**Context:** M6.5 in Arcadia, which ships Create Heat JS 0.0.6.
**Error:** `HeatCondition.testBlazeBurner` answers false for "no heat needed" on any lit burner, and for "heated" on a fading burner; Create answers true.
**Root cause:** Heat JS replaces `testBlazeBurner` by its own relation table, built for basins (which it handles apart, with a recipe context).
**Fix:** `FryerBlockEntity.heatAllows` applies Create's rules to Create's 3 conditions and 5 levels, and defers to `testBlazeBurner` only for what Heat JS adds. `heatKey` no longer switches on `HeatLevel` (Heat JS adds constants: an exhaustive switch would throw).
**Prevention:** Never switch exhaustively on a Create enum, and check Create's rules in the real pack (`heat.fryerRules` in ArcadiaSmokeRun).

## [2026-09-27 03:30] - LAN guest could not reach the machines in Arcadia
**Context:** M6.5, client B joining client A's world over LAN.
**Error:** B never found the fryer, and held a wooden sword instead of the food it was given.
**Root cause:** A LAN join lands anywhere within the spawn radius; the pack hands out a starter kit on join, which takes the hand.
**Fix:** The host teleports the guest beside it on login and puts the food in the last hotbar slot; each client selects the slot holding its food.
**Prevention:** In a real pack, never assume the spawn point or the held item of a joining player.
