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
| `incomplete_ultimate_fricadelle` | Incomplete THE_FRICADELLE | THE_FRICADELLE en cours | non | Item de transition (Sequenced Assembly du palier 3) |
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
| — | oignons : tag `c:crops/onion` | — | si tag vide → **betterave** en secours (D7) |

### Chaîne

1. **Hachoir Suprême**, mode *THE_* : quand `theFricadelleRatio` des aliments est atteint → 1× `exceptional_paste` + remise à zéro de la progression. ⚠️ taux à définir.
2. **Sequenced Assembly** (sur tapis, Create natif) :
   - Entrée : `exceptional_paste` — Item de transition : `incomplete_the_fricadelle`
   - Séquence :
     1. Deployer : `belgian_spices`
     2. Spout : 100 mB `mayonnaise`
     3. Spout : 100 mB `curry_ketchup`
     4. Deployer : oignon (`c:crops/onion`) ; sans aucun oignon, betterave (variante `sequenced_assembly/raw_the_fricadelle_from_beetroot`, D7)
     5. Press
   - Boucles : **3** (D4) : 3 épices, 300 mB de mayonnaise, 300 mB de curry ketchup et 3 oignons par THE_Fricadelle
   - Résultat : `raw_the_fricadelle`, **100 %** de réussite (D5)
3. **Friteuse** (chauffée) : `raw_the_fricadelle` → `the_fricadelle`, 200 ticks, 25 mB de n'importe quelle graisse `#frying_oils` par item, par lot de 16 (D20).

## Recettes — Palier 3 : THE_FRICADELLE

1. **Hachoir Suprême**, mode *ULTIME* : 100 % des aliments du FoodIndex → 1× `absolute_paste`.
2. **Sequenced Assembly** `sequenced_assembly/raw_ultimate_fricadelle` (décidée le 27/09/2026, remplace le Mixer et ne demande plus de THE_Fricadelle) :
   - Entrée : `absolute_paste` — Item de transition : `incomplete_ultimate_fricadelle`
   - Séquence : Spout 250 mB `melted_beef_tallow` → Deployer `belgian_spices` → Spout 250 mB `mayonnaise` → Spout 250 mB `curry_ketchup` → Deployer oignon (`c:crops/onion`, betterave sans aucun oignon : `…_from_beetroot`, D7) → Press
   - Boucles : **5** : 1250 mB de chaque fluide, 5 épices et 5 oignons ; **100 %** de réussite
3. **Friteuse** **super-chauffée** (`frying/ultimate_fricadelle`) : → `ultimate_fricadelle`, 600 ticks, 250 mB de `melted_beef_tallow` uniquement (aucune autre huile), une pièce à la fois.

## v2 — Frites belges (M11, 25/10/2026)

Détail et schéma : `13-cahier-des-charges-v2.md`, section 5.1.

| ID de recette | Machine | Entrée | Sortie |
|---|---|---|---|
| `cutting/raw_fries` | Mechanical Saw | 1 `#c:crops/potato` | 4 `raw_fries` |
| `frying/blanched_fries` | Friteuse **chauffée**, 100 ticks, 5 mB `#frying_oils` par item | `raw_fries` | `blanched_fries` |
| `frying/fries` | Friteuse **super-chauffée**, 60 ticks, 5 mB `#frying_oils` | `blanched_fries` | `fries` |
| `frying/fries_beef_tallow` | Friteuse **super-chauffée**, 60 ticks, 5 mB `melted_beef_tallow` seul | `blanched_fries` | `fries_beef_tallow` |
| `crafting/paper_cone` | Établi (ou Mechanical Crafter) : 3 papiers en V | `P P` / ` P ` | 4 `paper_cone` |
| `deploying/cone_of_fries` | Deployer tenant un `paper_cone` (consommé) | 1 `#create_belgian_snacks:fries` | `cone_of_fries` |
| `deploying/cone_of_fries_beef_tallow` | Deployer tenant un `paper_cone` | 1 `fries_beef_tallow` | `cone_of_fries_beef_tallow` |

