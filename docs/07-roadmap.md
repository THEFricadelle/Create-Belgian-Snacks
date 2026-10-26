# 07 — Roadmap

Chaque jalon = une (ou quelques) session(s) de dev. Pour chaque jalon : **mode plan → implémentation → `./gradlew build` → `runServer` qui démarre sans crash → checklist ci-dessous → commit**.

Les jalons marqués 🔒 dépendent d'une décision de `08-decisions-ouvertes.md`.

---

### M0 — Squelette ✅ (26/09/2026)
- [x] MDK ModDevGradle 1.21.1 renommé (modid, package, classe `BelgianSnacks`)
- [x] Versions NeoForge / Create / JEI / Jade relevées dans le manifest d'Arcadia V2 et reportées dans `gradle.properties`
- [x] Dépendances Create / Ponder / Flywheel / Registrate + JEI + Jade
- [x] `neoforge.mods.toml` : Create requis ; JEI, Jade, KubeJS, FD, S&D optionnels ; licence ARR
- [x] Fichier `LICENSE` (ARR) + NOTICE, CONTRIBUTING, CONTRIBUTORS, en-têtes SPDX
- [x] `CreateRegistrate` branché, onglet créatif vide
- [x] Datagen configuré (`runData` produit `src/generated/resources`)
- [x] CI GitHub Actions `./gradlew build`
- **Accepté si** : `runClient` ouvre un monde avec Create chargé ; `runServer` démarre.
  - Vanilla masque un onglet de catégorie sans item (`CreativeModeTab.shouldDisplay()`) : l'onglet est enregistré au M0 mais ne devient **visible qu'au M1**, avec les premiers items. Critère « onglet visible » reporté au M1.

### M1 — Items de base + lang + placeholders ✅ (26/09/2026)
- [x] Tous les items non-machines de `04-contenu-et-recettes.md` (sans les recettes)
- [x] `BSFoods` avec valeurs **provisoires** pour les 3 fricadelles
- [x] `tools/gen_placeholders.py` + textures générées
- [x] Lang `en_us` + `fr_fr` complètes, tooltips
- [x] Tags `bs:` + ajouts aux tags `c:`
- **Accepté si** : tous les items ont une texture et un nom traduit dans les 2 langues ; aucun `missing texture` ; l'onglet « Create: Belgian Snacks » est visible dans le menu créatif.

### M2 — Recettes Create simples + fluides ✅ (26/09/2026)
- [x] Fluides `frying_oil`, `melted_beef_tallow`, `mayonnaise`, `curry_ketchup` + seaux
- [x] Recettes crushing / milling / mixing / compacting / pressing (datagen)
- [x] Recettes de secours avec conditions `tag_empty` / `mod_loaded`
- [x] Tags d'interopérabilité (haché, chapelure, huiles) avec Create: Food, Farmer's Delight et les mods d'huile du pack
- [x] Tests : recettes chargées et conditions (sans FD), sorties et chances, bassins réels chauffés ou non, presse de bout en bout, fichiers de recettes (JUnit)
- [x] Affichage dans JEI : vérifié automatiquement par le test client (`runClientSmoke`) + captures
- [x] Testées **avec** et **sans** Farmer's Delight : `runGameTestServerCompat` / `runGameTestServer`
- **Accepté si** : dans JEI, toutes les recettes s'affichent ; testées avec **et** sans Farmer's Delight.

### M3 — Friteuse ✅ (26/09/2026)
- [x] Bloc, BE, capabilities item/fluide, lecture de chaleur
- [x] Type de recette `frying` + serializer + datagen
- [x] Rendu (huile, items), particules, son
- [x] Goggles, comparateur, plugin Jade
- [x] Catégorie JEI
- [x] Checklist `10-tests.md` automatisée (GameTests + test client) ; 2 clients sur un serveur dédié automatisés (`runMultiplayerSmoke`) ; reste manuel : le scénario spark
- **Accepté si** : la checklist Friteuse de `10-tests.md` passe, en solo et sur serveur dédié.

