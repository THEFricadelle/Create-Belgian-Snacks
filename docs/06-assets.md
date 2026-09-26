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
- Fluides : `still` et `flow` 16×16 (le `flow` en 16×32 animé si possible, sinon statique) + fichier `.mcmeta`.
- Blocs : 6 faces unies avec bordure, 2 couleurs distinctes pour la Friteuse (inox) et le Hachoir (laiton).
- Le script **n'écrase jamais** un fichier existant sans `--force` (pour ne pas écraser les vraies textures).

## Emplacements

```
src/main/resources/assets/create_belgian_snacks/
├── textures/item/<id>.png
├── textures/block/fryer/{side,top,bottom,inner}.png
├── textures/block/supreme_grinder/{side,top,bottom,blade}.png
├── textures/fluid/<fluid>_{still,flow}.png (+ .mcmeta)
├── sounds/  (ogg)
└── sounds.json   # généré par datagen si possible
```
Les modèles JSON d'items/blocs sont générés par datagen. Quand un modèle Blockbench arrive, il va dans `src/main/resources/assets/.../models/block/` et le datagen le référence au lieu d'en générer un.

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
