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
| `models/block/custom/fryer.json` | Friteuse : une cuve ouverte sur quatre pieds (fond de cuve à y = 4). Le code dessine la graisse et les items flottants, x/z de 2 à 14 : dès la première goutte la graisse monte à y = 9, puis jusqu'à y = 15 cuve pleine ; jusqu'à 4 items flottent côte à côte et frémissent pendant la friture. Laisser cet intérieur vide. Livré le 27/09/2026 |
| `models/block/custom/supreme_grinder.json` | (Livré le 27/09/2026 : quatre parois pleine hauteur autour d'une fosse profonde, fond à y = 2, la jauge sur les quatre faces.) Le code dessine le hachis (`supreme_grinder/mince`) dans la fosse, x/z de 2 à 14, de y = 2 à y = 10 selon la progression vers l'objectif : laisser la fosse vide sous les lames. Carter du Hachoir, **sans les lames**. Ses faces qui montrent la jauge utilisent la variable de texture `#side` : le datagen crée les 5 niveaux en ne changeant que `#side` (vers `side_0` … `side_4`). Arbre par le haut : laisser le centre du dessus ouvert pour l'arbre de Create |
| `models/block/custom/supreme_grinder_blades.json` | Les lames seules. Le code les fait tourner autour de l'axe vertical passant par le centre du bloc (8, y, 8) : modéliser centré sur ce point. Livré le 27/09/2026 : deux lames opposées et courbées (3 segments chacune, tournés de 0, 22,5 et 45°) sur un moyeu, à y = 11-12 (moyeu de 10,5 à 12,5) |

Rien d'autre à faire : blockstates, modèles d'items, variantes de jauge, rotation des lames et rendu de la graisse sont générés ou codés. Le PNJ THEFricadelle n'a besoin d'aucune texture (skin du compte, en ligne).

## Sons

Chaque son joue l'enregistrement déposé dans `src/main/resources/assets/create_belgian_snacks/sounds/` dès que le fichier existe (après `./gradlew runData`), le son vanilla de la dernière colonne en attendant.

| ID | Usage | Fichier à déposer | En attendant |
|---|---|---|---|
| `fryer.sizzle` | toutes les 2 s pendant la friture (environ 2 s) | `fryer/sizzle.ogg` | lave, feu de camp |
| `grinder.grind` | aliment accepté (court, moins d'1 s) | `grinder/grind.ogg` | meule |
| `grinder.complete` | pâte terminée (1 à 2 s) | `grinder/complete.ogg` | niveau gagné |
| `grinder.running` | toutes les 2 s tant que le hachoir tourne assez vite, plus aigu quand il tourne plus vite (environ 2 s, joué côté client) | `grinder/running.ogg` | wagonnet |
| `fricadelle.burp` | rot en mangeant (environ 1 s) | `fricadelle/burp.ogg` | rot du joueur |
| `npc.phrase.0` … `npc.phrase.4` | voix de THEFricadelle, une par phrase (texte dans `BSLang`, clés `npc.phrase.N`) | `npc/phrase_N.ogg` | grognement de villageois |

**Provenance** (27/09/2026) : les cinq bruitages viennent de [BigSoundBank](https://bigsoundbank.com), sous licence CC0 (domaine public : aucun crédit requis, usage commercial permis). Découpés, fondus et normalisés (crête à -1,5 dB) avec ffmpeg :

| Fichier | Source | Découpe |
|---|---|---|
| `fryer/sizzle.ogg` | [Frying bath #2](https://bigsoundbank.com/frying-bath-2-s2506.html) (bain de friture de churros) | 2,2 s à partir de 12 s, la friture stable |
| `grinder/grind.ogg` | [Raw carrot crunched #1](https://bigsoundbank.com/raw-carrot-crunched-1-s1594.html) | un seul croc, 0,35 s à partir de 0,95 s |
| `grinder/complete.ogg` | [Microwave Bell](https://bigsoundbank.com/microwave-bell-s1631.html) | 1,6 s, fin en fondu |
| `fricadelle/burp.ogg` | [Burp #3](https://bigsoundbank.com/burp-3-s1709.html) | entier (0,44 s) |
| `grinder/running.ogg` | [Electric hand mixer #1](https://bigsoundbank.com/electric-hand-mixer-1-s1752.html) (batteur dans une pâte à crêpes) | 2,2 s à partir de 3 s, joué plus grave (hauteur 0,6 à 1 selon la vitesse) |

La voix du PNJ est enregistrée par l'auteur (`npc/phrase_4.ogg` le 27/09/2026).

**Format** : tous en **Ogg Vorbis mono**. Un MP3 renommé ne se lit pas : `ffmpeg -i in.mp3 -ac 1 -c:a libvorbis -q:a 5 out.ogg`. `python tools/asset_status.py` liste ce qui est fait, et signale un fichier stéréo, un MP3 renommé ou un nom qu'aucun son n'utilise.

**Voix du PNJ** : enregistrer chaque phrase dans `sounds/npc/phrase_0.ogg` … `phrase_4.ogg`, en **Ogg Vorbis mono** (un son stéréo n'est pas positionné dans le monde ; Audacity : piste mono, Exporter > Ogg Vorbis). Puis `./gradlew runData` : le datagen prend l'enregistrement à la place du grognement. `python tools/asset_status.py` dit quelles phrases sont faites et signale un fichier stéréo ou pas en Vorbis. Les phrases : 0 « Une fricadelle, une ! », 1 « Tu as tout mangé. Tout. », 2 « Mayo ou curry ketchup ? Les deux, évidemment. », 3 et 4 : voir `BSLang` (la 3 cite le nombre d'aliments du pack ; la voix peut dire « tous les aliments » sans chiffre).

Sons vanilla joués par le PNJ, référencés et non copiés : fruit de chorus à l'apparition, lancement de fusée au décollage, explosion et feu d'artifice quand il éclate.

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