### M4 — Palier 1 bout à bout ✅ (27/09/2026)
- [x] Recette de craft de la Friteuse (Mechanical Crafter, provisoire)
- [x] Fricadelle mangeable (valeurs provisoires) + gag minimal (rot `fricadelle.burp`, retiré le 27/09/2026)
- [x] Une usine de test automatisée : viande → fricadelle sans intervention (`ProductionLineGameTests`, 16 fricadelles)
- **Accepté si** : une ligne entièrement automatique produit des fricadelles en continu.

### M5 — FoodIndex ✅ (27/09/2026)
- [x] `FoodIndexRules` (logique pure) + tests JUnit
- [x] `FoodIndex` (recalcul au start/reload) + payload de sync
- [x] Commandes `/belgiansnacks foods count|export`
- [x] Export sur le pack Arcadia complet (`tools/arcadia_export.py`) : `docs/data/`, D1 tranché (10 %)
- **Accepté si** : `export` produit un CSV cohérent avec le modpack Arcadia complet. → **On s'en sert pour trancher le taux de THE_Fricadelle.**

### M6 — Hachoir Suprême ✅ (27/09/2026)
- [x] Bloc cinétique, BE, stress, modes THE_/ULTIME
- [x] Acceptation / refus (nouveau, doublon, non-aliment)
- [x] Progression persistée (+ conservée au cassage, D10)
- [x] Goggles + Jade + écran des manquants + payloads
- [x] Commande `/belgiansnacks grinder fill` (+ `clear`)
- [x] Catégorie JEI « virtuelle »
- **Accepté si** : checklist Hachoir de `10-tests.md` OK, y compris retrait d'un mod entre deux sessions.

### M6.5 — Test dans Arcadia V2 ✅ (27/09/2026)
- [x] Jar installé dans une copie de l'instance Arcadia : démarrage sans crash, pas d'erreur de recette dans les logs
- [x] `foods export` sur le pack complet → CSV exporté
- [x] Test KubeJS : supprimer une de nos recettes et ajouter une recette `frying` par script
- [x] Vérifier conflits Polymorph et compat Create Heat JS
- **Accepté si** : tout passe sur une copie du serveur Arcadia avec 2 joueurs. Décidé ensemble : 2 clients en LAN sur le pack complet (pas de server pack local) ; le serveur dédié reste couvert par `runMultiplayerSmoke` en dev. Détail : `docs/11`, section « Test dans le pack ».

### M7 — Palier 2 : THE_Fricadelle ✅ (27/09/2026)
- [x] Sequenced Assembly (`sequenced_assembly/raw_the_fricadelle` + secours betterave) ; sauces et épices déjà livrées au M2
- [x] Friture THE_Fricadelle (`frying/the_fricadelle`)

### M8 — Palier 3 : THE_FRICADELLE ✅ (27/09/2026)
- [x] Sequenced Assembly du palier 3 (remplace le Mixer) + friture super-chauffée au blanc de bœuf
- [x] Effets, gags (dont le PNJ THEFricadelle), advancements

### M9 — Finitions & publication (27/09/2026, textures en attente)
- [x] Scènes Ponder (Friteuse, Hachoir, lignes des paliers 2 et 3) + tag
- [x] Recettes de craft des machines (D11 clos : recette de la Friteuse validée)
- [ ] Vraies textures / modèles Blockbench : **à faire par THEFricadelle** ; livrables, contrat et suivi (`tools/asset_status.py`) dans `06-assets.md`, code prêt à les prendre
- [x] Page mod préparée (`docs/publication/`), README à jour, brouillon de release GitHub
- **Reste avant publication** : textures, rapports spark, fusion de `dev` dans `main`, mise en ligne (par THEFricadelle) ; check-list dans `docs/publication/fiche.md`.

---

## v2

Jalons M10 à M23 (carte belge complète, retrait du mod sans casse) : voir `13-cahier-des-charges-v2.md`, section 13.

