# Debugging & Troubleshooting

When working with complex multiblock structures and YAML configurations, things can sometimes go wrong. This guide will help you diagnose and fix common issues.

## Common Issues

### 1. Structure Not Forming
**Symptoms:** You build the structure, right-click the controller, but nothing happens (no hologram, no "created" message).

**Possible Causes:**
*   **Pattern Mismatch:** The blocks placed in the world do not exactly match the `pattern` defined in YAML. Check for:
    *   Correct materials (e.g., `STONE` vs `COBBLESTONE`).
    *   Block data (e.g., stairs facing the wrong way, logs with wrong axis).
    *   Empty spaces (air) where blocks should be, or vice versa.
*   **Controller Position:** You are clicking a block that is not defined as the `controller` in the YAML, or the controller is not in the correct relative position within the pattern.
*   **YAML Syntax Error:** If the file has invalid YAML, it might not have loaded at all. Check the console on startup.

### 2. Actions Not Triggering
**Symptoms:** The structure is formed (hologram appears), but interactions don't do anything.

**Possible Causes:**
*   **Event Mismatch:** You are listening for `on_interact` but trying to trigger it by walking (`on_tick` needed) or breaking (`on_break` needed).
*   **Conditions Failing:** If you have `conditional` actions, the `else` block might be empty, so it fails silently. Add a message to the `else` block to debug.
*   **Cooldowns:** If you implemented a variable-based cooldown, ensure it's resetting correctly.

### 3. Holograms Not Disappearing
**Symptoms:** You break the structure, but the text display remains floating.

**Fix:**
*   Ensure the plugin is updated to the latest version (fixes were made to handle race conditions).
*   Run `/mbe reload` or restart the server to clear "ghost" instances (though persistent data should handle this).

## Using the Debug Mode

The MultiBlockEngine comes with a built-in debug mode that prints detailed information to the console.

**To enable:**
1.  Open `config.yml`.
2.  Set `debug: true`.
3.  Run `/mbe reload`.

**What to look for in logs:**
*   `[MultiBlockEngine] Loading multiblock: my_structure...` - Confirms the file is found.
*   `[MultiBlockEngine] Pattern check failed at (x, y, z). Expected: STONE, Found: DIRT` - Extremely helpful for fixing pattern issues.
*   `[MultiBlockEngine] Executing action: message` - Confirms the event fired.

## YAML Validation

YAML is sensitive to indentation.
*   **Do not use tabs.** Use spaces (usually 2 or 4).
*   Use a linter (like [YAMLLint](http://www.yamllint.com/)) to verify your files before uploading.

## Reporting Bugs

If you believe you've found a bug in the plugin itself (e.g., a crash or unexpected behavior not explained by config), please report it with:
1.  Your `config.yml` and the specific multiblock YAML file.
2.  The server log (latest.log) showing the error stack trace.
3.  Steps to reproduce the issue.
