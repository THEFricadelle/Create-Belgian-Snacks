# 03 — Architecture

## Arborescence cible

```
src/main/java/be/thefricadelle/belgiansnacks/
├── BelgianSnacks.java              # @Mod, MOD_ID, REGISTRATE, asResource()
├── registry/
│   ├── BSItems.java  BSBlocks.java  BSBlockEntities.java  BSFluids.java
│   ├── BSRecipeTypes.java  BSSoundEvents.java  BSCreativeTabs.java
│   ├── BSTags.java                 # tous les TagKey du mod
│   └── BSFoods.java                # FoodProperties des 3 fricadelles (valeurs provisoires)
├── content/
│   ├── fryer/                      # Friteuse
│   │   ├── FryerBlock.java  FryerBlockEntity.java
│   │   ├── FryingRecipe.java  (+ serializer / params)
│   │   └── FryerRenderer.java      # client
│   ├── grinder/                    # Hachoir Suprême
│   │   ├── SupremeGrinderBlock.java  SupremeGrinderBlockEntity.java
│   │   ├── GrinderMode.java        # THE_ / ULTIMATE
│   │   └── GrinderProgress.java    # set d'IDs consommés, (dé)sérialisation
│   ├── food/
│   │   ├── FoodIndex.java          # liste dynamique des aliments
│   │   ├── FoodIndexRules.java     # logique pure de filtrage (testable en JUnit)
│   │   └── FricadelleItem.java     # effets / gags à la consommation
│   └── advancement/                # triggers custom si nécessaire
├── network/                        # payloads (sync FoodIndex, progression hachoir)
├── config/BSConfig.java            # ModConfigSpec serveur
├── command/BSCommands.java         # /belgiansnacks ...
├── compat/
│   ├── jei/                        # catégories Friteuse + Hachoir (Arcadia utilise JEI)
│   ├── jade/                       # infobulles Friteuse + Hachoir
│   ├── kubejs/                     # (plus tard) schéma KubeJS pour frying
│   └── ponder/                     # scènes Ponder (client)
├── client/                         # tout le code client-only
└── data/                           # datagen : recipes, models, lang, tags, advancements, loot
```

## Registres

Utiliser `CreateRegistrate` (comme les autres addons Create 6) :

```java
public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);
```

Vérifié dans `../refs/Create` (M0) : `REGISTRATE.registerEventListeners(modEventBus)` dans le constructeur du mod ; l'onglet créatif par défaut est fixé avant toute entrée d'item.

## Types de recettes custom

