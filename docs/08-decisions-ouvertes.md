# 08 — Décisions ouvertes

À trancher par THEFricadelle. Tant qu'une décision n'est pas prise, le code utilise une valeur provisoire configurable, signalée comme telle.
Quand une décision est prise, remplir la colonne « Décision » + la date, puis mettre à jour les docs concernées.

## Équilibrage des paliers

| # | Question | Proposition provisoire | Décision |
|---|---|---|---|
| D1 | Quel **taux** d'aliments du modpack pour THE_Fricadelle ? | 25 % (config `grinder.theFricadelleRatio`). À fixer après un `/belgiansnacks foods export` sur le vrai modpack. | ✅ **10 %**, soit ≈ 180 aliments sur les 1804 d'Arcadia 2.0.32 (export du 27/09/2026, `docs/data/`), config `grinder.theFricadelleRatio` = 0.10 (27/09/2026) |
| D2 | Le taux D1 : n'importe quels aliments, ou des **catégories imposées** (x viandes, x fruits, x plats…) ? | N'importe lesquels (plus simple) | ✅ **N'importe lesquels** : X % de tous les aliments uniques du FoodIndex, sans catégories imposées (27/09/2026) |
| D3 | Palier 1 : 3 viandes **fixes** (porc/bœuf/poulet) ou **3 hachis différents quelconques** ? | Fixes en v1 (recette Create standard), quelconques = type de recette custom | ✅ **Fixes** (porc, bœuf, poulet) ; hachis de mouton et de lapin retirés de la v1 (26/09/2026) |
| D4 | Nombre de boucles de la Sequenced Assembly | 3 | ✅ **3 boucles** de la séquence épices → mayonnaise → curry ketchup → oignon → presse (27/09/2026) |
| D5 | Sequenced Assembly : 100 % de réussite, ou échecs possibles (sortie « pâte ratée ») ? | 100 % | ✅ **100 %** de réussite, pas de pâte ratée (27/09/2026) |
| D6 | Recette des épices belges | ? | Provisoire : **Meule**, 1 algue séchée → 1 épices + 25 % d'une 2e (26/09/2026) |
| D7 | Secours si pas d'oignon / tomate (sans FD) | betterave | ✅ **Betterave**, dans une variante active seulement si le tag est vide : `mixing/curry_ketchup_from_beetroot` (tomate) et `sequenced_assembly/raw_the_fricadelle_from_beetroot` (oignon). Arcadia a les deux via Farmer's Delight et ne charge aucune variante (27/09/2026) |
| D8 | Enchanted golden apple et autres items quasi introuvables : dans la liste ou blacklist ? | blacklist par défaut | ✅ Blacklist : mod `cosmeticarmoursmod` (config `blacklistedMods`), `minecraft:ominous_bottle`, `mynethersdelight:enchanted_golden_egg`, `artifacts:everlasting_beef` (tag). **La pomme d'or enchantée compte** (27/09/2026) |
| D9 | Hachoir : vider la progression après production, ou ne consommer que le nécessaire ? | vider | ✅ **Vider tout.** Si la collection dépasse l'objectif du mode THE_ (typiquement après un passage d'ULTIME à THE_), la pâte sort quand même et tout l'excédent part avec : c'est une tentative de THE_FRICADELLE ratée, annoncée aux joueurs proches (27/09/2026) |
| D10 | Hachoir : progression conservée quand on casse le bloc ? | oui | ✅ **Oui**, collection et mode dans l'item (data component `grinder_contents`), comme la graisse de la Friteuse (27/09/2026) |
| D11 | Coûts des machines (recettes de craft, stress, vitesse min) | voir `05-machines.md` | ✅ Friteuse, **validée le 27/09/2026** : **Mechanical Crafter**, 2 plaques de cuivre + barreaux de fer + réservoir + bassin + Precision Mechanism (27/09/2026). Hachoir (27/09/2026) : **64 tr/min minimum, 8 SU/tr/min** (512 SU, le coût d'une Crushing Wheel, config `grinder.minSpeed` / `stressImpact`) ; **Mechanical Crafter 3×3** : plaques de laiton + Precision Mechanism / Crushing Wheel + Brass Casing + Crushing Wheel / or + bloc de fer + or |
| D12 🖥️ | Garder notre `minced_beef` ou utiliser celui de Farmer's Delight (présent dans Arcadia) ? | utiliser un tag commun ; notre item seulement si FD absent | ✅ **Toujours enregistré** + tag `bs:minced_meats/beef` (le nôtre + celui de FD en optionnel). Enregistrement conditionnel écarté : il fait disparaître l'item des mondes existants (26/09/2026) |
| D13 🖥️ | Réutiliser huile/mayo/ketchup d'autres mods du pack s'ils existent ? | oui, via tags | ✅ **Huiles** : tag fluide `bs:frying_oils` (notre huile, blanc de bœuf fondu, `#c:plantoil`, `#c:vegetable_oil`) ; notre recette graines → huile désactivée si Crafts & Additions ou Diesel Generators est chargé. **Mayo/ketchup** : aucun dans le pack, les nôtres restent (26/09/2026) |
| D16 | Rôle du blanc de bœuf | — | ✅ **Fondu, il remplace l'huile pour THE_FRICADELLE** : fluide `melted_beef_tallow`, seule graisse du palier 3 (mixer super-chauffé et friture) ; utilisable aussi aux paliers 1 et 2 (26/09/2026) |
| D18 | Friteuse : par lot ou item par item ? | — | ✅ **Par lot de 16** (`fryer.maxBatch`), sauf le palier 3 : tag `fryer/one_at_a_time`, un par un (26/09/2026) |
| D19 | Friteuse cassée : huile perdue ou conservée ? | — | ✅ **Conservée** dans l'item (data component `fryer_fluid`) ; les items tombent (26/09/2026) |
| D20 | Friture de THE_Fricadelle | — | ✅ **Chauffé, toute graisse `#frying_oils`**, 200 ticks, 25 mB par item, par lot ; le blanc de bœuf seul reste réservé au palier 3 (27/09/2026) |
| D21 | Palier 3 : quelle part des aliments ? | 100 % | ✅ **100 %**, toutes les variantes de Create: Food comprises (config `grinder.ultimateRatio` = 1.0) (27/09/2026) |
| D22 | Hachoir : arrondi de l'objectif | — | ✅ **Au-dessus** : `ceil(taux × total)`, donc 181 aliments sur 1804 pour 10 %. Un index vide ne produit jamais rien (27/09/2026) |
| D17 | Haché et chapelure des autres mods | — | ✅ **Interchangeables dans les deux sens** : nos recettes lisent `bs:minced_meats/*` et `#c:bread_crumbs` (qui incluent `c:ground_*`, `c:minced_beef`) ; nos items rejoignent ces tags `c:` (26/09/2026) |
| D14 🖥️ | Spice of Life: Onion : impact sur l'équilibrage des fricadelles | à tester en jeu | |
| D15 🖥️ | Create Heat JS : quelles sources de chaleur la Friteuse accepte | à tester en jeu | |

🖥️ = à trancher pendant les sessions de dev, au moment du jalon concerné.

## Effets en mangeant (tranchés le 27/09/2026)

| Aspect | Fricadelle | THE_Fricadelle | THE_FRICADELLE |
|---|---|---|---|
| Nutrition (demi-cuisses) | 6 | 10 | 20 |
| Saturation (modificateur) | 0.6 | 1.0 | 2.0 |
| Temps pour manger | normal | normal | normal |
| Mangeable même rassasié | non | non | oui |
| Effets de potion | aucun | Régénération I 10 s, Absorption I 1 min | Régénération II 1 min, Absorption IV 3 min, Force II, Résistance II et Résistance au feu 5 min |
| Gag son | — | — | feu d'artifice (le rot des trois paliers est retiré le 27/09/2026) |
| Gag particules / message | — | — | feu d'artifice ; message à tout le serveur ; **THEFricadelle en personne** (voir plus bas) |
| Advancement | « Une fricadelle, une ! » | « Avec une spéciale » | « J'ai tout mangé » (défi) |

Nutrition, saturation et temps : valeurs gardées, « on reviendra sûrement dessus plus tard ». Toutes les valeurs vivent dans `BSFoods`.

**THEFricadelle en personne** : un PNJ apparaît dans un nuage de fumée quelques blocs devant celui qui mange THE_FRICADELLE, le rejoint en courant, le regarde, applaudit, dit une phrase tirée au hasard parmi 5 (à voix haute et au-dessus de sa tête, plus dans le chat depuis le 27/09/2026 ; voix enregistrée par l'auteur, voir `06-assets.md`), puis s'envole comme une fusée et éclate en feu d'artifice (visuel seulement : aucun bloc cassé, aucun dégât). Le tout dure 8 s au plus (course et envol décidés le 27/09/2026 ; sons vanilla : chorus à l'apparition, fusée et explosion au départ). Invulnérable, sans collision, sans loot, jamais sauvegardé. Son skin est **celui du compte Minecraft THE_Fricadelle** (le pseudo de l’auteur ; le nom affiché reste THEFricadelle), chargé en ligne comme une tête de joueur (skin par défaut sans internet).

**Advancements** (onglet « Create: Belgian Snacks ») : hacher de la viande (racine) → « Friterie ouverte » (poser une Friteuse) → « Blanc de bœuf » (seau) et « Une fricadelle, une ! » → « Le Hachoir Suprême » (en poser un) → « Exceptionnelle » → « Avec une spéciale » ; et « À mi-chemin » (Hachoir à 50 % de tous les aliments, déclencheur maison) → « Absolue » (défi) → « J'ai tout mangé » (défi). « 100 fricadelles produites » écarté pour l'instant.

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
