# 10 — Tests

## Niveaux

| Niveau | Outil | Pour quoi |
|---|---|---|
| Unitaire | JUnit 5 (`./gradlew test`) | `FoodIndexRules`, calcul de progression du Hachoir, parsing de config. Aucune classe Minecraft chargée. |
| Jeu automatisé | NeoForge GameTest (`runGameTestServer`) | Friteuse (cuit / pause sans huile / pause sans chaleur), Hachoir (accepte nouveau, refuse doublon, produit à l'objectif). |
| Manuel | checklists ci-dessous | Rendu, sons, Goggles, JEI, Jade, multi. |

## Automatisé : état actuel

Règle : tout ce qui peut être vérifié par la machine l'est. La checklist manuelle ne garde que le rendu, le son et le ressenti.

| Commande | Où | Ce qu'elle garantit |
|---|---|---|
| `./gradlew test` (inclus dans `build`) | `src/test/java` | `RecipeFilesTest` : IDs de recettes exactement ceux attendus ; `type` conforme au dossier ; items et fluides de notre namespace existants ; autre mod atteint uniquement par tag ou derrière `mod_loaded` ; tout tag `c:` pouvant être vide est protégé par `not tag_empty` et a une jumelle de secours ; chaque fluide a `still`, `flow` animé et seau. `ResourceConsistencyTest` : clés `en_us` = clés `fr_fr` ; aucune traduction vide ou brute ; noms des 3 fricadelles identiques dans les deux langues ; tooltip des 3 fricadelles dans les deux langues ; chaque modèle d'item a un nom et une texture existante ; chaque texture fait 16×16 et sert à un modèle ; chaque entrée de tag existe, ou est optionnelle si elle vient d'un autre mod ; `neoforge.mods.toml` garde Create requis et les compat optionnelles |
| `./gradlew runGameTestServer` | `src/gametest/java` (hors du jar) | `FryerGameTests` : voir la checklist Friteuse ; recettes de test `frying/test_*` dans `src/gametest/resources` (super-chauffée, blanc de bœuf seul, un par un). `FluidGameTests` : 4 fluides avec source, écoulement et seau (dans `c:buckets`) ; `frying_oils` contient huile et blanc de bœuf fondu, pas les sauces ni l'eau ; vider un seau pose une source. `RecipeGameTests` : recettes chargées selon les mods présents (secours betterave actif sans tomate, recette graines active sans mod d'huile) ; sorties et chances du broyage, de la meule et de la presse ; recettes de bassin sur un **vrai bassin** (pâte seulement avec les 3 viandes, mayo avec toute huile mais pas l'eau ni 99 mB, recettes chauffées refusées à froid et acceptées sur Blaze Burner, 8 graines mais pas 7) ; **bout en bout** : presse mécanique + dépôt + moteur créatif produisent une fricadelle crue. `ItemGameTests` : IDs d'items exactement ceux attendus (stabilité pour KubeJS) ; seules les fricadelles sont mangeables ; valeurs nutritives = `BSFoods` ; THE_FRICADELLE épique et brillante ; item de transition = `SequencedAssemblyItem` ; manger nourrit le joueur et consomme 1 item ; seule THE_FRICADELLE se mange rassasié ; tout aliment du mod est dans la blacklist du Hachoir ; tags résolus au runtime (`c:foods` compris) ; onglet créatif complet ; nom `en_us` pour chaque item |
| `./gradlew runGameTestServerCompat` | mêmes GameTests, source set `gametestCompat` (aucune source, **Farmer's Delight 1.3.4** en plus) | Configuration Arcadia des recettes : recette tomate active et betterave inactive ; le haché de bœuf de FD fait la pâte ; notre haché est dans `c:minced_beef` |
| `./gradlew runClientSmoke` | `ClientSmokeTest` (source set `gametest`, client réel, fenêtre ouverte) | Modèles d'items et sprites de fluide ≠ texture manquante ; tooltip Create des 3 fricadelles ; noms `fr_fr` (différents de l'anglais sauf les marques) ; onglet créatif affiché et complet ; **JEI** liste nos items et affiche chaque recette chargée (et pas la recette tomate désactivée). Rapport `run/clientsmoke/smoke-report.txt`, validé par `verifyClientSmoke` ; captures dans `run/clientsmoke/screenshots/` (onglet, fluides posés, pages JEI) |
| `./gradlew testAll` | tout | `build` (JUnit) → GameTests → GameTests avec FD → client |
| CI (`.github/workflows/build.yml`) | GitHub Actions | `build` (+ JUnit), `runGameTestServer`, `runGameTestServerCompat`, puis `runData` et échec si `src/generated` diffère de ce qui est commité. Le client ne tourne pas en CI (pas d'affichage) : `testAll` en local avant chaque push de fonctionnalité |

Les tests sont contrôlés par mutation. M1 : texture supprimée → JUnit échoue ; `alwaysEdible()` retiré → GameTest échoue. M2 : moteur retiré → le test de la presse échoue ; recette de secours supprimée → JUnit échoue.

Environnements : `runGameTestServer` = Create, JEI, Jade ; `runGameTestServerCompat` = idem + Farmer's Delight (même jar que le pack, sha1 vérifié). Aucun mod d'huile de graines : la condition `mod_loaded` de la recette graines est vérifiée sur les fichiers (JUnit).

Bugs trouvés par ces tests au M2 (voir `ERROR_LOG.md`) : haché de bœuf de FD refusé par la pâte (compat), textures de fluide absentes de l'atlas (client).

Conventions :
- GameTests : `@GameTestHolder(BelgianSnacks.MOD_ID)`, `@PrefixGameTestTemplate(false)`, structure `empty` (3×3×3 d'air, `src/gametest/resources/data/create_belgian_snacks/structure/empty.nbt`). Un test par contrat, message d'échec qui nomme l'item.
- JUnit : aucune classe Minecraft chargée. Lecture des fichiers générés avec Gson 2.10.1 (même version que le jeu, `testImplementation` uniquement).
- Le serveur GameTest ne construit pas les onglets créatifs : un test qui en a besoin appelle `CreativeModeTabs.tryRebuildTabContents` (sans risque, aucun client dans ce processus).
- Chaque jalon ajoute ses tests : Friteuse et Hachoir (M3, M6) en GameTest avec structures dédiées, `FoodIndexRules` (M5) en JUnit.
- Machines Create dans un GameTest : poser les blocs avec `helper.setBlock` ; un Blaze Burner doit être posé **avant** le bassin (le bassin met la chaleur en cache à la première lecture) ; la presse traite ce qui est **deux** blocs sous elle ; le moteur créatif transmet par la face de son `FACING`.

## Environnements de test manuel

1. **Minimal** : NeoForge + Create + JEI + Jade.
2. **Compat** : + Farmer's Delight + Slice & Dice + KubeJS.
3. **Arcadia V2 complet** (copie de l'instance + copie du serveur), au moins à M6.5 et avant chaque release.

## Checklist Friteuse
Automatisée par `FryerGameTests` (serveur) et `ClientSmokeTest` (client) sauf mention contraire.
- [x] Sans chaleur : ne cuit pas, le statut le dit (`noHeatMeansNoStartAndLostHeatPauses`).
- [x] Remplie par seau et par clic droit (`aPlayerFillsLoadsAndEmptiesItByHand`) ; tuyau et Spout passent par la même capability fluide (`fill`, testée partout).
- [x] Refuse l'eau et la mayonnaise, accepte le blanc de bœuf fondu (`onlyFryingFatsAndFryableItemsGetIn`).
- [x] Refuse un item sans recette, par capability et par tapis (`onlyFryingFats…`, `aBeltDropsItsItemsIn`).
- [x] Lot de 16, 160 mB consommés (`aBatchOfSixteenFriesTogether`) ; un par un pour le palier 3 (`oneAtATimeItemsFryAlone`).
- [x] Chaleur perdue en cours : pause sans perte, reprise au retour (`noHeatMeans…`). (Graisse épuisée en cours : impossible, elle est payée au démarrage ; pas assez de graisse = pas de démarrage, `tallowOnlyRecipeRefusesOil`.)
- [x] Recette super-chauffée refusée sur brûleur chauffé (`superheatedRecipesNeedASeethingBurner`).
- [x] Sortie extraite par trémie, jamais l'entrée (`aHopperBelowTakesOnlyTheOutput`) ; un funnel passe par la même capability.
- [x] Comparateur (`comparatorReadsTheOutput`).
- [x] Casser le bloc : items droppés, graisse conservée et restituée (`breakingKeepsTheFatAndDropsTheItems`).
- [x] Client : modèle et textures, rendu en marche synchronisé (graisse, lot de 16, statut), Goggles traduites, catégorie JEI (`fryer.*`, `jei.recipes`) + captures `fryer-in-world`, `jei-frying`.
- [ ] Manuel : serveur dédié avec 2 clients, rendu identique chez les deux.
- [ ] Manuel : scénario spark ci-dessous.

## Checklist Hachoir Suprême
- [ ] Sans rotation / sous la vitesse min : n'accepte rien.
- [ ] Accepte un aliment nouveau, refuse un doublon, refuse un non-aliment.
- [ ] Gâteau (extra tag) accepté ; fricadelles (blacklist) refusées.
- [ ] Mode THE_ : produit la Pâte d'exception au bon seuil (tester avec `grinder fill`).
- [ ] Mode ULTIME : produit la Pâte absolue à 100 %.
- [ ] `/reload` avec un tag modifié : total et progression recalculés sans crash.
- [ ] Retirer un mod de nourriture puis relancer : progression = intersection, pas de crash.
- [ ] Goggles : chiffres cohérents avec `/belgiansnacks foods count`.
- [ ] Écran des manquants : liste correcte, pas de lag avec ~1500 aliments.
- [ ] Serveur dédié avec 2 joueurs qui alimentent le même Hachoir.

## Performance

### Scénario spark Friteuse (M3)
1. Monde créatif plat, `/spark heapsummary` (référence).
2. Poser 50 friteuses sur des Blaze Burners allumés (creative blaze cake), chacune avec de l'huile et 16 fricadelles crues, alimentées en continu (funnels ou `/give` + clic droit), et se placer à portée de vue de toutes.
3. `/spark profiler start` + `/sparkc profiler start` pendant 2 minutes, puis `stop` : regarder le temps par tick de `FryerBlockEntity.tick` et le temps de frame de `FryerRenderer`.
4. Casser et reposer les 50 friteuses 10 fois (ou `/reload` 10 fois), puis `/spark heapsummary` : le nombre d'instances de `FryerBlockEntity` et `FryerRenderer` ne doit pas grandir au-delà des 50 présentes.

- [ ] FoodIndex : recalcul < 50 ms sur le modpack complet (log au démarrage).
- [ ] Aucun parcours du registre des items dans un `tick()`.

## Checklist Arcadia V2
- [ ] Démarrage client + serveur sans crash ni erreur de recette `create_belgian_snacks` dans `latest.log`.
- [ ] JEI : catégories Friture et Hachoir visibles, catalysts OK, pas d'item de transition dans la liste.
- [ ] Jade : infos Friteuse / Hachoir affichées.
- [ ] KubeJS : `event.remove({ id: 'create_belgian_snacks:frying/fricadelle' })` fonctionne ; `event.custom({ type: 'create_belgian_snacks:frying', ... })` fonctionne ; ajout d'un item au tag blacklist pris en compte après `/reload`.
- [ ] Aucun conflit Polymorph sur nos recettes.
- [ ] Friteuse chauffée par Blaze Burner **et** par les sources de chaleur ajoutées par Create Heat JS si le pack en utilise.
- [ ] Spice of Life: Onion compte bien nos fricadelles.
- [ ] Temps de calcul du FoodIndex (log) acceptable sur le pack complet.
