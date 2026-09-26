# Changelog

All notable changes to Create: Belgian Snacks are documented here.

---

## [Unreleased]

### Added

- **Project skeleton** — ModDevGradle 2.0.147 project on Gradle 9.2.1, NeoForge 21.1.250, compiled against Create 6.0.10 (build 280, the one shipped in Arcadia V2 2.0.32) with Registrate, Ponder and Flywheel. JEI and Jade are loaded in the development runtime only.
- **Creative tab** — `create_belgian_snacks:main`, placed after Create's tabs and used as Registrate's default tab. Its icon is the fricadelle.
- **Data generation** — `runData` writes `src/generated/resources`; `en_us` comes from Registrate, `fr_fr` from a dedicated language provider.
- **Licensing** — All Rights Reserved license with NOTICE, CONTRIBUTING and CONTRIBUTORS, SPDX headers on sources, license files shipped inside the jar.
- **Continuous integration** — GitHub Actions workflow running `./gradlew build` on JDK 21.
- **Items** — 16 base items for the three tiers: minced pork, beef and chicken, beef tallow, bread crumbs, Belgian spices, the three pastes, raw and fried fricadelles, and the incomplete THE_Fricadelle used by Sequenced Assembly. THE_FRICADELLE is epic with an enchantment glint.
- **Food values** — Provisional nutrition and saturation for the three fricadelles, centralised in `BSFoods`.
- **Tags** — `minced_meats`, `minced_meats/beef` (also accepts Farmer's Delight minced beef), `grinder/extra_foods` (cake), `grinder/blacklist` (our pastes and fricadelles, enchanted golden apple); fricadelles added to `c:foods/cooked_meat`.
- **Tooltips** — Create-style summaries on the three fricadelles, in English and French.
- **Placeholder textures** — `tools/gen_placeholders.py`, a standard-library generator that never overwrites an existing texture without `--force`.
- **Fluids** — Frying oil, melted beef tallow, mayonnaise and curry ketchup, each with a placeable source and a bucket. The `frying_oils` fluid tag also accepts other mods' plant and vegetable oils.
- **Create recipes** — Crushing (pork, beef with a chance of tallow, chicken, bread), milling (dried kelp into Belgian spices), mixing (fricadelle paste, melted tallow, mayonnaise, curry ketchup with a beetroot fallback), compacting (seeds into frying oil, only without another seed-oil mod) and pressing (paste into raw fricadelle), all generated with stable ids.
- **Interoperability** — Recipes accept any mod's minced meat and bread crumbs through tags, and our items join the matching convention tags.
- **Fryer** — First machine of the mod. Sits on a Blaze Burner and fries a whole batch (up to 16, configurable) in any frying fat; tier 3 items fry one at a time. Fat is paid when a batch starts, lost heat pauses without loss, and a broken fryer keeps its fat. Fills from pipes, spouts and buckets, takes items from funnels, belts and by hand, gives its output to funnels and hoppers only, and drives a comparator. Goggles, Jade and a Create-styled JEI category show its state and recipes.
- **Frying recipe type** — `create_belgian_snacks:frying`, in Create's processing recipe format: the fat is a fluid ingredient whose amount is per fried item. First recipe: raw fricadelle into fricadelle, 10 mB of any frying fat, heated, 5 s.
- **Server config** — `fryer.tankCapacity`, `fryer.speedMultiplier`, `fryer.maxBatch`.
- **Automated tests** — JUnit checks on generated resources (language parity, textures, tags, dependency metadata) and in-game GameTests on registration, food, tags and the creative tab. CI runs both, also with Farmer's Delight loaded, and fails when generated resources are out of date. A local client smoke run checks models, fluid sprites, tooltips, French names, the creative tab and JEI, and saves screenshots. A two-client run joins a dedicated server and checks the fryer stays in sync and reacts to a player's click.

### Ajouts

- **Squelette du projet** — Projet ModDevGradle 2.0.147 sur Gradle 9.2.1, NeoForge 21.1.250, compilé contre Create 6.0.10 (build 280, celui livré dans Arcadia V2 2.0.32) avec Registrate, Ponder et Flywheel. JEI et Jade ne sont chargés que dans le runtime de développement.
- **Onglet créatif** — `create_belgian_snacks:main`, placé après les onglets de Create et utilisé comme onglet par défaut de Registrate. Son icône est la fricadelle.
- **Génération de données** — `runData` écrit `src/generated/resources` ; `en_us` vient de Registrate, `fr_fr` d'un fournisseur de langue dédié.
- **Licence** — Licence Tous droits réservés avec NOTICE, CONTRIBUTING et CONTRIBUTORS, en-têtes SPDX sur les sources, fichiers de licence inclus dans le jar.
- **Intégration continue** — Workflow GitHub Actions qui lance `./gradlew build` sur JDK 21.
- **Items** — 16 items de base pour les trois paliers : hachis de porc, de bœuf et de poulet, blanc de bœuf, chapelure, épices belges, les trois pâtes, les fricadelles crues et frites, et la THE_Fricadelle en cours utilisée par la Sequenced Assembly. THE_FRICADELLE est épique, avec un reflet d'enchantement.
- **Valeurs nutritives** — Nutrition et saturation provisoires des trois fricadelles, centralisées dans `BSFoods`.
- **Tags** — `minced_meats`, `minced_meats/beef` (accepte aussi le bœuf haché de Farmer's Delight), `grinder/extra_foods` (gâteau), `grinder/blacklist` (nos pâtes et fricadelles, pomme d'or enchantée) ; fricadelles ajoutées à `c:foods/cooked_meat`.
- **Infobulles** — Résumés au format Create sur les trois fricadelles, en anglais et en français.
- **Textures provisoires** — `tools/gen_placeholders.py`, générateur en bibliothèque standard qui n'écrase jamais une texture existante sans `--force`.
- **Fluides** — Huile de friture, blanc de bœuf fondu, mayonnaise et curry ketchup, chacun avec une source posable et un seau. Le tag fluide `frying_oils` accepte aussi les huiles végétales des autres mods.
- **Recettes Create** — Broyage (porc, bœuf avec une chance de blanc de bœuf, poulet, pain), meule (algue séchée en épices belges), mélange (pâte à fricadelle, blanc de bœuf fondu, mayonnaise, curry ketchup avec secours à la betterave), compactage (graines en huile, seulement sans autre mod d'huile de graines) et presse (pâte en fricadelle crue), toutes générées avec des IDs stables.
- **Interopérabilité** — Les recettes acceptent le haché et la chapelure de n'importe quel mod via des tags, et nos items rejoignent les tags de convention correspondants.
- **Friteuse** — Première machine du mod. Posée sur un Blaze Burner, elle frit un lot entier (jusqu'à 16, configurable) dans n'importe quelle graisse de friture ; le palier 3 cuit un par un. La graisse est payée au démarrage du lot, une perte de chaleur met en pause sans perte, et une friteuse cassée garde sa graisse. Remplissage par tuyau, Spout et seau, entrée par funnel, tapis et clic droit, sortie vers funnels et trémies uniquement, signal de comparateur. Goggles, Jade et une catégorie JEI au style Create affichent son état et ses recettes.
- **Type de recette friture** — `create_belgian_snacks:frying`, au format des recettes de traitement Create : la graisse est un ingrédient fluide dont la quantité s'entend par item frit. Première recette : fricadelle crue en fricadelle, 10 mB de n'importe quelle graisse, chauffé, 5 s.
- **Config serveur** — `fryer.tankCapacity`, `fryer.speedMultiplier`, `fryer.maxBatch`.
- **Tests automatisés** — Contrôles JUnit des ressources générées (parité des langues, textures, tags, métadonnées de dépendances) et GameTests en jeu sur l'enregistrement, la nourriture, les tags et l'onglet créatif. La CI lance les deux, aussi avec Farmer's Delight chargé, et échoue si les ressources générées ne sont pas à jour. Un test client local vérifie les modèles, les textures de fluide, les infobulles, les noms français, l'onglet créatif et JEI, et enregistre des captures. Un test à deux clients rejoint un serveur dédié et vérifie que la friteuse reste synchronisée et réagit au clic d'un joueur.

---
