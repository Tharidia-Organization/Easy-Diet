# Easy Diet Datapack Usage

## Install / Enable

1. Copy the folder `datapack-easydiet-example` into:
   - Singleplayer: `.minecraft/saves/<YOUR_WORLD>/datapacks/`
   - Dedicated server: `<world_folder>/datapacks/`
2. In-game, run:
   - `/reload` (or restart the server)
3. Check it loaded:
   - `/datapack list`

## Main Config: `data/tharidia_easydiet/diet_config/example.json`

This is the mod’s custom datapack file. It controls the diet system configuration that the mod reads on datapack reload.

### `items` (per-item overrides, highest priority)

Use `items` to force an exact nutrient profile for specific items.

- Key: an item id string like `"minecraft:bread"` or `"modid:some_food"`
- Value: a nutrient object with any of the fields:
  - `grain`, `protein`, `vegetable`, `fruit`, `sugar`, `water`

If an item is present in `items`, the mod uses that profile directly (it won’t rely on recipe analysis or heuristics for that item).

Minimal example:

```json
{
  "items": {
    "minecraft:bread": { "grain": 6.0 },
    "minecraft:cooked_beef": { "protein": 8.0 }
  }
}
```

### `decay_rates` (how fast each category decays)

Controls how quickly each nutrition category goes down over time.

- Each field is a per-category decay rate:
  - `grain`, `protein`, `vegetable`, `fruit`, `sugar`, `water`
- Higher numbers = faster decay for that category.

Example (slower decay than default):

```json
{
  "decay_rates": {
    "grain": 0.075,
    "protein": 0.10,
    "vegetable": 0.125,
    "fruit": 0.125,
    "sugar": 0.15,
    "water": 0.175
  }
}
```

### `max_values` (per-category caps and ratio thresholds)

Sets the maximum value (cap) for each category:

- `grain`, `protein`, `vegetable`, `fruit`, `sugar`, `water`

This affects:

- How high a player can fill each category (values are clamped to these caps).
- When buffs/debuffs trigger, because effects use a ratio `current / max` internally (thresholds are based on percentage).

Example (lower water cap makes “low/high water” easier to reach in % terms):

```json
{
  "max_values": {
    "grain": 100.0,
    "protein": 100.0,
    "vegetable": 100.0,
    "fruit": 100.0,
    "sugar": 100.0,
    "water": 60.0
  }
}
```

### `settings.*` (decay timing + heuristic tuning)

`settings` controls timing and the parameters used when the mod needs to estimate nutrients automatically.

- `settings.decay_interval_seconds`
  - How often decay is applied.
  - Higher value = decay happens less frequently (slower in real time).

- `settings.heuristics.*`
  - Controls how heuristics compute nutrients for “base” items without usable recipes, or in fallback cases.
  - Important fields include:
    - `saturation_scale`
    - `fast_food_saturation_threshold`
    - `grain_nutrition_multiplier`, `fast_food_grain_bonus`
    - `protein_meat_multiplier`
    - `vegetable_hint_multiplier`
    - `fruit_hint_multiplier`
    - `fast_sugar_flat_bonus`, `fast_sugar_saturation_multiplier`
    - `water_always_eat_bonus`, `water_default_bonus`, `drink_water_bonus`

## Influencing Automatic Classification via Tags

The mod’s heuristics use several item tags to decide whether an item “looks like” meat, fruit, vegetables, grains, or drinks.

You can influence this by editing/adding tag files under:

- `data/c/tags/items/...`
- `data/forge/tags/items/...`
- `data/minecraft/tags/items/...`

Notes:

- Use `"replace": false` to add items without wiping other datapacks’ entries.
- Use `"replace": true` only if you want to fully override the tag.

Example (treat a modded soup as a drink-like item):

Create or edit `data/c/tags/items/drinks.json`:

```json
{
  "replace": false,
  "values": [
    "modid:tomato_soup"
  ]
}
```

After changing tags or `diet_config`, run `/reload` (or restart) to apply changes.

