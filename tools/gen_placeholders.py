#!/usr/bin/env python3
# Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
# SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
#
# Proprietary, closed-source software. Access to this source is restricted and
# grants no right to copy, share, reuse, redistribute, or create derivative works.
# See LICENSE and CONTRIBUTING.md at the repository root.
"""Generate 16x16 placeholder item textures, standard library only.

Items, fluids (still, flow), buckets and block faces. Existing files are never overwritten unless --force is given, so real textures
dropped in place are safe. Usage: python tools/gen_placeholders.py [--force]
"""

import argparse
import math
import pathlib
import struct
import zlib

SIZE = 16
ROOT = pathlib.Path(__file__).resolve().parent.parent
ITEM_DIR = ROOT / "src/main/resources/assets/create_belgian_snacks/textures/item"
FLUID_DIR = ROOT / "src/main/resources/assets/create_belgian_snacks/textures/fluid"
BLOCK_DIR = ROOT / "src/main/resources/assets/create_belgian_snacks/textures/block"

# id -> (shape, base colour, glyph). Digits mark the tier, letters the ingredient.
ITEMS = {
    "minced_pork": ("heap", (0xE8, 0x9A, 0x9A), "P"),
    "minced_beef": ("heap", (0xB0, 0x3A, 0x3A), "B"),
    "minced_chicken": ("heap", (0xF0, 0xC8, 0xA0), "C"),
    "beef_tallow": ("cube", (0xF4, 0xEE, 0xD8), "T"),
    "bread_crumbs": ("heap", (0xD8, 0xA8, 0x58), "R"),
    "belgian_spices": ("powder", (0xC0, 0x60, 0x20), "S"),
    "fricadelle_paste": ("ball", (0xC8, 0x80, 0x78), "1"),
    "raw_fricadelle": ("stick", (0xE8, 0x98, 0x98), "1"),
    "fricadelle": ("stick", (0x9A, 0x5A, 0x28), "1"),
    "exceptional_paste": ("ball", (0xD0, 0x90, 0x40), "2"),
    "incomplete_the_fricadelle": ("stick", (0xD8, 0xB0, 0x80), "2"),
    "raw_the_fricadelle": ("stick", (0xF0, 0xA8, 0x90), "2"),
    "the_fricadelle": ("stick", (0xB8, 0x70, 0x20), "2"),
    "absolute_paste": ("ball", (0xE8, 0xC8, 0x40), "3"),
    "incomplete_ultimate_fricadelle": ("stick", (0xF0, 0xD8, 0xA0), "3"),
    "raw_ultimate_fricadelle": ("stick", (0xF8, 0xC8, 0xA8), "3"),
    "ultimate_fricadelle": ("shiny_stick", (0xE0, 0xA8, 0x20), "3"),
    # v2 fries (docs/13, section 5.1).
    "raw_fries": ("stick", (0xEE, 0xE0, 0xB8), "F"),
    "blanched_fries": ("stick", (0xEE, 0xD8, 0x98), "F"),
    "fries": ("stick", (0xEE, 0xC1, 0x5A), "F"),
    "fries_beef_tallow": ("shiny_stick", (0xE6, 0xAE, 0x4E), "T"),
    "paper_cone": ("heap", (0xF2, 0xEE, 0xE2), "K"),
    "cone_of_fries": ("heap", (0xEE, 0xC1, 0x5A), "K"),
    "cone_of_fries_beef_tallow": ("heap", (0xE6, 0xAE, 0x4E), "K"),
    # v2 base ingredients (docs/13, section 5.7).
    "pearl_sugar": ("powder", (0xF8, 0xF8, 0xF0), "S"),
    "cassonade": ("powder", (0xA8, 0x6A, 0x2C), "C"),
    "powdered_sugar": ("powder", (0xFF, 0xFF, 0xFF), "S"),
    "salt": ("powder", (0xE8, 0xEC, 0xF0), "S"),
    "butter": ("cube", (0xF4, 0xD8, 0x6A), "B"),
    "young_herve_cheese": ("cube", (0xF0, 0xE0, 0xA0), "C"),
    "herve_cheese": ("cube", (0xD8, 0x8A, 0x3A), "C"),
    "maton": ("heap", (0xF4, 0xF0, 0xE6), "T"),
    "brewers_yeast": ("heap", (0xC8, 0xA8, 0x70), "B"),
    "sprouted_grain": ("heap", (0xC8, 0xB0, 0x60), "R"),
    "malt": ("heap", (0x9A, 0x62, 0x2C), "R"),
    "pistolet_dough": ("ball", (0xEC, 0xDC, 0xB8), "P"),
    "pistolet": ("ball", (0xC8, 0x8A, 0x40), "P"),
    "puff_pastry": ("cube", (0xF2, 0xE2, 0xB0), "P"),
    "brioche_dough": ("ball", (0xF4, 0xD8, 0x8C), "B"),
    "burger_bun_dough": ("ball", (0xF0, 0xD8, 0x98), "B"),
    "burger_bun": ("ball", (0xC8, 0x7C, 0x30), "B"),
    "pita_dough": ("ball", (0xEE, 0xE2, 0xC4), "P"),
    "raw_pita": ("heap", (0xEE, 0xE2, 0xC4), "P"),
    "pita_bread": ("heap", (0xDC, 0xB4, 0x74), "P"),
    "noodles": ("heap", (0xF0, 0xD8, 0x80), "R"),
    "lardons": ("heap", (0xE8, 0x9A, 0x9A), "P"),
    "brined_ham": ("ball", (0xE0, 0x88, 0x88), "P"),
    "ardennes_ham": ("ball", (0x9A, 0x3A, 0x30), "P"),
    "raw_meringue": ("heap", (0xF8, 0xF6, 0xF0), "S"),
    "meringue": ("heap", (0xF4, 0xE6, 0xC8), "S"),
    "raisins": ("heap", (0x4A, 0x2A, 0x3A), "R"),
    "apple_compote": ("heap", (0xD8, 0xC8, 0x6A), "C"),
    "chocolate_shavings": ("powder", (0x5A, 0x34, 0x1E), "C"),
    "vanilla_ice_cream": ("heap", (0xFA, 0xF2, 0xD8), "C"),
    # v2 crops and mussels (docs/13, section 5.8).
    "chicory_root": ("stick", (0x9a, 0x6a, 0x3a), "C"),
    "chicon": ("stick", (0xf2, 0xec, 0xc0), "C"),
    "leek": ("stick", (0x7c, 0xb0, 0x4a), "P"),
    "hop_seeds": ("powder", (0x8a, 0x6a, 0x3a), "S"),
    "hops": ("ball", (0x9a, 0xc0, 0x5a), "B"),
    "hop_shoots": ("stick", (0x8a, 0xb8, 0x4a), "S"),
    "brussels_sprout": ("ball", (0x6a, 0xa0, 0x3a), "B"),
    "asparagus_crown": ("heap", (0x8a, 0x6a, 0x4a), "R"),
    "white_asparagus": ("stick", (0xf0, 0xec, 0xd8), "P"),
    "green_asparagus": ("stick", (0x6a, 0xa0, 0x3a), "P"),
    "buckwheat": ("powder", (0x5a, 0x40, 0x2a), "B"),
    "buckwheat_flour": ("powder", (0xa8, 0x98, 0x88), "B"),
    "mustard_seeds": ("powder", (0xc8, 0xa0, 0x3a), "S"),
    "mustard_powder": ("powder", (0xe8, 0xc0, 0x3a), "S"),
    "mussel": ("ball", (0x2a, 0x2a, 0x3a), "R"),
    "bouchot": ("stick", (0x8a, 0x6a, 0x4a), "B"),
    # v2 friterie snacks (docs/13, section 5.10).
    "half_baguette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "incomplete_mitraillette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "mitraillette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "boulet_paste": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_boulet": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "boulet": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "boulets_liegeois": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "incomplete_breaded": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "shrimp_croquette_mix": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "cheese_croquette_mix": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "chicken_croquette_mix": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "lucifer_mix": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "potato_croquette_mix": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "chicken_strips": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "fish_strips": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "cheese_strips": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_shrimp_croquette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_cheese_croquette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_chicken_croquette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_lucifer": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_potato_croquette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_chicken_fingers": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_fish_sticks": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_cheese_sticks": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "shrimp_croquette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "cheese_croquette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "chicken_croquette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "lucifer": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "potato_croquette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "chicken_fingers": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "fish_sticks": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "cheese_sticks": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "cervelas_mix": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "mexicano_mix": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "viandelle_mix": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_cervelas": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_mexicano": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_viandelle": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "cervelas": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "mexicano": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "viandelle": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "loempia_wrapper": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "incomplete_loempia": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_loempia": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "loempia": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "filet_americain": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "pistolet_americain": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "americain_frites": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_patty": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "patty": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "incomplete_burger": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "friterie_burger": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "friterie_cheeseburger": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "hot_dog_bun_dough": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "hot_dog_bun": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "incomplete_hot_dog": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "hot_dog": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "gyros": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "incomplete_pita": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "pita": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "incomplete_brochette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_brochette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "brochette": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "bami_mix": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "nasi_mix": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_bami_disc": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_nasi_disc": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "bami_disc": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "nasi_disc": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_squid_rings": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "squid_rings": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_chicken_nuggets": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "chicken_nuggets": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "corn_flakes": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_chicken_crunch": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "chicken_crunch": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_chicken_wings": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "fried_chicken_wings": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "spicy_chicken_wings": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "raw_onion_rings": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "battered_onion_rings": ("ball", (0xD8, 0x9A, 0x44), "F"),
    "onion_rings": ("ball", (0xD8, 0x9A, 0x44), "F"),
    # v2 Belgian dishes (docs/13, section 5.11).
    "moules_marinieres": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "moules_a_la_biere": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "moules_frites": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "carbonnade": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "carbonnade_frites": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "stoemp": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "stoemp_saucisse": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "waterzooi": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "lapin_a_la_gueuze": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "brussels_sprouts_bacon": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "asparagus_flemish": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "hop_shoots_cream": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "salade_liegeoise": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "hochepot": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "escaveche": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "boudin_compote": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "steak_frites": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "incomplete_chicons_au_gratin": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "raw_chicons_au_gratin": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "chicons_au_gratin": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "raw_vol_au_vent_shell": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "vol_au_vent_shell": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "incomplete_vol_au_vent": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "vol_au_vent": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "raw_boudin": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "boudin": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "tart_shell": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "incomplete_tart": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "raw_flamiche": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "flamiche": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "incomplete_croque_monsieur": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "raw_croque_monsieur": ("heap", (0xA0, 0x64, 0x3A), "K"),
    "croque_monsieur": ("heap", (0xA0, 0x64, 0x3A), "K"),
    # v2 sauced cones (docs/13, section 5.2), in the colour of their sauce.
    **{f"cone_of_fries_{s}": ("heap", c, "K") for s, c in [
        ("mayonnaise", (0xF3, 0xEB, 0xC4)), ("curry_ketchup", (0xA8, 0x32, 0x1E)), ("andalouse", (0xE8, 0x86, 0x2A)),
        ("samourai", (0xD9, 0x54, 0x2B)), ("americaine", (0xE8, 0x9A, 0x7A)), ("sauce_lapin", (0x5A, 0x32, 0x18)),
        ("tartare", (0xE8, 0xE6, 0xC4)), ("pickles", (0xCC, 0xBC, 0x30)), ("brazil", (0xEE, 0xA0, 0x40))]},
    **{f"cone_of_fries_beef_tallow_{s}": ("heap", c, "K") for s, c in [
        ("mayonnaise", (0xF3, 0xEB, 0xC4)), ("curry_ketchup", (0xA8, 0x32, 0x1E)), ("andalouse", (0xE8, 0x86, 0x2A)),
        ("samourai", (0xD9, 0x54, 0x2B)), ("americaine", (0xE8, 0x9A, 0x7A)), ("sauce_lapin", (0x5A, 0x32, 0x18)),
        ("tartare", (0xE8, 0xE6, 0xC4)), ("pickles", (0xCC, 0xBC, 0x30)), ("brazil", (0xEE, 0xA0, 0x40))]},
}