- Les deux recettes de 2e cuisson acceptent le blanc de bœuf : la Friteuse prend la plus spécifique (voir `05-machines.md`). Avec une autre huile : frites normales.
- Le cornet se fait à l'établi et non à la presse : dans Arcadia, `create_things_and_misc` presse déjà le papier en ticket vide (`create:pressing` sur `minecraft:paper`), une 2e recette de presse sur le même item serait en conflit.
- Tags : `create_belgian_snacks:fries` = `fries` + `burgermod:fries` (optionnel, Arcadia) ; les frites au blanc n'y sont pas, pour ne jamais finir dans un cornet normal. `c:foods/fries` (dans `c:foods`) = les frites et les deux cornets.
- Les frites et les cornets sont des aliments finis : ils **comptent** pour le Hachoir Suprême (V-D1), donc ne sont pas dans `grinder/blacklist`. Les intermédiaires ne se mangent pas.

## v2 — Sauces et cornets sauce (M12, 25/10/2026)

Détail : `13-cahier-des-charges-v2.md`, section 5.2. La sauce lapin arrive au M13, avec le sirop de Liège dont elle a besoin.

| ID de recette | Mixer | Entrées | Sortie | Jumelle de secours |
|---|---|---|---|---|
| `mixing/andalouse` | froid | 250 mB `mayonnaise` + 1 tomate (`c:crops/tomato`) + 1 `belgian_spices` | 250 mB `andalouse` | `mixing/andalouse_from_beetroot` (betterave) si aucune tomate |
| `mixing/samourai` | **chauffé** | 250 mB `mayonnaise` + 1 `#create_belgian_snacks:hot_peppers` + 1 `belgian_spices` | 250 mB `samourai` | `mixing/samourai_from_blaze_powder` (poudre de blaze, V-D5) si aucun piment |
| `mixing/americaine` | froid | 250 mB `mayonnaise` + 50 mB `curry_ketchup` + 1 oignon (`c:crops/onion`) | 300 mB `americaine` | `mixing/americaine_from_beetroot` (betterave) si aucun oignon |

- `#create_belgian_snacks:hot_peppers` : `mynethersdelight:bullet_pepper` (Arcadia) et `#c:crops/chili_pepper`, optionnels.
- Tag fluide `create_belgian_snacks:sauces` : mayonnaise, curry ketchup, andalouse, samouraï, américaine (sources et écoulements). Aucune sauce n'est une graisse de friture.
- **Cornets sauce** : `filling/cone_of_fries_<sauce>` et `filling/cone_of_fries_beef_tallow_<sauce>`, un Spout verse 50 mB de la sauce sur le cornet nature. 10 items, comptés par le Hachoir (V-D1).

## v2 — Ingrédients de base (M13, 25/10/2026)

Détail : `13-cahier-des-charges-v2.md`, section 5.7, et les trois règles de bassin dans `03-architecture.md`. Les fours sont des ventilateurs de Create sur un feu de camp (recettes vanilla `smoking`, que le fumage en masse de Create exécute) ; les pains cuisent aussi au four.

