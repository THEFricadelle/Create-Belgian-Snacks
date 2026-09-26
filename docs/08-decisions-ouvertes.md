# 08 — Décisions ouvertes

À trancher par THEFricadelle. Tant qu'une décision n'est pas prise, le code utilise une valeur provisoire configurable, signalée comme telle.
Quand une décision est prise, remplir la colonne « Décision » + la date, puis mettre à jour les docs concernées.

## Équilibrage des paliers

| # | Question | Proposition provisoire | Décision |
|---|---|---|---|
| D1 | Quel **taux** d'aliments du modpack pour THE_Fricadelle ? | 25 % (config `grinder.theFricadelleRatio`). À fixer après un `/belgiansnacks foods export` sur le vrai modpack. | |
| D2 | Le taux D1 : n'importe quels aliments, ou des **catégories imposées** (x viandes, x fruits, x plats…) ? | N'importe lesquels (plus simple) | |
| D3 | Palier 1 : 3 viandes **fixes** (porc/bœuf/poulet) ou **3 hachis différents quelconques** ? | Fixes en v1 (recette Create standard), quelconques = type de recette custom | ✅ **Fixes** (porc, bœuf, poulet) ; hachis de mouton et de lapin retirés de la v1 (26/09/2026) |
| D4 | Nombre de boucles de la Sequenced Assembly | 3 | |
| D5 | Sequenced Assembly : 100 % de réussite, ou échecs possibles (sortie « pâte ratée ») ? | 100 % | |
| D6 | Recette des épices belges | ? | Provisoire : **Meule**, 1 algue séchée → 1 épices + 25 % d'une 2e (26/09/2026) |
| D7 | Secours si pas d'oignon / tomate (sans FD) | betterave | Provisoire : betterave pour le curry ketchup (`mixing/curry_ketchup_from_beetroot`) ; oignon au M7 |
| D8 | Enchanted golden apple et autres items quasi introuvables : dans la liste ou blacklist ? | blacklist par défaut | |
| D9 | Hachoir : vider la progression après production, ou ne consommer que le nécessaire ? | vider | |
| D10 | Hachoir : progression conservée quand on casse le bloc ? | oui | |
| D11 | Coûts des machines (recettes de craft, stress, vitesse min) | voir `05-machines.md` | |
| D12 🖥️ | Garder notre `minced_beef` ou utiliser celui de Farmer's Delight (présent dans Arcadia) ? | utiliser un tag commun ; notre item seulement si FD absent | ✅ **Toujours enregistré** + tag `bs:minced_meats/beef` (le nôtre + celui de FD en optionnel). Enregistrement conditionnel écarté : il fait disparaître l'item des mondes existants (26/09/2026) |
| D13 🖥️ | Réutiliser huile/mayo/ketchup d'autres mods du pack s'ils existent ? | oui, via tags | ✅ **Huiles** : tag fluide `bs:frying_oils` (notre huile, blanc de bœuf fondu, `#c:plantoil`, `#c:vegetable_oil`) ; notre recette graines → huile désactivée si Crafts & Additions ou Diesel Generators est chargé. **Mayo/ketchup** : aucun dans le pack, les nôtres restent (26/09/2026) |
| D16 | Rôle du blanc de bœuf | — | ✅ **Fondu, il remplace l'huile pour THE_FRICADELLE** : fluide `melted_beef_tallow`, seule graisse du palier 3 (mixer super-chauffé et friture) ; utilisable aussi aux paliers 1 et 2 (26/09/2026) |
| D17 | Haché et chapelure des autres mods | — | ✅ **Interchangeables dans les deux sens** : nos recettes lisent `bs:minced_meats/*` et `#c:bread_crumbs` (qui incluent `c:ground_*`, `c:minced_beef`) ; nos items rejoignent ces tags `c:` (26/09/2026) |
| D14 🖥️ | Spice of Life: Onion : impact sur l'équilibrage des fricadelles | à tester en jeu | |
| D15 🖥️ | Create Heat JS : quelles sources de chaleur la Friteuse accepte | à tester en jeu | |

🖥️ = à trancher pendant les sessions de dev, au moment du jalon concerné.

## Effets en mangeant (« on définira chaque point ensemble »)

| Aspect | Fricadelle | THE_Fricadelle | THE_FRICADELLE | Décision |
|---|---|---|---|---|
| Nutrition (demi-cuisses) | 6 ? | 10 ? | 20 ? | |
| Saturation (modificateur) | 0.6 ? | 1.0 ? | 2.0 ? | |
| Temps pour manger | normal ? | normal ? | long ? | |
| Mangeable même rassasié | non | non | oui ? | |
| Effets de potion | ? | ? | ? | |
| Gag son | burp | ? | ? | |
| Gag particules / message chat | ? | ? | message serveur global ? | |
| Advancement | « Une fricadelle, une ! » ? | ? | « J'ai tout mangé » ? | |

Idées d'advancements à trier : première Friteuse, première huile au blanc de bœuf, 100 fricadelles produites, Hachoir à 50 %, THE_FRICADELLE mangée.

## Technique / organisation

| # | Question | Proposition | Décision |
|---|---|---|---|
| T1 | JEI ou EMI ? | — | ✅ **JEI** (26/09/2026) |
| T2 | Modpack cible | — | ✅ **Arcadia: Echoes of Power V2** 2.0.32 — versions relevées dans `11-compat-arcadia.md` (26/09/2026) |
| T3 | Licence du mod | — | ✅ **All Rights Reserved** (code + assets) (26/09/2026) |
| T4 | Publication publique (Modrinth/CurseForge) ou privée au modpack ? | ? | ✅ **Public : CurseForge + Modrinth + GitHub Releases** ; Arcadia référence le fichier officiel CurseForge (licence §3(b)) (26/09/2026) |
| T5 | Où héberger le repo ? | repo **perso** de THEFricadelle (privé conseillé avec ARR) | ✅ **GitHub public** `THEFricadelle/Create-Belgian-Snacks` (source visible, ARR) ; issues = contact de la licence (§11) (26/09/2026) |
| T6 | Traduction `fr_fr` via datagen ou fichier manuel | selon ce qui est le plus simple au M1 | ✅ **Datagen** (`BSFrenchLangProvider`), Registrate ne générant que `en_us` (26/09/2026) |
| T7 | Titulaire des droits ARR | — | ✅ **THEFricadelle seul** — projet perso, la team ne contribue pas au code (26/09/2026) |
| T8 | Forme du nom d'auteur dans les métadonnées et le copyright | — | ✅ **`THEFricadelle`** (sans underscore) ; « THE_Fricadelle » reste le nom de l'item du palier 2 (26/09/2026) |
