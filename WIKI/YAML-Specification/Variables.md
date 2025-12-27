# Variables

Each instance can store persistent data.

## Definition
Define default values in the `variables` section.
```yaml
variables:
  energy: 0
  owner_name: "None"
  is_locked: false
```

## Usage in Actions
*   **Set**: `type: set_variable`, `key: energy`, `value: 100`
*   **Modify**: `type: modify_variable`, `key: energy`, `operation: ADD`, `amount: 10`

## Placeholders
Use `<variable:key>` in strings to display values.
*   `"Energy: <variable:energy>"` -> "Energy: 50"