| Ingrédient | Recette (ID) |
|---|---|
| `pearl_sugar` | Compacting 4 sucre → 2 (`compacting/pearl_sugar`) |
| `cassonade` | Mixer chauffé 2 sucre → 1 (`mixing/cassonade`) |
| `powdered_sugar` | Meule 1 sucre → 1 (`milling/powdered_sugar`), coupée avec Create: Food (dont le sucre glace entre dans `#powdered_sugars`) |
| `salt` | Mixer chauffé 500 mB d'eau + 1 algue → 1 (`mixing/salt`), coupée quand `#c:salt` est rempli (Create: Food dans Arcadia) |
| fluide `cream` | Mixer 1000 mB de lait + 1 `#salts` → 250 mB (`mixing/cream`) |
| `butter` | Mixer 250 mB de crème + 1 `#salts` → 2 (`mixing/butter`), coupée quand `#c:butter` est rempli |
| `young_herve_cheese` → `herve_cheese` | Compacting 1000 mB de lait + 1 `#salts` ; lavage au ventilateur (`compacting/young_herve_cheese`, `splashing/herve_cheese`) |
| `maton` | Mixer 500 mB de lait + 50 mB de vinaigre + 1 `#salts` (`mixing/maton`) |
| fluide `vinegar` | Mixer 3 pommes + 250 mB d'eau, 1200 ticks → 250 mB (`mixing/vinegar`) |
| fluide `liege_syrup` | Mixer chauffé 3 pommes + 1 sucre + 1 baie → 250 mB (`mixing/liege_syrup`) |
| fluide `bechamel` | Mixer chauffé 500 mB de lait + 1 farine + 1 `#butters` + 1 épices → 500 mB (`mixing/bechamel`) |
| fluide `sauce_lapin` | Mixer chauffé 250 mB de sirop de Liège + 1 oignon + 1 épices → 250 mB ; jumelle à la betterave (`mixing/sauce_lapin`, `…_from_beetroot`) ; 2 cornets sauce lapin de plus |
| `brewers_yeast` | Mixer 1 pain + 1 sucre + 250 mB d'eau → 2 (`mixing/brewers_yeast`) |
| `sprouted_grain` → `malt` | Lavage au ventilateur 1 `#malting_grains` (blé, orge) ; fumage (`splashing/sprouted_grain`, `smoking/malt`) |
| `pistolet_dough` → `pistolet` | Scie 1 `create:dough` → 2 ; fumage ou four (`cutting/pistolet_dough`, `smoking/pistolet`, `smelting/pistolet`) |
| `puff_pastry` | Compacting 1 `create:dough` + 1 `#butters` (`compacting/puff_pastry`) |
| `brioche_dough` | Mixer 1 `create:dough` + 1 `#butters` + 1 œuf + 1 sucre + 1 levure → 2 (`mixing/brioche_dough`) |
| `burger_bun_dough` → `burger_bun` | Scie 1 `brioche_dough` → 2 ; fumage ou four |
| `pita_dough` → `raw_pita` → `pita_bread` | Mixer 1 `create:dough` + 1 levure → 2 ; presse ; fumage ou four |
| `noodles` | Mixer 1 `create:dough` + 1 œuf → 2 (`mixing/noodles`) |
| `lardons` | Scie 1 côtelette de porc → 3 (`cutting/lardons`) |
| `brined_ham` → `ardennes_ham` | Mixer 1 côtelette + 1 `#salts` ; fumage (`mixing/brined_ham`, `smoking/ardennes_ham`) |
| `raw_meringue` → `meringue` | Mixer 1 œuf + 1 sucre → 2 ; fumage |
| `raisins` | Fumage d'un raisin (`#grapes`) ; jumelle aux baies sucrées sans raisin (`smoking/raisins`, `…_from_sweet_berries`) |
| `apple_compote` | Mixer chauffé 3 pommes + 1 sucre + 1 bol (`mixing/apple_compote`), rend le bol |
| `chocolate_shavings` | Concassage 1 barre de chocolat de Create → 2 (`crushing/chocolate_shavings`) |
| `vanilla_ice_cream` | Compacting 250 mB de crème + 1 sucre + 1 glace → 2 (`compacting/vanilla_ice_cream`) |

Se mangent tels quels (comptés par le Hachoir) : fromage de Herve (nausée 3 s : son odeur), pistolet, jambon d'Ardenne, meringue, raisins secs, compote, glace vanille. La moutarde et la farine de sarrasin arrivent avec leurs cultures (M14).

## v2 — Cultures et moules (M14, 25/10/2026)

Détail : `13-cahier-des-charges-v2.md`, section 5.8.

| Bloc | Planté avec | Pousse | Récolte mûre |
|---|---|---|---|
| `chicon_crop` (âges 0-3) | `chicory_root` | **seulement dans le noir** (lumière ≤ `crops.darkMaxLight`, 4) ; reste planté à la lumière sans pousser | 2-3 `chicon` + 50 % racine |
| `leek_crop` (0-7) | `leek` | normale | 1-3 `leek` |
| `hops_crop` (0-7) | `hop_seeds` | normale | 1-2 `hops`, 1 graine, 50 % `hop_shoots` |
| `brussels_sprouts_crop` (0-7) | `brussels_sprout` | normale | 2-4 `brussels_sprout` |
| `asparagus_crop` (0-3) | `asparagus_crown` | partout ; chaque pas retient s'il a poussé dans le noir | `white_asparagus` (noir) ou `green_asparagus` (lumière), 1-2, + 25 % griffe |
| `buckwheat_crop` (0-7) | `buckwheat` | normale | 2-3 `buckwheat` |
| `mustard_crop` (0-7) | `mustard_seeds` | normale | 2-4 `mustard_seeds` |
| `bouchot` (0-3, immergé, sans collision) | établi : 3 bâtons, 1 ficelle, 1 moule (`crafting/bouchot`) | tick aléatoire, seulement sous l'eau | 2-4 `mussel` (clic droit ou Harvester), le pieu repart à 0 |

