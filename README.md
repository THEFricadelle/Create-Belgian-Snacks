# Create: Belgian Snacks

[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-blue.svg)](LICENSE)

A [Create](https://modrinth.com/mod/create) addon for Minecraft 1.21.1 (NeoForge) that industrialises the Belgian fricadelle, in three tiers:

| Tier | Item | Production |
|---|---|---|
| 1 | Fricadelle | Minced meats, bread crumbs, mixing, pressing, then frying |
| 2 | THE_Fricadelle | An exceptional paste fed with a share of every food in the modpack, a Sequenced Assembly line, then frying |
| 3 | THE_FRICADELLE | Every single food of the modpack goes through the Supreme Grinder |

The mod is in early development. Nothing is playable yet.

## Features

Planned for the first release:

- Two machines: the **Fryer** (sits on a Blaze Burner, runs on frying oil) and the **Supreme Grinder** (kinetic, collects each unique food once).
- Everything else runs on native Create processing: crushing, milling, mixing, compacting, pressing, filling, deploying, sequenced assembly.
- Fully automatable, no mandatory manual step.
- The food list is computed from the running modpack, not hard-coded, and can be tuned through tags and server config.
- Optional integrations: JEI categories, Jade tooltips, KubeJS-friendly recipe ids and tags, Farmer's Delight ingredients through `c:` tags.

## Requirements

| Dependency | Version | Required |
|---|---|---|
| [Minecraft](https://www.minecraft.net) | 1.21.1 | yes |
| [NeoForge](https://neoforged.net) | 21.1.250 or newer | yes |
| [Create](https://modrinth.com/mod/create) | 6.0.10 up to (excluding) 6.1.0 | yes |
| [JEI](https://modrinth.com/mod/jei) | 19.x | no |
| [Jade](https://modrinth.com/mod/jade) | 15.x | no |
| [KubeJS](https://modrinth.com/mod/kubejs) | 2101.x | no |
| [Farmer's Delight](https://modrinth.com/mod/farmers-delight) | 1.21.1 | no |

## Installation

1. Install NeoForge 21.1.250 or newer for Minecraft 1.21.1.
2. Put the Create 6.0.10 jar in the `mods` folder.
3. Put the `create-belgian-snacks` jar in the `mods` folder, on the client and on the server.

## Building from source

Requires JDK 21.

```bash
./gradlew build              # jar in build/libs, runs the JUnit tests
./gradlew runGameTestServer  # in-game tests, headless
./gradlew runData            # regenerate src/generated/resources
./gradlew runClient          # development client
./gradlew runServer          # development dedicated server
```

## License

All Rights Reserved. The source is public for reading, auditing and contributing, but the mod is not open-source. See [LICENSE](LICENSE) for the binding terms and [NOTICE.md](NOTICE.md) for a plain-language summary. Modpacks may include the official, unmodified file when they reference an official channel.

This is an unofficial addon, not affiliated with or endorsed by the Create team.

## Credits

Author: THEFricadelle

See [CONTRIBUTORS.md](CONTRIBUTORS.md) and [CONTRIBUTING.md](CONTRIBUTING.md).

---

# Create: Belgian Snacks (Version Française)

[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-blue.svg)](LICENSE)

Un addon [Create](https://modrinth.com/mod/create) pour Minecraft 1.21.1 (NeoForge) qui industrialise la fricadelle belge, en trois paliers :

| Palier | Item | Production |
|---|---|---|
| 1 | Fricadelle | Viandes hachées, chapelure, mélange, presse, puis friture |
| 2 | THE_Fricadelle | Une pâte d'exception nourrie d'une part de tous les aliments du modpack, une chaîne Sequenced Assembly, puis friture |
| 3 | THE_FRICADELLE | Chaque aliment du modpack passe dans le Hachoir Suprême |

Le mod est en début de développement. Rien n'est encore jouable.

## Caractéristiques

Prévu pour la première version :

- Deux machines : la **Friteuse** (posée sur un Blaze Burner, fonctionne à l'huile de friture) et le **Hachoir Suprême** (cinétique, collectionne chaque aliment unique une seule fois).
- Tout le reste utilise les traitements natifs de Create : broyage, meule, mélange, compactage, presse, remplissage, deployer, sequenced assembly.
- Entièrement automatisable, aucune étape manuelle obligatoire.
- La liste des aliments est calculée à partir du modpack chargé, pas écrite en dur, et se règle par tags et config serveur.
- Intégrations optionnelles : catégories JEI, infobulles Jade, IDs de recettes et tags adaptés à KubeJS, ingrédients de Farmer's Delight via les tags `c:`.

## Prérequis

| Dépendance | Version | Requise |
|---|---|---|
| [Minecraft](https://www.minecraft.net) | 1.21.1 | oui |
| [NeoForge](https://neoforged.net) | 21.1.250 ou plus récent | oui |
| [Create](https://modrinth.com/mod/create) | 6.0.10 jusqu'à 6.1.0 (exclu) | oui |
| [JEI](https://modrinth.com/mod/jei) | 19.x | non |
| [Jade](https://modrinth.com/mod/jade) | 15.x | non |
| [KubeJS](https://modrinth.com/mod/kubejs) | 2101.x | non |
| [Farmer's Delight](https://modrinth.com/mod/farmers-delight) | 1.21.1 | non |

## Installation

1. Installer NeoForge 21.1.250 ou plus récent pour Minecraft 1.21.1.
2. Placer le jar de Create 6.0.10 dans le dossier `mods`.
3. Placer le jar `create-belgian-snacks` dans le dossier `mods`, côté client et côté serveur.

## Compiler depuis les sources

Nécessite le JDK 21.

```bash
./gradlew build              # jar dans build/libs, lance les tests JUnit
./gradlew runGameTestServer  # tests en jeu, sans interface
./gradlew runData            # régénère src/generated/resources
./gradlew runClient          # client de développement
./gradlew runServer          # serveur dédié de développement
```

## Licence

Tous droits réservés. Le code est public pour être lu, audité et amélioré, mais le mod n'est pas open-source. Voir [LICENSE](LICENSE) pour les conditions contraignantes et [NOTICE.md](NOTICE.md) pour un résumé en langage clair. Les modpacks peuvent inclure le fichier officiel non modifié s'ils référencent un canal officiel.

Addon non officiel, ni affilié ni approuvé par l'équipe de Create.

## Credits

Author: THEFricadelle

Voir [CONTRIBUTORS.md](CONTRIBUTORS.md) et [CONTRIBUTING.md](CONTRIBUTING.md).