# Fluid id -> colour. Each gets still/flow textures and a bucket item texture.
FLUIDS = {
    "frying_oil": (0xE8, 0xC5, 0x47),
    "melted_beef_tallow": (0xFB, 0xFA, 0xF4),
    "mayonnaise": (0xF3, 0xEB, 0xC4),
    "curry_ketchup": (0xA8, 0x32, 0x1E),
    "andalouse": (0xE8, 0x86, 0x2A),
    "samourai": (0xD9, 0x54, 0x2B),
    "americaine": (0xE8, 0x9A, 0x7A),
    "cream": (0xfa, 0xf6, 0xe8),
    "bechamel": (0xf0, 0xe6, 0xc8),
    "liege_syrup": (0x4a, 0x2a, 0x14),
    "vinegar": (0xc8, 0xa4, 0x58),
    "sauce_lapin": (0x5a, 0x32, 0x18),
    "mustard": (0xd8, 0xa8, 0x28),
    "wort": (0x8a, 0x5a, 0x1e),
    "blonde_beer": (0xe8, 0xb8, 0x3a),
    "brown_beer": (0x5a, 0x2e, 0x14),
    "gueuze": (0xd8, 0x9a, 0x3a),
    "kriek": (0x9a, 0x1e, 0x2a),
    "tartare": (0xe8, 0xe6, 0xc4),
    "pickles": (0xcc, 0xbc, 0x30),
    "brazil": (0xee, 0xa0, 0x40),
}

