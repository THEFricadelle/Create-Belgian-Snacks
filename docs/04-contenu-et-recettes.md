# 04 — Contenu & recettes

Toutes les quantités et durées sont des **brouillons**. Celles marquées ⚠️ sont à valider par THEFricadelle ; les autres peuvent être ajustées librement pendant l'équilibrage.

Namespace : `create_belgian_snacks` (abrégé `bs:` ci-dessous).

## Items

| ID | EN | FR | Mangeable ? | Notes |
|---|---|---|---|---|
| `minced_pork` | Minced Pork | Hachis de porc | non | |
| `minced_beef` | Minced Beef | Hachis de bœuf | non | Toujours enregistré (D12). Les recettes lisent `bs:minced_meats/beef` = le nôtre + `farmersdelight:minced_beef` (optionnel) |
| `minced_chicken` | Minced Chicken | Hachis de poulet | non | |
| `beef_tallow` | Beef Tallow | Blanc de bœuf | non | Sous-produit du bœuf |
| `bread_crumbs` | Bread Crumbs | Chapelure | non | |
| `belgian_spices` | Belgian Spices | Épices belges | non | |
| `fricadelle_paste` | Fricadelle Paste | Pâte à fricadelle | non | |
| `raw_fricadelle` | Raw Fricadelle | Fricadelle crue | non ⚠️ | Mangeable avec malus ? |
| `fricadelle` | Fricadelle | Fricadelle | **oui** | Palier 1 |
| `exceptional_paste` | Exceptional Paste | Pâte d'exception | non | Sortie Hachoir mode THE_ |
| `incomplete_the_fricadelle` | Incomplete THE_Fricadelle | THE_Fricadelle en cours | non | Item de transition (Sequenced Assembly) |
| `raw_the_fricadelle` | Raw THE_Fricadelle | THE_Fricadelle crue | non | |
| `the_fricadelle` | THE_Fricadelle | THE_Fricadelle | **oui** | Palier 2 |
| `absolute_paste` | Absolute Paste | Pâte absolue | non | Sortie Hachoir mode ULTIME |
| `raw_ultimate_fricadelle` | Raw THE_FRICADELLE | THE_FRICADELLE crue | non | |
| `ultimate_fricadelle` | THE_FRICADELLE | THE_FRICADELLE | **oui** | Palier 3, rareté EPIC, effet brillant |