### M10 — Socle v2 ✅ (25/10/2026)
- [x] `dev` local rejoué sur `origin/dev` (licence 3.0 closed-source, 1.0.1) ; `main` à jour
- [x] PNJ THEFricadelle débranché, code et tests gardés (le visiteur est appelé directement dans les tests et le smoke client)
- [x] IDs figés (`frozen-ids.txt`, `RegistryGameTests`, `FrozenIdsFileTest`) et garde contre les enregistrements fragiles, vérifiés par mutation
- [x] `/belgiansnacks uninstall scan | purge-references | prepare`, config `uninstall.prepare`, purge Solonion sans dépendance de build
- [x] `tools/uninstall_smoke.py` : monde rempli du mod, ouvert sans lui, puis avec lui, 3 phases vertes
- [ ] Dans le pack : purge Solonion sur un vrai joueur et scénario sans purge (reporté au M22, le pack complet ne tourne pas dans ce jalon)
- **Accepté si** : test de retrait vert en environnement minimal ; PNJ absent ; build cassé par un ID retiré.

### M11 — Frites ✅ (25/10/2026)
- [x] `raw_fries`, `blanched_fries`, `fries`, `fries_beef_tallow`, `paper_cone`, `cone_of_fries`, `cone_of_fries_beef_tallow` : textures originales, noms et tooltips en/fr, valeurs nutritives provisoires (`08`)
- [x] Recettes : scie, double cuisson (chauffée puis super-chauffée), frites au blanc, cornet à l'établi (conflit de presse dans Arcadia), mise en cornet au Deployer
- [x] La Friteuse choisit sa recette par la chaleur **et** la graisse du réservoir, la plus spécifique d'abord
- [x] Les frites et les cornets comptent pour le Hachoir (V-D1) ; tags `fries` et `c:foods/fries`
- [x] Ligne automatique pomme de terre → cornet (GameTest), mutations attrapées
- **Accepté si** : une ligne automatique produit des cornets de frites depuis des pommes de terre. ✅

