# Create: Belgian Snacks

[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-blue.svg)](LICENSE)

A [Create](https://modrinth.com/mod/create) addon for Minecraft 1.21.1 (NeoForge) that industrialises the Belgian fricadelle, in three tiers:

| Tier | Item | Production |
|---|---|---|
| 1 | Fricadelle | Minced meats, bread crumbs, mixing, pressing, then frying |
| 2 | THE_Fricadelle | The Supreme Grinder collects 10 % of every food in the modpack into an Exceptional Paste; a Sequenced Assembly line adds spices, mayonnaise, curry ketchup and onion; then frying |
| 3 | THE_FRICADELLE | Every single food of the modpack goes into an Absolute Paste; a longer line with beef tallow; superheated frying in beef tallow only |

All three tiers are playable and automatable end to end. The 42 original pixel-art textures cover foods, buckets, animated fluids and machine faces, with at most 16 colours each.

## Features

- Two machines: the **Fryer** (sits on a Blaze Burner, runs on any frying fat) and the **Supreme Grinder** (kinetic, collects each unique food once, two modes).
- Everything else runs on native Create processing: crushing, milling, mixing, compacting, pressing, filling, deploying, sequenced assembly.
- Fully automatable, no mandatory manual step.
- The food list is computed from the running modpack, not hard-coded, and can be tuned through tags and server config.
- Optional integrations: JEI categories, Jade tooltips, KubeJS-friendly recipe ids and tags, Farmer's Delight ingredients through `c:` tags.
- Ponder scenes for both machines and both assembly lines, an advancement tab, and a visit from THEFricadelle to whoever eats THE_FRICADELLE.
- English and French.

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
./gradlew testAll            # everything, including the compat tests, a live client and two clients on a server
./gradlew runData            # regenerate src/generated/resources
./gradlew runClient          # development client
./gradlew runServer          # development dedicated server
```

## Texture artwork

The artwork follows [the texture brief](docs/12-prompts-textures.md). Run `python tools/draw_textures.py` with [Python 3](https://www.python.org/) (standard library only) to reproduce it and create `build/texture-preview.html` and `build/texture-preview.png`. Existing manual edits are preserved unless `--force` is explicitly supplied. Run `python tools/asset_status.py` to check the textures and `./gradlew runClientSmoke` to check them in game. PNGs live in `src/main/resources/assets/create_belgian_snacks/textures/`; flow animation metadata stays alongside them. Custom 3D block models remain a separate task.

## License

All Rights Reserved. The source code is not published, and the mod is not open-source. See [LICENSE](LICENSE) for the binding terms and [NOTICE.md](NOTICE.md) for a plain-language summary. Modpacks may include the official, unmodified file when they reference an official channel.

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
| 2 | THE_Fricadelle | Le Hachoir Suprême rassemble 10 % de tous les aliments du modpack en une Pâte d'exception ; une chaîne Sequenced Assembly ajoute épices, mayonnaise, curry ketchup et oignon ; puis friture |
| 3 | THE_FRICADELLE | Absolument tous les aliments du modpack dans une Pâte absolue ; une chaîne plus longue au blanc de bœuf ; friture super-chauffée au blanc de bœuf seulement |

Les trois paliers sont jouables et automatisables de bout en bout. Les 42 textures originales en pixel art couvrent aliments, seaux, fluides animés et faces des machines, avec au plus 16 couleurs chacune.

## Caractéristiques

- Deux machines : la **Friteuse** (posée sur un Blaze Burner, fonctionne avec n'importe quelle graisse de friture) et le **Hachoir Suprême** (cinétique, collectionne chaque aliment unique une seule fois, deux modes).
- Tout le reste utilise les traitements natifs de Create : broyage, meule, mélange, compactage, presse, remplissage, deployer, sequenced assembly.
- Entièrement automatisable, aucune étape manuelle obligatoire.
- La liste des aliments est calculée à partir du modpack chargé, pas écrite en dur, et se règle par tags et config serveur.
- Intégrations optionnelles : catégories JEI, infobulles Jade, IDs de recettes et tags adaptés à KubeJS, ingrédients de Farmer's Delight via les tags `c:`.
- Scènes Ponder pour les deux machines et les deux chaînes d'assemblage, un onglet d'advancements, et une visite de THEFricadelle à qui mange THE_FRICADELLE.
- Anglais et français.

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
./gradlew testAll            # tout, y compris les tests de compat, un client réel et deux clients sur un serveur
./gradlew runData            # régénère src/generated/resources
./gradlew runClient          # client de développement
./gradlew runServer          # serveur dédié de développement
```

## Textures

Les dessins suivent [le cahier des textures](docs/12-prompts-textures.md). Exécuter `python tools/draw_textures.py` avec [Python 3](https://www.python.org/) (bibliothèque standard uniquement) pour les reproduire et créer `build/texture-preview.html` et `build/texture-preview.png`. Les retouches manuelles existantes sont préservées sauf avec l'option explicite `--force`. Vérifier les textures avec `python tools/asset_status.py`, puis leur chargement en jeu avec `./gradlew runClientSmoke`. Les PNG se trouvent dans `src/main/resources/assets/create_belgian_snacks/textures/` ; les métadonnées d'animation des fluides restent à leurs côtés. Les modèles 3D personnalisés restent une tâche distincte.

## Licence

Tous droits réservés. Le code source n'est pas publié, et le mod n'est pas open-source. Voir [LICENSE](LICENSE) pour les conditions contraignantes et [NOTICE.md](NOTICE.md) pour un résumé en langage clair. Les modpacks peuvent inclure le fichier officiel non modifié s'ils référencent un canal officiel.

Addon non officiel, ni affilié ni approuvé par l'équipe de Create.

## Credits

Author: THEFricadelle

Voir [CONTRIBUTORS.md](CONTRIBUTORS.md) et [CONTRIBUTING.md](CONTRIBUTING.md).