- Pas mûre, une culture rend ce qui l'a plantée. Le Mechanical Harvester de Create récolte les sept cultures et le bouchot mûrs et les replante ; récolté, le bouchot ne donne que des moules (le pieu ne tombe que pour un joueur, sinon il se dupliquerait).
- Graines : 1 herbe haute ou courte sur ~7 en donne une des sept (`loot_table/gameplay/wild_seeds`, modificateur `neoforge:add_table`) ; 1 prise de pêche sur 20 une moule (`gameplay/fishing_mussels`), en tout biome.
- Transformations : Meule `buckwheat` → `buckwheat_flour`, `mustard_seeds` → `mustard_powder` ; Mixer 2 poudres de moutarde + 100 mB de vinaigre + 100 mB d'eau → 250 mB fluide `mustard`.
- Tags : produits dans `c:crops` et `c:crops/<nom>`, graines dans `c:seeds` et `c:seeds/<nom>`. Rien ne se mange cru.
- Botany Pots : `recipe/botanypots/<culture>.json`, `botanypots:block_derived_crop`, chargées seulement avec le mod.

## v2 — Bières de cuisine (M15, 25/10/2026)

Fluides de cuisine seulement : aucune bouteille, aucun effet. Le Mixer de Create respecte la durée de la recette, mise à l'échelle par sa vitesse (`03-architecture.md`).

