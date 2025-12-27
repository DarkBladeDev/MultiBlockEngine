# Controllers

The `controller` field defines the block type that acts as the center of the structure.

## Simple Material
```yaml
controller: DIAMOND_BLOCK
```

## With Block Data
You can specify exact block states (like facing or open/closed).
```yaml
controller: "minecraft:chest[facing=north]"
```
*Note: When using directional controllers, the structure's rotation is determined by the block's facing.*
