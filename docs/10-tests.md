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
| `./gradlew test` (inclus dans `build`) | `src/test/java` | `ResourceConsistencyTest` : clés `en_us` = clés `fr_fr` ; aucune traduction vide ou brute ; noms des 3 fricadelles identiques dans les deux langues ; tooltip des 3 fricadelles dans les deux langues ; chaque modèle d'item a un nom et une texture existante ; chaque texture fait 16×16 et sert à un modèle ; chaque entrée de tag existe, ou est optionnelle si elle vient d'un autre mod ; `neoforge.mods.toml` garde Create requis et les compat optionnelles |
| `./gradlew runGameTestServer` | `src/gametest/java` (hors du jar) | `ItemGameTests` : IDs d'items exactement ceux attendus (stabilité pour KubeJS) ; seules les fricadelles sont mangeables ; valeurs nutritives = `BSFoods` ; THE_FRICADELLE épique et brillante ; item de transition = `SequencedAssemblyItem` ; manger nourrit le joueur et consomme 1 item ; seule THE_FRICADELLE se mange rassasié ; tout aliment du mod est dans la blacklist du Hachoir ; tags résolus au runtime (`c:foods` compris) ; onglet créatif complet ; nom `en_us` pour chaque item |
| CI (`.github/workflows/build.yml`) | GitHub Actions | `build` (+ JUnit), `runGameTestServer`, puis `runData` et échec si `src/generated` diffère de ce qui est commité |

Les tests ont été contrôlés par mutation au M1 : texture supprimée → JUnit échoue ; `alwaysEdible()` retiré → GameTest échoue.

Conventions :
- GameTests : `@GameTestHolder(BelgianSnacks.MOD_ID)`, `@PrefixGameTestTemplate(false)`, structure `empty` (3×3×3 d'air, `src/gametest/resources/data/create_belgian_snacks/structure/empty.nbt`). Un test par contrat, message d'échec qui nomme l'item.
- JUnit : aucune classe Minecraft chargée. Lecture des fichiers générés avec Gson 2.10.1 (même version que le jeu, `testImplementation` uniquement).
- Le serveur GameTest ne construit pas les onglets créatifs : un test qui en a besoin appelle `CreativeModeTabs.tryRebuildTabContents` (sans risque, aucun client dans ce processus).
- Chaque jalon ajoute ses tests : Friteuse et Hachoir (M3, M6) en GameTest avec structures dédiées, `FoodIndexRules` (M5) en JUnit.

## Environnements de test manuel

1. **Minimal** : NeoForge + Create + JEI + Jade.
2. **Compat** : + Farmer's Delight + Slice & Dice + KubeJS.
3. **Arcadia V2 complet** (copie de l'instance + copie du serveur), au moins à M6.5 et avant chaque release.

## Checklist Friteuse
- [ ] Posée sur Blaze Burner éteint : ne cuit pas, Goggles le dit.
- [ ] Remplie par tuyau, par seau, par Spout.
- [ ] Refuse un fluide autre que l'huile.
- [ ] Refuse un item sans recette (funnel bloque).
- [ ] Cuisson OK, consommation d'huile correcte.
- [ ] Huile épuisée en cours : pause, reprend quand on remplit.
- [ ] Recette super-chauffée refusée si seulement chauffée.
- [ ] Sortie extraite par funnel et par entonnoir.
- [ ] Casser le bloc : items droppés, huile perdue (ou conservée ⚠️).
- [ ] Serveur dédié : aucun crash, rendu OK chez 2 clients.

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