| Fluide | Mixer | Durée |
|---|---|---|
| `wort` (moût) | chauffé : 2 `malt` + 1 `hops` + 1000 mB d'eau → 1000 mB | normale |
| `blonde_beer` | 1000 mB de moût + 1 levure → 1000 mB | 600 |
| `brown_beer` | 1000 mB de moût + 1 cassonade + 1 levure → 1000 mB (plus d'items que la blonde : avec de la cassonade, Create fait la brune) | 600 |
| `gueuze` | 1000 mB de moût + 1 blé (le lambic se brasse avec du froment non malté, sans levure) → 1000 mB | 2400 |
| `kriek` | 1000 mB de gueuze + 4 cerises (`#cherries`) → 1000 mB ; jumelle aux baies sucrées sans cerise | 600 |
| vinaigre de bière | 1000 mB de bière blonde + 1 pomme (la « mère » du vinaigre) → 1000 mB de vinaigre (`mixing/vinegar_from_beer`) | 2400 |

Tag fluide `create_belgian_snacks:beers` : les quatre bières.

## v2 — Snacks de friterie (M16, 25/10/2026)

Aucun œuf et aucun oignon dans nos bassins : Create: Food change un œuf seul en œuf liquide dans n'importe quel bassin, et chaque oignon demanderait une jumelle à la betterave. Toute friture : Friteuse chauffée, 100 ticks, 10 mB de `#frying_oils` par pièce.

| Sauce (Mixer) | Recette |
|---|---|
| `tartare` | 250 mB mayonnaise + 50 mB pickles + 1 épices → 300 mB |
| `pickles` | chauffé : 250 mB vinaigre + 50 mB moutarde + 1 carotte + 1 chou de Bruxelles + 1 épices → 250 mB |
| `brazil` | 250 mB mayonnaise + 50 mB curry ketchup + 1 ananas (`#pineapples`) → 300 mB ; jumelle à la tranche de pastèque |

Les trois rejoignent `#sauces` et les cornets : 6 cornets sauce de plus (`filling/cone_of_fries_<sauce>`, `..._beef_tallow_<sauce>`).

| Snack | Chaîne |
|---|---|
| `mitraillette` | Scie : pain → 2 `half_baguette` ; assemblage (`incomplete_mitraillette`) : Deployer `#mitraillette_meats` → Deployer `#fries` → Spout 100 mB `#sauces` |
| `boulet`, `boulets_liegeois` | Mixer : porc + bœuf + chapelure + épices + sel → 2 `boulet_paste` ; Presse → `raw_boulet` ; Friteuse ; Mixer chauffé : 2 boulets + 250 mB sauce lapin + 1 bol → `boulets_liegeois` (rend le bol) |
| croquettes | Mixer : 2 crevettes (`#shrimps`, jumelle 2 cabillauds) + 250 mB béchamel ; 1 fromage (`c:foods/cheese`) + 250 mB béchamel ; 2 poulet haché + 250 mB béchamel ; lucifer : porc + 50 mB samouraï + 100 mB béchamel ; pommes de terre, chauffé : 2 pommes de terre + beurre + sel. Chaque appareil sort par 2 |
| panure (8 snacks) | Assemblage partagé `incomplete_breaded` : Deployer œuf → Deployer chapelure → Presse. Entrées : les 5 appareils de croquette, `chicken_strips` (scie : poulet → 3), `fish_strips` (scie : cabillaud ou saumon → 3), `cheese_strips` (scie : fromage → 3) |
| `cervelas`, `mexicano`, `viandelle` | Mixer : porc + bœuf + épices ; 2 bœuf + 50 mB samouraï + épices ; porc + poulet + chapelure + épices + sel → mix ×2 ; Presse → cru ; Friteuse |
| `loempia` | Presse : `create:dough` → `loempia_wrapper` ; assemblage : Deployer poulet haché → poireau → carotte → Presse |
| `filet_americain` | Mixer : 2 bœuf + épices + 100 mB mayonnaise + 25 mB moutarde → 2 ; Deployer sur un pistolet → `pistolet_americain` ; Deployer `#fries` dessus → `americain_frites` |
| burgers | Presse : bœuf haché → `raw_patty` ; Friteuse → `patty` ; assemblage sur `burger_bun` : Deployer `patty` → Spout 50 mB pickles → Spout 50 mB `#sauces` → `friterie_burger` ; Deployer fromage dessus → `friterie_cheeseburger` |
| `hot_dog` | Presse : `burger_bun_dough` → `hot_dog_bun_dough` ; fumage ou four → `hot_dog_bun` ; assemblage : Deployer `#sausages` → Deployer `onion_rings` → Spout 50 mB `#sauces` |
| `pita` | Mixer chauffé : 2 porc + épices → 2 `gyros` ; assemblage sur `pita_bread` : Deployer `gyros` → Deployer tomate (jumelle betterave) → Spout 50 mB `#sauces` |
| `brochette` | Assemblage sur un bâton : Deployer côtelette crue → lardons → côtelette crue → `raw_brochette` ; Friteuse |
| `bami_disc`, `nasi_disc` | Mixer (froid) : 2 nouilles / 2 riz (`c:crops/rice`, jumelle 2 blés) + porc + poireau + épices → 2 mix ; Presse → galette crue ; Friteuse |
| `squid_rings` | Scie : calmar (`#squids` : `c:foods/raw_squid`, `c:foods/raw_calamari` de Cultural Delights) → 3 ; Friteuse. Sans calmar, pas de recette |
| `chicken_nuggets`, `chicken_crunch` | Mixer : 2 poulet + chapelure + sel → 4 nuggets crus ; scie : maïs (`c:crops/corn`) → 2 `corn_flakes` ; Mixer : 2 poulet + épices + 2 flocons → 2 croquants crus (sans maïs : 2 chapelure) ; Friteuse |
| ailes | Deployer qui **garde** ses cisailles (`keep_held_item`) sur un poulet cru → 2 ailes crues ; Friteuse → `fried_chicken_wings` ; Spout 25 mB samouraï → `spicy_chicken_wings` |
| `onion_rings` | Scie : oignon (jumelle betterave) → 3 rondelles ; Mixer : 3 rondelles + farine + 250 mB bière blonde → 3 en pâte ; Friteuse |

Tags : `mitraillette_meats` (fricadelle, boulet, cervelas, mexicano, viandelle, filet américain, chicken fingers, brochette), `sausages` (fricadelle, cervelas, mexicano, viandelle + `c:foods/sausage`), `shrimps`, `squids`, `pineapples`. Snacks finis dans `c:foods`, les viandes dans `c:foods/cooked_meat`, le poisson dans `c:foods/cooked_fish`.

## v2 — Plats belges (M17, 26/10/2026)

Les plats en bol prennent leur bol dans le bassin et le rendent quand on les mange (empilables par 16). `#onions` = `c:crops/onion` + notre poireau : jamais vide, donc aucune jumelle betterave. Fumage = ventilateur sur feu de camp, ou four.

| Plat | Machine | Recette |
|---|---|---|
| `moules_marinieres` / `moules_a_la_biere` | Mixer chauffé | 4 moules + poireau + `#onions` + bol + 250 mB d'eau / de bière blonde |
| `moules_frites` | Deployer | `#fries` sur l'un ou l'autre (`deploying/moules_frites`, `..._from_beer`) |
| `carbonnade` | Mixer chauffé | 2 bœuf cru + `#onions` + pistolet + cassonade + bol + 250 mB bière brune ; `#fries` dessus → `carbonnade_frites` |
| `stoemp` | Mixer chauffé | 2 pommes de terre + poireau + carotte + beurre + bol + 250 mB de lait ; `#sausages` dessus → `stoemp_saucisse` |
| `waterzooi` | Mixer chauffé | poulet cru + poireau + carotte + bol + 250 mB de lait |
| `lapin_a_la_gueuze` | Mixer chauffé | lapin cru + 2 `#prunes` (jumelle : baies sucrées) + `#onions` + bol + 250 mB gueuze |
| `brussels_sprouts_bacon` | Mixer chauffé | 4 choux de Bruxelles + `#lardons` + beurre + bol |
| `asparagus_flemish` | Mixer chauffé | 3 asperges blanches + beurre + épices + bol (sans œuf : règle 4 des bassins) |
| `hop_shoots_cream` | Mixer chauffé | 3 jets de houblon + bol + 250 mB crème |
| `salade_liegeoise` | Mixer froid | 2 pommes de terre cuites + `#lardons` + `#onions` + bol + 50 mB vinaigre |
| `hochepot` | Mixer chauffé | bœuf + côtelette + 2 choux de Bruxelles + carotte + poireau + bol + 500 mB d'eau |
| `escaveche` | Mixer froid, 600 ticks | poisson cuit (`c:foods/cooked_fish`) + `#onions` + bol + 100 mB vinaigre |
| `steak_frites` | Deployer | `#fries` sur un steak cuit |
| `boudin`, `boudin_compote` | Mixer froid : porc haché + chapelure + 250 mB lait → `raw_boudin` ; fumage → `boudin` ; Deployer compote dessus |
| `chicons_au_gratin` | Assemblage sur un chicon (`incomplete_chicons_au_gratin`) : `#hams` → 250 mB béchamel → fromage ; fumage |
| `vol_au_vent` | Scie : pâte feuilletée → 2 bouchées crues ; fumage → `vol_au_vent_shell` ; assemblage : poulet cuit → champignon (`c:mushrooms`) → 100 mB béchamel |
| `flamiche` | Presse : pâte feuilletée → `tart_shell` ; assemblage (`incomplete_tart`, partagé avec les tartes du M19) : poireau → 250 mB crème → œuf ; fumage |
| `croque_monsieur` | Assemblage sur du pain (`incomplete_croque_monsieur`) : `#hams` → fromage → Presse ; fumage |

## v2 — Gaufres et Gaufrier (M18, 26/10/2026)

| Item | Recette |
|---|---|
| `liege_waffle_dough` | Mixer : pâte à brioche + sucre perlé + beurre → 2 (la brioche porte déjà l'œuf) |
| `brussels_waffle_batter` | Mixer : farine + sucre + levure + 250 mB de lait → 2 |
| `lacquemant_batter` | Mixer : farine + beurre + cassonade + levure + 250 mB de lait → 2 |
| `liege_waffle` / `brussels_waffle` / `lacquemant_wafer` | Gaufrier chauffé : 200 / 160 / 120 ticks |
| `brussels_waffle_sugared` | Deployer `#powdered_sugars` sur une gaufre de Bruxelles |
| `brussels_waffle_chocolate` | Spout 50 mB de chocolat de Create |
| `lacquemant` | Spout 50 mB de sirop de Liège sur la gaufrette |
| `waffle_iron` (bloc) | Mechanical Crafter : 2 plaques de fer, Precision Mechanism, bassin |

## v2 — Douceurs (M19, 26/10/2026)

Fumage = ventilateur sur feu de camp, ou four. Friture : Friteuse chauffée, 100 ticks, 10 mB par pièce.

| Douceur | Recette |
|---|---|
| `speculoos` | Mixer farine + cassonade + beurre + épices → 4 pâtes ; Presse → cru ; fumage |
| `speculoos_spread` (fluide) | Broyage speculoos → 2 miettes ; Mixer 4 miettes + sucre + 100 mB `#frying_oils` → 250 mB ; Spout 50 mB sur un pistolet ou une gaufre de Bruxelles |
| `cuberdon` | Mixer chauffé : 1 sucre + 2 `#raspberries` (jumelle : baies sucrées) + 100 mB de miel → 4 |
| `praline`, `praline_kriek` | Spout 50 mB de chocolat sur `#praline_fillings` (fèves de cacao, `c:nuts`) ; Spout 50 mB de kriek sur une praline |
| `ballotin` | Établi : 8 `#pralines` autour d'un papier |
| `croustillons` | Mixer farine + sucre + levure + 250 mB bière blonde → 4 pâtes ; friture ; Deployer sucre glace → `croustillons_sugared` |
| `couque_de_dinant` | Mixer farine + 250 mB de miel → pâte ; Presse → crue ; fumage. Se mange en 6,4 s |
| tartes | `tart_shell` (M17) ; sucre : assemblage cassonade → œuf ; mattentaart : maton → œuf (`incomplete_tart` partagé avec la flamiche) ; riz : Mixer froid (600 ticks) riz (`#rice`) + sucre + 250 mB de lait → appareil, Deployer sur le fond ; les trois au fumage |
| `craquelin`, `cramique` | Mixer pâte à brioche + 2 sucre perlé / + 2 raisins secs → cru ; fumage |
| `pain_a_la_grecque` | Presse pâte à brioche → brioche aplatie ; Deployer cassonade → cru ; fumage |
| `merveilleux` | Assemblage sur une meringue : Spout 100 mB crème → meringue → copeaux de chocolat |
| `bouquette` | Mixer farine de sarrasin + levure + raisins secs + 250 mB de lait → 4 pâtes ; friture ; Deployer cassonade → `bouquette_cassonade` |
| `dame_blanche` | Spout 50 mB de chocolat sur `#vanilla_ice_creams` |
| `babelutte` | Mixer chauffé 2 cassonade + beurre → 4 |
| `hot_chocolate` (fluide) | Mixer chauffé sucre + 250 mB de lait + 100 mB de chocolat → 250 mB ; Spout 250 mB sur une bouteille → `hot_chocolate_bottle` (se boit, rend la bouteille) |

## v2 — Huile usagée (M20, 26/10/2026)

| Élément | Recette |
|---|---|
| `used_frying_oil` (fluide) | Rendue par la Friteuse : la moitié de la graisse payée (`fryer.usedOilRatio`). Pas dans `#frying_oils` |
| `oil_filter` | Compacting : 2 papiers + 1 charbon de bois |
| filtration | Mixer : 1000 mB d'huile usagée + 1 filtre → 800 mB d'huile de friture + 1 `used_oil_filter` (`mixing/frying_oil_from_used_oil`) |
| `used_oil_filter` | Combustible : 200 ticks (`neoforge:furnace_fuels`) |
| biodiesel (Diesel Generators) | Mixer : 250 mB d'huile usagée + 100 mB d'éthanol (`c:ethanol`) + 1 sel → 250 mB `createdieselgenerators:biodiesel`, seulement avec le mod (`mixing/biodiesel_from_used_oil`) |

## v2 — Décoration et familles du menu (M21, 26/10/2026)

| Bloc | Recette (établi) |
|---|---|
| `friterie_sign`, `frituur_sign`, `chip_shop_sign` | 6 planches + 1 colorant noir + 1 colorant blanc ; panneau accroché au mur, face au joueur |
| `red_friterie_neon`, `yellow_…`, `blue_…`, `green_…` | 4 vitres + 1 colorant de la couleur + 1 poudre de glowstone ; lumière 15, éclairé même dans le noir |

Aucun ne porte d'état : au retrait du mod, ils deviennent de l'air.

Tags `create_belgian_snacks:menu/{fries,friterie,dishes,waffles,sweets}` : les aliments finis de chaque famille (cornets, snacks de friterie, plats, gaufres, douceurs), remplis au datagen et extensibles par KubeJS. L'advancement « Tour de Belgique » demande un aliment de chacune.

## Dans Arcadia V2

Farmer's Delight est présent : les tags `c:crops/onion` et `c:crops/tomato` sont remplis, donc ce sont les recettes « normales » qui s'activent. Les recettes de secours servent hors du pack. Aucune mayonnaise ni aucun ketchup dans le pack (vérifié au M2) ; les huiles existantes passent par `#bs:frying_oils`. Voir `11-compat-arcadia.md`.

## Recettes de secours (sans compat)

Chaque recette qui lit un tag `c:` potentiellement vide a une jumelle protégée par la condition `neoforge:tag_empty` qui utilise un ingrédient vanilla. Le datagen doit générer les deux.
