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

⚠️ Vérifier dans `../refs/Create` la façon actuelle d'enregistrer Registrate sur le mod bus (`REGISTRATE.registerEventListeners(modEventBus)`) et de définir l'onglet créatif par défaut.

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
| `grinder.theFricadelleRatio` | `0.10` (D1) | Part des aliments nécessaire pour la Pâte d'exception (arrive au M6) |
| `grinder.ultimateRatio` | `1.0` | Part pour la Pâte absolue (laisser à 1.0 sauf besoin serveur) |
| `grinder.rejectDuplicates` | `true` | Recracher les doublons au lieu de les détruire |
| `grinder.blacklistedMods` / `blacklistedItems` | `["cosmeticarmoursmod"]` / `[]` (M5) | Exclusions du FoodIndex, en plus du tag `grinder/blacklist` |
| `grinder.minSpeed` / `stressImpact` | `64 rpm` / `16 SU/rpm` ⚠️ | Coût cinétique |
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
| `GrinderMissingResponsePayload` | S→C | liste des IDs manquants (paginée si > ~2000) |
| `FricadelleGagPayload` | S→C | déclenche particules/son côté clients proches (si pas faisable en vanilla) |

## Intégration Create

- Friteuse (M3) : `FryerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation`.
  - `SmartFluidTankBehaviour.single` avec validateur `#frying_oils` ; `DirectBeltInputBehaviour` pour les tapis.
  - Capability item combinée : slot 0 = entrée (insertion seule, recette requise), slots 1-2 = sortie (extraction seule). Une trémie ou un funnel ne prennent jamais l'entrée.
  - Chaleur : `BasinBlockEntity.getHeatLevelOf(état dessous)` + `HeatCondition.testBlazeBurner`, l'API de Create (compatible avec les sources de chaleur qu'un autre mod y branche).
  - Recette mise en cache par item et revalidée contre le `RecipeManager` (un `/reload` ne sert jamais une recette périmée) ; tentative de démarrage toutes les 10 ticks à l'arrêt, immédiate après un changement d'inventaire ou de graisse.
  - Synchronisation client uniquement aux changements d'état ; le client estime la progression entre deux paquets. Le statut (graisse, chaleur, sortie) se calcule aussi côté client, pour les Goggles et Jade.
  - Graisse conservée à la casse : `collectImplicitComponents` / `applyImplicitComponents` + `copy_components` dans la table de loot (data component `fryer_fluid`).
  - Piège : `SmartBlockEntity` appelle `addBehaviours` depuis son constructeur, avant les initialiseurs de champs de la sous-classe ; un champ affecté là ne doit pas avoir d'initialiseur.
  - Compat : `compat/jei` (catégorie `CreateRecipeCategory`, style Create), `compat/jade` (ligne de statut côté client ; Jade affiche déjà réservoir et slots via les capabilities).
- Hachoir : `KineticBlockEntity` (consomme du stress), accepte les items par funnel/tapis/entonnoir, sélection du mode via `ScrollOptionBehaviour` (la petite boîte de valeur Create), tooltip Goggles via `IHaveGoggleInformation`.
- Ponder : une scène par machine (jalon M9).

⚠️ Toutes ces classes Create sont à vérifier dans les sources 6.0.10 avant usage.
