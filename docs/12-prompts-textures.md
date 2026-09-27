# 12 — Prompts pour générer les textures

Pour générer les 41 textures du mod avec un assistant d'image (GPT Astra dans VS Code). Les modèles 3D viendront ensuite, sur Blockbench (`06-assets.md`, « Livrables »).

## Mode d'emploi

1. Donne d'abord le **brief général** ci-dessous, une seule fois, en début de conversation.
2. Puis un prompt par texture, dans l'ordre des sections (chaque section réutilise la précédente : même bâtonnet, même seau, même panneau).
3. Pour chaque image obtenue :
   ```
   python tools/pixelate.py <image générée>.png <chemin de la texture> [--size 16x32] [--opaque]
   ```
   `--opaque` pour les faces de blocs et les fluides ; `--size 16x32` pour les textures `_flow`. Le script réduit sans lissage et rend le fond magenta transparent.
4. `python tools/asset_status.py` : chaque texture passe de « placeholder » à « hand-made », et le script signale ce qui ne va pas (taille, pixels semi-transparents, trop de couleurs).
5. `./gradlew runClient` pour voir le résultat en jeu, ou `./gradlew runClientSmoke` pour les captures automatiques (`run/clientsmoke/screenshots`).

Si l'assistant sait écrire un PNG 16×16 pixel par pixel directement, c'est encore mieux : l'étape 3 devient inutile, l'étape 4 reste.

## Brief général (à coller en premier)

```
You are drawing original pixel-art textures for a Minecraft mod, "Create: Belgian Snacks", an addon
for the Create mod about the Belgian fricadelle (a long, skinless, deep-fried minced-meat sausage,
served "spéciale" with mayonnaise, curry ketchup and chopped onion).

Style
- Minecraft item and block style: flat pixel art on a 16x16 grid (16x32 for animated fluid frames).
- The feel of the Create mod: warm brass, brushed steel, chunky rivets, clean industrial shapes.
  Create-inspired, never copied: do not reproduce any texture from Minecraft or from Create.
- Light comes from the top left; one-pixel darker outline around items; 2 to 4 shades per colour.
- A limited palette: at most 16 colours per texture. No gradients, no anti-aliasing, no blur,
  no dithering noise, no text, no letters, no numbers, no drop shadow, no glow halo.

Output
- A square image of 512x512 where every one of the 16x16 cells is a solid 32x32 block of one
  colour, aligned to the grid (for 16x32 textures: 512x1024, same 32x32 cells).
- Items: the background is pure magenta #FF00FF (it becomes transparent); nothing magenta inside
  the item. Blocks and fluids: no background, the texture fills the whole grid and must tile.
- One texture per image. I will give you the file name, size and content of each.
```

## 1. Les fricadelles et leurs pâtes (items, 16×16, fond magenta)

Tous les bâtonnets partagent la même silhouette : diagonale du bas-gauche vers le haut-droit, comme un bâton ou une carotte vanilla, bouts arrondis, 2 à 3 pixels d'épaisseur. Génère d'abord `raw_fricadelle`, puis dis à l'assistant de **garder exactement la même forme** pour les suivants.

| Fichier `textures/item/…` | Prompt |
|---|---|
| `raw_fricadelle.png` | Item icon, 16x16: a raw fricadelle, a long thin skinless sausage lying diagonally from bottom-left to top-right, rounded ends, pale pink-beige raw minced meat with a few lighter fat specks. This silhouette is the reference for every sausage that follows. |
| `fricadelle.png` | Same silhouette as raw_fricadelle, now deep-fried: dark golden brown, crisp slightly bumpy surface, two or three bright highlight pixels on the top-left edge. |
| `raw_the_fricadelle.png` | Same silhouette, raw pale pink-beige, dressed "spéciale": a thin white mayonnaise line along the top, a red curry ketchup line beside it, and tiny white-and-yellow diced onion pixels scattered on top. |
| `the_fricadelle.png` | Same as raw_the_fricadelle but the sausage is fried golden brown; mayonnaise, curry ketchup and onion bits on top, clearly readable. |
| `raw_ultimate_fricadelle.png` | Same silhouette, one pixel thicker, raw, glossy pale gold (glazed in beef tallow), full "spéciale" toppings, two or three tiny gold sparkle pixels. |
| `ultimate_fricadelle.png` | The ultimate version: fried deep gold-bronze, thicker, full toppings, a few bright gold highlight pixels. Do not draw an enchantment glint: the game adds it. |
| `incomplete_the_fricadelle.png` | Same silhouette as raw_the_fricadelle but unfinished: flattened, a half-drawn mayonnaise line, no onion yet, a slightly paler tone. |
| `incomplete_ultimate_fricadelle.png` | Same silhouette as raw_ultimate_fricadelle but unfinished: partly glazed with white beef tallow, half the toppings, paler. |
| `fricadelle_paste.png` | A round ball of raw meat paste, 10 pixels wide, sitting low in the frame, smooth pink-beige with a few darker specks. |
| `exceptional_paste.png` | Same ball shape, warm golden-orange paste with small multicolour flecks (green, red, yellow, brown: many different foods mixed in). |
| `absolute_paste.png` | Same ball shape, rich bright gold, many multicolour flecks, three or four white-gold sparkle pixels around it: every food in one paste. |

## 2. Les ingrédients (items, 16×16, fond magenta)

Même famille pour les trois hachis : un petit tas conique posé en bas de l'image.

