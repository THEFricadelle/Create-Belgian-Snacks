# 05 — Machines

## Friteuse (`fryer`)

### Rôle
Plonger des items dans l'huile chaude. Seule machine qui exécute les recettes `create_belgian_snacks:frying`.

### Comportement

| Aspect | Spéc |
|---|---|
| Taille | 1 bloc, se pose **sur** un Blaze Burner (comme un Basin) |
| Cinétique | **Aucune** (une friteuse ne tourne pas) |
| Chaleur | Lue sur le bloc en dessous : aucune / chauffée / super-chauffée. Réutiliser l'enum et la lecture de chaleur de Create (cf. Basin). |
| Graisse | Réservoir interne `fryer.tankCapacity` (4000 mB). Accepte **uniquement** le tag `#frying_oils` (notre huile, le blanc de bœuf fondu, les huiles végétales des autres mods). Remplissable par tuyau, seau, Spout. |
| Huile usagée (v2, M20) | 2e réservoir `fryer.usedOilCapacity` (2000 mB). À la fin d'un lot, `fryer.usedOilRatio` (0.5) × la graisse payée y entre en `used_frying_oil`, quelle que soit la graisse. Un lot ne démarre que s'il reste la place pour son huile usagée (statut « huile usagée à vider »). Tuyaux et seaux : on verse la graisse, on retire l'huile usagée ; à `usedOilRatio = 0`, pas d'huile usagée et l'on retire la graisse comme en v1. L'huile usagée reste dans l'item quand on casse la Friteuse. Une Friteuse v1 se charge avec un réservoir vide |
| Items en entrée | 1 slot, pile max `fryer.maxBatch` (16). Par funnel, tapis qui débouche dessus, clic droit. Refuse ce qui n'a pas de recette. |
| Items en sortie | 2 slots (une recette peut avoir 2 résultats). Extraction par funnel ou trémie ; l'entrée n'est jamais extraite. Clic droit main vide = récupérer la sortie, puis l'entrée s'il n'y a plus de sortie. |
| Traitement | **Par lot** (décidé) : toute la pile d'entrée cuit ensemble, jusqu'à `maxBatch` (16). Les items du tag `create_belgian_snacks:fryer/one_at_a_time` (THE_FRICADELLE crue) cuisent **un par un**. Durée = `processing_time` ÷ `speedMultiplier`. La graisse de tout le lot (quantité de la recette × nombre d'items) est payée **au démarrage**. |
| Conditions d'arrêt | Un lot ne démarre que si chaleur, graisse (pour tout le lot) et place en sortie sont réunies. Chaleur perdue en cours de lot → pause, sans perte ni perte de progression. La graisse ne peut pas manquer en cours de lot (déjà payée). |
| Rendu | Niveau de graisse visible, items qui flottent à la surface, bulles et fumée pendant la cuisson, grésillement toutes les 2 s (`create_belgian_snacks:fryer.sizzle`, sons vanilla en attendant). |
| Goggles / Jade | Statut (friture + %, ou la raison du blocage : rien à frire, pas assez chaud, pas assez de graisse, huile usagée à vider, sortie pleine), chaleur, les deux réservoirs (mB / capacité). |
| Casser le bloc | Les items tombent ; la **graisse reste dans l'item** et revient à la pose. |
| Comparateur | Signal = remplissage de la sortie. |

### Recette `frying`

**Choix de la recette** : Parmi les recettes `frying` de l'item, celles que la chaleur **et la graisse du réservoir** permettent, la graisse la plus spécifique d'abord (le moins de fluides acceptés ; une recette sans graisse en dernier ; à égalité, le plus petit ID de recette) : des frites blanchies dans le blanc de bœuf donnent des frites au blanc, bien que la recette normale accepte toute graisse (v2, M11). Sans recette possible : la première que la chaleur permet (le statut dit « pas assez de graisse »), sinon la première (le statut dit « pas assez chaud »). L'entrée accepte tout item qui a une recette de friture, même avant la graisse ; un lot déjà dans le panier garde sa recette.

Format des recettes de traitement Create 6 ; la graisse est l'ingrédient fluide, sa quantité s'entend **par item frit**. Exemple et détails : `03-architecture.md`. Palier 3 : l'ingrédient fluide sera `melted_beef_tallow` seul (D16).

### Recette de craft (provisoire, D11)
Mechanical Crafter, ID `create_belgian_snacks:mechanical_crafting/fryer` (remplaçable par KubeJS) :

```
C I C      C = #c:plates/copper   I = barreaux de fer   T = réservoir à fluide
T B P      B = bassin             P = Precision Mechanism
```

---

## Gaufrier (`waffle_iron`) — v2, M18

### Rôle
Cuire les pâtes à gaufres entre deux plaques chaudes. Seule machine qui exécute les recettes `create_belgian_snacks:waffle_baking` (V-D8 : une machine plutôt que le ventilateur).

### Comportement

| Aspect | Spéc |
|---|---|
| Taille | 1 bloc bas (9 px), posé **sur** une source de chaleur comme la Friteuse ; la poignée face au joueur qui le pose (`FACING`), la charnière derrière |
| Cinétique | Aucune |
| Chaleur | Mêmes règles que la Friteuse (`FryerBlockEntity.heatAllows`, compatible Create Heat JS) |
| Entrée | 1 slot, `waffleIron.maxBatch` (4) pâtes ; funnel, tapis, clic droit ; refuse ce qui n'a pas de recette |
| Sortie | 2 slots, extraction par funnel ou trémie, jamais l'entrée ; clic droit main vide : la sortie, puis l'entrée |
| Traitement | Par lot : jusqu'à 4 pâtes passent sur les plaques au démarrage ; durée = `processing_time` ÷ `waffleIron.speedMultiplier`. Un lot ne démarre qu'avec la chaleur et la place en sortie ; chaleur perdue → pause sans perte |
| Rendu | Couvercle (modèle partiel `block/waffle_iron/lid`) fermé pendant la cuisson, ouvert à 70° sinon, en 6 ticks ; la pâte ou la gaufre posée sur la plaque ; vapeur ; grésillement doux (`waffle_iron.sizzle`) toutes les 2,5 s |
| Goggles / Jade | Statut (cuisson + %, rien à cuire, pas assez chaud, sortie pleine) et chaleur |
| Casser | Pâtes en attente, pâtes sur les plaques et gaufres tombent |
| Comparateur | Remplissage de la sortie |

### Recette `waffle_baking`
Même format que `frying`, sans fluide : un ingrédient, jusqu'à deux résultats avec chances, `processing_time`, `heat_requirement`. KubeJS par `event.custom`. Parmi les recettes de l'item, la première que la chaleur permet.

### Recette de craft (V-D9)
Mechanical Crafter, ID `create_belgian_snacks:mechanical_crafting/waffle_iron` : une colonne plaque de fer (`#c:plates/iron`), Precision Mechanism, plaque de fer, bassin.

---

## Hachoir Suprême (`supreme_grinder`)

### Rôle
Collectionner **chaque aliment unique** du modpack. Produit la Pâte d'exception (mode THE_) ou la Pâte absolue (mode ULTIME).

### Comportement

| Aspect | Spéc |
|---|---|
| Taille | 1 bloc (option 2×2 multibloc écartée pour la v1) |
| Cinétique | **Oui.** Arbre par le dessus. Vitesse min `grinder.minSpeed` (64 rpm), impact `grinder.stressImpact` (8 SU/rpm, D11). Sous la vitesse min : n'accepte rien. |
| Mode | `ScrollOptionBehaviour` (boîte de valeur Create, molette + clé) : **THE_** / **ULTIME**. Changer de mode **ne remet pas** la progression à zéro (le set d'aliments est partagé). |
| Entrée | Par le haut et les côtés : funnel, tapis, entonnoir, Mechanical Arm, clic droit. Accepte **1 item à la fois**. |
| Si l'item est un aliment **nouveau** | Consommé (1 exemplaire), ajouté à `consumed`, particules d'item + son de broyage. |
| Si l'item est un **doublon** | Refusé (`rejectDuplicates=true`) : il n'entre pas (funnel/tapis bloque ou contourne). Sinon détruit. |
| Si l'item n'est **pas un aliment** | Refusé. |
| Quand l'objectif est atteint | Mode THE_ : dès que `|consumed ∩ index| ≥ ceil(ratio × |index|)` → produit 1 `exceptional_paste` dans le slot de sortie et **vide** `consumed` (D9, D22). Mode ULTIME : idem à 100 % → `absolute_paste`. Au-delà de l'objectif (passage d'ULTIME à THE_), tout part quand même : tentative de THE_FRICADELLE ratée, message aux joueurs à moins de 16 blocs. |
| Sortie | 1 slot, extraction par funnel/entonnoir dessous. Tant qu'elle est pleine, le Hachoir n'accepte rien. Comparateur : 0 à 15 selon la progression. |
| Goggles | Mode, `count / objectif` (ex. `312 / 1 184`), %, 5 aliments manquants aléatoires en exemple ; en sneak : bouton/indication pour ouvrir la liste complète. |
| Casser le bloc | **Conservée** (D10) : collection et mode dans l'item (data component `grinder_contents`), restaurés à la pose. La pâte en sortie tombe au sol. |
| Rendu | Lames qui tournent (visuel Flywheel `SingleAxisRotatingVisual`, renderer en repli), jauge de remplissage en 4 segments sur les 4 côtés (propriété de blockstate `fill`, aucun rendu par tick). |

### Pourquoi « vider » au lieu de soustraire ?
Pour la v1, c'est le plus lisible : on remplit, on obtient la pâte, on recommence. Alternative à discuter : le mode THE_ ne consomme **que** les aliments nécessaires et garde le reste.

### Recette de craft (brouillon)
Mechanical Crafter 3×3 (D11), ID `create_belgian_snacks:mechanical_crafting/supreme_grinder` :

```
S M S    S plaque de laiton (c:plates/brass), M Precision Mechanism
W C W    W Crushing Wheel, C Brass Casing
G I G    G lingot d'or, I bloc de fer
```

### Écran de liste des manquants (v1 simple)
Pas de GUI d'inventaire. Un écran lecture seule (client) listant les items manquants sous forme d'icônes, ouvert en sneak-clic droit avec les Goggles. Données fournies par `GrinderMissingResponsePayload`.
