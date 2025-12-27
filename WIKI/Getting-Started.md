# Getting Started

## Requirements
*   **Server**: Spigot/Paper 1.20.4 or higher (1.21+ recommended).
*   **Java**: Java 21.
*   **Optional**: PlaceholderAPI (for variable support in messages).

## Installation
1.  Download the latest `MultiBlockEngine.jar`.
2.  Place it in your server's `plugins` folder.
3.  Restart the server.
4.  A default example `example_portal.yml` will be generated in `plugins/MultiBlockEngine/multiblocks/`.

## Your First Multiblock
Create a new file `simple_tower.yml` in the `multiblocks` folder:

```yaml
id: simple_tower
version: "1.0"
display_name: "&eSimple Tower"

controller: DIAMOND_BLOCK

pattern:
  - offset: [0, -1, 0]
    match: GOLD_BLOCK
  - offset: [0, 1, 0]
    match: EMERALD_BLOCK

actions:
  on_interact:
    - type: message
      value: "&aYou touched the tower!"
```

1.  Reload the plugin using `/mb reload`.
2.  Build the structure in-game:
    *   Gold Block (Bottom)
    *   Diamond Block (Center/Controller)
    *   Emerald Block (Top)
3.  Right-click the Diamond Block to test!
