# 11 — Compatibilité avec « Arcadia: Echoes of Power V2 »

Le mod est développé **pour** ce modpack : il doit y tourner sans conflit, en solo et sur les serveurs de la team. Il reste utilisable hors du pack (Create seul suffit).

## Le pack en chiffres

| | |
|---|---|
| Minecraft / loader | 1.21.1 / NeoForge |
| Nombre de mods | ~464 (liste CurseForge, relevée le 26/09/2026, pack en 2.0.x) |
| Style | Pack « expert » tech + magie, progression par FTB Quests, recettes modifiées par **KubeJS** |
| Recipe viewer | **JEI** (+ JER, JEED, JEBr, JEP, FTB JEI Extras) |
| Infobulles monde | **Jade** (+ Jade Addons) |

### Versions relevées sur le PC de THEFricadelle (26/09/2026)

Instance CurseForge à jour : `curseforge/minecraft/Instances/Arcadia Echoes Of Power V2 (2)` — pack **Arcadia V2 2.0.32**, 464 mods. (L'autre instance, sans « (2) », est une ancienne 2.0.25 sur NeoForge 21.1.232 : ne pas s'en servir comme référence.)

| Mod | Version dans le pack | Fichier |
|---|---|---|
| NeoForge | **21.1.250** | manifest.json |
| Java | 21 | manifest.json |
| Create | **6.0.10** | `create-1.21.1-6.0.10.jar` |
| JEI | **19.57.0.447** | `jei-1.21.1-neoforge-19.57.0.447.jar` |
| Jade | **15.10.6** | `Jade-1.21.1-NeoForge-15.10.6.jar` |
| KubeJS | 2101.7.2-build.377 (+ KubeJS Create 2101.3.1, KubeJS Delight 1.1.6) | |
| Farmer's Delight | 1.3.4 | `FarmersDelight-1.21.1-1.3.4.jar` |
| Create Slice & Dice | 4.3.4 | |
| Create: Central Kitchen | 2.3.0 **arcadia-fix** (jar patché par la team, pas sur CurseForge) | |
| Create: Food | 2.7.1 | |
| Spice of Life: Onion | 1.5.6 | |
| Create Heat JS | 0.0.6 | |

Quand le pack est mis à jour : relire `manifest.json` et le dossier `mods/` de l'instance, puis mettre à jour ce tableau + `gradle.properties`. Le pack contient aussi des mods maison (`arcadia-patch-create`, `arcadia-lib`, jars `*-arcadia-fix`) : ne jamais dépendre d'eux.

## Mods du pack qui nous concernent

### Create et addons (extraits)
Create, Create Slice & Dice, **Create: Central Kitchen**, **Create: Food**, **Create Confectionery**, **Create: Arm-made Cuisine**, **Create: Ice & Creams**, **Create: Winery**, **Create: Integrated Farming**, Create Crafts & Additions, Create: Enchantment Industry, Create: Diesel Generators, Create Mechanical Extruder, Create Nuclear, Create: Ultimate Factory, Steam 'n' Rails, Create Heat JS, KubeJS Create, Arcadia Patch Create, TFMG Arcadia Patch… (~46 mods Create au total).

