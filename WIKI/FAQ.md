# Frequently Asked Questions (FAQ)

## General

**Q: Can I use this plugin on 1.8 - 1.12?**
A: No. MultiBlockEngine is designed for modern Minecraft versions (1.20+) to take advantage of new features like `TextDisplay` entities (for high-performance holograms) and updated BlockData APIs.

**Q: Is there a limit to how big a multiblock can be?**
A: Technically, no, but very large structures (e.g., 50x50x50) may cause lag when the plugin checks the pattern, especially if `tick_interval` is low. Keep structures reasonably sized (e.g., under 10x10x10) for best performance.

## Creation & Customization

**Q: Can I use custom items (like from ItemsAdder or Oraxen) as part of the structure?**
A: Currently, the pattern matcher only supports vanilla `Material` types. However, you can add `on_interact` conditions to check for specific ItemMeta (name, lore, custom model data) in the player's hand.

**Q: How do I rotate a structure?**
A: The plugin currently supports fixed-orientation patterns. If you need a structure to be buildable in 4 directions, you currently need to define 4 separate valid patterns or wait for a future update supporting automatic rotation checks.

**Q: Can I execute console commands?**
A: Yes! Use the `command` action with `console: true`.
```yaml
- type: command
  console: true
  value: "give %player% diamond 1"
```

## Troubleshooting

**Q: My hologram text is overlapping.**
A: Check your `y_offset` in the multiblock configuration. If it's too low, it might clip into the block.

**Q: Variables aren't saving.**
A: Variables are stored in memory for active instances. If the server restarts, variables *should* persist if persistence is enabled in the config (feature dependency). Ensure your server shuts down cleanly.

**Q: The plugin says "Invalid Action Type".**
A: Double-check the spelling in the [Actions Reference](YAML-Specification/Actions.md). YAML is case-sensitive for keys, though usually values like `ACTIVE` are parsed leniently, keys like `type` must be exact.