| Type | ID | Base | Remarques |
|---|---|---|---|
| Friture | `create_belgian_snacks:frying` | `FryingRecipe extends StandardProcessingRecipe<SingleRecipeInput>`, enregistrée par l'enum `BSRecipeTypes` (même forme que `AllRecipeTypes` de Create) | 1 ingrédient item, 1-2 résultats (avec chances), `processing_time`, `heat_requirement` (none/heated/superheated), **1 ingrédient fluide = la graisse, quantité par item frit**. Format identique aux recettes de traitement Create, donc `event.custom` KubeJS sans schéma dédié |
| Hachoir (affichage) | `create_belgian_snacks:grinding_goal` | Recette « virtuelle » | Sert uniquement à afficher le Hachoir dans JEI. La logique réelle est dans le BE (la liste n'est pas connue au datagen). |

Toutes les autres étapes utilisent des types Create existants : `create:crushing`, `create:milling`, `create:mixing`, `create:compacting`, `create:pressing`, `create:filling`, `create:deploying`, `create:sequenced_assembly`.

Datagen : l'API publique `com.simibubi.create.api.data.recipe` (`CrushingRecipeGen`, `MixingRecipeGen`, …) ; pour `frying`, `BSFryingRecipeGen extends StandardProcessingRecipeGen<FryingRecipe>`. L'ID est préfixé par le type automatiquement (`frying/fricadelle`).

Exemple généré :

```json
{
  "type": "create_belgian_snacks:frying",
  "heat_requirement": "heated",
  "ingredients": [
    { "item": "create_belgian_snacks:raw_fricadelle" },
    { "type": "neoforge:tag", "amount": 10, "tag": "create_belgian_snacks:frying_oils" }
  ],
  "processing_time": 100,
  "results": [ { "id": "create_belgian_snacks:fricadelle" } ]
}
```

## FoodIndex — la liste dynamique des aliments

C'est le cœur de THE_FRICADELLE. Doit être **déterministe**, **identique client/serveur** et **bon marché**.

### Définition d'un aliment

```
aliments = { item ∈ registre ITEM | item.components().has(DataComponents.FOOD) }
         ∪ tag #create_belgian_snacks:grinder/extra_foods      (ex. minecraft:cake)
         − tag #create_belgian_snacks:grinder/blacklist        (nos fricadelles, pâtes, items créatifs...)
         − config grinder.blacklistedMods  (liste de modid)
         − config grinder.blacklistedItems (liste d'IDs)
```

- On travaille sur l'**ID d'item** (pas sur les variantes de composants : une seule potion suspecte, etc.).
- Nos propres items produits par le Hachoir **doivent** être dans la blacklist (sinon boucle).
- Le tri de la liste finale suit l'ID complet `namespace:path` (chaîne) : stable, et chaque mod reste groupé. Attention, l'ordre naturel de `ResourceLocation` compare le chemin avant le namespace ; ce n'est pas celui-là.

### Cycle de vie

1. Recalcul au `ServerStartedEvent` et au `TagsUpdatedEvent` (côté serveur, reload `/reload`).
2. Le serveur envoie au client un payload `FoodIndexSyncPayload` (liste d'IDs) à la connexion et après chaque recalcul. Le client **n'essaie pas** de recalculer seul (évite les divergences config/tags).
3. Accès : `FoodIndex.get(level)` → vue immuable `List<ResourceLocation>` + `Set` pour le `contains`.

### Commandes de debug (op level 2)

- `/belgiansnacks foods count` → nombre d'aliments.
- `/belgiansnacks foods export` → écrit `config/create_belgian_snacks/foods_export.csv` (id, modid, nutrition, saturation, source : food/extra). **Sert à décider du taux de THE_Fricadelle et de la blacklist.**
- `/belgiansnacks grinder fill <pos> [percent]` → remplit un Hachoir (tests).

### Implémentation (M5)

- `content/food/FoodIndexRules` : logique pure (chaînes), testée en JUnit.
- `content/food/FoodIndex` : instantané immuable par côté (serveur, client), `compute(mods, items)` réutilisable par les tests, `recompute(server)` qui journalise la durée et diffuse.
- `content/food/FoodIndexEvents` : recalcul au `ServerStartedEvent`, au `TagsUpdatedEvent` côté serveur (`/reload`, KubeJS) et au rechargement de la config ; envoi au joueur à la connexion. Au démarrage, le premier passage (pendant le chargement des tags) tourne avec les valeurs par défaut de la config, qui n'est pas encore chargée ; le passage du `ServerStartedEvent` corrige.
- `network/FoodIndexSyncPayload` + `BSNetwork` (protocole `1`) ; le client ne calcule jamais, il vide sa liste à la déconnexion (`client/BSClientEvents`).
- `command/BSCommands` : `/belgiansnacks foods count|export` (op 2). `export` écrit aussi `has_recipe` : une recette connue produit-elle l'item ? C'est un indice pour la blacklist, pas une preuve (poissons pêchés, viandes de mobs, baies cueillies n'ont pas de recette).

### Logique pure testable

`FoodIndexRules` prend en entrée des listes simples (IDs, flags food, tags, config) et renvoie la liste finale. Aucune dépendance au registre Minecraft → tests JUnit rapides.

### IDs de recettes stables (KubeJS)

Toutes nos recettes ont un ID lisible et définitif, rangé par type : `create_belgian_snacks:<type>/<résultat>` (ex. `create_belgian_snacks:crushing/beef`, `create_belgian_snacks:frying/fricadelle`). Ne jamais renommer un ID après une release : les scripts KubeJS du pack s'en servent.

## Progression du Hachoir

- Stockée dans le BlockEntity : `Set<ResourceLocation> consumed`, sérialisé en liste de strings.
- Progression affichée = `|consumed ∩ FoodIndex| / |FoodIndex|` (l'intersection gère le cas où le modpack a perdu un mod).
- Sync client : seulement `(count, total, mode)` via le mécanisme d'update du BE ; la liste des aliments **manquants** n'est envoyée qu'à la demande (goggles + sneak, ou écran d'inspection) pour éviter des paquets lourds.

## Configuration serveur (`BSConfig`)

| Clé | Défaut provisoire | Rôle |
|---|---|---|
| `grinder.theFricadelleRatio` | `0.10` (D1) | Part des aliments nécessaire pour la Pâte d'exception, arrondie au-dessus (D22) |
| `grinder.ultimateRatio` | `1.0` (D21) | Part pour la Pâte absolue (laisser à 1.0 sauf besoin serveur) |
| `grinder.rejectDuplicates` | `true` | Laisser les doublons sur le tapis ou dans le funnel au lieu de les détruire |
| `grinder.blacklistedMods` / `blacklistedItems` | `["cosmeticarmoursmod"]` / `[]` (M5) | Exclusions du FoodIndex, en plus du tag `grinder/blacklist` |
| `grinder.minSpeed` / `stressImpact` | `64 rpm` / `8 SU/rpm` (D11) | Coût cinétique ; l'impact est lu à chaque calcul du réseau (`BlockStressValues.IMPACTS`) |
| `ultimate.announceInChat` | `true` | Annonce à tout le serveur, dans le chat, quand quelqu'un mange THE_FRICADELLE. Les opérateurs le changent aussi en jeu : `/belgiansnacks announce true\|false` (écrit dans le fichier de config, pris en compte tout de suite). Le rot, le feu d'artifice et le PNJ restent |
| `fryer.tankCapacity` | `4000` mB | Capacité du réservoir de graisse (friteuses chargées après le changement) |
| `fryer.speedMultiplier` | `1.0` | Divise chaque temps de friture |
| `fryer.maxBatch` | `16` | Items frits ensemble ; le tag `fryer/one_at_a_time` force 1 |

La consommation de graisse n'est plus une config : c'est l'ingrédient fluide de chaque recette (modifiable par KubeJS).

La config est côté **serveur** et synchronisée ; le client ne l'utilise que pour l'affichage. Implémentée dans `config/BSConfig` (M3 : section `fryer`) ; chaque getter retombe sur la valeur par défaut tant que la config du monde n'est pas chargée.

## Réseau

| Payload | Sens | Contenu |
|---|---|---|
| `FoodIndexSyncPayload` | S→C | liste d'IDs |
| `GrinderMissingRequestPayload` | C→S | position du BE |
| `GrinderMissingResponsePayload` | S→C | position, IDs manquants et total. Environ 55 Ko pour 1800 aliments, loin de la limite de 1 Mo : pas de pagination, liste plafonnée à 20 000 IDs |
| `FricadelleGagPayload` | S→C | déclenche particules/son côté clients proches (si pas faisable en vanilla) |

## Intégration Create

- Friteuse (M3) : `FryerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation`.
  - `SmartFluidTankBehaviour.single` avec validateur `#frying_oils` ; `DirectBeltInputBehaviour` pour les tapis.
  - Capability item combinée : slot 0 = entrée (insertion seule, recette requise), slots 1-2 = sortie (extraction seule). Une trémie ou un funnel ne prennent jamais l'entrée.
  - Chaleur : `BasinBlockEntity.getHeatLevelOf(état dessous)`, l'API de Create (compatible avec les sources de chaleur qu'un autre mod y branche). Règle (`FryerBlockEntity.heatAllows`) : celles de Create pour ses 3 conditions et ses 5 niveaux (sans chaleur : toujours ; chauffé : brûleur allumé ; super-chauffé : SEETHING), `HeatCondition.testBlazeBurner` pour le reste. Create Heat JS, présent dans Arcadia, réécrit `testBlazeBurner` (« sans chaleur » refusé sur un brûleur allumé) et ajoute des constantes à `HeatLevel` : aucun `switch` exhaustif sur ces enums (M6.5).
  - Recette (`findRecipe`) : Parmi les recettes `frying` de l'item, celles que la chaleur **et la graisse du réservoir** permettent, la graisse la plus spécifique d'abord (le moins de fluides acceptés ; une recette sans graisse en dernier ; à égalité, le plus petit ID de recette) : des frites blanchies dans le blanc de bœuf donnent des frites au blanc, bien que la recette normale accepte toute graisse (v2, M11). Sans recette possible : la première que la chaleur permet (le statut dit « pas assez de graisse »), sinon la première (le statut dit « pas assez chaud »). L'entrée accepte tout item qui a une recette de friture, même avant la graisse ; un lot déjà dans le panier garde sa recette. Pas de cache : un pack a peu de recettes de friture, et une recette ajoutée par `/reload` ou KubeJS s'applique tout de suite. Tentative de démarrage toutes les 10 ticks à l'arrêt, immédiate après un changement d'inventaire ou de graisse.
  - Synchronisation client uniquement aux changements d'état ; le client estime la progression entre deux paquets. Le statut (graisse, chaleur, sortie) se calcule aussi côté client, pour les Goggles et Jade.
  - Graisse conservée à la casse : `collectImplicitComponents` / `applyImplicitComponents` + `copy_components` dans la table de loot (data component `fryer_fluid`).
  - Piège : `SmartBlockEntity` appelle `addBehaviours` depuis son constructeur, avant les initialiseurs de champs de la sous-classe ; un champ affecté là ne doit pas avoir d'initialiseur.
  - Compat : `compat/jei` (catégorie `CreateRecipeCategory`, style Create), `compat/jade` (ligne de statut côté client ; Jade affiche déjà réservoir et slots via les capabilities).
- Huile usagée (v2, M20) : la Friteuse a un 2e `SmartFluidTankBehaviour` de type `OUTPUT` (clé NBT à part : les sauvegardes v1 se chargent avec un réservoir vide) ; la capability fluide est un `FryerFluidHandler` à 2 réservoirs (remplir : la graisse ; vider : l'huile usagée, ou la graisse si `usedOilRatio` vaut 0) ; la graisse payée d'un lot (`FatPaid`) donne l'huile usagée en fin de lot. Le biodiesel de Diesel Generators est écrit par `BSCompatRecipeProvider` (leur fluide n'existe pas au datagen), le combustible du filtre par `BSDataMapProvider` (`neoforge:furnace_fuels`). Piège : `FluidEntry.get()` est le fluide qui coule ; comparer avec `isSame`.
- Gaufrier (v2, M18) : `content/waffle/` — `WaffleIronBlockEntity` reprend la Friteuse sans réservoir (lot posé sur les « plaques », pause sans perte, capability item 1 + 2 slots, `DirectBeltInputBehaviour`, Goggles) et ses règles statiques de chaleur (`FryerBlockEntity.heatAllows`, `heatKey`) ; la Friteuse n'a pas été refactorisée (risque inutile sur une machine bien testée), seul le contrôle de place en sortie est partagé (`content/processing/OutputSpace`). `WaffleIronRenderer` (client) tourne le couvercle autour de sa charnière (`CachedBuffers.partial`, `PoseStack`), avec la même rotation que le blockstate (`horizontalBlock`, modèle tourné vers le nord). L'ouverture du couvercle est un état client (6 ticks), rien de plus n'est synchronisé. Modèles faits main possibles : `custom/waffle_iron.json` (base) et `custom/waffle_iron_lid.json` (couvercle).
- Hachoir (M6) : `SupremeGrinderBlockEntity extends KineticBlockEntity`, arbre par le haut (`hasShaftTowards(UP)`, axe Y).
  - Stress : `BlockStressValues.IMPACTS.register(bloc, BSConfig::grinderStressImpact)` ; `CStress.setImpact` n'accepte que les blocs de Create.
  - Mode : `ScrollOptionBehaviour<GrinderMode>` sur les 4 côtés, icônes `AllIcons` de Create (référencées, pas copiées).
  - Entrée : capability item (slot 0 insertion d'un exemplaire à la fois, toujours vide ; slot 1 la pâte, extraction seule) et `DirectBeltInputBehaviour`. Refuser = rendre la pile intacte, donc le tapis ou le funnel attend.
  - Logique pure dans `GrinderProgress` (décision, objectif, intersection), testée en JUnit.
  - Le compte n'est recalculé que quand l'instantané du FoodIndex change (comparaison d'identité) ou quand la collection change ; un tick ordinaire ne fait qu'un calcul d'objectif.
  - Sync client : compte, objectif, total, mode et 5 exemples manquants ; jamais la collection. La liste complète passe par `GrinderMissingRequestPayload` (chunk chargé, joueur à moins de 8 blocs).
  - Jauge : propriété de blockstate `fill` 0 à 4 (5 modèles datagen), pas de rendu par tick. Lames : modèle partiel `block/supreme_grinder/blades`, visuel Flywheel `SingleAxisRotatingVisual`, `SupremeGrinderRenderer` en repli. Les partials sont chargés par le point d'entrée client `BelgianSnacksClient` (`@Mod(dist = CLIENT)`).
  - Les lignes Goggles de `KineticBlockEntity` passent par la police du client : elles ne s'appellent jamais côté serveur (un GameTest l'a montré).
- Palier 3 (M8) :
  - `content/food/UltimateFricadelleItem` : gags côté serveur (rot géant, feu d'artifice, message à tout le serveur par `PlayerList.broadcastSystemMessage`, apparition du PNJ). Les effets de potion sont dans `BSFoods` (`FoodProperties.Builder.effect`).
  - Le visiteur est débranché depuis la v2 (25/10/2026) : `UltimateFricadelleItem` n'appelle plus `TheFricadelleNpc.appear` ; tout le reste ci-dessous est gardé et testé en l'appelant directement.
  - `content/npc/TheFricadelleNpc` (`PathfinderMob` sans objectifs d'IA, invulnérable, sans collision, 160 ticks de vie au plus). Trois phases synchronisées par une donnée d'entité : `RUN` (apparaît jusqu'à 8 blocs devant le mangeur, sur un bloc où les entités sont actives, et le rejoint en courant par la navigation vanilla ; téléporté à côté s'il n'y est pas en 3 s), `TALK` (applaudit et dit sa phrase à voix haute, toujours la phrase 4, la seule enregistrée, son `npc.phrase.4`, et au-dessus de sa tête, jamais dans le chat, 3,5 s), `LAUNCH` (monte comme une fusée sans collision, puis éclate : particules et sons seulement, aucune `Explosion`) ; type d'entité `noSave()`, donc jamais écrit dans un chunk. Sa phrase est son nom visible (synchronisé par l'entité), aucun paquet maison.
  - `client/TheFricadelleNpcRenderer` + `ClappingPlayerModel` (modèle de joueur large ou fin : course normale, bras qui applaudissent, poings levés au décollage) ; `client/NpcSkin` résout le skin une fois (`SkullBlockEntity.fetchGameProfile`, puis `SkinManager.getOrLoad`) et le garde.
  - `registry/BSTriggers.GRINDER_HALF` (`SimpleCriterionTrigger`), lancé par le Hachoir quand son compte augmente et atteint 50 % du total, pour les joueurs à 16 blocs. `registry/BSAdvancements` : l'onglet, généré par Registrate.
- Ponder : une scène par machine (jalon M9).

Toutes ces classes ont été vérifiées dans les sources 6.0.10 au moment de leur usage (règle du `CLAUDE.md`).

## Retrait du mod sans casse (v2, M10)

Exigence E1 du cahier des charges v2 (`13-cahier-des-charges-v2.md`, section 3).

- **IDs figés** : `src/gametest/resources/create_belgian_snacks/frozen-ids.txt`, une ligne `<registre> <id>` par ID jamais livré (registres du jeu, plus `data/recipe` et `data/advancement`). `RegistryGameTests` compare les registres, `FrozenIdsFileTest` (JUnit) les recettes et advancements générés. `ItemGameTests` et `RecipeFilesTest` lisent leur liste dans ce fichier. Renommer un ID : alias de registre (`DeferredRegister.addAlias`), gardé à vie.
- **Rien de fragile chez les autres** : aucun attachment, effet, attribut, métier, POI ni composant d'enchantement à notre namespace ; entités `noSave()` ; nos data components seulement sur nos items. Vérifié par `RegistryGameTests.nothingFragileIsRegistered`.
- `content/uninstall/` : `ReferencePurger` (données d'un autre mod qui gardent nos IDs dans un joueur), `Uninstall` (liste des purgeurs, mode préparation sur `PlayerLoggedInEvent`), `UninstallScan` (lecture seule, une fois par commande : joueurs de chaque monde, chunks chargés autour d'eux et chunks forcés ; un handler partagé par un multibloc compté une fois), `SavedStackLists` (retire les entrées d'un namespace d'une liste de piles sauvegardées en la reconstruisant, donc sans pile vide).
- Commande `/belgiansnacks uninstall scan | purge-references | prepare [true|false]` (op 2), config `uninstall.prepare`.
- `compat/solonion/SolonionPurger` : chargé seulement si `solonion` est présent, **sans aucune classe de Solonion à la compilation ni dépendance de build**. L'historique est l'attachment NeoForge `solonion:foodlist` (`FoodPlayerDataImpl`, un `INBTSerializable<ListTag>` de piles sauvegardées) : lu et réécrit par l'interface de NeoForge. Ensuite `SOLOnion.EVENT.updatePlayerBenefits(player)` et `SOLOnionAPI.syncFoodList(player)` par réflexion, résolus une fois ; s'ils manquent (autre version), la purge reste enregistrée et le client se met à jour au prochain sync. Vérifié sur Solonion 1.5.7 (jar d'Arcadia) le 25/10/2026.
- Test de bout en bout : `tools/uninstall_smoke.py` (voir `10-tests.md`), avec la sonde `src/uninstallProbe` (mod de test `uninstall_probe`, jamais livré, chargé par ses deux runs seulement : les autres runs ne chargent que ce mod, et la sonde reste inactive sans `uninstall_probe.phase`).

## Cultures (v2, M14)

- `registry/BSCrops` : chaque bloc de culture et l'item qui le plante, enregistrés ensemble (aucune classe n'a à charger l'autre d'abord) ; modèles « crop » à 4 dessins (âges 0-7 deux par deux, âges 0-3 un chacun), tables de loot générées.
- `content/crop/BSCropBlock` (âges 0-7, vanilla), `ShortCropBlock` (âges 0-3, pousse à toute lumière et survit dans le noir, contrairement à `CropBlock` qui exige 9), `ChiconCropBlock` (ne pousse qu'à lumière ≤ `crops.darkMaxLight`), `AsparagusCropBlock` (propriété `blanched` fixée à chaque pas de croissance), `BouchotBlock` (immergeable, sans collision, `age` 0-3 : le Harvester de Create le reconnaît, `HarvesterMovementBehaviour.isValidCrop`, vérifié dans les sources 6.0.10).
- `data/BSLootProviders` : deux tables et trois modificateurs `neoforge:add_table` (rien d'enregistré chez nous, règle R2) ; `data/BSBotanyPotsProvider` : JSON brut, Botany Pots n'est pas une dépendance.

## Durée au Mixer de Create (v2, M15)

Le Mixer lit `processing_time` des recettes `mixing` et le met à l'échelle de sa vitesse (`MechanicalMixerBlockEntity`, Create 6.0.10) : ticks ≈ log2(512 / tr/min) × ⌈durée / 100 × 15⌉ + 1. La gueuze (2400) dure environ 1081 ticks à 64 tr/min et 361 à 256 tr/min ; une recette sans durée environ 46 ticks à 64 tr/min. Vérifié sur un vrai Mixer (`BreweryGameTests`).

## Recettes de bassin : quatre règles (v2, M13 et M16)

Établies au jalon M13 après un scan de toutes les recettes de traitement d'Arcadia 2.0.32 et la lecture de `BasinOperatingBlockEntity.getMatchingRecipes` dans Create 6.0.10 : quand plusieurs recettes de bassin correspondent, **Create prend celle qui a le plus d'items** (les fluides ne comptent pas).

1. **Toute recette de Mixer ou de Compacting a au moins un item** (`RecipeFilesTest.basinRecipesNeedAnItem`). Une recette de fluides seuls correspondrait à tout bassin qui contient ce fluide et le volerait à une autre ligne qui attend encore ses items (par exemple le lait d'une ligne de chocolat de Create). D'où le sel dans la crème, le beurre, le fromage de Herve et le maton.
2. **Quand le pack fabrique déjà un ingrédient sous un tag `c:`, notre recette se coupe** et nos recettes lisent un tag qui contient le sien (`RecipeFilesTest.packIngredientsTurnOurRecipeOff`) : sel (`c:salt`, `#salts`), beurre (`c:butter`, `#butters`), sucre glace (Create: Food, `#powdered_sugars`).
3. **Une recette qui en recoupe une du pack a strictement plus d'items**, ou part d'un autre ingrédient : nos pâtes partent de la pâte de Create (`create:dough`), jamais de farine + eau (que Create mélange déjà en pâte) ; la béchamel porte trois items (farine, beurre, épices) pour passer devant la pâte au beurre de Create: Food.

4. **Pas d'œuf dans un bassin froid de nos snacks** (M16) : Create: Food change un œuf seul (`minecraft:egg`) en œuf liquide dans n'importe quel bassin. L'œuf passe au Deployer (panure). La mayonnaise, la brioche, les nouilles et la meringue (M13) gardent leur œuf : leur bassin le reçoit avec ses autres ingrédients, à vérifier dans le pack au M22.

Contrôle JUnit (M17) : deux de nos recettes de bassin sans condition n'ont jamais exactement les mêmes ingrédients (`RecipeFilesTest.noBasinRecipeHasTheSameIngredientsAsAnother`).

**Items de transition partagés** (M16) : une fois un item entré dans un assemblage, Create l'identifie par son item de transition **et** l'identifiant de recette rangé dans son composant `SEQUENCED_ASSEMBLY` (`SequencedAssemblyRecipe.appliesTo`) ; le premier pas se choisit par l'ingrédient d'entrée. Un seul `incomplete_breaded` sert donc aux huit snacks panés, chacun partant de son appareil (vérifié sur une vraie ligne : deux croquettes différentes à la suite).

## Ponder (M9)

- `client/ponder/BSPonderPlugin` : 5 scènes (Friteuse, Hachoir « collectionner » et « modes », lignes des paliers 2 et 3) et un tag ; ajouté à `PonderIndex` sur `FMLClientSetupEvent`, comme Create.
- Le monde Ponder est côté client : aucune logique serveur n'y tourne. Les scènes montrent les états en modifiant le NBT des block entities (panier et sortie de la Friteuse), la jauge par blockstate, et les animations de Create (Deployer, Spout, Presse, items sur tapis).
- Textes : tous dans `registry/BSPonderText` (anglais et français), sous les clés que Ponder lit (`<modid>.ponder.shared.<clé>`, `<modid>.ponder.<scène>.header`, `<modid>.ponder.tag.<tag>`) ; `BSLang` les écrit dans les deux fichiers de langue.
- Schémas : `assets/create_belgian_snacks/ponder/*.nbt`, construits avec les vrais blocs de Create par `./gradlew runPonderSchematics` (`gametest/ponder/PonderSchematicsRun`) puis commités. Couche 0 = plaque de base.

## Modèles faits main (M9)

Un modèle Blockbench déposé dans `models/block/custom/` (`fryer`, `supreme_grinder`, `supreme_grinder_blades`) remplace le placeholder : le modèle généré du même bloc en devient l'enfant (`BSBlocks.handMade`), donc blockstates, modèles d'items, jauge et partial des lames ne changent pas. Contrat détaillé dans `06-assets.md`.
