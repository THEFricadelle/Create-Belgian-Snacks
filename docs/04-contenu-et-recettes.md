# 04 — Contenu & recettes

Toutes les quantités et durées sont des **brouillons**. Celles marquées ⚠️ sont à valider par THEFricadelle ; les autres peuvent être ajustées librement pendant l'équilibrage.

Namespace : `create_belgian_snacks` (abrégé `bs:` ci-dessous).

## Items

| ID | EN | FR | Mangeable ? | Notes |
|---|---|---|---|---|
| `minced_pork` | Minced Pork | Hachis de porc | non | |
| `minced_beef` | Minced Beef | Hachis de bœuf | non | ⚠️ FD a déjà `farmersdelight:minced_beef` (présent dans Arcadia) → voir D12 |
| `minced_chicken` | Minced Chicken | Hachis de poulet | non | |
| `minced_mutton` | Minced Mutton | Hachis de mouton | non | |
| `minced_rabbit` | Minced Rabbit | Hachis de lapin | non | |
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
| `bs:fryable_meats` ⚠️ | viandes acceptées par le broyage générique (voir ci-dessous) |
| `bs:grinder/extra_foods` | `minecraft:cake` + ajouts manuels |
| `bs:grinder/blacklist` | nos pâtes et fricadelles, `minecraft:enchanted_golden_apple` ⚠️, items debug |
| `c:foods/raw_meat` (étendu) | on n'y ajoute rien, on le **lit** pour la compat |
| On ajoute nos items aux tags `c:` pertinents | `c:foods/cooked_meat` ← fricadelles, etc. |

## Recettes — Palier 1 : Fricadelle

| # | Machine | Entrée | Sortie | Notes |
|---|---|---|---|---|
| 1 | Crushing Wheels | `minecraft:porkchop` | 2× `minced_pork` | |
| 2 | Crushing Wheels | `minecraft:beef` | 2× `minced_beef` + 50 % `beef_tallow` | |
| 3 | Crushing Wheels | `minecraft:chicken` | 2× `minced_chicken` | |
| 4 | Crushing Wheels | `minecraft:mutton` | 2× `minced_mutton` | |
| 5 | Crushing Wheels | `minecraft:rabbit` | 1× `minced_rabbit` | |
| 5b | Crushing Wheels | viandes crues FD (tag `c:foods/raw_meat` hors vanilla) | selon le cas ⚠️ | Si FD présent : FD a déjà du bœuf haché (`minced_beef`) → **utiliser leur item via tag**, ne pas dupliquer |
| 6 | Crushing Wheels | `minecraft:bread` | 3× `bread_crumbs` | |
| 7 | Mixer (non chauffé) | 1 hachis porc + 1 bœuf + 1 poulet + 1 chapelure | 2× `fricadelle_paste` | ⚠️ exiger 3 viandes *différentes* quelconques ? (voir décisions) |
| 8 | Mechanical Press | `fricadelle_paste` | `raw_fricadelle` | « extrusion » |
| 9 | **Friteuse** (chauffée) | `raw_fricadelle` | `fricadelle` | 100 ticks, 10 mB d'huile |

### Huile de friture

| Machine | Entrée | Sortie |
|---|---|---|
| Mixer **chauffé** | 2× `beef_tallow` | 250 mB `frying_oil` |
| Compacting (Basin + Press) | 8× graines (tag `c:seeds`) | 100 mB `frying_oil` |

## Recettes — Palier 2 : THE_Fricadelle

### Ingrédients intermédiaires

| Machine | Entrée | Sortie | Compat |
|---|---|---|---|
| Milling (Millstone) | `minecraft:dried_kelp` + … ⚠️ | `belgian_spices` | recette à inventer ensemble |
| Mixer | 1 œuf (`c:eggs`) + 100 mB `frying_oil` | 250 mB `mayonnaise` | |
| Mixer **chauffé** | 1 tomate (`c:crops/tomato`) + 1 sucre + 1 `belgian_spices` | 250 mB `curry_ketchup` | si tag tomate vide → **betterave** en secours |
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
2. Mixer **super-chauffé** : `absolute_paste` + `the_fricadelle` + 1000 mB `frying_oil` → `raw_ultimate_fricadelle`. ⚠️
3. **Friteuse** **super-chauffée** : → `ultimate_fricadelle`, 600 ticks, 250 mB.

## Dans Arcadia V2

Farmer's Delight est présent : les tags `c:crops/onion` et `c:crops/tomato` sont remplis, donc ce sont les recettes « normales » qui s'activent. Les recettes de secours servent hors du pack. Avant de figer les recettes de mayonnaise, curry ketchup et huile : vérifier (export M5) si Burger Mod, Cook's Collection ou Create: Food en fournissent déjà. Voir `11-compat-arcadia.md`.

## Recettes de secours (sans compat)

Chaque recette qui lit un tag `c:` potentiellement vide a une jumelle protégée par la condition `neoforge:tag_empty` qui utilise un ingrédient vanilla. Le datagen doit générer les deux.
