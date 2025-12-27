# Actions Reference

Actions are the "output" of the ECA system. They are defined in lists under event keys (e.g., `actions.on_interact`).

## General Actions

### `message`
Sends a chat message to the player or targets.
*   **value** (String): The message text. Supports color codes (`&a`) and placeholders (`%player%`, `<variable:key>`).
*   **target** (String, optional): Who receives the message.
    *   Default: The player who triggered the event.
    *   `nearby:radius` (e.g., `nearby:10`)
    *   `all`
    *   `permission:node`

### `command`
Executes a command from the console.
*   **value** (String): The command to run (without `/`). Supports `%player%`.

### `conditional`
Executes different actions based on conditions.
*   **conditions** (List): A list of conditions to check (AND logic).
*   **then** (List): Actions to execute if ALL conditions are true.
*   **else** (List): Actions to execute if ANY condition fails.

---

## State & Variable Actions

### `set_state`
Changes the lifecycle state of the multiblock instance.
*   **value** (String): The new state.
    *   `ACTIVE`
    *   `INACTIVE`
    *   `DISABLED`
    *   `DAMAGED`
    *   `OVERLOADED`

### `set_variable`
Sets a persistent variable for this specific instance.
*   **key** (String): The variable name.
*   **value** (Any): The value to store (String, Number, Boolean).

### `modify_variable`
Performs arithmetic on a numeric variable.
*   **key** (String): The variable name.
*   **operation** (String): The math operation.
    *   `ADD`
    *   `SUBTRACT`
    *   `MULTIPLY`
    *   `DIVIDE`
    *   `SET` (Same as set_variable but strictly numeric)
*   **amount** (Number): The number to use in the operation.

---

## World & Visual Actions

### `spawn_item`
Drops an item at a specific location.
*   **material** (String): The Bukkit Material name (e.g., `DIAMOND`).
*   **amount** (Integer, optional): Number of items (default: 1).
*   **offset** (List `[x, y, z]`, optional): Position relative to the controller (default: `[0, 1, 0]`).

### `spawn_entity`
Spawns a living entity or mob.
*   **entity_type** (String): The EntityType (e.g., `ZOMBIE`, `VILLAGER`).
*   **name** (String, optional): Custom name for the entity.
*   **offset** (List `[x, y, z]`, optional): Position relative to controller.

### `teleport`
Teleports the target to a location relative to the controller.
*   **offset** (List `[x, y, z]`): The destination coordinates relative to the anchor.
*   **target** (String, optional): Selector (default: triggering player).

### `title`
Shows a large title on screen.
*   **title** (String): Main text.
*   **subtitle** (String): Smaller text below.
*   **fade_in** (Int): Ticks to fade in.
*   **stay** (Int): Ticks to stay.
*   **fade_out** (Int): Ticks to fade out.
*   **target** (String, optional).

### `actionbar`
Shows text above the hotbar.
*   **message** (String): The text.
*   **target** (String, optional).
