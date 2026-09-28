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

## [2026-09-27 15:30] - THEFricadelle NPC wore another player's skin
**Context:** M8 NPC, skin looked up online by the account name.
**Error:** The NPC showed a stranger's skin; the client test reported "the THEFricadelle account's skin" and passed.
**Root cause:** The lookup used the display name THEFricadelle, which is a different, existing Minecraft account. The author's account is THE_Fricadelle. The test only checked that some skin loaded.
**Fix:** `TheFricadelleNpc.SKIN_ACCOUNT = "THE_Fricadelle"` for the lookup (the display name stays THEFricadelle); the client test waits for the skin and requires the account Mojang answers with to be THE_Fricadelle.
**Prevention:** When a feature relies on an external identity, assert on the identity itself, not on "something loaded".

## [2026-09-27] — Texture generation blocked
**Context:** Generating raw_fricadelle.png from docs/12-prompts-textures.md with the built-in image tool.
**Error:** HTTP 400 moderation_blocked at output stage, category sexual; request ID fd2a2c4a-3d07-45d1-9a75-4c679635d459.
**Root cause:** The service rejected the generated output for a food pixel-art request; the underlying classification reason is unavailable.
**Fix:** No texture was produced or replaced. Reported the block; the CLI/API fallback requires explicit user selection.
**Prevention:** Do not report generation as successful without a returned image.

## [2026-09-27] — Incorrect texture import script path
**Context:** Inspecting the texture import workflow.
**Error:** Get-Content could not find tools/import_texture.py.
**Root cause:** The script name was assumed instead of using the documented tools/pixelate.py path.
**Fix:** Identified tools/pixelate.py in docs/12-prompts-textures.md.
**Prevention:** Use script paths from the project documentation.

## [2026-09-27] — Culinary texture retry blocked
**Context:** Retrying raw_fricadelle.png after explicitly describing the Belgian food and cooking-game context.
**Error:** HTTP 400 moderation_blocked at output stage, category sexual; request ID 809ad769-a1e0-4fee-96f0-9c85e50b7662.
**Root cause:** The image service again classified its output as sexual despite the culinary request; the underlying reason is unavailable.
**Fix:** Stopped further retries and reported that no image was returned.
**Prevention:** Distinguish a service output rejection from the nature of the user's food request.

## [2026-09-27 15:58] — Redirected client command reported a conflicting exit status
**Context:** Running runClientSmoke through PowerShell with combined output redirected to build/texture-smoke.log.
**Error:** The command runner returned exit code 1 although Gradle printed BUILD SUCCESSFUL and the smoke report ended with RESULT PASS 20 checks.
**Root cause:** The shell/runner exit status disagreed with the task report; the precise shell cause was not established.
**Fix:** Inspected the report and screenshots, then ran verifyClientSmoke directly to verify the current report independently.
**Prevention:** Check both the named task report and the process result; re-run the verifier directly when redirected execution disagrees.

## [2026-09-27 16:28] - Running visitor frozen in the GameTest world
**Context:** THEFricadelle now spawns up to 8 blocks ahead of the eater and runs to them.
**Error:** GameTest timed out with the visitor still in phase RUN: it never ticked.
**Root cause:** The spawn spot lay in a chunk that was loaded but not entity-ticking (outside the test's forced chunks); the same can happen at the edge of a real player's simulation distance.
**Fix:** The spawn spot must pass `ServerLevel.isPositionEntityTicking`; the test forces the chunks around the eater and waits for them to load.
**Prevention:** Any entity placed away from its trigger must be placed where entities tick.

## [2026-09-27 16:35] - Multiplayer smoke took the fryer output early
**Context:** Client A eats THE_FRICADELLE by holding right click while facing the fryer.
**Error:** `client A holds 16 create_belgian_snacks:fricadelle` before the take step.
**Root cause:** The held click repeats once after the last bite, with the hand just emptied, and takes the batch. The visitor used to spawn between the player and the fryer and absorbed that click.
**Fix:** Client A looks at the sky while eating and turns back to the fryer before the take step.
**Prevention:** A held key in a scripted client must never point at an interactive block when its purpose ends.

## [2026-09-27 16:50] - No items on the belts of our Ponder scenes
**Context:** Ponder scenes put items on belts with `createItemOnBelt`.
**Error:** The items were never drawn; every belt segment of the scene reported controller 0, 0, 0.
**Root cause:** A belt links to its controller on its first server tick (`BeltBlock.initBelt`). `PonderSchematicsRun` saved the schematic in the tick the belts were placed, so no segment had a controller; the Ponder world is client side and never initialises them. Only the controller carries and renders belt items.
**Fix:** Call `BeltBlock.initBelt` after `createBelts`, and rewrite each segment's `Controller` relative to the schematic origin before saving. The client smoke checks an item rides a belt in three scenes.
**Prevention:** Blocks that link up on their first tick must be initialised before a structure is exported; world positions inside block entity NBT must be made relative.

## [2026-09-27 17:20] - Assembly Ponder: paste ran past the line and appeared twice
**Context:** The tier 2 and 3 line scenes put a moving paste on the belt, then a stalled copy under each station.
**Error:** The moving paste kept going down the line while the copies appeared: two items, one too far.
**Root cause:** The intro item was never stalled nor removed in time. Riding a single item instead then stopped one segment short: inserted from the west it starts at 0.1, not at the segment centre, and the first deployer held it at its centre, shifting every stop.
**Fix:** One item, inserted from above (centre of the first segment), moved one block per 15 ticks at 32 RPM and stalled under each station. The press is set back to idle after its stroke, since only a server ends a pressing cycle. The client smoke samples the belt every tick: one item at most, stops at every segment centre.
**Prevention:** Belt items in Ponder scenes start at a segment centre and move by whole blocks; check real machine behaviours that may hold them.

## [2026-09-28 02:30] - CurseForge update-file fails on dependencies
**Context:** Setting the dependencies (Create required, JEI, Jade, KubeJS, Farmer's Delight optional) of the published 1.0.0 file: the author console's Related Projects search only offered a short list without them, so `POST /api/projects/1715566/update-file` of the Upload API was used instead.
**Error:** `HTTP 500 {"errorCode":500,"errorMessage":"An unhandled exception occurred while processing the request."}` whenever the metadata held `relations` (any single slug, including `create`) or `gameVersions`. `changelog`, `changelogType`, `displayName` and `releaseType` alone returned 200.
**Root cause:** Server side on CurseForge: the endpoint validates the fields (missing `fileID`, `slug` or an environment version give a 400) then crashes applying relations or game versions to an existing file.
**Fix:** None on an existing file. `tools/curseforge.py upload` sets the dependencies and game versions at upload through `upload-file`.
**Prevention:** Always upload a CurseForge file through `tools/curseforge.py upload` so its dependencies are declared at creation; never plan on adding them afterwards.
