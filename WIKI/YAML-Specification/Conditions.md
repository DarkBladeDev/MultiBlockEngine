# Conditions Reference

Conditions determine whether an action (typically inside a `conditional` action) should execute.

## General Conditions

### `state`
Checks the current lifecycle state of the instance.
*   **value** (String): The state to check for (e.g., `ACTIVE`, `INACTIVE`).

### `variable`
Checks the value of a persistent variable.
*   **key** (String): The variable name.
*   **value** (Any): The value to compare against.
*   **comparison** (String, optional): How to compare (Default: `EQUALS`).
    *   `EQUALS`
    *   `NOT_EQUALS`
    *   `GREATER_THAN`
    *   `GREATER_OR_EQUAL`
    *   `LESS_THAN`
    *   `LESS_OR_EQUAL`

## Player Conditions

### `permission`
Checks if the interacting player has a specific permission node.
*   **value** (String): The permission node (e.g., `mbe.use.teleport`).

### `sneaking`
Checks if the player is currently sneaking (crouching).
*   **value** (Boolean, optional): `true` to require sneaking, `false` to require standing (Default: `true`).
