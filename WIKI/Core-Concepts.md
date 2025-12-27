# Core Concepts

## 1. The Controller
Every multiblock has exactly **one** Controller block. This is the "brain" of the structure.
*   It serves as the **Anchor Point** (0, 0, 0) for all coordinate offsets.
*   It is the block players interact with to trigger events.
*   If the controller is broken, the multiblock is destroyed.

## 2. Patterns & Rotation
Structures are defined using offsets relative to the Controller.
*   **Rotation is automatic**: If you define a pattern facing NORTH, the plugin automatically calculates positions for EAST, SOUTH, and WEST.
*   The plugin detects which way the player or block is facing when creating the structure.

## 3. Instances
When a player builds a structure and right-clicks the controller, a **MultiblockInstance** is created.
*   **Persistent**: Saved to the database (SQLite/MySQL).
*   **Stateful**: Each instance has its own State (ACTIVE, INACTIVE) and Variables.

## 4. ECA System (Event-Condition-Action)
The engine runs on a reactive system:
1.  **Event**: Something happens (Interact, Tick, Break).
2.  **Condition**: Are requirements met? (Is player sneaking? Is state ACTIVE?).
3.  **Action**: Execute logic (Send message, spawn item, change state).
