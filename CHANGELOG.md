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

---
