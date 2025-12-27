# Events

Events trigger lists of actions.

## `on_create`
Triggered once when the structure is first formed.
*   *Use case: Set initial state, send confirmation message.*

## `on_interact`
Triggered when a player right-clicks the Controller block.
*   *Use case: Open GUIs, toggle state, give items.*

## `on_tick`
Triggered periodically based on `tick_interval`.
*   *Use case: Process resources, play particles, update variables.*

## `on_break`
Triggered when the structure is destroyed (any block broken).
*   *Use case: Drop stored items, explosion, warning message.*
