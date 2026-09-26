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
| Items en entrée | 1 slot, pile max `fryer.maxBatch` (16). Par funnel, tapis qui débouche dessus, clic droit. Refuse ce qui n'a pas de recette. |
| Items en sortie | 2 slots (une recette peut avoir 2 résultats). Extraction par funnel ou trémie ; l'entrée n'est jamais extraite. Clic droit main vide = récupérer la sortie, puis l'entrée s'il n'y a plus de sortie. |
| Traitement | **Par lot** (décidé) : toute la pile d'entrée cuit ensemble, jusqu'à `maxBatch` (16). Les items du tag `create_belgian_snacks:fryer/one_at_a_time` (THE_FRICADELLE crue) cuisent **un par un**. Durée = `processing_time` ÷ `speedMultiplier`. La graisse de tout le lot (quantité de la recette × nombre d'items) est payée **au démarrage**. |
| Conditions d'arrêt | Un lot ne démarre que si chaleur, graisse (pour tout le lot) et place en sortie sont réunies. Chaleur perdue en cours de lot → pause, sans perte ni perte de progression. La graisse ne peut pas manquer en cours de lot (déjà payée). |
| Rendu | Niveau de graisse visible, items qui flottent à la surface, bulles et fumée pendant la cuisson, grésillement toutes les 2 s (`create_belgian_snacks:fryer.sizzle`, sons vanilla en attendant). |
| Goggles / Jade | Statut (friture + %, ou la raison du blocage : rien à frire, pas assez chaud, pas assez de graisse, sortie pleine), chaleur, réservoir (mB / capacité). |
| Casser le bloc | Les items tombent ; la **graisse reste dans l'item** et revient à la pose. |
| Comparateur | Signal = remplissage de la sortie. |

### Recette `frying`

Format des recettes de traitement Create 6 ; la graisse est l'ingrédient fluide, sa quantité s'entend **par item frit**. Exemple et détails : `03-architecture.md`. Palier 3 : l'ingrédient fluide sera `melted_beef_tallow` seul (D16).

### Recette de craft (provisoire, D11)
Mechanical Crafter, ID `create_belgian_snacks:mechanical_crafting/fryer` (remplaçable par KubeJS) :

```
C I C      C = #c:plates/copper   I = barreaux de fer   T = réservoir à fluide
T B P      B = bassin             P = Precision Mechanism
```

---

## Hachoir Suprême (`supreme_grinder`)

### Rôle
Collectionner **chaque aliment unique** du modpack. Produit la Pâte d'exception (mode THE_) ou la Pâte absolue (mode ULTIME).

### Comportement

| Aspect | Spéc |
|---|---|
| Taille | 1 bloc ⚠️ (option 2×2 multibloc écartée pour la v1) |
| Cinétique | **Oui.** Arbre par le dessus. Vitesse min `grinder.minSpeed` (64 rpm), impact `grinder.stressImpact`. Sous la vitesse min : n'accepte rien. |
| Mode | `ScrollOptionBehaviour` (boîte de valeur Create, molette + clé) : **THE_** / **ULTIME**. Changer de mode **ne remet pas** la progression à zéro (le set d'aliments est partagé). |
| Entrée | Par le haut et les côtés : funnel, tapis, entonnoir, Mechanical Arm, clic droit. Accepte **1 item à la fois**. |
| Si l'item est un aliment **nouveau** | Consommé (1 exemplaire), ajouté à `consumed`, particules d'item + son de broyage. |
| Si l'item est un **doublon** | Refusé (`rejectDuplicates=true`) : il n'entre pas (funnel/tapis bloque ou contourne). Sinon détruit. |
| Si l'item n'est **pas un aliment** | Refusé. |
| Quand l'objectif est atteint | Mode THE_ : dès que `|consumed ∩ index| ≥ ratio × |index|` → produit 1 `exceptional_paste` dans le slot de sortie et **vide** `consumed`. Mode ULTIME : idem à 100 % → `absolute_paste`. |
| Sortie | 1 slot, extraction par funnel/entonnoir dessous. |
| Goggles | Mode, `count / objectif` (ex. `312 / 1 184`), %, 5 aliments manquants aléatoires en exemple ; en sneak : bouton/indication pour ouvrir la liste complète. |
| Casser le bloc | ⚠️ La progression est-elle conservée dans l'item (data component) ? Proposition : **oui**, pour éviter les drames sur serveur. |
| Rendu | Lames qui tournent (instance Flywheel ou rendu simple), jauge de remplissage sur une face. |

### Pourquoi « vider » au lieu de soustraire ?
Pour la v1, c'est le plus lisible : on remplit, on obtient la pâte, on recommence. Alternative à discuter : le mode THE_ ne consomme **que** les aliments nécessaires et garde le reste.

### Recette de craft (brouillon)
Mechanical Crafter : Crushing Wheel ×2 + Brass Casing + Precision Mechanism + ... ⚠️ Doit coûter cher.

### Écran de liste des manquants (v1 simple)
Pas de GUI d'inventaire. Un écran lecture seule (client) listant les items manquants sous forme d'icônes, ouvert en sneak-clic droit avec les Goggles. Données fournies par `GrinderMissingResponsePayload`.