# Block faces: "block/face" -> colour. Stainless steel for the fryer (docs/06).
BLOCK_FACES = {
    "fryer/side": (0xB4, 0xB8, 0xBC),
    "fryer/top": (0xD0, 0xD4, 0xD8),
    "fryer/bottom": (0x80, 0x84, 0x88),
    "fryer/inner": (0x60, 0x64, 0x68),
    # Brass for the supreme grinder (docs/06); its sides carry the gauge, see GRINDER_GAUGE.
    "supreme_grinder/top": (0xD8, 0xA8, 0x48),
    "supreme_grinder/bottom": (0x90, 0x68, 0x28),
    "supreme_grinder/blade": (0xC8, 0xCC, 0xD0),
    "supreme_grinder/mince": (0xB0, 0x70, 0x58),
    # v2 crop stages and the mussel pole (docs/13, section 5.8).
    "crop/chicon_stage0": (0x6A, 0xA0, 0x3A),
    "crop/chicon_stage1": (0x6A, 0xA0, 0x3A),
    "crop/chicon_stage2": (0x6A, 0xA0, 0x3A),
    "crop/chicon_stage3": (0x6A, 0xA0, 0x3A),
    "crop/leek_stage0": (0x6A, 0xA0, 0x3A),
    "crop/leek_stage1": (0x6A, 0xA0, 0x3A),
    "crop/leek_stage2": (0x6A, 0xA0, 0x3A),
    "crop/leek_stage3": (0x6A, 0xA0, 0x3A),
    "crop/hops_stage0": (0x6A, 0xA0, 0x3A),
    "crop/hops_stage1": (0x6A, 0xA0, 0x3A),
    "crop/hops_stage2": (0x6A, 0xA0, 0x3A),
    "crop/hops_stage3": (0x6A, 0xA0, 0x3A),
    "crop/brussels_sprouts_stage0": (0x6A, 0xA0, 0x3A),
    "crop/brussels_sprouts_stage1": (0x6A, 0xA0, 0x3A),
    "crop/brussels_sprouts_stage2": (0x6A, 0xA0, 0x3A),
    "crop/brussels_sprouts_stage3": (0x6A, 0xA0, 0x3A),
    "crop/asparagus_stage0": (0x6A, 0xA0, 0x3A),
    "crop/asparagus_stage1": (0x6A, 0xA0, 0x3A),
    "crop/asparagus_stage2": (0x6A, 0xA0, 0x3A),
    "crop/buckwheat_stage0": (0x6A, 0xA0, 0x3A),
    "crop/buckwheat_stage1": (0x6A, 0xA0, 0x3A),
    "crop/buckwheat_stage2": (0x6A, 0xA0, 0x3A),
    "crop/buckwheat_stage3": (0x6A, 0xA0, 0x3A),
    "crop/mustard_stage0": (0x6A, 0xA0, 0x3A),
    "crop/mustard_stage1": (0x6A, 0xA0, 0x3A),
    "crop/mustard_stage2": (0x6A, 0xA0, 0x3A),
    "crop/mustard_stage3": (0x6A, 0xA0, 0x3A),
    "crop/asparagus_stage3_white": (0xF0, 0xEC, 0xD8),
    "crop/asparagus_stage3_green": (0x6A, 0xA0, 0x3A),
    "bouchot/stage0": (0x8A, 0x6A, 0x4A),
    "bouchot/stage1": (0x8A, 0x6A, 0x4A),
    "bouchot/stage2": (0x8A, 0x6A, 0x4A),
    "bouchot/stage3": (0x8A, 0x6A, 0x4A),
}

