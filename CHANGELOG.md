# Changelog

All notable changes to Create: Belgian Snacks are documented here.

---

## [Unreleased]

### Added

- **Project skeleton** — ModDevGradle 2.0.147 project on Gradle 9.2.1, NeoForge 21.1.250, compiled against Create 6.0.10 (build 280, the one shipped in Arcadia V2 2.0.32) with Registrate, Ponder and Flywheel. JEI and Jade are loaded in the development runtime only.
- **Creative tab** — `create_belgian_snacks:main`, placed after Create's tabs and used as Registrate's default tab. It stays hidden until the first items are registered.
- **Data generation** — `runData` writes `src/generated/resources`; `en_us` comes from Registrate, `fr_fr` from a dedicated language provider.
- **Licensing** — All Rights Reserved license with NOTICE, CONTRIBUTING and CONTRIBUTORS, SPDX headers on sources, license files shipped inside the jar.
- **Continuous integration** — GitHub Actions workflow running `./gradlew build` on JDK 21.

### Ajouts

- **Squelette du projet** — Projet ModDevGradle 2.0.147 sur Gradle 9.2.1, NeoForge 21.1.250, compilé contre Create 6.0.10 (build 280, celui livré dans Arcadia V2 2.0.32) avec Registrate, Ponder et Flywheel. JEI et Jade ne sont chargés que dans le runtime de développement.
- **Onglet créatif** — `create_belgian_snacks:main`, placé après les onglets de Create et utilisé comme onglet par défaut de Registrate. Il reste masqué tant qu'aucun item n'est enregistré.
- **Génération de données** — `runData` écrit `src/generated/resources` ; `en_us` vient de Registrate, `fr_fr` d'un fournisseur de langue dédié.
- **Licence** — Licence Tous droits réservés avec NOTICE, CONTRIBUTING et CONTRIBUTORS, en-têtes SPDX sur les sources, fichiers de licence inclus dans le jar.
- **Intégration continue** — Workflow GitHub Actions qui lance `./gradlew build` sur JDK 21.

---
