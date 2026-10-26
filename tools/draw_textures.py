#!/usr/bin/env python3
# Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
# SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
"""Draw the original pixel artwork from docs/12-prompts-textures.md.

Standard library only. Replaces placeholders, preserves other artwork unless --force.
Writes a labelled HTML gallery and enlarged PNG contact sheet under existing build/.
"""

import argparse
import html
import math

import gen_placeholders as gen


def colour(value):
    return tuple(bytes.fromhex(value)) + (255,)


def canvas(fill=None):
    return [[fill for _ in range(16)] for _ in range(16)]


def bitmap(rows, palette):
    assert len(rows) == 16 and all(len(row) == 16 for row in rows)
    return [[None if c == '.' else colour(palette[c]) for c in row] for row in rows]


def dots(px, points, value):
    for x, y in points:
        px[y][x] = colour(value)


RAW = ['805951', 'BA8276', 'DDAA94', 'F3C9AD']
FRIED = ['583320', '92502B', 'BD793C', 'E9AE60']
GOLD = ['765026', 'BC8134', 'DEA947', 'FFE08B']


def sausage(palette, toppings=False, thick=False, incomplete=False):
    px = canvas()
    # A narrow diagonal capsule; all variations reuse exactly the same mask.
    cells = set()
    for y in range(16):
        for x in range(16):
            t = max(0, min(1, ((x - 3) * 9 - (y - 12) * 9) / 162))
            if math.hypot(x - (3 + t * 9), y - (12 - t * 9)) <= (2 if thick else 1.5):
                cells.add((x, y))
    for x, y in cells:
        edge = any((x + dx, y + dy) not in cells for dx, dy in [(1, 0), (-1, 0), (0, 1), (0, -1)])
        shade = 0 if edge else (3 if x + y < 15 else 2 if x + y == 15 else 1)
        px[y][x] = colour(palette[shade])
    for x, y in [(5, 9), (8, 6), (11, 3)]:
        if (x, y) in cells:
            px[y][x] = colour(palette[3])
    if toppings:
        for x in range(4, 9 if incomplete else 12):
            px[14 - x][x] = colour('FFF0CC')
            if not incomplete or thick:
                px[15 - x][x] = colour('B44026' if x % 3 else 'E26535')
        if not incomplete:
            dots(px, [(5, 11), (8, 8), (11, 5)], 'FFF9DE')
            dots(px, [(6, 9), (9, 6)], 'DFCC83')
        if thick:
            dots(px, [(4, 10), (8, 5), (12, 3)], 'FFF1AA' if not incomplete else 'F4E5C4')
    return px


# v2 fries (docs/12, section 1): sticks of potato, pale when raw, golden when fried twice; beef
# tallow gives a deeper gold. Each stick is two pixels wide, lit on its left, outlined in its darkest
# shade; sticks drawn later sit in front.
FRIES_RAW = ['9C8A62', 'D3C194', 'EEE0B8', 'FFF6DA']
FRIES_BLANCHED = ['A38E55', 'D6BC78', 'EED898', 'FFF0C0']
FRIES_GOLDEN = ['91591F', 'D2953A', 'EEC15A', 'FFE38C']
FRIES_TALLOW = ['7A4519', 'BF7C2E', 'E6AE4E', 'FFD77A']
PAPER = ['8C8476', 'D6D0BE', 'F2EEE2', 'FFFFFF']
STRIPE = ['7E211B', 'C8352B']
PILE = [(1, 14, 3, 4), (10, 15, 12, 5), (4, 15, 5, 2), (7, 15, 8, 3)]


def stick_cells(x0, y0, x1, y1):
    cells = set()
    steps = 2 * max(abs(x1 - x0), abs(y1 - y0))
    for i in range(steps + 1):
        t = i / steps
        x, y = round(x0 + (x1 - x0) * t), round(y0 + (y1 - y0) * t)
        cells.update({(x, y), (x + 1, y)})
    return cells


def draw_sticks(px, segments, palette, clip=16):
    for segment in segments:
        cells = {(x, y) for x, y in stick_cells(*segment) if 0 <= x < 16 and 0 <= y < clip}
        for x, y in cells:
            for dx, dy in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                nx, ny = x + dx, y + dy
                if 0 <= nx < 16 and 0 <= ny < clip and (nx, ny) not in cells:
                    px[ny][nx] = colour(palette[0])
        top = min(y for _, y in cells)
        for x, y in cells:
            left = (x - 1, y) not in cells
            px[y][x] = colour(palette[3] if y == top else palette[2] if left else palette[1])


def fries(palette, sparkle=False):
    px = canvas()
    draw_sticks(px, PILE, palette)
    if sparkle:
        dots(px, [(6, 5), (10, 7)], 'FFF6CF')
    return px


