# Examples

This page provides complete, functional examples of multiblock structures to help you understand how to combine all the features of the MultiBlockEngine.

## 1. The Magic Altar (Altar Mágico)

A simple structure that gives a regeneration effect to nearby players when clicked, but requires "mana" (represented by a variable) to function.

### Structure Layout
*   **Center (0,0,0)**: Enchanting Table (Controller)
*   **Below Center (0,-1,0)**: Obsidian
*   **Corners (-1,-1,-1), (1,-1,1), etc.**: Gold Blocks (surrounding the obsidian base)

```yaml
id: magic_altar
version: "1.0"
tick_interval: 20 # Checks every second
display_name: "&dMagic Altar"

controller: ENCHANTING_TABLE

# Define the surrounding blocks relative to the controller
pattern:
  # Base Center
  - offset: [0, -1, 0]
    match: OBSIDIAN
  # Corners (X, Z)
  - offset: [1, -1, 1]
    match: GOLD_BLOCK
  - offset: [-1, -1, -1]
    match: GOLD_BLOCK
  - offset: [1, -1, -1]
    match: GOLD_BLOCK
  - offset: [-1, -1, 1]
    match: GOLD_BLOCK

actions:
  # Initialize mana when created
  on_create:
    - type: set_variable
      key: mana
      value: 100

  # Regeneration when clicked if enough mana
  on_interact:
    - type: conditional
      conditions:
        - type: variable
          key: mana
          value: 10
          comparison: GREATER_OR_EQUAL
      then:
        - type: modify_variable
          key: mana
          operation: SUBTRACT
          amount: 10
        - type: message
          value: "&aYou feel the altar's energy! (-10 Mana)"
        - type: command
          value: "effect give %player% regeneration 10 1"
          console: true
      else:
        - type: message
          value: "&cThe altar doesn't have enough mana!"

  # Recharge mana periodically
  on_tick:
    - type: conditional
      conditions:
        - type: variable
          key: mana
          value: 100
          comparison: LESS_THAN
      then:
        - type: modify_variable
          key: mana
          operation: ADD
          amount: 1
```

## 2. Teleporter Gate (Portal de Teletransporte)

A structure that teleports the player when they right-click the controller, if they have permission.

### Structure Layout
*   **Bottom (0,0,0)**: Gold Block (Controller)
*   **Top (0,1,0)**: Light Weighted Pressure Plate

```yaml
id: teleport_gate
version: "1.0"
tick_interval: 20

controller: GOLD_BLOCK

pattern:
  - offset: [0, 1, 0]
    match: LIGHT_WEIGHTED_PRESSURE_PLATE

actions:
  on_interact:
    - type: conditional
      conditions:
        - type: permission
          value: "mbe.use.teleport"
      then:
        - type: teleport
          offset: [100, 64, 100] # Relative teleport? No, typically absolute or relative to controller. 
          # Note: The basic TeleportAction usually takes relative coords. 
          # For absolute, use console command.
          target: player
        - type: message
          value: "&bWarping..."
      else:
        - type: message
          value: "&cYou don't have permission to use this gate."
```

## 3. Industrial Furnace (Horno Industrial)

A machine that changes state while "processing" items.

### Structure Layout
*   **Center (0,0,0)**: Blast Furnace (Controller)
*   **Left (-1,0,0)**: Bricks
*   **Right (1,0,0)**: Bricks

```yaml
id: industrial_furnace
version: "1.0"
tick_interval: 20
display_name: "&6Industrial Furnace"

controller: BLAST_FURNACE

pattern:
  - offset: [-1, 0, 0]
    match: BRICKS
  - offset: [1, 0, 0]
    match: BRICKS

actions:
  on_create:
    - type: set_state
      value: INACTIVE

  on_interact:
    - type: conditional
      conditions:
        - type: state
          value: INACTIVE
        # Note: Item check condition would need to be implemented or use permission/sneaking as proxy
        - type: sneaking
          value: true
      then:
        - type: set_state
          value: ACTIVE
        - type: message
          value: "&6Furnace started!"
        # 'wait' is not a standard action in the basic parser, usually handled via state + tick
        # This example assumes a simple state switch for demonstration
        - type: set_state
          value: INACTIVE
      else:
        - type: message
          value: "&7Furnace is busy or you need to sneak."
```

### Key Differences from "Standard" YAML
*   **Explicit Offsets**: MultiBlockEngine requires exact `[x, y, z]` offsets for each block relative to the controller.
*   **Actions Key**: All event listeners (`on_interact`, etc.) must be nested under the `actions:` section.
*   **Variables**: Use `set_variable` and `modify_variable` actions to handle custom data.