# 3x5 bitmap glyphs, one string per row.
GLYPHS = {
    "P": ["###", "#.#", "###", "#..", "#.."],
    "B": ["##.", "#.#", "##.", "#.#", "##."],
    "C": [".##", "#..", "#..", "#..", ".##"],
    "T": ["###", ".#.", ".#.", ".#.", ".#."],
    "R": ["##.", "#.#", "##.", "#.#", "#.#"],
    "S": [".##", "#..", ".#.", "..#", "##."],
    "F": ["###", "#..", "##.", "#..", "#.."],
    "K": ["#.#", "#.#", "##.", "#.#", "#.#"],
    "1": [".#.", "##.", ".#.", ".#.", "###"],
    "2": ["##.", "..#", ".#.", "#..", "###"],
    "3": ["##.", "..#", ".#.", "..#", "##."],
}


def shade(colour, factor):
    return tuple(max(0, min(255, int(c * factor))) for c in colour)


def noise(x, y):
    # Deterministic per-pixel value in [0, 1) so reruns produce identical files.
    return ((x * 73856093) ^ (y * 19349663) ^ 0x5BD1E995) % 1000 / 1000.0


def mask(shape, x, y):
    cx, cy = x + 0.5, y + 0.5
    if shape == "heap":
        return cy > 7 and abs(cx - 8) < (cy - 5) * 1.1 and cy < 15
    if shape == "powder":
        return (cy > 10 and abs(cx - 8) < (cy - 9) * 2.2 and cy < 15) or (noise(x, y) > 0.93 and cy < 10)
    if shape == "ball":
        return (cx - 8) ** 2 + (cy - 9) ** 2 < 5.5 ** 2
    if shape == "cube":
        return 3 <= x <= 12 and 4 <= y <= 13
    if shape in ("stick", "shiny_stick"):
        # Distance to the segment (3,13)-(13,3), capsule-shaped.
        ax, ay, bx, by = 3.0, 13.0, 13.0, 3.0
        t = max(0.0, min(1.0, ((cx - ax) * (bx - ax) + (cy - ay) * (by - ay)) / ((bx - ax) ** 2 + (by - ay) ** 2)))
        px, py = ax + t * (bx - ax), ay + t * (by - ay)
        return math.hypot(cx - px, cy - py) < 2.4
    raise ValueError(shape)