### Nourriture (sources d'aliments pour le FoodIndex)
Farmer's Delight + addons : **Create: Central Kitchen** (version arcadia-fix), Trail and Tales Delight (arcadia-fix), Aquaculture Delight, Barbeque's Delight, Chef's Delight, Corn Delight, Crabber's Delight, Crate Delight, Cuisine Delight, Cultural Delights, End's Delight, Ender's Delight, My Nether's Delight, Ocean's Delight, Storage Delight, Ars Nouveau's Flavors & Delight, Twilight's Flavors & Delight, Farmer's Knives.
Autres : Aquaculture 2, Burger Mod, Cook's Collection, Biomes O' Plenty, Twilight Forest, The Aether / Deep Aether, Mowzie's Mobs, Occultism, Iron's Spells, Ars Nouveau, Botany Pots…
Liés à la nourriture sans en ajouter : **Spice of Life: Onion** (diversité alimentaire), **AppleSkin**, **.3D Placeable Food**, Sophisticated Backpacks (upgrade d'alimentation).

→ On peut s'attendre à **plusieurs centaines, voire plus de mille** aliments. THE_FRICADELLE est donc un vrai projet de fin de partie : c'est voulu, mais la blacklist doit être soignée.

### Scripting / outils
KubeJS (+ KubeJS Create, KubeJS Delight, LootJS…), FTB Quests, Polymorph, Jade, JEI.

## Exigences de compatibilité

### JEI (obligatoire)
- Catégories JEI pour : **Friture** (entrée, sortie, chaleur requise, huile consommée), **Hachoir Suprême** (mode, taux, liste d'aliments défilable), et nos recettes Create sont affichées automatiquement par Create.
- La catégorie Hachoir se base sur le FoodIndex **synchronisé depuis le serveur** (donc cohérent avec les modifs KubeJS du pack).
- Enregistrer la Friteuse et le Hachoir comme *catalysts*.
- Les items de transition (Sequenced Assembly) sont cachés de la liste d'ingrédients JEI si Create le fait pour les siens.

### Jade (fortement recommandé)
- Plugin Jade optionnel (`compat/jade`) : huile + progression pour la Friteuse, `count / total` + mode pour le Hachoir. Chargé seulement si Jade est présent.
- Les données serveur passent par le mécanisme « server data » de Jade (pas de requête custom).

### KubeJS (obligatoire pour l'équipe du pack)
Le pack modifie ses recettes par KubeJS, notre mod doit s'y prêter :
- Le type `create_belgian_snacks:frying` doit fonctionner via `event.custom({ type: 'create_belgian_snacks:frying', ... })` (JSON standard, codec propre, messages d'erreur clairs).
- Nos recettes doivent pouvoir être supprimées / remplacées par `event.remove({ id: ... })` → IDs de recettes **stables et lisibles** (`create_belgian_snacks:frying/fricadelle`, `.../crushing/beef`…).
- La blacklist/extra du Hachoir sont des **tags** → modifiables par `ServerEvents.tags('item', ...)`.
- Le FoodIndex est recalculé après le reload des tags → les modifs KubeJS sont prises en compte.
- Bonus (plus tard) : schéma KubeJS pour `frying` si simple à faire.

### Farmer's Delight et ses addons
- FD fournit déjà `farmersdelight:minced_beef`, oignons, tomates… → on **ne double pas** ce qui existe. Nos recettes lisent des **tags** (`c:crops/onion`, `c:crops/tomato`, tags de viande). Notre `minced_beef` (D12, décidé) : toujours enregistré ; les recettes lisent `create_belgian_snacks:minced_meats/beef`, qui contient le nôtre et celui de FD.
- Vérifier au jalon M5 (export CSV) si un mod du pack fournit déjà **huile**, **mayonnaise** ou **ketchup** (Burger Mod, Cook's Collection, Create: Food…). Si oui : les accepter dans nos recettes via un tag `create_belgian_snacks:frying_oils` / sauces, pour éviter les doublons.

### Spice of Life: Onion
Le mod pénalise la nourriture répétitive. Nos 3 fricadelles doivent être des aliments normaux (composant `food`), donc elles seront comptées par SoL. Rien de spécial à coder, mais à garder en tête pour l'équilibrage (D-effets).

### Create Heat JS
Ce mod peut ajouter des niveaux de chaleur custom. La Friteuse lit la chaleur **via l'API de Create** (`BasinBlockEntity.getHeatLevelOf`), pas en testant `instanceof BlazeBurnerBlock`.

Testé dans le pack au M6.5 (0.0.6) :
- Il **ajoute des constantes** aux enums `HeatLevel` et `HeatCondition` de Create (5 niveaux et 5 conditions dans le pack). Aucun `switch` exhaustif sur ces enums chez nous : une constante inconnue y lèverait une exception.
- Il **réécrit `HeatCondition.testBlazeBurner`** avec sa propre table : « sans chaleur » y est refusé sur tout brûleur allumé, « chauffé » sur un brûleur `FADING`. Le bassin de Create y échappe (Heat JS le traite à part). La Friteuse applique donc les règles de Create pour les 3 conditions et 5 niveaux de Create, et ne passe par `testBlazeBurner` que pour ce que Heat JS ajoute.
- Le pack n'enregistre aucune source de chaleur custom aujourd'hui. Si la team en ajoute, un niveau custom passera par la table de Heat JS.

### Polymorph
Éviter les conflits de recettes (deux recettes avec les mêmes ingrédients). En cas de conflit : on retire la nôtre, pas celle du pack.

Testé dans le pack au M6.5 : aucune de nos recettes n'est d'un type que Polymorph arbitre (établi, four, tailleur de pierre…) ; notre seul craft passe par le Mechanical Crafter. Aucune recette du pack, de même type, n'accepte les mêmes entrées que les nôtres (broyage, meule, mixer, compacteur, presse, friture, et grille des deux crafts testée contre toutes les recettes de craft et de Mechanical Crafter).

Vérifié au M2 (scan des jars et de `kubejs/` de l'instance 2.0.32) :
- **Aucun** autre `create:crushing` sur bœuf, porc, poulet ou pain : nos recettes de broyage sont seules. Create: Food fait du haché à la **presse** (`createfood:ground_*`), autre machine, pas de conflit.
- `create:compacting` sur `#c:seeds` : déjà **deux** recettes (`createaddition:seed_oil`, `createdieselgenerators:plant_oil`). Notre recette graines → huile est donc désactivée dans le pack (conditions `mod_loaded`).
- Conventions du pack reprises dans nos tags : `c:ground_beef`, `c:ground_pork`, `c:ground_chicken` (Create: Food), `c:minced_beef` (FD), `c:bread_crumbs` (Create: Food), fluides `c:plantoil` (IE, C&A, Diesel) et `c:vegetable_oil` (Create: Food).
- Pas de mayonnaise ni de ketchup dans le pack.

### Serveur
- Pack lourd (464 mods) : FoodIndex calculé une fois par reload, jamais en tick. Log du temps de calcul.
- Tester sur une copie du serveur Arcadia avant toute mise à jour.

## Test dans le pack (M6.5, 27/09/2026)

`python tools/arcadia_smoke.py` (environ 35 minutes, deux fenêtres, 8 Go par client) : l'instance CurseForge est seulement lue ; tout se passe dans `run/arcadia`, `run/arcadia-b` et `run/arcadia-jar`. Trois phases :

1. **Client A** (pack complet + notre mod en dev, `ArcadiaSmokeRun`) : crée un monde et vérifie les recettes, les modifications KubeJS, les conflits, Create Heat JS, le Hachoir, JEI et l'export, puis ouvre le monde en LAN.
2. **Client B** rejoint en LAN : même index d'aliments (1803, même empreinte), Friteuse synchronisée, les deux joueurs nourrissent le même Hachoir par clic droit, B reçoit la liste des manquants.
3. **Le jar de release** (`build/libs`) avec les jars exacts du pack, Create compris, sans aucune classe du projet : charge le monde de A, dont un datapack lance `belgiansnacks foods export`. Aucune erreur `create_belgian_snacks` dans le log, 1803 aliments exportés.

Le script KubeJS de test (`tools/arcadia/zz_belgian_snacks_m65_test.js`, copié dans `run/` seulement) montre ce que la team peut faire sans toucher au jar : supprimer `create_belgian_snacks:frying/fricadelle`, ajouter une recette `create_belgian_snacks:frying` (pomme de terre vers pomme de terre cuite, frite par une vraie Friteuse), retirer un aliment du Hachoir par le tag `grinder/blacklist`.

Trouvé et corrigé en chemin : la Friteuse prenait la première recette de l'item sans regarder la chaleur (bloquée si une variante demande plus de chaleur), et Create Heat JS change les règles de chaleur (voir plus haut). Dans le pack, `incomplete_the_fricadelle` apparaît dans la liste d'items de JEI (voir `docs/10`).

## Le FoodIndex réel d'Arcadia 2.0.32 (export du 27/09/2026)

Obtenu par `python tools/arcadia_export.py` : le pack complet (449 mods, sans Create/JEI/Jade/MezzConfig déjà fournis par le dev), ses scripts KubeJS, sa config, ses datapacks et son pack de ressources, lancés dans un client de dev qui crée un monde, exporte et quitte (environ 9 minutes). Données : `docs/data/arcadia-2.0.32-foods.csv` (une ligne par aliment) et `…-summary.txt`.

| | |
|---|---|
| Aliments comptés | **1804** (39 mods) |
| Exclus | 22 : nos 3 fricadelles, les 16 objets de `cosmeticarmoursmod`, `minecraft:ominous_bottle`, `mynethersdelight:enchanted_golden_egg`, `artifacts:everlasting_beef` |
| Palier 2 (10 %) | ≈ 180 aliments (arrondi fixé au M6) |
| Palier 3 (100 %) | 1804 aliments |
| Sans recette connue | 168 (indice seulement : poissons d'Aquaculture, viandes de mobs, baies de Twilight Forest s'obtiennent sans recette) |
| Temps de calcul | 26 ms en régime établi, 57 ms au démarrage du monde |

Répartition : Create: Food 909, Create Ice Creams 89, Cultural Delights 86, Farmer's Delight 77, My Nether's Delight 60, Ars Delight 51, End's Delight 47, Create Confectionery 46, Twilight Delight 46, Crabber's Delight 42, vanilla 42, puis 28 mods sous 40.

Pour lancer le pack en dev, deux pièges trouvés : ne lire que les `modId` des blocs `[[mods]]` (les blocs de dépendances en contiennent aussi), et passer `log4j2.configurationFile` en URI `file:///` (Create Numismatics la lit comme une URI ; un chemin Windows fait échouer la construction de tous les mods).

## Blacklist par défaut (D8, tranchée le 27/09/2026)

- Nos fricadelles : toujours exclues par le code.
- Mod `cosmeticarmoursmod` (objets décoratifs comestibles) : config `grinder.blacklistedMods`.
- Objets uniques : `minecraft:ominous_bottle`, `mynethersdelight:enchanted_golden_egg`, `artifacts:everlasting_beef` : tag `create_belgian_snacks:grinder/blacklist` (entrées optionnelles).
- **La pomme d'or enchantée compte.**

La team du pack peut étendre la liste sans toucher au jar (tag via KubeJS, ou config). La colonne `has_recipe` de `foods export` repère les candidats : un aliment sans recette connue n'est pas forcément inobtenable, il faut vérifier au cas par cas.

## Intégration au pack
Le mod est un projet perso de THEFricadelle : la team du pack **n'intervient pas dans le code**. Elle peut seulement l'ajuster de l'extérieur (KubeJS, tags, config serveur, éventuellement un chapitre FTB Quests « La Friterie »). D'où l'importance que tout soit réglable sans toucher au jar.

## Mods proches à connaître (vérifié le 26/09/2026)

Le nom « Create: Belgian Snacks » et le modid `create_belgian_snacks` ne sont utilisés par aucun mod trouvé sur CurseForge ni Modrinth. Mods au thème proche :

| Mod | Plateformes / versions | Recoupement |
|---|---|---|
| **Create: Bitterballen** (pyzpre) | 1.21.1 NeoForge inclus, maj sept. 2026 | Addon Create hollandais : Mechanical Fryer, Frying Oil, frikandellen, frites… **Le plus proche.** Pas dans Arcadia aujourd'hui. Si un jour il y est : accepter leur huile via le tag `create_belgian_snacks:frying_oils` et éviter les doublons de recettes. |
| Fritkot (aginji) | 1.20.1 Forge | Friteuse + huiles, frites. Pas de 1.21.1. |
| Belgian Fries (Asarim) | 1.20.1 Forge | Frites au blanc de bœuf. Pas de 1.21.1. |

Nos IDs (namespace `create_belgian_snacks:`) ne peuvent pas entrer en conflit technique avec eux, mais les noms affichés « Frying Oil » / « Fryer » pourraient prêter à confusion dans JEI si les deux mods sont installés.
