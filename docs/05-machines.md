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
| Huile | Réservoir interne `fryer.tankCapacity` (4000 mB). Accepte **uniquement** `frying_oil`. Remplissable par tuyau (toutes faces sauf dessous), seau, Spout. |
| Items en entrée | 1 slot, pile max 16 ⚠️. Par funnel, tapis qui débouche dessus, entonnoir, clic droit. Refuse ce qui n'a pas de recette. |
| Items en sortie | 1 slot (pile 64). Extraction par funnel/entonnoir sur les côtés ou dessous (comme le Basin : sortie vers un funnel/tapis adjacent si orienté). Clic droit main vide = récupérer. |
| Traitement | Un lot à la fois (tout le stack d'entrée cuit ensemble ou item par item ⚠️). Durée = `processing_time` ÷ `speedMultiplier`. Consomme `oil_consumption` mB par item **au début** de la cuisson. |
| Conditions d'arrêt | Pas assez d'huile, chaleur insuffisante, sortie pleine → la progression se met en pause (pas de perte). |
| Rendu | Niveau d'huile visible, items qui « flottent » dedans, bulles/particules de fumée pendant la cuisson, grésillement en boucle (`bs:fryer.sizzle`). |
| Goggles | Huile (mB / capacité), chaleur, recette en cours, progression %. |
| Comparateur | Signal = remplissage de la sortie. |

### Recette `frying` (format visé)

```jsonc
{
  "type": "create_belgian_snacks:frying",
  "ingredients": [ { "item": "create_belgian_snacks:raw_fricadelle" } ],
  "results":     [ { "id": "create_belgian_snacks:fricadelle", "count": 1 } ],
  "processing_time": 100,
  "heat_requirement": "heated",   // none | heated | superheated
  "oil_consumption": 10
}
```
Le format exact des champs `ingredients`/`results` doit **suivre celui des recettes de traitement Create 6** (vérifier dans les sources). Ce JSON est généré par datagen, pas écrit à la main.

### Recette de craft (brouillon)
Mechanical Crafter ou table : Basin + Fluid Tank + tôles de cuivre + grille (iron bars). ⚠️

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
