# Fiche de publication (CurseForge, Modrinth, GitHub)

Tout ce qu'il faut remplir sur les deux plateformes. Le texte de la page est dans `page-en.md` (à coller tel quel) ; `page-fr.md` est sa traduction, pour relecture ou pour une description française si une plateforme en propose. Les notes de version sont dans `release-1.0.0.md`, l'icône dans `icon.png`.

## Champs

| Champ | Valeur |
|---|---|
| Nom | Create: Belgian Snacks |
| Slug | `create-belgian-snacks` |
| Résumé court (Modrinth : 256 caractères max, ici 127) | The Belgian fricadelle, industrialised with Create: three tiers, up to a snack that contains every single food of your modpack. |
| Catégories Modrinth | Food, Technology (+ tag « Create addon » si proposé) |
| Catégories CurseForge | Food, Technology, Addons > Create |
| Environnement | Client **et** serveur (requis des deux côtés) |
| Loader | NeoForge |
| Versions de jeu | 1.21.1 |
| Dépendances requises | Create (6.0.10 à 6.1.0 exclu) |
| Dépendances optionnelles | JEI, Jade, KubeJS, Farmer's Delight |
| Licence CurseForge | All Rights Reserved |
| Licence Modrinth | Personnalisée : identifiant SPDX `LicenseRef-Create-Belgian-Snacks-ARR`, URL `https://github.com/THEFricadelle/Create-Belgian-Snacks/blob/main/LICENSE` |
| Environnement Modrinth | Client : **Required** ; Serveur : **Required** |
| Java | 21 |
| Distribution dans les modpacks (CurseForge) | **Activée** (licence §3(b) : un modpack qui référence le fichier officiel est autorisé) |
| Source | https://github.com/THEFricadelle/Create-Belgian-Snacks |
| Issues | https://github.com/THEFricadelle/Create-Belgian-Snacks/issues |
| Icône | `docs/publication/icon.png` (512×512, la THE_FRICADELLE agrandie sans lissage, `python tools/make_icon.py`) ; la même image est le logo du mod dans la liste des mods du jeu |

## Fichier

- `build/libs/create-belgian-snacks-<version>.jar` (le jar de release ; pas le `-sources`).
- Version : **1.0.0** tant que tu ne demandes pas de changement de numéro. Canal : **release** (première version stable).
- Type de version : **Release** sur les deux plateformes.
- Notes de version : bloc anglais de `release-1.0.0.md` (même texte sur CurseForge, Modrinth et GitHub). Le `CHANGELOG.md` garde l'historique de développement.
- Nom du fichier affiché : `Create: Belgian Snacks 1.0.0`, numéro de version `1.0.0`.

## Terrain de démonstration

`./gradlew runShowcase` ouvre un monde plat (créatif, toujours midi, sans mobs) construit au premier lancement dans `run/showcase` :

