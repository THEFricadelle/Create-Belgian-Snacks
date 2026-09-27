# Fiche de publication (CurseForge, Modrinth, GitHub)

Tout ce qu'il faut remplir sur les deux plateformes. Le texte de la page est dans `page-en.md` (à coller tel quel) ; `page-fr.md` est sa traduction, pour relecture ou pour une description française si une plateforme en propose.

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
| Licence | All Rights Reserved (lien vers `LICENSE` du repo) |
| Distribution dans les modpacks (CurseForge) | **Activée** (licence §3(b) : un modpack qui référence le fichier officiel est autorisé) |
| Source | https://github.com/THEFricadelle/Create-Belgian-Snacks |
| Issues | https://github.com/THEFricadelle/Create-Belgian-Snacks/issues |
| Icône | à faire (512×512) une fois les vraies textures prêtes ; la THE_FRICADELLE s'y prête |

## Fichier

- `build/libs/create-belgian-snacks-<version>.jar` (le jar de release ; pas le `-sources`).
- Version : **1.0.0** tant que tu ne demandes pas de changement de numéro. Canal : **release** (première version stable).
- Notes de version : la section `[Unreleased]` du `CHANGELOG.md` (à fermer au passage à une vraie version).

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

1. Une ligne complète du palier 1 : broyage, mixer, presse, Friteuse sur son Blaze Burner.
2. Le Hachoir Suprême nourri par un tapis, Goggles en main (compte et objectif visibles).
3. La chaîne de Sequenced Assembly du palier 2.
4. La page JEI de la Sequenced Assembly de THE_FRICADELLE.
5. THEFricadelle qui applaudit après la THE_FRICADELLE (feu d'artifice).
6. Le Ponder de la Friteuse.

Le terrain de démonstration a de quoi faire les captures 1 à 3 et 5 ; les captures des tests (`run/clientsmoke/screenshots`) montrent les cadrages de JEI et Ponder.

## Avant de cliquer sur « publier »

- [ ] Tes rapports spark (scénarios Friteuse, Hachoir et THEFricadelle, `docs/10-tests.md`) : relus ensemble, ou dispense explicite.
- [ ] Vraies textures en place (`python tools/asset_status.py` : 0 placeholder), ou décision de publier avec les placeholders.
- [ ] `dev` fusionnée dans `main` : `main` ne contient encore que les documents de conception, et la page renvoie vers `main` (NOTICE, LICENSE). La fusion suit les rapports spark.
- [ ] `./gradlew testAll` vert et CI verte sur le commit publié.
- [ ] Brouillon de release GitHub relu (jar joint, notes), puis publié.
