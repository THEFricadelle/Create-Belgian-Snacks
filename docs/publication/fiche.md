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
- Version : **0.1.0** tant que tu ne demandes pas de changement de numéro. Canal conseillé pour une 0.x : **beta**.
- Notes de version : la section `[Unreleased]` du `CHANGELOG.md` (à fermer au passage à une vraie version).

## Captures pour la galerie (une fois les vraies textures faites)

1. Une ligne complète du palier 1 : broyage, mixer, presse, Friteuse sur son Blaze Burner.
2. Le Hachoir Suprême nourri par un tapis, Goggles en main (compte et objectif visibles).
3. La chaîne de Sequenced Assembly du palier 2.
4. La page JEI de la Sequenced Assembly de THE_FRICADELLE.
5. THEFricadelle qui applaudit après la THE_FRICADELLE (feu d'artifice).
6. Le Ponder de la Friteuse.

Les captures des tests (`run/clientsmoke/screenshots`) montrent les cadrages ; elles ont encore les placeholders.

## Avant de cliquer sur « publier »

- [ ] Tes rapports spark (scénarios Friteuse, Hachoir et THEFricadelle, `docs/10-tests.md`) : relus ensemble, ou dispense explicite.
- [ ] Vraies textures en place (`python tools/asset_status.py` : 0 placeholder), ou décision de publier avec les placeholders.
- [ ] `dev` fusionnée dans `main` : `main` ne contient encore que les documents de conception, et la page renvoie vers `main` (NOTICE, LICENSE). La fusion suit les rapports spark.
- [ ] `./gradlew testAll` vert et CI verte sur le commit publié.
- [ ] Brouillon de release GitHub relu (jar joint, notes), puis publié.
