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
| `./gradlew test` (inclus dans `build`) | `src/test/java` | `FoodIndexRulesTest` : union aliments + extra, blacklists (tag, mod, item), la blacklist l'emporte sur extra, pas de doublon, ordre indépendant de l'entrée. `RecipeFilesTest` : IDs de recettes exactement ceux attendus ; `type` conforme au dossier ; items et fluides de notre namespace existants ; autre mod atteint uniquement par tag ou derrière `mod_loaded` ; tout tag `c:` pouvant être vide est protégé par `not tag_empty` et a une jumelle de secours ; chaque fluide a `still`, `flow` animé et seau. `ResourceConsistencyTest` : clés `en_us` = clés `fr_fr` ; aucune traduction vide ou brute ; noms des 3 fricadelles identiques dans les deux langues ; tooltip des 3 fricadelles dans les deux langues ; chaque modèle d'item a un nom et une texture existante ; chaque texture fait 16×16 et sert à un modèle ; chaque entrée de tag existe, ou est optionnelle si elle vient d'un autre mod ; `neoforge.mods.toml` garde Create requis et les compat optionnelles |
| `./gradlew runGameTestServer` | `src/gametest/java` (hors du jar) | `GrinderGameTests` : voir la checklist Hachoir ; chaque test qui change la config tourne dans un lot à lui (`grinder_config_*`) : les lots passent l'un après l'autre, les tests d'un même lot en même temps. `FoodIndexGameTests` : aliments vanilla + gâteau (extra), pomme d'or enchantée comprise, sans nos fricadelles ni la bouteille menaçante ; tri par ID complet et recalcul identique ; blacklist de mod et d'item par `compute` ; recalcul < 50 ms ; aliments de FD présents seulement si FD est chargé ; commandes `count` et `export` (en-tête, une ligne par aliment, lignes du pain et du gâteau). `EatingGameTests` : effets exacts de chaque palier (niveau et durée) ; THE_FRICADELLE fait apparaître THEFricadelle à côté du mangeur, avec une des 5 phrases, puis il disparaît après 160 ticks ; jamais sauvegardé, invulnérable, non poussable ; l'onglet d'advancements complet avec ses parents ; « À mi-chemin » donné à un joueur proche quand un Hachoir atteint 50 % (joueur de test maison `TestPlayers` : le joueur factice de Minecraft fait planter le serveur de test avec les payloads de Create et Jade, et celui de NeoForge a des advancements factices). `TheFricadelleLineGameTests` : **lignes des paliers 2 et 3 sur de vraies machines** dans `empty_large` : tapis, Deployer (épices), 2 Spouts (mayonnaise, curry ketchup), Deployer (oignon, betterave sans FD), Presse, tous à deux blocs au-dessus du tapis (Create y cherche les machines) ; le test ramène seulement l'item inachevé en tête de tapis, comme une boucle ; succès après exactement 3 passages, 300 mB de chaque sauce et 3 épices et 3 oignons consommés, puis une vraie Friteuse frit la THE_Fricadelle ; même ligne pour le palier 3 (Spout de blanc de bœuf en tête, 5 passages, 1250 mB de chaque fluide : le test remplit les Spouts, qui ne tiennent que 1000 mB, comme des tuyaux, et compte), puis Friteuse sur brûleur SEETHING au blanc de bœuf. `ProductionLineGameTests` : **usine complète du palier 1** construite par code dans `empty_large` (5×11×11) : coffre de matières premières (4 porc, 4 bœuf, 4 poulet, 4 pain, rempli une fois) → trémie → roues de broyage → trémie → bassin + mixer → tapis sous une presse → friteuse sur Blaze Burner ; succès à 16 fricadelles frites sans action du test. L'échec liste les étapes atteintes. `FryerGameTests` : voir la checklist Friteuse ; recettes de test `frying/test_*` dans `src/gametest/resources` (super-chauffée, blanc de bœuf seul, un par un). `FluidGameTests` : 4 fluides avec source, écoulement et seau (dans `c:buckets`) ; `frying_oils` contient huile et blanc de bœuf fondu, pas les sauces ni l'eau ; vider un seau pose une source. `RecipeGameTests` : recettes chargées selon les mods présents (secours betterave actif sans tomate, recette graines active sans mod d'huile) ; sorties et chances du broyage, de la meule et de la presse ; recettes de bassin sur un **vrai bassin** (pâte seulement avec les 3 viandes, mayo avec toute huile mais pas l'eau ni 99 mB, recettes chauffées refusées à froid et acceptées sur Blaze Burner, 8 graines mais pas 7) ; **bout en bout** : presse mécanique + dépôt + moteur créatif produisent une fricadelle crue. `ItemGameTests` : IDs d'items exactement ceux attendus (stabilité pour KubeJS) ; seules les fricadelles sont mangeables ; valeurs nutritives = `BSFoods` ; THE_FRICADELLE épique et brillante ; item de transition = `SequencedAssemblyItem` ; manger nourrit le joueur et consomme 1 item ; seule THE_FRICADELLE se mange rassasié ; tout aliment du mod est dans la blacklist du Hachoir ; tags résolus au runtime (`c:foods` compris) ; onglet créatif complet ; nom `en_us` pour chaque item |
| `./gradlew runGameTestServerCompat` | mêmes GameTests, source set `gametestCompat` (aucune source, **Farmer's Delight 1.3.4** en plus) | Configuration Arcadia des recettes : recette tomate active et betterave inactive ; le haché de bœuf de FD fait la pâte ; notre haché est dans `c:minced_beef` |
| `./gradlew runClientSmoke` | `ClientSmokeTest` (source set `gametest`, client réel, fenêtre ouverte) | Modèles d'items et sprites de fluide ≠ texture manquante ; tooltip Create des 3 fricadelles ; noms `fr_fr` (différents de l'anglais sauf les marques) ; onglet créatif affiché et complet ; **JEI** liste nos items et affiche chaque recette chargée (et pas la recette tomate désactivée). Rapport `run/clientsmoke/smoke-report.txt`, validé par `verifyClientSmoke` ; Ponder : les 9 scènes (5 scènes, 8 composants) se construisent sans erreur et trouvent leur schéma, tag présent, pages Ponder ouvertes ; captures dans `run/clientsmoke/screenshots/` (onglet, fluides posés, pages JEI, Ponder, THEFricadelle) |
| `./gradlew runMultiplayerSmoke` (= `python tools/mp_smoke.py`) | serveur dédié `serverSmoke` + 2 clients `clientSmokeA/B` (source set `gametest`, 2 fenêtres) | Le serveur pose une friteuse en marche au spawn et place les joueurs de part et d'autre. Chaque client : connexion, friteuse synchronisée, les 2 joueurs se voient (monde + tab), lot fini vu identique (16 fricadelles, 1840 mB). B s'accroupit ; A attend de voir cet accroupissement (relayé par le serveur, donc B a tout vérifié avant qu'A agisse), puis récupère la sortie par un **vrai clic droit envoyé au serveur** et la reçoit dans son inventaire ; B voit la sortie se vider. Chaque client échoue proprement (rapport écrit) si la connexion tombe. Le serveur s'arrête seul quand les 2 sont partis et vérifie la sortie. Rapports `run/mpsmoke/*/smoke-mp-*.txt`, captures par client |
| `python tools/arcadia_export.py` (manuel, ~9 min, 10 Go) | pack Arcadia complet en dev (`arcadiaExport`) | Export réel du FoodIndex vers `docs/data/` ; à relancer à chaque mise à jour du pack |
| `python tools/arcadia_smoke.py` (manuel, ~35 min, 2 × 8 Go) | pack Arcadia complet : client A, client B en LAN, jar de release | Checklist Arcadia ci-dessous ; rapports `run/arcadia*/arcadia-smoke-report.txt`, captures `run/arcadia/screenshots` |
| `./gradlew runPonderSchematics` (outil, pas un test) | serveur `ponderSchematics` | Reconstruit les 4 schémas Ponder avec les vrais blocs et les écrit dans `src/main/resources` |
| `python tools/asset_status.py` (outil) | textures et modèles | Placeholder, fait main ou manquant, pour chaque fichier attendu ; pour les textures faites main : taille, format RGBA, pixels semi-transparents, nombre de couleurs |
| `python tools/pixelate.py` (outil) | une image générée | Réduction sans lissage à 16×16 (ou 16×32), fond magenta rendu transparent |
| `./gradlew testAll` | tout | `build` (JUnit) → GameTests → GameTests avec FD → client → deux clients |
| CI (`.github/workflows/build.yml`) | GitHub Actions | `build` (+ JUnit), `runGameTestServer`, `runGameTestServerCompat`, puis `runData` et échec si `src/generated` diffère de ce qui est commité. Le client ne tourne pas en CI (pas d'affichage) : `testAll` en local avant chaque push de fonctionnalité |

Les tests sont contrôlés par mutation. M1 : texture supprimée → JUnit échoue ; `alwaysEdible()` retiré → GameTest échoue. M2 : moteur retiré → le test de la presse échoue ; recette de secours supprimée → JUnit échoue.

Environnements : `runGameTestServer` = Create, JEI, Jade ; `runGameTestServerCompat` = idem + Farmer's Delight (même jar que le pack, sha1 vérifié). Aucun mod d'huile de graines : la condition `mod_loaded` de la recette graines est vérifiée sur les fichiers (JUnit).

Bugs trouvés par ces tests au M2 (voir `ERROR_LOG.md`) : haché de bœuf de FD refusé par la pâte (compat), textures de fluide absentes de l'atlas (client).

Conventions :
- GameTests : `@GameTestHolder(BelgianSnacks.MOD_ID)`, `@PrefixGameTestTemplate(false)`, structure `empty` (3×3×3 d'air, `src/gametest/resources/data/create_belgian_snacks/structure/empty.nbt`). Un test par contrat, message d'échec qui nomme l'item.
- JUnit : aucune classe Minecraft chargée. Lecture des fichiers générés avec Gson 2.10.1 (même version que le jeu, `testImplementation` uniquement).
- Le serveur GameTest ne construit pas les onglets créatifs : un test qui en a besoin appelle `CreativeModeTabs.tryRebuildTabContents` (sans risque, aucun client dans ce processus).
- Chaque jalon ajoute ses tests : Friteuse et Hachoir (M3, M6) en GameTest avec structures dédiées, `FoodIndexRules` (M5) en JUnit.
- Réglages trouvés pour la ligne : roues de broyage d'axe Z, moteur ouest à -64 et est à +64 (l'inverse projette la viande dehors) ; alimenter les roues par une trémie qui descend dans le contrôleur (lâcher des items d'un coup les fait rebondir) ; le mixer n'a pas d'arbre, il tourne par un engrenage voisin entraîné par un moteur ; la sortie du bassin va en diagonale vers le bas, sur le départ du tapis (`BeltConnectorItem.createBelts`).
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
- [x] Serveur dédié avec 2 clients : synchronisation et interaction réseau (`runMultiplayerSmoke`), captures des deux côtés (Jade visible chez les deux).
- [ ] Manuel : scénario spark ci-dessous.

## Checklist Hachoir Suprême
Automatisée par `GrinderGameTests` (serveur), `GrinderProgressTest` (JUnit), `ClientSmokeTest` et `runMultiplayerSmoke`, sauf mention contraire. Chaque test a été vérifié par mutation (8 bugs injectés, 8 détectés).
- [x] Sans rotation et à 32 tr/min : n'accepte rien (`refusesEverythingWithoutRotation`, `refusesBelowTheMinimumSpeed`) ; 512 SU à 64 tr/min (`grinder.goggles`, impact enregistré : `stressImpactIsTheConfiguredOne`).
- [x] Accepte un aliment nouveau (un exemplaire), refuse un doublon et un non-aliment, par capability, clic droit et tapis (`takesEachNewFood…`, `aPlayerFeeds…`, `aBeltFeeds…`) ; doublon détruit si `rejectDuplicates=false` (`duplicatesAreDestroyed…`).
- [x] Gâteau (extra) et pomme d'or enchantée acceptés ; fricadelles refusées (`takesEachNewFood…`).
- [x] Mode THE_ : pâte d'exception à l'objectif exact, collection vidée (`theModeMakes…`) ; objectif arrondi au-dessus (`GrinderProgressTest`).
- [x] Mode ULTIME : pâte absolue à 100 %, pas à 100 % moins un (`ultimateModeNeedsEveryFood`, commande `fill`).
- [x] Passage d'ULTIME à THE_ au-delà de l'objectif : pâte et tout consommé (`switchingToTheMode…`, D9).
- [x] Sortie pleine : n'accepte plus rien et n'écrase jamais la pâte en attente ; une trémie l'extrait (`aFullOutput…`, `aWaitingPaste…`).
- [x] Recalcul de l'index (`/reload`, config) : le compte suit (`aFoodModLeaving…`).
- [x] Retrait d'un mod entre deux sessions : la collection garde ses IDs, le compte suit l'intersection et revient avec le mod ; IDs inconnus conservés à la sauvegarde (`aFoodModLeaving…`, `unknownIdsSurvive…`).
- [x] Casser le bloc : collection et mode conservés dans l'item et restaurés (`breakingKeeps…`).
- [x] Jauge (blockstate `fill`) et comparateur (`gaugeAndComparator…`).
- [x] Goggles : mode, `compte / objectif`, 5 exemples, sans clé non traduite (`grinder.goggles`, `grinder.synced`).
- [x] Écran des manquants par le vrai paquet réseau, liste correcte, seules les lignes visibles dessinées (`grinder.missingScreen`, `mp.grinderMissing`).
- [x] JEI : recette du Hachoir et catégorie « Hachoir Suprême » à 2 entrées (`jei.recipes`, `jei.grinding`) + captures `jei-grinder-craft`, `jei-grinding-goal`.
- [x] Serveur dédié, 2 joueurs qui nourrissent le même Hachoir par clic droit, compte identique chez les deux (`mp.grinderFeed`, `mp.grinderShared`, `server.grinderFed`).
- [ ] Manuel : rendu des lames qui tournent (capture `grinder-in-world` à regarder) et scénario spark ci-dessous.

## Performance

### Scénario spark Friteuse (M3)
1. Monde créatif plat, `/spark heapsummary` (référence).
2. Poser 50 friteuses sur des Blaze Burners allumés (creative blaze cake), chacune avec de l'huile et 16 fricadelles crues, alimentées en continu (funnels ou `/give` + clic droit), et se placer à portée de vue de toutes.
3. `/spark profiler start` + `/sparkc profiler start` pendant 2 minutes, puis `stop` : regarder le temps par tick de `FryerBlockEntity.tick` et le temps de frame de `FryerRenderer`.
4. Casser et reposer les 50 friteuses 10 fois (ou `/reload` 10 fois), puis `/spark heapsummary` : le nombre d'instances de `FryerBlockEntity` et `FryerRenderer` ne doit pas grandir au-delà des 50 présentes.

### Scénario spark Hachoir (M6)
1. Monde créatif plat, `/spark heapsummary` (référence).
2. Poser 20 Hachoirs sous des moteurs créatifs à 64 tr/min, chacun alimenté par un tapis d'aliments variés (doublons compris, pour tester l'attente), et se placer à portée de vue de tous.
3. `/spark profiler start` + `/sparkc profiler start` pendant 2 minutes, puis `stop` : temps par tick de `SupremeGrinderBlockEntity.tick` (un calcul d'objectif hors changement d'index) et temps de frame du visuel des lames.
4. Accroupi avec les Goggles, ouvrir et fermer l'écran des manquants 50 fois, puis `/reload` 10 fois et `/spark heapsummary` : `GrinderMissingScreen` et `GrinderMissingResponsePayload` doivent retomber à 0 instance, `SupremeGrinderBlockEntity` rester à 20.

### Scénario spark THEFricadelle (M8)
1. `/spark heapsummary` (référence).
2. Manger 20 THE_FRICADELLE d'affilée (`/give` + manger), attendre 10 s que les visiteurs partent.
3. `/spark heapsummary` : `TheFricadelleNpc` retombe à 0 instance ; `/sparkc profiler` pendant l'apparition : pas de pic de frame dû au renderer (le skin se charge une seule fois).

- [ ] FoodIndex : recalcul < 50 ms sur le modpack complet (log au démarrage).
- [ ] Aucun parcours du registre des items dans un `tick()`.

### Scenario automatise (27/09/2026)

`./gradlew runShowcaseSpark` joue le scenario ci-dessus seul dans le monde du showcase (spark doit etre dans `run/showcase/mods`, jamais dans le build) : 30 s de mise en route, heap summary, profil serveur 60 s, profil client 60 s, 20 THE_FRICADELLE mangees d'un coup, 50 ouvertures de l'ecran des aliments manquants, second heap summary. Les rapports sont ecrits dans `run/showcase/config/spark/` et jamais envoyes (les publier sur spark.lucko.me reste le choix de l'auteur) ; les messages de spark vont dans `run/showcase/spark-report.txt`.

Resultat du 27/09/2026 (0.9.0, dev) : serveur 20 TPS, ticks 2,5 ms en mediane et 9,5 ms au pire sur 1 min ; le mod pese 0,23 % du thread serveur et 1,1 % du thread de rendu (le rendu des trois friteuses). Apres le scenario : 0 `TheFricadelleNpc`, 0 `GrinderMissingScreen`, friteuses et hachoirs en nombre constant.

Resultat du 28/09/2026 (1.0.0, dev) : serveur 20 TPS, ticks 2,4 ms en mediane et 13,3 ms au pire sur 1 min, CPU du processus 8 %. Apres le scenario : 0 `TheFricadelleNpc`, 0 `GrinderMissingScreen`, 8 `FryerBlockEntity` et 10 `SupremeGrinderBlockEntity` avant comme apres.

## Checklist Arcadia V2
Automatisée par `python tools/arcadia_smoke.py` (M6.5, voir `docs/11`), sauf mention contraire.
- [x] Démarrage sans crash ni erreur de recette `create_belgian_snacks` : le jar de release avec les jars exacts du pack (phase jar), et le client de dev (`recipes.loaded`).
- [x] JEI : catégories Friture et Hachoir, recette KubeJS affichée, recette supprimée absente (`jei.*`). `incomplete_the_fricadelle` apparaît dans la liste d'items, comme les items de transition de Create : **on le laisse** (décidé le 27/09/2026).
- [ ] Jade : infos Friteuse / Hachoir affichées (manuel, capture à regarder ; à faire par THEFricadelle, avec les scénarios spark).
- [x] KubeJS : `event.remove` et `event.custom` de type `frying` (frite par une vraie Friteuse), ajout au tag blacklist pris en compte (`kubejs.*`).
- [x] Aucun conflit Polymorph ni conflit de recette de même type (`recipes.polymorph`, `recipes.conflicts`).
- [x] Friteuse chauffée par Blaze Burner dans le pack, règles de Create respectées malgré Create Heat JS (`heat.fryerRules`, `kubejs.fryingWorks`) ; toutes les constantes de chaleur du pack gérées (`heat.createHeatJs`). Pas de source de chaleur custom dans le pack aujourd'hui.
- [x] 2 joueurs en LAN sur le pack complet : même index, Friteuse synchronisée, Hachoir partagé, liste des manquants (`lan.*`).
- [ ] Spice of Life: Onion compte bien nos fricadelles.
- [ ] Temps de calcul du FoodIndex (log) acceptable sur le pack complet.
