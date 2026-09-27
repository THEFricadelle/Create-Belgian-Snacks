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
- [x] Fricadelle mangeable (valeurs provisoires) + gag minimal (rot `fricadelle.burp`)
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

### M9 — Finitions & publication
- [ ] Scènes Ponder (Friteuse, Hachoir)
- [ ] Recettes de craft des machines équilibrées
- [ ] Vraies textures / modèles Blockbench
- [ ] Page mod (Modrinth/CurseForge) — voir `09-licence-publication.md`