Les noms des 3 fricadelles sont **identiques dans toutes les langues** (c'est une marque).

## Fluides (+ seau)

| ID | EN | FR | Couleur indicative |
|---|---|---|---|
| `frying_oil` | Frying Oil | Huile de friture | jaune doré `#E8C547` |
| `melted_beef_tallow` | Melted Beef Tallow | Blanc de bœuf fondu | blanc `#FBFAF4` ; seule graisse acceptée par THE_FRICADELLE |
| `mayonnaise` | Mayonnaise | Mayonnaise | crème `#F3EBC4` |
| `curry_ketchup` | Curry Ketchup | Curry ketchup | rouge-brun `#A8321E` |

## Blocs

| ID | EN | FR | Voir |
|---|---|---|---|
| `fryer` | Fryer | Friteuse | `05-machines.md` |
| `supreme_grinder` | Supreme Grinder | Hachoir Suprême | `05-machines.md` |

## Tags

| Tag | Contenu |
|---|---|
| `bs:minced_meats` | tous nos hachis |
| `bs:minced_meats/pork` | `minced_pork` + `#c:ground_pork` (optionnel) : lu par les recettes à la place de l'item |
| `bs:minced_meats/beef` | `minced_beef` + `#c:ground_beef` + `#c:minced_beef` (optionnels) (D12) |
| `bs:minced_meats/chicken` | `minced_chicken` + `#c:ground_chicken` (optionnel) |
| `bs:frying_oils` (fluide) | `frying_oil` + `melted_beef_tallow` + `#c:plantoil` + `#c:vegetable_oil` (optionnels) : toute graisse des paliers 1 et 2 (D13) |
| `bs:grinder/extra_foods` | `minecraft:cake` + ajouts manuels |
| `bs:grinder/blacklist` | nos pâtes et fricadelles, `minecraft:enchanted_golden_apple` ⚠️, items debug |
| `c:foods/raw_meat` (étendu) | on n'y ajoute rien, on le **lit** pour la compat |
| On ajoute nos items aux tags `c:` pertinents | `c:foods/cooked_meat` ← fricadelles ; `c:ground_pork`, `c:ground_beef`, `c:minced_beef`, `c:ground_chicken` ← nos hachis ; `c:bread_crumbs` ← chapelure ; `c:buckets` ← nos seaux. Les recettes des autres mods (Create: Food, FD) acceptent ainsi les nôtres |

## Recettes — Palier 1 : Fricadelle

| # | Machine | Entrée | Sortie | Notes |
|---|---|---|---|---|
| 1 | Crushing Wheels | `minecraft:porkchop` | 2× `minced_pork` | |
| 2 | Crushing Wheels | `minecraft:beef` | 2× `minced_beef` + 50 % `beef_tallow` | |
| 3 | Crushing Wheels | `minecraft:chicken` | 2× `minced_chicken` | |
| 6 | Crushing Wheels | `minecraft:bread` | 3× `bread_crumbs` | |
| 7 | Mixer (non chauffé) | `#bs:minced_meats/pork` + `#bs:minced_meats/beef` + `#bs:minced_meats/chicken` + `#c:bread_crumbs` | 2× `fricadelle_paste` | 3 viandes fixes (D3) ; accepte le haché et la chapelure de n'importe quel mod |
| 8 | Mechanical Press | `fricadelle_paste` | `raw_fricadelle` | « extrusion » |
| 9 | **Friteuse** (chauffée) | `raw_fricadelle` | `fricadelle` | 100 ticks, 10 mB de `#bs:frying_oils` (M3) |

IDs des recettes livrées au M2 : `crushing/porkchop`, `crushing/beef`, `crushing/chicken`, `crushing/bread`, `mixing/fricadelle_paste`, `pressing/fricadelle_paste` (namespace `create_belgian_snacks`).

### Graisses de friture

| ID | Machine | Entrée | Sortie | Condition |
|---|---|---|---|---|
| `mixing/melted_beef_tallow` | Mixer **chauffé** | 2× `beef_tallow` | 250 mB `melted_beef_tallow` | aucune |
| `compacting/frying_oil_from_seeds` | Compacting | 8× `#c:seeds` | 100 mB `frying_oil` | ni Create Crafts & Additions ni Create Diesel Generators chargés : ils compactent déjà `#c:seeds` en leur propre huile, que `#bs:frying_oils` accepte |

Le blanc de bœuf fondu est la graisse des vraies frites belges : il sert aux paliers 1 et 2 comme n'importe quelle huile, et c'est la **seule** graisse acceptée pour THE_FRICADELLE (palier 3).

## Recettes — Palier 2 : THE_Fricadelle

### Ingrédients intermédiaires

| Machine | Entrée | Sortie | Compat |
|---|---|---|---|
| `milling/dried_kelp` : Meule | 1 `minecraft:dried_kelp` | `belgian_spices` + 25 % `belgian_spices` | provisoire (D6) |
| `mixing/mayonnaise` : Mixer | 1 œuf (`c:eggs`) + 100 mB `#bs:frying_oils` | 250 mB `mayonnaise` | toute huile ou le blanc de bœuf fondu |
| `mixing/curry_ketchup` : Mixer **chauffé** | 1 tomate (`c:crops/tomato`) + 1 sucre + 1 `belgian_spices` | 250 mB `curry_ketchup` | condition : tag tomate non vide |
| `mixing/curry_ketchup_from_beetroot` : Mixer **chauffé** | 1 betterave + 1 sucre + 1 `belgian_spices` | 250 mB `curry_ketchup` | secours si tag tomate vide (D7, provisoire) |
| — | oignons : tag `c:crops/onion` | — | si tag vide → **betterave** ⚠️ en secours |

### Chaîne

1. **Hachoir Suprême**, mode *THE_* : quand `theFricadelleRatio` des aliments est atteint → 1× `exceptional_paste` + remise à zéro de la progression. ⚠️ taux à définir.
2. **Sequenced Assembly** (sur tapis, Create natif) :
   - Entrée : `exceptional_paste` — Item de transition : `incomplete_the_fricadelle`
   - Séquence :
     1. Deployer : `belgian_spices`
     2. Spout : 100 mB `mayonnaise`
     3. Spout : 100 mB `curry_ketchup`
     4. Deployer : oignon (`c:crops/onion`)
     5. Press
   - Boucles : **3** ⚠️
   - Résultat : `raw_the_fricadelle` (taux de réussite ⚠️ 100 % ? ou 80 % + « pâte ratée » en sortie de repli)
3. **Friteuse** (chauffée) : `raw_the_fricadelle` → `the_fricadelle`, 200 ticks, 25 mB.

## Recettes — Palier 3 : THE_FRICADELLE

1. **Hachoir Suprême**, mode *ULTIME* : 100 % des aliments du FoodIndex → 1× `absolute_paste`.
2. Mixer **super-chauffé** : `absolute_paste` + `the_fricadelle` + 1000 mB `melted_beef_tallow` → `raw_ultimate_fricadelle`. ⚠️
3. **Friteuse** **super-chauffée** : → `ultimate_fricadelle`, 600 ticks, 250 mB de `melted_beef_tallow` uniquement (aucune autre huile).

## Dans Arcadia V2

Farmer's Delight est présent : les tags `c:crops/onion` et `c:crops/tomato` sont remplis, donc ce sont les recettes « normales » qui s'activent. Les recettes de secours servent hors du pack. Aucune mayonnaise ni aucun ketchup dans le pack (vérifié au M2) ; les huiles existantes passent par `#bs:frying_oils`. Voir `11-compat-arcadia.md`.

## Recettes de secours (sans compat)

Chaque recette qui lit un tag `c:` potentiellement vide a une jumelle protégée par la condition `neoforge:tag_empty` qui utilise un ingrédient vanilla. Le datagen doit générer les deux.
