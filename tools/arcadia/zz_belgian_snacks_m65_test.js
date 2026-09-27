// Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
// SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
//
// M6.5 test script, copied by tools/arcadia_smoke.py into run/arcadia only, never into the pack:
// what a pack maintainer would do with our recipes and tags, checked by ArcadiaSmokeRun.

ServerEvents.recipes(event => {
  // Remove one of ours by its stable id.
  event.remove({ id: 'create_belgian_snacks:frying/fricadelle' })
  // Add a frying recipe of our type.
  event.custom({
    type: 'create_belgian_snacks:frying',
    heat_requirement: 'heated',
    ingredients: [
      { item: 'minecraft:potato' },
      { type: 'neoforge:tag', tag: 'create_belgian_snacks:frying_oils', amount: 10 }
    ],
    processing_time: 40,
    results: [{ id: 'minecraft:baked_potato' }]
  }).id('create_belgian_snacks:kubejs_test/frying_potato')
})

ServerEvents.tags('item', event => {
  // Take a food out of the Supreme Grinder's list.
  event.add('create_belgian_snacks:grinder/blacklist', 'minecraft:apple')
})
