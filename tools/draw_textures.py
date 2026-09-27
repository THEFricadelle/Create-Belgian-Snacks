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
}


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
    out.update({gen.BLOCK_DIR / f'{name}.png': px for name, px in blocks().items()})
    return out


def preview(outputs):
    (gen.ROOT / 'build').mkdir(exist_ok=True)
    entries = list(outputs.items())
    sheet = [[colour('202830') for _ in range(7 * 144)] for _ in range(6 * 280)]
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
        '</style><h1>Create: Belgian Snacks</h1><p>41 textures / 41 textures — THEFricadelle</p><main>'
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
    preview(outputs)
    print(f'{len(outputs)} textures verified; {written} written; preview: build/texture-preview.html')


if __name__ == '__main__':
    main()