| Fichier `textures/item/…` | Prompt |
|---|---|
| `minced_pork.png` | A small cone-shaped heap of raw minced pork at the bottom of the frame, pink with lighter pink fat specks, granular texture. The reference heap shape for the next two. |
| `minced_beef.png` | Same heap shape as minced_pork, raw minced beef: dark red with small white fat specks. |
| `minced_chicken.png` | Same heap shape, raw minced chicken: pale beige-pink. |
| `bread_crumbs.png` | Same heap shape, golden-brown bread crumbs, coarse grains with light and dark crumbs. |
| `belgian_spices.png` | A small flat pile of spice powder: orange-red paprika and curry tones with a few dark grains and a couple of loose grains above the pile. |
| `beef_tallow.png` | A small block of solid beef tallow: a rounded brick, creamy off-white, waxy, soft yellowish shadow on the right and bottom. |

## 3. Les seaux (items, 16×16, fond magenta)

Génère d'abord le seau de `frying_oil_bucket`, puis demande **le même seau, pixel pour pixel, en ne changeant que la couleur du liquide**.

| Fichier `textures/item/…` | Prompt |
|---|---|
| `frying_oil_bucket.png` | An original metal bucket (grey steel pail, three-quarter view, visible handle, rim seen from slightly above), full of golden frying oil #E8C547 with a lighter reflection pixel. Draw your own bucket, do not reproduce Minecraft's bucket texture. The reference bucket for the next three. |
| `melted_beef_tallow_bucket.png` | Exactly the same bucket, filled with melted beef tallow: milky off-white #FBFAF4 with a warm cream shade. |
| `mayonnaise_bucket.png` | Exactly the same bucket, filled with mayonnaise: pale cream #F3EBC4, thick and glossy. |
| `curry_ketchup_bucket.png` | Exactly the same bucket, filled with curry ketchup: deep red #A8321E, thick. |

## 4. Les fluides (sans fond, `--opaque`)

Surface liquide vue de dessus, qui se répète sans couture. Le `flow` est une animation de 2 images empilées (16×32).

| Fichier `textures/fluid/…` | Taille | Prompt |
|---|---|---|
| `frying_oil_still.png` | 16x16 | Seamless tileable top view of still hot frying oil, golden #E8C547, gentle ripples and two or three small bubbles, fully opaque. |
| `frying_oil_flow.png` | 16x32 | Two stacked 16x16 frames of flowing frying oil (same colours), streaks running downward, the second frame shifted down by 4 pixels so it loops, each frame tileable. |
| `melted_beef_tallow_still.png` | 16x16 | Seamless tileable still melted beef tallow, milky off-white #FBFAF4 with warm cream shades, soft ripples. |
| `melted_beef_tallow_flow.png` | 16x32 | Two stacked frames of flowing melted beef tallow, same rules as frying_oil_flow. |
| `mayonnaise_still.png` | 16x16 | Seamless tileable still mayonnaise, pale cream #F3EBC4, thick with soft glossy swirls. |
| `mayonnaise_flow.png` | 16x32 | Two stacked frames of flowing mayonnaise, thick slow streaks, same rules as frying_oil_flow. |
| `curry_ketchup_still.png` | 16x16 | Seamless tileable still curry ketchup, deep red #A8321E with darker spice specks, thick. |
| `curry_ketchup_flow.png` | 16x32 | Two stacked frames of flowing curry ketchup, same rules as frying_oil_flow. |

## 5. Les blocs (sans fond, `--opaque`)

### Friteuse (inox)

| Fichier `textures/block/fryer/…` | Prompt |
|---|---|
| `side.png` | Seamless 16x16 block side: brushed stainless steel panel, light blue-grey, one-pixel darker frame, four small rivets in the corners, a thin horizontal vent slot near the bottom. Industrial kitchen appliance feel, Create-inspired. |
| `top.png` | 16x16 top face of the vat walls: the same stainless steel, a polished rim with a bright highlight along one edge. |
| `bottom.png` | 16x16 bottom plate: darker stainless steel, four bolts, a faint cross-hatched heat-resistant pattern. |
| `inner.png` | 16x16 inside wall of the vat: darker worn steel with an oily golden tide mark two pixels high near the middle and a few burnt specks. |

### Hachoir Suprême (laiton)

| Fichier `textures/block/supreme_grinder/…` | Prompt |
|---|---|
| `side_0.png` | Seamless 16x16 block side: warm brass casing (golden yellow with brown outlines, Create-inspired, not copied), riveted frame. In the centre a vertical gauge window 4 pixels wide and 12 pixels tall (columns 6 to 9, rows 2 to 13), dark inside, split into 4 equal segments, all unlit. This is the reference for side_1 to side_4. |
| `side_1.png` | Exactly side_0, with the bottom gauge segment lit bright green. |
| `side_2.png` | Exactly side_0, with the two bottom segments lit bright green. |
| `side_3.png` | Exactly side_0, with the three bottom segments lit bright green. |
| `side_4.png` | Exactly side_0, with all four segments lit bright green. |
| `top.png` | 16x16 top face: brass plate with a dark round opening in the centre (the grinding pit, where a shaft comes in), a ring of bolts around it. |
| `bottom.png` | 16x16 bottom and outer rim: darker aged brass and iron plate, bolts at the corners. |
| `blade.png` | 16x16 texture for the grinder blades: polished steel, a bright sharp edge along one side, a darker spine. |

## Après les textures

- `python tools/asset_status.py` doit afficher 41 textures « hand-made » et aucune ligne « to fix ».
- Les textures `_flow` gardent leur fichier `.mcmeta` (déjà en place) : ne pas le supprimer.
- Commit : `feat: hand-made textures` (les placeholders ne reviennent jamais : `gen_placeholders.py` n'écrase rien sans `--force`).