| Où (depuis l'apparition, face au sud) | Quoi |
|---|---|
| Devant toi | Panneau d'accueil, rappel Ponder, comptoir avec tous les items du mod dans des cadres |
| À droite (ouest) | Les 4 fluides en bassins ; la ligne du palier 1 (coffre de viandes, roues de concassage, mixer, presse, Friteuse, coffre de sortie) |
| Au milieu | Un coffre de THE_FRICADELLE : en manger une fait venir THEFricadelle |
| À gauche (est) | Palier 2 : coffre d'aliments, Hachoir Suprême en mode THE_, ligne de Sequenced Assembly (3 tours à la suite), Friteuse. Palier 3 : pareil en mode ULTIME, 5 tours, Friteuse super-chauffée au blanc de bœuf |

Tout tourne seul : les stocks (viandes, aliments, sauces, épices, graisse) sont remis à niveau chaque seconde, et les coffres de sortie sont vidés avant d'être pleins. Les noms d'items sur les panneaux suivent la langue du jeu. Le monde est gardé d'un lancement à l'autre ; supprimer `run/showcase/saves/belgian-snacks-showcase` pour le reconstruire. Réservé au développement : le code est dans le source set `gametest`, jamais dans le jar.

`./gradlew runShowcaseCheck` le construit dans un monde neuf, attend la fricadelle de chaque palier dans son coffre de sortie (27/09/2026 : palier 2 en 31 s, palier 1 en 57 s, palier 3 en 86 s), prend 5 captures (`run/showcase-check/screenshots`) et se ferme.

## Captures pour la galerie

Les captures automatiques (`run/showcase-check/screenshots`, `run/clientsmoke/screenshots`) font 854×480 : elles servent de repères de cadrage, pas de galerie. Prendre les vraies en 1920×1080 (F2 en jeu, fichiers dans `run/showcase/screenshots`), interface masquée (F1) sauf pour JEI et Ponder :

1. Une ligne complète du palier 1 : broyage, mixer, presse, Friteuse sur son Blaze Burner (showcase, à l'ouest).
2. Le Hachoir Suprême nourri par un tapis, Goggles en main (compte et objectif visibles).
3. La chaîne de Sequenced Assembly du palier 2 (showcase, à l'est).
4. La page JEI de la Sequenced Assembly de THE_FRICADELLE.
5. THEFricadelle qui applaudit après la THE_FRICADELLE (coffre du milieu du showcase).
6. Le Ponder de la Friteuse (W au-dessus d'une Friteuse).

La première image de la galerie Modrinth sert de bannière : mettre la 1 ou la 5 en « Featured ».

## Mise en ligne

### CurseForge

1. **Create project** > Minecraft > Mods. Nom, résumé, catégories et licence : tableau « Champs ». Description : coller `page-en.md` (éditeur en mode Markdown). Icône : `icon.png`.
2. Onglet **Files** > Upload : `build/libs/create-belgian-snacks-1.0.0.jar`, Release, NeoForge, 1.21.1, Java 21, notes : bloc anglais de `release-1.0.0.md`.
3. **Related projects** du fichier : Create en *Required Dependency* ; JEI, Jade, KubeJS, Farmer's Delight en *Optional Dependency*.
4. Settings du projet : laisser activée la distribution dans les modpacks. Liens : Source et Issues.
5. Le premier fichier passe en modération (quelques heures à quelques jours) avant d'être visible.

### Modrinth

1. **Create a project** : nom, slug `create-belgian-snacks`, résumé du tableau. Type : Mod.
2. Description : `page-en.md`. Icône : `icon.png`. Tags : Food, Technology. Environnement et licence : tableau « Champs ». Liens : Source, Issues.
3. **Versions** > Create version : le jar, numéro `1.0.0`, canal Release, loader NeoForge, version de jeu 1.21.1, notes : bloc anglais de `release-1.0.0.md`. Dépendances : Create *required* ; JEI, Jade, KubeJS, Farmer's Delight *optional*.
4. Galerie : les captures ci-dessus.
5. **Submit for review** : la modération de Modrinth relit la page (un mod sous licence ARR avec source publique est accepté) ; compter quelques jours.

### GitHub

1. Tag `v1.0.0` sur le commit de `main` publié.
2. Release `Create: Belgian Snacks 1.0.0` : notes de `release-1.0.0.md`, jar joint.
3. Une fois les pages en ligne, ajouter leurs liens au README et à `page-en.md` / `page-fr.md` (section Links).

## Avant de cliquer sur « publier »

- [x] Rapports spark relus (28/09/2026, `docs/10-tests.md`).
- [x] Vraies textures en place (`python tools/asset_status.py` : 42 faites main, 0 placeholder).
- [ ] `dev` fusionnée dans `main` et poussée : la page renvoie vers `main` (NOTICE, LICENSE).
- [ ] CI verte sur le commit publié.
- [ ] Jar uploadé = jar construit depuis ce commit (`./gradlew build`, puis comparer le SHA-256 affiché par la plateforme avec `sha256sum build/libs/create-belgian-snacks-1.0.0.jar`).
- [ ] Procédure `test-procedures/TEST_PROCEDURE_v1.0.0.html` passée, ou ses campagnes manuelles explicitement dispensées.