def cone(px, top=6):
    # A paper cone with the red and white stripes of a friterie, tip at the bottom.
    cells = {(x, y) for y in range(top, 16) for x in range(16) if abs(x + 0.5 - 8) <= (15.6 - y) * 0.62}
    for x, y in cells:
        edge = any((x + dx, y + dy) not in cells for dx, dy in [(1, 0), (-1, 0), (0, 1)])
        if edge or y == top:
            px[y][x] = colour(PAPER[0] if edge else STRIPE[0])
        elif (x // 2) % 2 == 0:
            px[y][x] = colour(STRIPE[1] if x < 9 else STRIPE[0])
        else:
            px[y][x] = colour(PAPER[3] if x < 6 else PAPER[2] if x < 10 else PAPER[1])
    return px


def paper_cone():
    px = cone(canvas())
    # The opening, seen from slightly above.
    for x in range(4, 12):
        px[6][x] = colour('5E574B')
    for x in range(3, 13):
        px[5][x] = colour(PAPER[0]) if x in (3, 12) else colour(PAPER[1])
    return px


def cone_of_fries(palette, sauce=None):
    px = canvas()
    draw_sticks(px, [(3, 8, 4, 1), (11, 8, 12, 2), (5, 8, 6, 0), (8, 8, 9, 1)], palette, clip=8)
    px = cone(px, top=7)
    if sauce:
        # A ribbon of sauce over the fries, light on top, darker underneath.
        for x in range(3, 14):
            y = 4 + (1 if x % 4 in (1, 2) else 0)
            px[y][x] = colour(sauce[2])
            px[y + 1][x] = colour(sauce[1])
            if x % 3 == 0:
                px[y][x] = colour(sauce[3])
    return px


# v2 base ingredients (docs/12, section 2 ter): a few shared shapes, recoloured per item. A shape is a
# set of cells; edges take the darkest shade, cells lit from the top-left the lightest.
def shape(cells, palette, px=None):
    px = px or canvas()
    for x, y in cells:
        if not (0 <= x < 16 and 0 <= y < 16):
            continue
        if any((x + dx, y + dy) not in cells for dx, dy in [(1, 0), (-1, 0), (0, 1), (0, -1)]):
            px[y][x] = colour(palette[0])
        elif (x - 1, y - 1) not in cells or (x, y - 2) not in cells:
            px[y][x] = colour(palette[3])
        elif (x + 1, y + 1) not in cells or (x, y + 2) not in cells:
            px[y][x] = colour(palette[1])
        else:
            px[y][x] = colour(palette[2])
    return px


def ellipse(cx, cy, rx, ry, keep=lambda x, y: True):
    return {(x, y) for y in range(16) for x in range(16)
            if ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2 <= 1 and keep(x, y)}


def roll(palette, crease=None):
    px = shape(ellipse(8, 9.5, 6.5, 3.6), palette)
    if crease:
        dots(px, [(5, 8), (6, 8), (7, 9), (8, 9), (9, 9), (10, 10)], crease)
    return px


def bun(palette, seeds=False):
    cells = ellipse(8, 10, 6.5, 4.5, keep=lambda x, y: y <= 10) | {(x, y) for x in range(3, 13) for y in (11, 12)}
    px = shape(cells, palette)
    if seeds:
        dots(px, [(5, 8), (8, 7), (10, 9), (7, 10)], 'FFF6D8')
    return px


def disc(palette, spots=None):
    px = shape(ellipse(8, 10, 6.8, 2.6), palette)
    if spots:
        dots(px, [(5, 10), (9, 9), (11, 10)], spots)
    return px


def wedge(palette, rind=None):
    cells = {(x, y) for y in range(5, 13) for x in range(2, 15) if x - 2 >= (12 - y) * 1.5 or y >= 11}
    px = shape(cells, palette)
    if rind:
        for x, y in cells:
            if y == max(yy for xx, yy in cells if xx == x):
                px[y][x] = colour(rind)
        dots(px, [(10, 8), (7, 10), (12, 10)], palette[1])
    return px


def block(palette):
    return shape({(x, y) for x in range(3, 13) for y in range(6, 12)}, palette)


def ham(palette, bone='F2EAD8'):
    px = shape(ellipse(7, 10, 5.5, 4), palette)
    for x, y in [(11, 6), (12, 5), (13, 4), (12, 4), (13, 5)]:
        px[y][x] = colour(bone)
    dots(px, [(5, 9), (8, 11)], palette[3])
    return px


def peak(palette):
    cells = {(x, y) for y in range(4, 14) for x in range(16) if abs(x + 0.5 - 8) <= (y - 3) * 0.55 + 0.6}
    px = shape(cells, palette)
    dots(px, [(7, 7), (6, 10), (9, 11)], palette[1])
    return px


def bowl(content, highlight=None):
    px = canvas()
    shape(ellipse(8, 8, 6.5, 6, keep=lambda x, y: y >= 8), ['6E4A2C', '8C6038', 'A8784A', 'C8955E'], px)
    for x in range(3, 13):
        px[8][x] = colour(content[2])
        px[7][x] = colour(content[3] if x < 9 else content[2])
    for x in (2, 13):
        px[8][x] = colour('6E4A2C')
    if highlight:
        dots(px, [(5, 7), (9, 8)], highlight)
    return px


def scoop(palette):
    px = bowl(['000000', '000000', 'B8B0A0', 'D8D0C0'])
    shape(ellipse(8, 6.5, 4.5, 3.5), palette, px)
    return px


def nest(palette):
    px = canvas()
    for i, y0 in enumerate((7, 9, 11)):
        for x in range(3, 14):
            y = y0 + (1 if (x + i) % 4 in (1, 2) else 0)
            px[y][x] = colour(palette[2 if (x + i) % 3 else 3])
            px[y + 1][x] = colour(palette[1])
    return px


def heap(palette, sparkle=None):
    px = bitmap(HEAP, dict(zip('0123', palette)))
    if sparkle:
        dots(px, [(7, 8), (5, 11), (10, 10)], sparkle)
    return px


# v2 crops (docs/12, section 2 quater): leaves rise with the stage; the last stages show what is
# harvested. Stems are a pixel wide, leaves fan out from them.
LEAF = ['2E5A1C', '4A8028', '6AA03A', '9ACC5A']


def plant(stage, produce=None):
    px = canvas()
    height = [4, 7, 10, 12][stage]
    for x in (4, 8, 12):
        for dy in range(height - (x % 3)):
            y = 15 - dy
            px[y][x] = colour(LEAF[1])
            if dy > 1 and dy % 2 == 0:
                px[y][x - 1] = colour(LEAF[2])
                px[y - 1][x + 1] = colour(LEAF[3] if dy % 4 == 0 else LEAF[2])
    if produce and stage >= 2:
        produce(px, stage)
    return px


def spots(points, value):
    def draw(px, stage):
        for x, y in points[:len(points) if stage == 3 else len(points) // 2]:
            px[y][x] = colour(value)
    return draw


def heads(palette, tall=3):
    def draw(px, stage):
        for x in (4, 8, 12):
            for dy in range(tall if stage == 3 else tall - 1):
                px[12 - dy][x] = colour(palette[2])
                px[12 - dy][x - 1] = colour(palette[3])
            px[12 - tall][x] = colour(palette[1])
    return draw


def crop_stages():
    out = {}
    produce = {
        'chicon': heads(['B8A860', 'D8CC88', 'F0E8B8', 'FFFAE0'], tall=4),
        'leek': heads(['B8B8A0', 'DCDCC8', 'F0F0E2', 'FFFFFF'], tall=4),
        'hops': spots([(3, 6), (9, 5), (13, 7), (5, 9), (11, 10), (7, 4)], 'B8D878'),
        'brussels_sprouts': spots([(4, 8), (8, 7), (12, 9), (4, 11), (8, 10), (12, 12)], '5A9A2E'),
        'buckwheat': spots([(3, 4), (8, 3), (12, 5), (5, 6), (10, 6), (7, 5)], 'F4D8E0'),
        'mustard': spots([(3, 4), (8, 3), (12, 5), (5, 6), (10, 6), (7, 5)], 'F4D23A'),
    }
    for name, draw in produce.items():
        for stage in range(4):
            out[f'crop/{name}_stage{stage}'] = plant(stage, draw)
    for stage in range(3):
        out[f'crop/asparagus_stage{stage}'] = plant(stage)
    for colour_name, spear in [('white', ['B8B09A', 'DCD6C2', 'F0ECD8', 'FFFFF2']), ('green', ['2E5A1C', '4A8028', '6AA03A', '9ACC5A'])]:
        px = canvas()
        for x in (4, 7, 10, 13):
            for y in range(4 + x % 3, 16):
                px[y][x] = colour(spear[2])
                px[y][x - 1] = colour(spear[3] if y < 8 else spear[1])
            px[3 + x % 3][x] = colour(spear[0])
        out[f'crop/asparagus_stage3_{colour_name}'] = px
    for stage in range(4):
        px = canvas()
        for y in range(16):
            px[y][7] = colour('6E4A2C')
            px[y][8] = colour('8C6038')
        clusters = [(6, 13), (9, 12), (6, 10), (9, 9), (6, 7), (9, 6), (6, 4), (9, 3)][:stage * 3]
        for x, y in clusters:
            px[y][x] = colour('20202E')
            px[y + 1][x] = colour('3A3A52')
        out[f'bouchot/stage{stage}'] = px
    return {gen.BLOCK_DIR / f'{name}.png': px for name, px in out.items()}


def crops_items():
    out = {
        'chicory_root': canvas(),
        'chicon': peak(['B8A860', 'D8CC88', 'F0E8B8', 'FFFAE0']),
        'leek': canvas(),
        'hop_seeds': heap(['3A2814', '5A4024', '8A6A3A', 'AC8A54']),
        'hops': peak(['4A7028', '6E9A3A', '9AC05A', 'C4E088']),
        'hop_shoots': canvas(),
        'brussels_sprout': shape(ellipse(8, 8.5, 5, 5.5), ['2E5A1C', '4A8028', '6AA03A', '9ACC5A']),
        'asparagus_crown': heap(['4E3424', '6E4C34', '8A6A4A', 'AC8C68']),
        'white_asparagus': canvas(),
        'green_asparagus': canvas(),
        'buckwheat': heap(['2A1C10', '42301E', '5A402A', '7C5C40']),
        'buckwheat_flour': heap(['807464', 'A09484', 'BCB0A0', 'DAD0C2']),
        'mustard_seeds': heap(['7A5A18', 'A07A24', 'C8A03A', 'E6C66A']),
        'mustard_powder': heap(['A07A10', 'C89E1E', 'E8C03A', 'FFE07A']),
        'mussel': shape(ellipse(8, 9, 6, 3.5), ['14141E', '20202E', '2E2E44', '58587A']),
        'bouchot': canvas(),
    }
    # Long vegetables: the thick sticks of the fries, along the diagonal.
    WHITE = ['8C8672', 'DCD6C2', 'F0ECD8', 'FFFFF2']
    GREEN = ['2E5A1C', '4A8028', '6AA03A', '9ACC5A']
    draw_sticks(out['white_asparagus'], [(3, 13, 11, 2)], WHITE)
    draw_sticks(out['green_asparagus'], [(3, 13, 11, 2)], GREEN)
    draw_sticks(out['hop_shoots'], [(2, 14, 7, 4), (7, 15, 12, 3)], GREEN)
    draw_sticks(out['chicory_root'], [(6, 14, 9, 3)], ['4E3018', '7A5028', '9A6A3A', 'C08A50'])
    draw_sticks(out['leek'], [(3, 13, 11, 2)], WHITE)
    draw_sticks(out['leek'], [(8, 7, 12, 1)], GREEN)
    for x, y in [(6, 5), (8, 7), (10, 9)]:
        out['brussels_sprout'][y][x] = colour('4A8028')
    px = out['bouchot']
    for y in range(1, 16):
        px[y][7] = colour('6E4A2C')
        px[y][8] = colour('8C6038')
    for x, y in [(6, 12), (9, 10), (6, 8), (9, 6)]:
        px[y][x] = colour('20202E')
    return {gen.ITEM_DIR / f'{name}.png': px for name, px in out.items()}


def base_ingredients():
    WHITE = ['B8B4A8', 'DCD8CC', 'F2F0E8', 'FFFFFF']
    DOUGH = ['B8A47C', 'D8C49C', 'ECDCB8', 'FAF0DA']
    CRUST = ['6E3E14', 'A0662A', 'C8873C', 'E8B06A']
    out = {
        'pearl_sugar': heap(WHITE, 'FFFFFF'),
        'cassonade': heap(['5E3714', '875323', 'A86A2C', 'C98C4A']),
        'powdered_sugar': heap(['D2D0CC', 'E8E6E2', 'F8F8F6', 'FFFFFF']),
        'salt': heap(['A6AEB4', 'C8D0D4', 'E8ECF0', 'FFFFFF'], 'FFFFFF'),
        'butter': block(['B8962E', 'D8B84A', 'F4D86A', 'FFF0A0']),
        'young_herve_cheese': wedge(['B8A660', 'DCCC88', 'F0E0A0', 'FFF4C8']),
        'herve_cheese': wedge(['B88A40', 'D8B060', 'F0D078', 'FFE8A8'], rind='C26A24'),
        'maton': heap(['C4BEB0', 'E0DCD2', 'F4F0E6', 'FFFFFF']),
        'brewers_yeast': heap(['8E7444', 'B0925C', 'C8A870', 'E2C894']),
        'sprouted_grain': heap(['8C7638', 'B09A4E', 'C8B060', 'E2CC84'], '7CB04A'),
        'malt': heap(['5C3818', '7E4E22', '9A622C', 'BC8248']),
        'pistolet_dough': roll(DOUGH),
        'pistolet': roll(CRUST, crease='F2D49A'),
        'puff_pastry': block(['BCA674', 'DCC896', 'F2E2B0', 'FFF4D4']),
        'brioche_dough': roll(['C0A256', 'DCC070', 'F4D88C', 'FFEDB8']),
        'burger_bun_dough': bun(['BCA466', 'DCC484', 'F0D898', 'FFEEC4']),
        'burger_bun': bun(['7A4618', 'A8642A', 'C87C30', 'E8A658'], seeds=True),
        'pita_dough': roll(['BAAE90', 'DCD0B0', 'EEE2C4', 'FFF6E2']),
        'raw_pita': disc(['BAAE90', 'DCD0B0', 'EEE2C4', 'FFF6E2']),
        'pita_bread': disc(['94703A', 'C49A5C', 'DCB474', 'F2D29C'], spots='8A5A26'),
        'noodles': nest(['A8902E', 'D2B654', 'F0D880', 'FFF0B8']),
        'lardons': heap(['9A5050', 'C87878', 'E89A9A', 'FFD6D0'], 'FFF2EC'),
        'brined_ham': ham(['A85858', 'D07878', 'E89898', 'FFC4C0']),
        'ardennes_ham': ham(['4E1C16', '762A22', '9A3A30', 'C25C4C']),
        'raw_meringue': peak(['CEC8BC', 'E8E4DC', 'F8F6F0', 'FFFFFF']),
        'meringue': peak(['C0A478', 'DCC8A4', 'F4E6C8', 'FFF8E8']),
        'raisins': heap(['20101A', '3A2030', '4A2A3A', '6E4A5E'], '8A6A7A'),
        'apple_compote': bowl(['8C7C30', 'B0A048', 'D8C86A', 'F0E498'], 'F8F0B8'),
        'chocolate_shavings': heap(['2E1A0E', '442814', '5A341E', '7E4E30'], '9A6A48'),
        'vanilla_ice_cream': scoop(['D6CCAE', 'ECE4C8', 'FAF2D8', 'FFFFFF']),
    }
    return {gen.ITEM_DIR / f'{name}.png': px for name, px in out.items()}


BALL = [
    '................', '................', '................', '................',
    '................', '......0000......', '....00333300....', '...0333322220...',
    '...0333222220...', '...0322222220...', '...0222222210...', '...0222222110...',
    '....02221110....', '.....000000.....', '................', '................',
]
HEAP = [
    '................', '................', '................', '................',
    '................', '................', '.......00.......', '......0330......',
    '.....032230.....', '....03321220....', '...0322332120...', '..033212221210..',
    '..022232122110..', '...0011111000...', '.....00000......', '................',
]


def paste(palette, tier):
    px = bitmap(BALL, dict(zip('0123', palette)))
    dots(px, [(6, 9), (9, 11), (10, 8)], palette[1])
    if tier:
        for value, points in [('688040', [(5, 8), (9, 10)]), ('B54A30', [(8, 7), (6, 11)]),
                              ('F7D67B', [(10, 9), (7, 9)]), ('785136', [(9, 12)])]:
            dots(px, points, value)
    if tier == 2:
        dots(px, [(2, 6), (12, 4), (14, 10), (4, 14)], 'FFF3BE')
    return px


BUCKET = [
    '................', '......0000......', '.....033330.....', '....03....30....',
    '...0000000000...', '..033hhhhhh330..', '..03hhllllll30..', '..013lllllld10..',
    '..013333333210..', '...0322222210...', '...0322222110...', '...0322222110...',
    '....03222110....', '....03322110....', '.....000000.....', '................',
]
METAL = {'0': '394650', '1': '647783', '2': '9AACB5', '3': 'D9E6E8'}
LIQUIDS = {
    'frying_oil': ['B88B2D', 'D5AC38', 'E8C547', 'FFE58C'],
    'melted_beef_tallow': ['D3C6A9', 'E8DFC9', 'FBFAF4', 'FFFFFF'],
    'mayonnaise': ['C3AD79', 'DED09D', 'F3EBC4', 'FFF8DD'],
    'curry_ketchup': ['722B23', '8C2C21', 'A8321E', 'D65A36'],
    'andalouse': ['9C4F12', 'C46A1C', 'E8862A', 'FFB062'],
    'samourai': ['8A2A12', 'B03C1C', 'D9542B', 'F58358'],
    'americaine': ['A85A45', 'C9786A', 'E89A7A', 'FFC0A8'],
    'cream': ['CFC6AE', 'E8E2D2', 'FAF6E8', 'FFFFFF'],
    'bechamel': ['C8BC98', 'E0D6B6', 'F0E6C8', 'FFF8E4'],
    'liege_syrup': ['24120A', '3A1E0E', '4A2A14', '74482A'],
    'vinegar': ['8C6A2C', 'AC8840', 'C8A458', 'E6C67C'],
    'sauce_lapin': ['2E180A', '442410', '5A3218', '83542E'],
    'mustard': ['8A6A10', 'B08A18', 'D8A828', 'F4D25C'],
    'wort': ['4E2E0C', '6E4214', '8A5A1E', 'B07E3C'],
    'blonde_beer': ['A07818', 'C89A24', 'E8B83A', 'FFF2C8'],
    'brown_beer': ['2A140A', '42200E', '5A2E14', 'C8A27A'],
    'gueuze': ['8E5E1A', 'B47A28', 'D89A3A', 'F6E2B8'],
    'kriek': ['560E16', '781420', '9A1E2A', 'E8B0B8'],
    'tartare': ['A8A47C', 'CECAA0', 'E8E6C4', 'FAFAE4'],
    'pickles': ['7A6A0E', 'A4961C', 'CCBC30', 'EEE070'],
    'brazil': ['A85E14', 'D07E24', 'EEA040', 'FFCE84'],
    'speculoos_spread': ['6E3A12', '9A5620', 'BC7432', 'DC9850'],
    'hot_chocolate': ['3A1E0C', '5A3018', '7A4424', '9A6038'],
}
CONE_SAUCES = ['mayonnaise', 'curry_ketchup', 'andalouse', 'samourai', 'americaine', 'sauce_lapin', 'tartare', 'pickles', 'brazil']


def fluid(palette, flow=False):
    px = canvas(colour(palette[2]))
    if flow:
        # Period eight makes both four-pixel animation transitions identical.
        for x, start in [(2, 0), (7, 3), (12, 6)]:
            for offset in [0, 8]:
                for dy in range(5):
                    y = (start + offset + dy) % 16
                    px[y][x] = colour(palette[1])
                    px[y][x + 1] = colour(palette[3] if dy < 3 else palette[2])
        return px + [row[:] for row in px[-4:] + px[:-4]]
    # Hand-placed broad ripple clusters with flat, matching tile edges.
    for x, y in [(2, 3), (9, 9), (3, 12)]:
        dots(px, [(x, y), (x + 1, y - 1), (x + 2, y - 1), (x + 3, y)], palette[3])
        dots(px, [(x + 1, y + 1), (x + 2, y + 1), (x + 3, y)], palette[1])
    if palette == LIQUIDS['frying_oil']:
        dots(px, [(11, 3), (6, 7)], palette[3])
        dots(px, [(12, 4), (7, 8)], palette[0])
    if palette == LIQUIDS['curry_ketchup']:
        dots(px, [(6, 3), (12, 7), (4, 8)], palette[0])
    return px


STEEL = ['475662', '6C808B', 'A5B8BF', 'DEE9E9']
BRASS = ['60442C', 'A27439', 'CAA052', 'F0CF7D']


def panel(palette, bolts=True):
    px = canvas(colour(palette[2]))
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                px[y][x] = colour(palette[0])
            elif x == 1 or y == 1:
                px[y][x] = colour(palette[3])
            elif x == 14 or y == 14:
                px[y][x] = colour(palette[1])
    for x, y in [(4, 4), (9, 7), (3, 10)]:
        dots(px, [(x, y), (x + 1, y), (x + 2, y)], palette[3])
    if bolts:
        for x, y in [(2, 2), (12, 2), (2, 12), (12, 12)]:
            dots(px, [(x, y)], palette[3])
            dots(px, [(x + 1, y), (x, y + 1)], palette[1])
            dots(px, [(x + 1, y + 1)], palette[0])
    return px


def blocks():
    out = {}
    side = panel(STEEL)
    for x in range(4, 12):
        side[11][x] = colour(STEEL[0])
        side[12][x] = colour(STEEL[3])
    out['fryer/side'] = side
    out['fryer/top'] = panel(STEEL, False)
    bottom = panel(['35434B', '536570', '71838D', '92A4AB'])
    for y in range(4, 12):
        for x in range(4, 12):
            if (x - y) % 4 == 0 or (x + y) % 4 == 0:
                bottom[y][x] = colour('647680')
    out['fryer/bottom'] = bottom
    inner = canvas(colour('647781'))
    for y in (2, 12):
        for x in range(16):
            inner[y][x] = colour('70828B')
    for x in range(16):
        inner[7][x] = colour('A89755')
        inner[8][x] = colour('877746')
    dots(inner, [(3, 10), (10, 11), (12, 5)], '424C4C')
    out['fryer/inner'] = inner
    for fill in range(5):
        px = panel(BRASS)
        for y in range(2, 14):
            for x in range(6, 10):
                segment = (13 - y) // 3
                lit = segment < fill and (y - 2) % 3 < 2
                px[y][x] = colour(('B4EB78' if x == 6 else '62BD51' if x < 9 else '37804A')
                                  if lit else '29372E' if (y - 2) % 3 < 2 else '493726')
        out[f'supreme_grinder/side_{fill}'] = px
    top = panel(BRASS)
    for y in range(3, 13):
        for x in range(3, 13):
            radius = math.hypot(x - 7.5, y - 7.5)
            if radius < 4.8:
                top[y][x] = colour('E6BD69' if radius > 3.8 else '49392D' if radius > 2.8 else '272D2D')
    dots(top, [(7, 2), (2, 7), (12, 7), (7, 12)], 'E3DDD0')
    out['supreme_grinder/top'] = top
    out['supreme_grinder/bottom'] = panel(['423C32', '65543B', '8D7546', 'B49B61'])
    blade = canvas(colour('91A5AE'))
    for y in range(16):
        for x in range(16):
            blade[y][x] = colour('ECF4F2' if x < 2 else 'C4D5D9' if x < 5 else '91A5AE' if x < 12 else '586D79')
    out['supreme_grinder/blade'] = blade
    # The minced food filling the pit, seen from above: meat grains with flecks of every food.
    mince = canvas(colour('A5624E'))
    for y in range(16):
        for x in range(16):
            grain = (x * 7 + y * 13 + (x * y) % 5) % 9
            if grain < 2:
                mince[y][x] = colour('D08F73')
            elif grain == 4:
                mince[y][x] = colour('7A3F35')
    for value, points in [('F2D6B8', [(3, 2), (11, 6), (6, 13)]), ('6E9443', [(8, 3), (2, 9), (13, 12)]),
                          ('E5B94C', [(12, 1), (5, 7), (10, 14)]), ('B8342B', [(14, 4), (7, 10)])]:
        dots(mince, points, value)
    out['supreme_grinder/mince'] = mince
    return out


# v2 friterie snacks (docs/12, section 2 quinquies): breaded snacks are capsules dotted with crumbs,
# sandwiches are stacked layers, rings are hollow ellipses. Raw items are pale, fried ones golden.
BREADED_RAW = ['A08A60', 'C8B080', 'E0CC9C', 'F4E6C0']
BREADED = ['7A4A18', 'B8782E', 'D89A44', 'F2C46A']
EGG_WASH = ['A89040', 'CCB458', 'E6D07A', 'FAEAA8']
PINK = ['9A5A5A', 'C88080', 'E6A4A0', 'FAD0C8']
BROWN = ['4A2414', '6E3820', '8E4C2C', 'B06A40']
SAUCE_WHITE = ['DED09D', 'F3EBC4']
TOMATO = 'C8352B'
GREENS = '6AA03A'
CRUST = ['6E3E14', 'A0662A', 'C8873C', 'E8B06A']


def capsule(x0, y0, x1, y1, r):
    cells = set()
    length = math.hypot(x1 - x0, y1 - y0)
    for y in range(16):
        for x in range(16):
            t = max(0, min(1, ((x - x0) * (x1 - x0) + (y - y0) * (y1 - y0)) / length ** 2))
            if math.hypot(x - (x0 + t * (x1 - x0)), y - (y0 + t * (y1 - y0))) <= r:
                cells.add((x, y))
    return cells


def crumbs(px, cells, light, dark):
    inner = {(x, y) for x, y in cells if all((x + dx, y + dy) in cells for dx, dy in [(1, 0), (-1, 0), (0, 1), (0, -1)])}
    for x, y in inner:
        if (x * 3 + y * 5) % 7 == 0:
            px[y][x] = colour(light)
        elif (x * 5 + y * 3) % 11 == 0:
            px[y][x] = colour(dark)
    return px


def croquette(palette, breaded=True):
    cells = capsule(4, 11, 11, 4, 2.4)
    px = shape(cells, palette)
    return crumbs(px, cells, palette[3], palette[1]) if breaded else px


def fingers(palette, breaded=True, straight=False):
    px = canvas()
    for segment in ([(3, 4, 12, 4), (3, 9, 12, 9)] if straight else [(3, 12, 8, 5), (8, 13, 13, 6)]):
        cells = capsule(*segment, 1.6)
        shape(cells, palette, px)
        if breaded:
            crumbs(px, cells, palette[3], palette[1])
    return px


def nuggets(palette):
    px = canvas()
    for cx, cy in [(7.5, 6.5), (5, 11), (11, 10.5)]:
        cells = ellipse(cx, cy, 3.1, 2.4)
        shape(cells, palette, px)
        crumbs(px, cells, palette[3], palette[1])
    return px


def rings(palette, crumb=False):
    px = canvas()
    for cx, cy in [(6, 9), (10.5, 7)]:
        cells = ellipse(cx, cy, 4.2, 4.2) - ellipse(cx, cy, 2.2, 2.2)
        shape(cells, palette, px)
        if crumb:
            crumbs(px, cells, palette[3], palette[1])
    return px


def strips(palette):
    px = canvas()
    draw_sticks(px, [(3, 12, 8, 5), (8, 13, 12, 6), (5, 14, 10, 8)], palette)
    return px


def baguette(px, palette, top=7):
    cells = ellipse(8, top + 3, 7.2, 2.8)
    shape(cells, palette, px)
    dots(px, [(4, top + 2), (7, top + 2), (10, top + 2)], palette[3])
    return px


def mitraillette(meat=None, fries=None, sauce=None):
    px = canvas()
    if fries:
        draw_sticks(px, [(4, 9, 5, 4), (8, 9, 9, 3), (11, 9, 12, 5)], fries, clip=9)
    if meat:
        shape(capsule(2, 8, 13, 8, 1.3), meat, px)
    baguette(px, CRUST, top=8)
    if sauce:
        for x in range(3, 13):
            px[7 + (x % 2)][x] = colour(sauce[x % 2])
    return px


def burger(patty=True, top=True, cheese=False, sauce=None, bun=None):
    bun = bun or ['7A4618', 'A8642A', 'C87C30', 'E8A658']
    px = canvas()
    shape({(x, y) for x in range(3, 13) for y in (12, 13)}, bun, px)
    if patty:
        shape({(x, y) for x in range(2, 14) for y in (10, 11)}, BROWN, px)
    if sauce:
        for x in range(3, 13):
            px[9][x] = colour(sauce[x % 2])
    if cheese:
        for x in range(2, 14):
            px[9 if x % 3 else 10][x] = colour('F4C430')
    if top:
        shape(ellipse(8, 8.6, 6.3, 4.4, keep=lambda x, y: y <= 8), bun, px)
        dots(px, [(5, 6), (8, 5), (10, 7)], 'FFF6D8')
    return px


def hot_dog(sausage_palette=None, onions=False, sauce=None, bun=None):
    px = canvas()
    bun = bun or CRUST
    baguette(px, bun, top=8)
    if sausage_palette:
        shape(capsule(2, 9, 13, 9, 1.4), sausage_palette, px)
    if onions:
        dots(px, [(4, 8), (7, 7), (10, 8), (12, 7)], 'E9AE60')
    if sauce:
        for x in range(3, 13):
            px[8 + (x % 2)][x] = colour(sauce[x % 2])
    return px


def pita(filled=False, sauce=None):
    px = shape(ellipse(8, 12.5, 6.6, 7, keep=lambda x, y: y <= 12), ['94703A', 'C49A5C', 'DCB474', 'F2D29C'])
    if filled:
        for x, y in [(4, 7), (6, 6), (9, 6), (11, 7)]:
            px[y][x] = colour(BROWN[2])
        dots(px, [(5, 6), (10, 6)], TOMATO)
        dots(px, [(7, 6), (8, 7)], GREENS)
    if sauce:
        for x in range(4, 12):
            px[6 if x % 2 else 5][x] = colour(sauce[x % 2])
    return px


def brochette(meat, cubes=3):
    px = canvas()
    draw_sticks(px, [(2, 14, 13, 1)], ['5E4024', '8C6038', 'A8784A', 'C8955E'])
    for i, (x, y) in enumerate([(3, 10), (6, 7), (9, 4)][:cubes]):
        shape({(x + dx, y + dy) for dx in range(3) for dy in range(3)}, meat if i % 2 == 0 else PINK, px)
    return px


def bowl_of_boulets():
    px = bowl(LIQUIDS['sauce_lapin'], '83542E')
    for cx, cy in [(6, 6), (10, 6.5)]:
        shape(ellipse(cx, cy, 2.6, 2.4), FRIED, px)
    return px


def thick_disc(palette, spots_value):
    cells = ellipse(8, 9.5, 6.5, 4)
    px = shape(cells, palette)
    crumbs(px, cells, palette[3], palette[1])
    dots(px, [(5, 9), (9, 8), (11, 10), (7, 11)], spots_value)
    return px


def friterie():
    RAW_CHICKEN = ['A07868', 'D0A898', 'EAC8B8', 'FFE8DC']
    FISH = ['A09890', 'D0C8C0', 'E8E2DC', 'FFFAF4']
    CHEESE = ['B8962E', 'DCC050', 'F0D870', 'FFF0A8']
    out = {
        'half_baguette': baguette(canvas(), CRUST),
        'incomplete_mitraillette': mitraillette(meat=FRIED),
        'mitraillette': mitraillette(meat=FRIED, fries=FRIES_GOLDEN, sauce=SAUCE_WHITE),
        'boulet_paste': paste(PINK, 0),
        'raw_boulet': shape(ellipse(8, 9, 4.6, 4.4), PINK),
        'boulet': shape(ellipse(8, 9, 4.6, 4.4), FRIED),
        'boulets_liegeois': bowl_of_boulets(),
        'incomplete_breaded': croquette(EGG_WASH, breaded=False),
        'shrimp_croquette_mix': paste(['A86048', 'D88A6A', 'F0AE8C', 'FFD8C0'], 0),
        'cheese_croquette_mix': paste(['B8A060', 'DCC888', 'F0E0A8', 'FFF6D4'], 0),
        'chicken_croquette_mix': paste(['B0A088', 'D4C8B0', 'ECE2CC', 'FFF8EC'], 0),
        'lucifer_mix': paste(['A04830', 'C8684A', 'E08A68', 'F8B898'], 0),
        'potato_croquette_mix': paste(['B8A468', 'DCCA8C', 'F0E2AE', 'FFF6D8'], 0),
        'chicken_strips': strips(RAW_CHICKEN),
        'fish_strips': strips(FISH),
        'cheese_strips': strips(CHEESE),
        'raw_shrimp_croquette': croquette(BREADED_RAW),
        'raw_cheese_croquette': croquette(BREADED_RAW),
        'raw_chicken_croquette': croquette(BREADED_RAW),
        'raw_lucifer': croquette(['A07858', 'C89C78', 'E0B898', 'F4D8BC']),
        'raw_potato_croquette': croquette(BREADED_RAW),
        'raw_chicken_fingers': fingers(BREADED_RAW),
        'raw_fish_sticks': fingers(BREADED_RAW, straight=True),
        'raw_cheese_sticks': fingers(['A89058', 'D0B47A', 'E8CE94', 'FAE8B8'], straight=True),
        'shrimp_croquette': croquette(BREADED),
        'cheese_croquette': croquette(['8A5A1A', 'C08830', 'E0AA48', 'FAD070']),
        'chicken_croquette': croquette(['7E4C1C', 'B47A32', 'D49C4C', 'F0C470']),
        'lucifer': croquette(['6E2A14', 'A8461E', 'D06A2C', 'F09A50']),
        'potato_croquette': croquette(['8E6020', 'C89038', 'E6B254', 'FFD87C']),
        'chicken_fingers': fingers(BREADED),
        'fish_sticks': fingers(['8E5E1C', 'C48A30', 'E0AC4C', 'FAD278'], straight=True),
        'cheese_sticks': fingers(['8A5A1A', 'C08830', 'E0AA48', 'FAD070'], straight=True),
        'cervelas_mix': paste(['A06060', 'CC8484', 'E8A8A4', 'FCD4CC'], 0),
        'mexicano_mix': paste(['8A3428', 'B44C38', 'D46A50', 'F09478'], 0),
        'viandelle_mix': paste(['A87868', 'D09E8A', 'EABEA8', 'FCDCC8'], 0),
        'raw_cervelas': sausage(['A06060', 'CC8484', 'E8A8A4', 'FCD4CC'], thick=True),
        'raw_mexicano': sausage(['8A3428', 'B44C38', 'D46A50', 'F09478'], thick=True),
        'raw_viandelle': sausage(['A87868', 'D09E8A', 'EABEA8', 'FCDCC8']),
        'cervelas': sausage(['5A2418', '8E3C28', 'B85C3C', 'E08A64'], thick=True),
        'mexicano': sausage(['4A140C', '7A2414', 'A43A1E', 'D46030'], thick=True),
        'viandelle': sausage(['5E3418', '925428', 'B87038', 'E09C58']),
        'loempia_wrapper': shape({(x, y) for x in range(3, 13) for y in range(4, 13)}, ['B8AC8C', 'DCD2B4', 'F0E8D0', 'FFFAEC']),
        'incomplete_loempia': crumbs(shape(capsule(4, 11, 11, 4, 2.4), ['B8AC8C', 'DCD2B4', 'F0E8D0', 'FFFAEC']), capsule(4, 11, 11, 4, 2.4), GREENS, 'E9862A'),
        'raw_loempia': croquette(['B8AC8C', 'DCD2B4', 'F0E8D0', 'FFFAEC'], breaded=False),
        'loempia': croquette(['8A5A1A', 'C8882E', 'E8AE4C', 'FFDA80'], breaded=False),
        'filet_americain': heap(['8E2A2A', 'B84040', 'D86060', 'F09090'], 'F4D25C'),
        'pistolet_americain': hot_dog(sausage_palette=['8E2A2A', 'B84040', 'D86060', 'F09090']),
        'americain_frites': mitraillette(meat=['8E2A2A', 'B84040', 'D86060', 'F09090'], fries=FRIES_GOLDEN),
        'raw_patty': disc(['8E2A2A', 'B84040', 'D86060', 'F09090'], spots='F4C0B0'),
        'patty': disc(BROWN, spots='3A1A0E'),
        'incomplete_burger': burger(top=False),
        'friterie_burger': burger(sauce=SAUCE_WHITE),
        'friterie_cheeseburger': burger(cheese=True),
        'hot_dog_bun_dough': baguette(canvas(), ['BCA466', 'DCC484', 'F0D898', 'FFEEC4']),
        'hot_dog_bun': baguette(canvas(), ['7A4618', 'A8642A', 'C87C30', 'E8A658']),
        'incomplete_hot_dog': hot_dog(['5A2418', '8E3C28', 'B85C3C', 'E08A64'], bun=['7A4618', 'A8642A', 'C87C30', 'E8A658']),
        'hot_dog': hot_dog(['5A2418', '8E3C28', 'B85C3C', 'E08A64'], onions=True, sauce=['A8321E', 'D65A36'],
                           bun=['7A4618', 'A8642A', 'C87C30', 'E8A658']),
        'gyros': heap(BROWN, 'D49A5A'),
        'incomplete_pita': pita(),
        'pita': pita(filled=True, sauce=SAUCE_WHITE),
        'incomplete_brochette': brochette(PINK, cubes=1),
        'raw_brochette': brochette(['A06060', 'CC8484', 'E8A8A4', 'FCD4CC']),
        'brochette': brochette(BROWN),
        'bami_mix': paste(['B0902E', 'D4B454', 'ECD27A', 'FFF0B0'], 0),
        'nasi_mix': paste(['B0A890', 'D4CCB4', 'ECE6D4', 'FFFFF4'], 0),
        'raw_bami_disc': thick_disc(BREADED_RAW, 'F0D880'),
        'raw_nasi_disc': thick_disc(BREADED_RAW, 'FFFFF4'),
        'bami_disc': thick_disc(['8A4E14', 'C47424', 'E6963A', 'FFC060'], 'FFE08A'),
        'nasi_disc': thick_disc(['8A4E14', 'C47424', 'E6963A', 'FFC060'], 'FFFFF0'),
        'raw_squid_rings': rings(['B8A8B8', 'DCD0DC', 'F0E8F0', 'FFFAFF']),
        'squid_rings': rings(BREADED, crumb=True),
        'raw_chicken_nuggets': nuggets(BREADED_RAW),
        'chicken_nuggets': nuggets(BREADED),
        'corn_flakes': heap(['A07818', 'D0A030', 'ECC450', 'FFE890'], 'FFF6C8'),
        'raw_chicken_crunch': croquette(['B89A40', 'DCBE60', 'F0D884', 'FFF0B8']),
        'chicken_crunch': croquette(['8A5A10', 'C88A20', 'E8B03C', 'FFDA6A']),
        'raw_chicken_wings': ham(RAW_CHICKEN),
        'fried_chicken_wings': ham(BREADED),
        'spicy_chicken_wings': ham(['6E2010', 'A83A1A', 'D05A28', 'F08A48']),
        'raw_onion_rings': rings(['A8A0B8', 'D0CAE0', 'ECE8F4', 'FFFFFF']),
        'battered_onion_rings': rings(['B8AC88', 'DCD2AC', 'F0E8CA', 'FFFAE8']),
        'onion_rings': rings(BREADED, crumb=True),
    }
    return {gen.ITEM_DIR / f'{name}.png': px for name, px in out.items()}


# v2 Belgian dishes (docs/12): bowls with their stew and a topping, oval baking dishes, tarts seen
# from the side, a puff pastry cup, a pressed sandwich.
STEW_BROWN = ['3E200E', '5A3018', '7A4424', 'A0643A']
CERAMIC = ['8A8A92', 'B8B8C0', 'D8D8E0', 'F4F4FA']
MUSSEL_SHELL = '20202E'
PASTRY = ['8E5E1C', 'C48A30', 'E0AC4C', 'FAD278']
PASTRY_RAW = ['B8A068', 'D8C490', 'ECDCB0', 'FAF0D4']


def topped_bowl(content, toppings=(), fries_on=False, sausage_on=None):
    px = bowl(content, content[3])
    for x, y in toppings:
        px[y][x] = colour(MUSSEL_SHELL)
        px[y][x + 1] = colour('3A3A52')
    if fries_on:
        draw_sticks(px, [(4, 7, 5, 2), (9, 7, 10, 3)], FRIES_GOLDEN, clip=7)
    if sausage_on:
        shape(capsule(3, 5, 12, 5, 1.4), sausage_on, px)
    return px


def baking_dish(top, spots_value=None):
    px = shape(ellipse(8, 10, 7.2, 4), CERAMIC)
    shape(ellipse(8, 9.2, 5.6, 2.6), top, px)
    if spots_value:
        dots(px, [(5, 9), (8, 8), (10, 10), (7, 10)], spots_value)
    return px


def tart(filling=None, spots_value=None):
    px = shape({(x, y) for x in range(2, 14) for y in range(9, 13)}, PASTRY_RAW if filling is None else PASTRY)
    if filling:
        shape(ellipse(8, 9.5, 5.6, 1.8), filling, px)
        if spots_value:
            dots(px, [(5, 9), (8, 9), (10, 9)], spots_value)
    return px


def pastry_cup(palette, filling=None):
    px = shape({(x, y) for x in range(4, 12) for y in range(5, 14)}, palette)
    if filling:
        shape(ellipse(8, 5, 4.2, 1.8), filling, px)
        dots(px, [(6, 4), (9, 5)], 'D8C8A8')
    return px


def croque(bread, toasted):
    px = canvas()
    shape({(x, y) for x in range(2, 14) for y in (11, 12)}, bread, px)
    for x in range(2, 14):
        px[10][x] = colour('F4C430' if x % 3 else 'E8A0A0')
    shape({(x, y) for x in range(2, 14) for y in range(6, 10)}, bread, px)
    if toasted:
        dots(px, [(4, 7), (7, 8), (10, 7), (12, 8)], '6E3E14')
    return px


def dishes():
    MUSSELS = ['6A5A3A', '8E7A50', 'B09A6A', 'D4C08E']
    BEER_BROTH = ['8A6420', 'B08430', 'D0A448', 'F0CC78']
    MASH = ['A89A6A', 'CCBE8C', 'E6DAAE', 'FAF2D4']
    CREAM_SOUP = ['B0A888', 'D0C8A8', 'E8E2C8', 'FCF8E8']
    shells = [(4, 6), (7, 5), (10, 6), (6, 7)]
    out = {
        'moules_marinieres': topped_bowl(MUSSELS, shells),
        'moules_a_la_biere': topped_bowl(BEER_BROTH, shells),
        'moules_frites': topped_bowl(MUSSELS, shells, fries_on=True),
        'carbonnade': topped_bowl(STEW_BROWN),
        'carbonnade_frites': topped_bowl(STEW_BROWN, fries_on=True),
        'stoemp': topped_bowl(MASH, [(5, 7), (9, 7)]),
        'stoemp_saucisse': topped_bowl(MASH, sausage_on=['5A2418', '8E3C28', 'B85C3C', 'E08A64']),
        'waterzooi': topped_bowl(CREAM_SOUP),
        'lapin_a_la_gueuze': topped_bowl(['5A3014', '7E4820', 'A0602C', 'C88448']),
        'brussels_sprouts_bacon': topped_bowl(['2E5A1C', '4A8028', '6AA03A', '9ACC5A']),
        'asparagus_flemish': topped_bowl(['B8A848', 'D8C868', 'F0E08C', 'FFF4C0']),
        'hop_shoots_cream': topped_bowl(['8A9A5A', 'AAB878', 'CCD49C', 'ECF0C8']),
        'salade_liegeoise': topped_bowl(['8A7A3A', 'B09A50', 'D0BA6A', 'ECD898']),
        'hochepot': topped_bowl(['4A3018', '6A4824', '8A6034', 'B08048']),
        'escaveche': topped_bowl(['A8A070', 'C8C090', 'E0DAB0', 'F6F2D8']),
        'boudin_compote': topped_bowl(['8C7C30', 'B0A048', 'D8C86A', 'F0E498'], sausage_on=['3A3030', '585050', '787070', 'A09898']),
        'steak_frites': fries(FRIES_GOLDEN),
        'incomplete_chicons_au_gratin': peak(['C8A8A0', 'E0C4BC', 'F0E0D8', 'FFF4F0']),
        'raw_chicons_au_gratin': baking_dish(['C8BC98', 'E0D6B6', 'F0E6C8', 'FFF8E4'], 'F4C430'),
        'chicons_au_gratin': baking_dish(['8E5E1C', 'C48A30', 'E0AC4C', 'FAD278'], '6E3E14'),
        'raw_vol_au_vent_shell': pastry_cup(PASTRY_RAW),
        'vol_au_vent_shell': pastry_cup(PASTRY),
        'incomplete_vol_au_vent': pastry_cup(PASTRY, ['B0A888', 'D0C8A8', 'E8E2C8', 'FCF8E8']),
        'vol_au_vent': pastry_cup(PASTRY, ['C8BC98', 'E0D6B6', 'F0E6C8', 'FFF8E4']),
        'raw_boudin': sausage(['8A8080', 'B0A8A8', 'D0C8C8', 'ECE6E6']),
        'boudin': sausage(['3A3030', '585050', '787070', 'A09898']),
        'tart_shell': tart(),
        'incomplete_tart': tart(['B8B088', 'D8D0A8', 'ECE6C4', 'FCF8E4'], '6AA03A'),
        'raw_flamiche': tart(['C8B868', 'E0D488', 'F0E6A8', 'FFF6D0'], '6AA03A'),
        'flamiche': tart(['A87A20', 'D0A030', 'E8C04C', 'FFE080'], '4A8028'),
        'incomplete_croque_monsieur': croque(['B8A47C', 'D8C49C', 'ECDCB8', 'FAF0DA'], False),
        'raw_croque_monsieur': croque(['B8A47C', 'D8C49C', 'ECDCB8', 'FAF0DA'], False),
        'croque_monsieur': croque(['8E5E1C', 'C48A30', 'E0AC4C', 'FAD278'], True),
    }
    # The plate of steak-frites: a steak beside the fries.
    shape(ellipse(5.5, 11, 4, 2.6), ['3A1A0E', '5A2A16', '7A3C20', '9A5430'], out['steak_frites'])
    # The unfinished croque has no cheese yet.
    for x in range(2, 14):
        out['incomplete_croque_monsieur'][10][x] = colour('E8A0A0')
    return {gen.ITEM_DIR / f'{name}.png': px for name, px in out.items()}


# v2 waffles (docs/12): a Liège waffle is a rounded slab with a shallow grid and pearls, a Brussels waffle a
# rectangle of deep squares, the lacquemant a thin round wafer.
WAFFLE = ['8A5418', 'C08030', 'E0A84C', 'FAD282']
WAFFLE_PALE = ['A8864C', 'D0B070', 'E8CC90', 'FAE6B8']


def waffle(palette, deep=False, rounded=False):
    if rounded:
        cells = ellipse(8, 9, 6.6, 4.8)
    else:
        cells = {(x, y) for x in range(2, 14) for y in range(4, 14)}
    px = shape(cells, palette)
    for x, y in cells:
        if x % 3 == 1 or y % 3 == 0:
            continue
        if all((x + dx, y + dy) in cells for dx, dy in [(1, 0), (-1, 0), (0, 1), (0, -1)]):
            px[y][x] = colour(palette[0] if deep else palette[1])
    return px


def waffles():
    choc = ['3A2010', '52301A', '6E4428', '8E5E3A']
    out = {
        'liege_waffle_dough': paste(['B89A50', 'D8BC70', 'ECD490', 'FFF0C0'], 0),
        'brussels_waffle_batter': bowl(['C8B888', 'E0D4A8', 'F0E8C8', 'FFFAEC']),
        'lacquemant_batter': bowl(['B89A60', 'D4B880', 'E8D0A0', 'FAEAC8']),
        'lacquemant_wafer': shape(ellipse(8, 9, 6.4, 2.4), WAFFLE_PALE),
        'liege_waffle': waffle(WAFFLE, rounded=True),
        'brussels_waffle': waffle(WAFFLE, deep=True),
        'brussels_waffle_sugared': waffle(WAFFLE, deep=True),
        'brussels_waffle_chocolate': waffle(WAFFLE, deep=True),
        'lacquemant': shape(ellipse(8, 9, 6.4, 2.4), WAFFLE_PALE),
    }
    # Pearl sugar caught in the Liège grid.
    dots(out['liege_waffle'], [(5, 7), (9, 6), (11, 10), (6, 11), (8, 9)], 'FFFFFF')
    # Powdered sugar dusted over the squares.
    for x in range(3, 13):
        for y in range(5, 13):
            if (x * 5 + y * 3) % 7 == 0:
                out['brussels_waffle_sugared'][y][x] = colour('FFFFFF')
    # Chocolate poured across the top half.
    for x in range(3, 13):
        for y in range(5, 9 + (x % 3 == 0)):
            out['brussels_waffle_chocolate'][y][x] = colour(choc[2] if (x + y) % 3 else choc[3])
        out['brussels_waffle_chocolate'][4][x] = colour(choc[0])
    # The syrup shows at the seam of the folded wafer.
    for x in range(3, 13):
        out['lacquemant'][9][x] = colour('5A3218' if x % 2 else '83542E')
    return {gen.ITEM_DIR / f'{name}.png': px for name, px in out.items()}


def waffle_iron_faces():
    iron = ['2A2E32', '44494E', '5E6469', '858B90']
    out = {'waffle_iron/side': panel(iron, False), 'waffle_iron/top': panel(iron), 'waffle_iron/bottom': panel(['1A1C1E', '2A2E32', '3A3E42', '4E5358'], False)}
    plate = canvas(colour(iron[2]))
    for y in range(16):
        for x in range(16):
            if x % 4 == 0 or y % 4 == 0:
                plate[y][x] = colour(iron[0])
            elif x % 4 == 1 or y % 4 == 1:
                plate[y][x] = colour(iron[3])
    out['waffle_iron/plate'] = plate
    return out


# v2 sweets (docs/12): biscuits are flat slabs, sweets small cones and cubes, pastries balls and rolls,
# tarts the M17 tart shape, the hot chocolate a glass bottle.
SPECULOOS_BROWN = ['6E3A12', '9A5620', 'BC7432', 'DC9850']
CHOC = ['2E180A', '42240E', '5A3418', '7A4E2C']
GLASS = ['8AA8B8', 'B8D0DC', 'DCEAF0', 'FFFFFF']


def biscuit(palette, pattern=None):
    cells = {(x, y) for x in range(3, 13) for y in range(5, 12)}
    px = shape(cells, palette)
    if pattern:
        for x in range(4, 12):
            for y in range(6, 11):
                if (x + y) % 3 == 0:
                    px[y][x] = colour(pattern)
    return px


def cones(palette, tip):
    px = canvas()
    for cx in (5, 11):
        cells = {(x, y) for y in range(5, 13) for x in range(16) if abs(x + 0.5 - cx) <= (y - 4.5) * 0.45 + 0.6}
        shape(cells, palette, px)
        dots(px, [(cx, 5)], tip)
    return px


def pralines(palette, mark=None):
    px = canvas()
    for x0, y0 in [(2, 8), (9, 8), (6, 3)]:
        cells = {(x, y) for x in range(x0, x0 + 5) for y in range(y0, y0 + 4)}
        shape(cells, palette, px)
        if mark:
            dots(px, [(x0 + 2, y0 + 1)], mark)
    return px


def ballotin():
    px = shape({(x, y) for x in range(2, 14) for y in range(5, 14)}, ['B8A070', 'DCC898', 'F0E2C0', 'FFFAE8'])
    for x in range(2, 14):
        px[8][x] = colour('C8352B')
    for y in range(5, 14):
        px[y][8] = colour('C8352B')
    dots(px, [(7, 4), (9, 4), (8, 3)], 'C8352B')
    return px


def bottle(liquid):
    px = canvas()
    shape({(x, y) for x in range(7, 10) for y in range(2, 6)}, GLASS, px)
    shape(ellipse(8, 10.5, 4.6, 4.6), GLASS, px)
    for x, y in ellipse(8, 11, 3.4, 3.2):
        if y >= 9:
            px[y][x] = colour(liquid[2] if (x + y) % 4 else liquid[3])
    dots(px, [(6, 8), (6, 9)], 'FFFFFF')
    return px


def sweets():
    BRIOCHE = ['8A5A1A', 'C08A30', 'E0AC4C', 'FAD278']
    BRIOCHE_RAW = ['B8A060', 'D8C484', 'ECDCA8', 'FAF0D4']
    out = {
        'speculoos_dough': paste(['8A5A28', 'B07A40', 'CC985A', 'E8B87C'], 0),
        'raw_speculoos': biscuit(['A07848', 'C49A64', 'DCB680', 'F0D0A0'], 'B08856'),
        'speculoos': biscuit(SPECULOOS_BROWN, '8A4818'),
        'speculoos_crumbs': heap(SPECULOOS_BROWN),
        'pistolet_speculoos': roll(CRUST, crease='9A5620'),
        'brussels_waffle_speculoos': waffle(WAFFLE, deep=True),
        'cuberdon': cones(['4A1838', '6E2450', '8E346A', 'B85A90'], 'D890B8'),
        'praline': pralines(CHOC, 'C8A070'),
        'praline_kriek': pralines(CHOC, 'B0283A'),
        'ballotin': ballotin(),
        'babelutte': biscuit(['8A5A18', 'B88030', 'D8A048', 'F4C470']),
        'croustillon_dough': paste(['B8A468', 'D8C48C', 'ECDCAE', 'FFF6D8'], 0),
        'croustillons': nuggets(['8A5A18', 'C08830', 'E0AA4C', 'FFD27A']),
        'croustillons_sugared': nuggets(['A88A58', 'D8C090', 'F0E0C0', 'FFFFFF']),
        'bouquette_batter': bowl(['5A4030', '7A5A44', '9A7860', 'BC9C80']),
        'bouquette': disc(['4A3020', '6E4A32', '8E6448', 'B08864'], spots='2A1810'),
        'bouquette_cassonade': disc(['4A3020', '6E4A32', '8E6448', 'B08864'], spots='C98C4A'),
        'couque_dough': paste(['8A5A20', 'A8763A', 'C49450', 'DCB270'], 0),
        'raw_couque': biscuit(['A8864C', 'C8A468', 'E0C088', 'F4DCAC'], 'B8945C'),
        'couque_de_dinant': biscuit(['5A3010', '7E4618', '9E5E24', 'C0823C'], '4A2408'),
        'rice_filling': bowl(['C8C0A8', 'E0DAC8', 'F2EEE2', 'FFFFFA']),
        'raw_tarte_au_riz': tart(['C8C0A8', 'E0DAC8', 'F2EEE2', 'FFFFFA']),
        'tarte_au_riz': tart(['B8A060', 'D8C484', 'ECDCA8', 'FAF0D4'], 'A87A20'),
        'raw_tarte_au_sucre': tart(['8A5A20', 'A8763A', 'C49450', 'DCB270']),
        'tarte_au_sucre': tart(['5E3714', '875323', 'A86A2C', 'C98C4A'], 'E8B06A'),
        'raw_mattentaart': tart(['C4BEB0', 'E0DCD2', 'F4F0E6', 'FFFFFF']),
        'mattentaart': tart(['A8864C', 'C8A468', 'E0C088', 'F4DCAC'], '7A5428'),
        'raw_craquelin': roll(BRIOCHE_RAW, crease='FFFFFF'),
        'craquelin': roll(BRIOCHE, crease='FFFFFF'),
        'raw_cramique': roll(BRIOCHE_RAW, crease='3A2030'),
        'cramique': roll(BRIOCHE, crease='3A2030'),
        'flat_brioche': biscuit(BRIOCHE_RAW),
        'raw_pain_a_la_grecque': biscuit(BRIOCHE_RAW, 'A86A2C'),
        'pain_a_la_grecque': biscuit(BRIOCHE, '875323'),
        'incomplete_merveilleux': peak(['C8C0A8', 'E0DAC8', 'F2EEE2', 'FFFFFA']),
        'merveilleux': peak(['C8C0A8', 'E0DAC8', 'F2EEE2', 'FFFFFA']),
        'dame_blanche': scoop(['D6CCAE', 'ECE4C8', 'FAF2D8', 'FFFFFF']),
        'hot_chocolate_bottle': bottle(['3A1E0C', '5A3018', '7A4424', '9A6038']),
    }
    dots(out['merveilleux'], [(6, 8), (8, 6), (9, 10), (7, 11), (10, 8)], CHOC[1])
    # Chocolate poured over the ice cream.
    for x in range(5, 12):
        out['dame_blanche'][4 + (x % 2)][x] = colour(CHOC[2])
    # Speculoos spread across the waffle.
    for x in range(3, 13):
        out['brussels_waffle_speculoos'][5][x] = colour(SPECULOOS_BROWN[2])
        out['brussels_waffle_speculoos'][6 + (x % 2)][x] = colour(SPECULOOS_BROWN[1])
    return {gen.ITEM_DIR / f'{name}.png': px for name, px in out.items()}


def artwork():
    out = {}
    for name, palette, topped, thick, unfinished in [
        ('raw_fricadelle', RAW, False, False, False),
        ('fricadelle', FRIED, False, False, False),
        ('raw_the_fricadelle', RAW, True, False, False),
        ('the_fricadelle', FRIED, True, False, False),
        ('raw_ultimate_fricadelle', ['977346', 'CFAC72', 'E8C98D', 'FFF0B9'], True, True, False),
        ('ultimate_fricadelle', GOLD, True, True, False),
        ('incomplete_the_fricadelle', ['997765', 'C6A08A', 'E1BDA1', 'F6D8B8'], True, False, True),
        ('incomplete_ultimate_fricadelle', ['A18B61', 'CDB98C', 'E5D4A7', 'F4E5C4'], True, True, True),
    ]:
        out[gen.ITEM_DIR / f'{name}.png'] = sausage(palette, topped, thick, unfinished)
    for tier, name, palette in [(0, 'fricadelle_paste', RAW), (1, 'exceptional_paste', FRIED), (2, 'absolute_paste', GOLD)]:
        out[gen.ITEM_DIR / f'{name}.png'] = paste(palette, tier)
    for name, palette in [('minced_pork', RAW), ('minced_beef', ['632E2B', '973F38', 'BF6251', 'E6B29B']),
                          ('minced_chicken', ['91705A', 'C79D82', 'E7C3A3', 'FFE0BF']), ('bread_crumbs', FRIED)]:
        out[gen.ITEM_DIR / f'{name}.png'] = bitmap(HEAP, dict(zip('0123', palette)))
    for name, palette in [('raw_fries', FRIES_RAW), ('blanched_fries', FRIES_BLANCHED), ('fries', FRIES_GOLDEN)]:
        out[gen.ITEM_DIR / f'{name}.png'] = fries(palette)
    out[gen.ITEM_DIR / 'fries_beef_tallow.png'] = fries(FRIES_TALLOW, sparkle=True)
    out[gen.ITEM_DIR / 'paper_cone.png'] = paper_cone()
    out[gen.ITEM_DIR / 'cone_of_fries.png'] = cone_of_fries(FRIES_GOLDEN)
    out[gen.ITEM_DIR / 'cone_of_fries_beef_tallow.png'] = cone_of_fries(FRIES_TALLOW)
    for sauce in CONE_SAUCES:
        out[gen.ITEM_DIR / f'cone_of_fries_{sauce}.png'] = cone_of_fries(FRIES_GOLDEN, LIQUIDS[sauce])
        out[gen.ITEM_DIR / f'cone_of_fries_beef_tallow_{sauce}.png'] = cone_of_fries(FRIES_TALLOW, LIQUIDS[sauce])
    out[gen.ITEM_DIR / 'beef_tallow.png'] = bitmap([
        '................', '................', '................', '................',
        '.....000000.....', '...0033333300...', '..033333332220..', '..033333322220..',
        '..032222222210..', '..032222222210..', '..022222222110..', '...0222222110...',
        '....00111100....', '......0000......', '................', '................',
    ], dict(zip('0123', ['A38E61', 'D0BC89', 'F0E5BE', 'FFF7DC'])))
    out[gen.ITEM_DIR / 'belgian_spices.png'] = bitmap([
        '................', '................', '................', '................',
        '................', '................', '................', '......2.........',
        '..........1.....', '.......00.......', '.....003300.....', '...0033222200...',
        '..033223212210..', '..022122212110..', '...0000000000...', '................',
    ], dict(zip('0123', ['743621', 'A54825', 'CD6B2B', 'EEA044'])))
    for name, palette in LIQUIDS.items():
        out[gen.ITEM_DIR / f'{name}_bucket.png'] = bitmap(BUCKET, {**METAL, 'h': palette[3], 'l': palette[2], 'd': palette[0]})
        out[gen.FLUID_DIR / f'{name}_still.png'] = fluid(palette)
        out[gen.FLUID_DIR / f'{name}_flow.png'] = fluid(palette, True)
    out.update(base_ingredients())
    out.update(crops_items())
    out.update(friterie())
    out.update(dishes())
    out.update(waffles())
    out.update(sweets())
    out.update(crop_stages())
    out.update({gen.BLOCK_DIR / f'{name}.png': px for name, px in blocks().items()})
    out.update({gen.BLOCK_DIR / f'{name}.png': px for name, px in waffle_iron_faces().items()})
    return out


def preview(outputs):
    (gen.ROOT / 'build').mkdir(exist_ok=True)
    entries = list(outputs.items())
    rows = -(-len(entries) // 7)
    sheet = [[colour('202830') for _ in range(7 * 144)] for _ in range(rows * 280)]
    cards = []
    for index, (path, px) in enumerate(entries):
        ox, oy = (index % 7) * 144 + 8, (index // 7) * 280 + 8
        for y, row in enumerate(px):
            for x, value in enumerate(row):
                value = value or colour('37434D' if (x + y) % 2 else '2C363E')
                for dy in range(8):
                    sheet[oy + y * 8 + dy][ox + x * 8:ox + x * 8 + 8] = [value] * 8
        relative = path.relative_to(gen.ROOT).as_posix()
        cards.append(f'<figure><img src="../{relative}" alt="{html.escape(path.stem)}"><figcaption>{html.escape(path.relative_to(gen.ITEM_DIR.parent).as_posix())}</figcaption></figure>')
    (gen.ROOT / 'build/texture-preview.png').write_bytes(gen.png_bytes(sheet))
    (gen.ROOT / 'build/texture-preview.html').write_text(
        '<!doctype html><html lang="en"><meta charset="utf-8"><meta name="author" content="THEFricadelle">'
        '<title>Belgian Snacks textures</title><style>body{background:#202830;color:#eee;font:14px system-ui}'
        'main{display:flex;flex-wrap:wrap}figure{width:180px;margin:12px;overflow-wrap:anywhere}'
        'img{width:128px;image-rendering:pixelated;background:repeating-conic-gradient(#37434d 0% 25%,#2c363e 0% 50%) 0/16px 16px}'
        '</style><h1>Create: Belgian Snacks</h1><p>' + str(len(entries)) + ' textures — THEFricadelle</p><main>'
        + ''.join(cards) + '</main></html>', encoding='utf-8')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--force', action='store_true', help='replace existing non-placeholder artwork')
    args = parser.parse_args()
    outputs, placeholders = artwork(), gen.planned()
    assert outputs.keys() == placeholders.keys()
    written = 0
    for path, px in outputs.items():
        assert len({p for row in px for p in row if p}) <= 16, path
        expected = placeholders[path]()
        assert (len(px), len(px[0])) == (len(expected), len(expected[0])), path
        data = gen.png_bytes(px)
        if path.exists() and path.read_bytes() not in (gen.png_bytes(expected), data) and not args.force:
            raise SystemExit(f'Preserving edited artwork: {path}; use --force explicitly to replace it')
    for path, px in outputs.items():
        data = gen.png_bytes(px)
        if not path.exists() or path.read_bytes() != data:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
            written += 1
        # A flow texture holds two stacked frames: the game animates it only with this file beside it.
        meta = path.with_name(path.name + '.mcmeta')
        if path.name.endswith('_flow.png') and not meta.exists():
            meta.write_text('{"animation": {"frametime": 8}}' + chr(10), encoding='utf-8', newline=chr(10))
            written += 1
    preview(outputs)
    print(f'{len(outputs)} textures verified; {written} written; preview: build/texture-preview.html')


if __name__ == '__main__':
    main()
