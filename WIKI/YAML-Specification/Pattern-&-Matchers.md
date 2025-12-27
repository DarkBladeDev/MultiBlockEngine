# Pattern & Matchers

The `pattern` list defines the structure's shape.

## Structure
```yaml
pattern:
  - offset: [x, y, z]
    match: <MATCHER>
    optional: <BOOLEAN> # Default: false
```

## Types of Matchers

### 1. Material
Matches a specific material.
```yaml
match: OBSIDIAN
```

### 2. Air
Matches empty space (must be air).
```yaml
match: AIR
```

### 3. Tags (Groups)
Matches any block within a Vanilla Tag.
```yaml
match: "#minecraft:logs" # Matches oak_log, birch_log, etc.
match: "#minecraft:wool"
```

### 4. BlockData
Matches exact state.
```yaml
match: "minecraft:switch[face=floor,facing=north]"
```

### 5. AnyOf (List)
Matches if the block is ANY of the specified types.
```yaml
match:
  - COBBLESTONE
  - MOSSY_COBBLESTONE
  - STONE_BRICKS
```
