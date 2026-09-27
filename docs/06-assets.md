# 06 — Assets (textures, modèles, sons, traductions)

## Règle d'or

**Aucun fichier d'asset de Create ne doit être copié ou modifié** (ils sont *All Rights Reserved*). On peut imiter le style (palette laiton/andésite, contours), jamais reprendre leurs PNG.

## Placeholders

Tant que les vraies textures ne sont pas faites, des placeholders sont générés par un script Python **sans dépendance** (bibliothèque standard : encodeur PNG `zlib` + `struct`) : `python tools/gen_placeholders.py [--force]`. Pas de Pillow, pour ne rien ajouter à installer.

Spécs du script :
- PNG 16×16, fond transparent pour les items.
- Forme simple par famille : hachis = tas granuleux ; pâte = boule ; fricadelle crue = bâtonnet rose ; fricadelle = bâtonnet brun doré ; seaux = teinte du fluide sur un seau générique **dessiné par le script**.
- Couleur de base par item (table `ITEMS` du script), + 1 glyphe 3×5 en surimpression : une lettre pour les ingrédients (P, B, C, T, R, S), le chiffre du palier (1, 2, 3) pour les pâtes et les fricadelles.
- État au M2 : 16 items, 4 fluides (`still` 16×16, `flow` 16×32 sur 2 frames + `.mcmeta`) et leurs 4 seaux (seau dessiné par le script, rempli de la couleur du fluide). Blocs (M3) : à ajouter.
- THE_FRICADELLE : bâtonnet doré avec bord brillant (le glint est géré par l'item, pas la texture).
- `incomplete_ultimate_fricadelle` : bâtonnet crème, glyphe 3 (placeholder).
- PNJ THEFricadelle : **aucune texture livrée**. Le skin du compte Minecraft **THE_Fricadelle** (le pseudo de l’auteur, pas le nom affiché THEFricadelle, qui appartient à un autre joueur) est chargé en ligne au premier affichage (comme une tête de joueur), avec le modèle large ou fin qu'il déclare ; sans connexion, skin par défaut.
- Fluides : `still` et `flow` 16×16 (le `flow` en 16×32 animé si possible, sinon statique) + fichier `.mcmeta`.
- Blocs : 6 faces unies avec bordure, 2 couleurs distinctes pour la Friteuse (inox) et le Hachoir (laiton).
- Le script **n'écrase jamais** un fichier existant sans `--force` (pour ne pas écraser les vraies textures).

## Emplacements

```
src/main/resources/assets/create_belgian_snacks/
├── textures/item/<id>.png
├── textures/block/fryer/{side,top,bottom,inner}.png
├── textures/block/supreme_grinder/{side_0..side_4,top,bottom,blade,mince}.png   (side_N : jauge à N quarts)
├── textures/fluid/<fluid>_{still,flow}.png (+ .mcmeta)
├── sounds/  (ogg)
└── sounds.json   # généré par datagen si possible
```
Les modèles JSON d'items/blocs sont générés par datagen. Un modèle Blockbench fait main va dans `models/block/custom/` (voir « Livrables » ci-dessous) : le datagen le prend à la place du sien.

## Livrables Blockbench (M9, à faire par THEFricadelle)

Pour générer les textures avec un assistant d'image : prompts prêts dans `12-prompts-textures.md`, avec `tools/pixelate.py` pour ramener une grande image générée à 16×16.

Où en est-on : `python tools/asset_status.py` liste chaque texture (placeholder, faite main, manquante) et les 3 modèles facultatifs. Un fichier fait main se reconnaît tout seul : il suffit qu'il diffère du placeholder. Après un dépôt : `./gradlew runData`, puis `./gradlew runClient` pour voir le résultat ; le test client vérifie qu'aucune texture ni aucun modèle ne manque.

### Textures (même nom de fichier que le placeholder, on le remplace)

| Fichiers | Taille | Remarques |
|---|---|---|
| `textures/item/<id>.png`, 17 items | 16×16, fond transparent | ids : ceux de `docs/04` ; les 3 fricadelles et leurs versions crues, les 3 pâtes, les 2 items de transition, hachis, suif, chapelure, épices |
| `textures/item/<fluide>_bucket.png`, 4 seaux | 16×16 | frying_oil, melted_beef_tallow, mayonnaise, curry_ketchup |
| `textures/fluid/<fluide>_still.png` | 16×16 (ou 16×N animé + `.mcmeta`) | la surface immobile |
| `textures/fluid/<fluide>_flow.png` | 16×32, 2 frames, `.mcmeta` déjà présent | l'écoulement |
| `textures/block/fryer/{side,top,bottom,inner}.png` | 16×16 | utilisées par le modèle généré ; un modèle fait main peut en utiliser d'autres |
| `textures/block/supreme_grinder/{top,bottom,blade,mince}.png` | 16×16 | `mince` : le hachis vu de dessus, qui se répète sans couture |
| `textures/block/supreme_grinder/side_0.png` … `side_4.png` | 16×16 | **la jauge** : la même face, remplie de 0 à 4 quarts. Les 5 fichiers sont obligatoires |

### Modèles (facultatifs : sans eux, le modèle généré reste)

| Fichier | Contrat |
|---|---|
| `models/block/custom/fryer.json` | Friteuse : une cuve ouverte sur quatre pieds (fond de cuve à y = 4). Le code dessine la graisse et les items flottants, x/z de 2 à 14 : dès la première goutte la graisse monte à y = 9, puis jusqu'à y = 15 cuve pleine. Laisser cet intérieur vide. Livré le 27/09/2026 |
| `models/block/custom/supreme_grinder.json` | (Livré le 27/09/2026 : quatre parois pleine hauteur autour d'une fosse profonde, fond à y = 2, la jauge sur les quatre faces.) Le code dessine le hachis (`supreme_grinder/mince`) dans la fosse, x/z de 2 à 14, de y = 2 à y = 11,5 selon la progression vers l'objectif : laisser la fosse vide sous les lames. Carter du Hachoir, **sans les lames**. Ses faces qui montrent la jauge utilisent la variable de texture `#side` : le datagen crée les 5 niveaux en ne changeant que `#side` (vers `side_0` … `side_4`). Arbre par le haut : laisser le centre du dessus ouvert pour l'arbre de Create |
| `models/block/custom/supreme_grinder_blades.json` | Les lames seules. Le code les fait tourner autour de l'axe vertical passant par le centre du bloc (8, y, 8) : modéliser centré sur ce point. Livré le 27/09/2026 : deux lames opposées et courbées (3 segments chacune, tournés de 0, 22,5 et 45°) sur un moyeu |

Rien d'autre à faire : blockstates, modèles d'items, variantes de jauge, rotation des lames et rendu de la graisse sont générés ou codés. Le PNJ THEFricadelle n'a besoin d'aucune texture (skin du compte, en ligne).

## Sons

| ID | Usage | Placeholder |
|---|---|---|
| `fryer.sizzle` | boucle pendant la friture | son vanilla réutilisé (ex. feu de camp / lave) via `sounds.json` |
| `grinder.grind` | aliment accepté | son vanilla (meule) |
| `grinder.complete` | objectif atteint | son vanilla (niveau) |
| `fricadelle.burp` | gag en mangeant | `minecraft:entity.player.burp` |

Les sous-titres ont une clé de traduction (`subtitles.create_belgian_snacks.*`).

## Traductions

- `en_us` = référence, **générée par datagen** (Registrate `lang`).
- `fr_fr` = fichier généré aussi (provider de lang séparé) ou fichier manuel dans `src/main/resources` si Registrate ne gère qu'une langue — à décider au jalon M1 selon ce qui est le plus simple.
- Test : au jalon M1, vérifier qu'aucune clé brute (`item.create_belgian_snacks.xxx`) n'apparaît en jeu dans les deux langues.

### Tooltips d'humour (validés le 26/09/2026)

Affichés au format Create (« Maintenir [Maj] »), clé `item.create_belgian_snacks.<id>.tooltip.summary`.

| Item | FR | EN |
|---|---|---|
| Fricadelle | « Personne ne sait vraiment ce qu'il y a dedans. » | "Nobody really knows what's inside." |
| THE_Fricadelle | « Avec une spéciale, s'il vous plaît. » | "Make it a spéciale, please." |
| THE_FRICADELLE | « Elle contient littéralement tout. » | "It literally contains everything." |