### M12 — Sauces ✅ (25/10/2026)
- [x] Fluides `andalouse`, `samourai`, `americaine` + seaux, textures, animation et atlas (l'atlas lit désormais tous les fluides)
- [x] Recettes au Mixer et jumelles de secours (betterave, poudre de blaze) ; tags `sauces` et `hot_peppers`
- [x] 10 cornets sauce remplis au Spout (5 sauces × cornet nature ou au blanc)
- [x] Tests : bassin réel, ligne de Spouts, jumelles de tous nos tags (mutation qui a révélé le trou : corrigé)
- [x] Sauce lapin : livrée au M13 (sirop de Liège)
- **Accepté si** : les cornets sauce sortent d'une ligne ; testé avec et sans FD. ✅

### M13 — Ingrédients de base ✅ (25/10/2026)
- [x] Scan des recettes d'Arcadia et lecture du bassin de Create : trois règles de bassin (`03-architecture.md`)
- [x] 30 items, 5 fluides (crème, béchamel, sirop de Liège, vinaigre, sauce lapin), 2 cornets sauce lapin, textures
- [x] Ventilateurs de Create pour laver et fumer (GameTest sur de vraies machines)
- [x] Sel, beurre et sucre glace se coupent quand le pack les fournit
- **Accepté si** : chaque ingrédient sort d'une machine Create ; aucun conflit de recette avec le pack. ✅ (scan statique ; vérification en jeu au M22)

### M14 — Cultures et moules ✅ (25/10/2026)
- [x] 7 cultures, bouchot, 15 items, fluide moutarde, farine de sarrasin ; textures des stades
- [x] Chicon forcé dans le noir, asperge blanche ou verte selon la lumière (`crops.darkMaxLight`)
- [x] Graines de l'herbe, moules de la pêche (modificateurs `neoforge:add_table`)
- [x] Harvester de Create : cultures et bouchot récoltés et replantés, sans duplication du pieu
- [x] Botany Pots (si présent)
- **Accepté si** : une ferme automatique (Harvester) de chaque culture ; chicon refusé à la lumière ; moules au bouchot. ✅ (le Harvester est vérifié par sa propre logique, pas sur un contraption en mouvement)

### M15 — Bières ✅ (25/10/2026)
- [x] Moût, blonde, brune, gueuze, kriek (fluides, seaux, textures), tag `beers`, vinaigre de bière
- [x] Durées au Mixer vérifiées dans les sources et sur un vrai Mixer
- **Accepté si** : une brasserie automatique sort les 4 bières. ✅ (recettes et Mixer réels testés ; la ligne complète malt → bière sera montrée en Ponder au M21)

### M21 — Finitions de contenu ✅ (26/10/2026)
- [x] Enseignes (3) et néons (4), recettes à l'établi
- [x] Ponder : lignes des frites, Gaufrier, huile usagée, friterie vendue par Create (schémas construits par `runPonderSchematics`)
- [x] Advancements : Cave à chicons, Brasseur, Ballotin, Le Gaufrier, Tour de Belgique (tags `menu/*`)
- [ ] Schéma KubeJS : non fait (optionnel), `event.custom` suffit
- **Accepté si** : client smoke et multijoueur smoke verts. ✅

### M20 — Huile usagée ✅ (26/10/2026)
- [x] 2e réservoir de la Friteuse, blocage, capability (graisse dedans, huile usagée dehors), huile usagée gardée à la casse, sauvegardes v1
- [x] Filtre, filtration au Mixer, filtre usagé combustible, biodiesel avec Diesel Generators
- **Accepté si** : ratio 0 = comportement v1 exact ; ligne friture + filtration en boucle fermée sans intervention. ✅ (`ratioZeroIsTheV1Fryer`, `UsedOilLoopGameTests` sur 2400 ticks ; l'heure complète est à la checklist manuelle)

### M19 — Douceurs ✅ (26/10/2026)
- [x] 22 douceurs, 38 items, 2 fluides (pâte de speculoos, chocolat chaud) : speculoos, cuberdon, pralines et ballotin, croustillons, couque de Dinant, trois tartes, craquelin, cramique, pain à la grecque, merveilleux, bouquettes, dame blanche, babelutte, chocolat chaud ; gaufre et pistolet au speculoos
- **Accepté si** : chaque douceur sort d'une ligne automatique. ✅ (recettes et bassins testés ; tartes au sucre et mattentaart, merveilleux et pralines sur de vraies lignes)

### M18 — Gaufrier et gaufres ✅ (26/10/2026)
- [x] Bloc `waffle_iron` (lot de 4, chaleur, pause, couvercle animé, vapeur, son), recette `waffle_baking`, JEI, Jade, Goggles, craft au Mechanical Crafter
- [x] Gaufres de Liège et de Bruxelles, sucre glace et chocolat, lacquemant
- **Accepté si** : checklist Gaufrier en solo et serveur dédié. ✅ (`WaffleIronGameTests`, smoke client avec captures couvercle fermé et ouvert, smoke multijoueur : deux clients voient les gaufres, l'un les prend, l'autre voit la sortie vide)

### M17 — Plats belges ✅ (26/10/2026)
- [x] 19 plats (22 aliments finis), 33 items : moules, carbonnade, stoemp, waterzooi, lapin à la gueuze, légumes, salade liégeoise, hochepot, escavèche, steak-frites, boudin-compote, chicons au gratin, vol-au-vent, flamiche, croque-monsieur
- [x] Bols rendus en mangeant ; tag `onions` (oignon ou poireau)
- **Accepté si** : chaque plat sort d'une ligne automatique. ✅ (recettes et bassins réels testés ; trois assemblages sur de vraies machines : croque-monsieur, chicons au gratin, flamiche ; fumage au ventilateur)

### M16 — Snacks de friterie ✅ (25/10/2026)
- [x] Sauces tartare, pickles, brazil et leurs 6 cornets
- [x] 33 snacks finis (mitraillette, boulets, croquettes, saucisses, loempia, américain, burgers, hot-dog, pita, brochette, galettes, calamars, poulet, rondelles d'oignon), 82 items
- [x] Un item de transition partagé pour les 8 panés ; Deployer qui garde ses cisailles
- **Accepté si** : chaque snack sort d'une ligne automatique. ✅ en partie : chaque recette est chargée et testée (GameTests de recettes et de bassin) ; trois lignes réelles (mitraillette, panure de deux croquettes, ailes) tournent sur de vraies machines ; les autres chaînes utilisent les mêmes machines et seront montrées en Ponder au M21
