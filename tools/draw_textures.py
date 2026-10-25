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
}
CONE_SAUCES = ['mayonnaise', 'curry_ketchup', 'andalouse', 'samourai', 'americaine', 'sauce_lapin']


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
    out.update({gen.BLOCK_DIR / f'{name}.png': px for name, px in blocks().items()})
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