def render(shape, colour, glyph):
    px = [[None] * SIZE for _ in range(SIZE)]
    inside = [[mask(shape, x, y) for x in range(SIZE)] for y in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            if not inside[y][x]:
                continue
            edge = any(
                not (0 <= x + dx < SIZE and 0 <= y + dy < SIZE and inside[y + dy][x + dx])
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))
            )
            if edge:
                c = (255, 240, 150) if shape == "shiny_stick" else shade(colour, 0.55)
            elif shape in ("heap", "powder"):
                c = shade(colour, 0.8 + 0.4 * noise(x, y))
            else:
                # Light from the top-left.
                c = shade(colour, 1.15 - 0.03 * (x + y))
            px[y][x] = c + (255,)
    rows = GLYPHS[glyph]
    for gy, row in enumerate(rows):
        for gx, cell in enumerate(row):
            if cell == "#":
                px[gy + 1][gx + 1] = (255, 255, 255, 255)
                if gx + 2 < SIZE and (gx + 1 >= len(row) or row[gx + 1] != "#"):
                    px[gy + 2][gx + 2] = px[gy + 2][gx + 2] or (40, 30, 20, 255)
    return px


def render_fluid(colour, frame):
    # Opaque liquid surface with soft ripples; the frame shifts them so flow textures animate.
    px = [[None] * SIZE for _ in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            ripple = math.sin((x + 2 * y + 4 * frame) * 0.7) * 0.06 + noise(x, y + frame) * 0.08
            px[y][x] = shade(colour, 0.95 + ripple) + (255,)
    return px


def render_block_face(colour):
    # Brushed-metal fill with a darker one-pixel border, so block edges read in game.
    px = [[None] * SIZE for _ in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            edge = x in (0, SIZE - 1) or y in (0, SIZE - 1)
            factor = 0.7 if edge else 0.95 + 0.08 * noise(x // 2, y)
            px[y][x] = shade(colour, factor) + (255,)
    return px


GRINDER_SIDE = (0xC0, 0x90, 0x38)


def render_grinder_side(fill):
    # Brass face with a vertical gauge in the middle: 4 segments, lit from the bottom up to fill.
    px = render_block_face(GRINDER_SIDE)
    for y in range(2, 14):
        for x in range(6, 10):
            px[y][x] = (0x30, 0x24, 0x10, 255)
    for segment in range(fill):
        for y in range(11 - 3 * segment, 14 - 3 * segment - 1):
            for x in range(7, 9):
                px[y][x] = (0x60, 0xD0, 0x50, 255)
    return px


def render_bucket(colour):
    metal, dark = (0xB8, 0xB8, 0xC0), (0x50, 0x50, 0x58)
    px = [[None] * SIZE for _ in range(SIZE)]
    for y in range(4, 15):
        # Tapered pail: wider at the rim than at the base.
        half = 6 - (y - 4) // 4
        for x in range(8 - half, 8 + half):
            edge = x in (8 - half, 8 + half - 1) or y == 14
            px[y][x] = (dark if edge else metal) + (255,)
    for x in range(3, 13):
        px[5][x] = shade(colour, 1.0) + (255,)
        px[6][x] = shade(colour, 0.85) + (255,)
    for x in (4, 5, 10, 11):
        px[3][x] = dark + (255,)
    for x in range(6, 10):
        px[2][x] = dark + (255,)
    return px


def png_bytes(px):
    height, width = len(px), len(px[0])
    raw = b"".join(
        b"\x00" + b"".join(bytes(p if p else (0, 0, 0, 0)) for p in row) for row in px
    )

    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    return png


def write_png(path, px):
    path.write_bytes(png_bytes(px))


def planned():
    """Every placeholder texture: path -> function drawing its pixels. tools/asset_status.py reads it too."""
    outputs = {}
    for item_id, (shape, colour, glyph) in ITEMS.items():
        outputs[ITEM_DIR / f"{item_id}.png"] = lambda s=shape, c=colour, g=glyph: render(s, c, g)
    for fluid_id, colour in FLUIDS.items():
        outputs[FLUID_DIR / f"{fluid_id}_still.png"] = lambda c=colour: render_fluid(c, 0)
        # Two stacked frames, animated by the .mcmeta written next to it.
        outputs[FLUID_DIR / f"{fluid_id}_flow.png"] = lambda c=colour: render_fluid(c, 0) + render_fluid(c, 1)
        outputs[ITEM_DIR / f"{fluid_id}_bucket.png"] = lambda c=colour: render_bucket(c)
    for face, colour in BLOCK_FACES.items():
        outputs[BLOCK_DIR / f"{face}.png"] = lambda c=colour: render_block_face(c)
    for fill in range(5):
        outputs[BLOCK_DIR / f"supreme_grinder/side_{fill}.png"] = lambda f=fill: render_grinder_side(f)
    return outputs


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--force", action="store_true", help="overwrite existing textures")
    args = parser.parse_args()

    written = skipped = 0
    for path, draw in planned().items():
        path.parent.mkdir(parents=True, exist_ok=True)
        if path.exists() and not args.force:
            skipped += 1
            continue
        write_png(path, draw())
        if path.name.endswith("_flow.png"):
            path.with_name(path.name + ".mcmeta").write_text('{"animation": {"frametime": 8}}\n', encoding="utf-8")
        written += 1
    print(f"written: {written}, kept existing: {skipped}")


if __name__ == "__main__":
    main()
