# States

Instances have a state machine to control their lifecycle.

## Built-in States
1.  **`ACTIVE`**: The default working state. Ticks run normally.
2.  **`INACTIVE`**: Ticks still run (to allow checking conditions), but logic is usually paused by convention.
3.  **`DISABLED`**: Ticks **do not run**. Hard stop.
4.  **`DAMAGED`**: Automatically set if part of the structure is broken (if partial-break logic is enabled, otherwise structure is destroyed).
5.  **`OVERLOADED`**: Custom state often used for error conditions.

## Custom States
You can use `set_state` to set any custom string, but these 5 are handled specially by the engine.
