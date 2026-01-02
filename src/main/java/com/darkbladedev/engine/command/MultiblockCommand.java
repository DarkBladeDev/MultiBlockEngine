package com.darkbladedev.engine.command;

import com.darkbladedev.engine.MultiBlockEngine;
import com.darkbladedev.engine.manager.MetricsManager;
import com.darkbladedev.engine.manager.MultiblockManager;
import com.darkbladedev.engine.command.services.ServicesCommandRouter;
import com.darkbladedev.engine.command.services.impl.ItemsCommandService;
import com.darkbladedev.engine.command.services.impl.UiCommandService;
import com.darkbladedev.engine.model.MultiblockInstance;
import com.darkbladedev.engine.model.MultiblockType;
import com.darkbladedev.engine.model.PatternEntry;
import com.darkbladedev.engine.parser.MultiblockParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MultiblockCommand implements CommandExecutor, TabCompleter {

    private final MultiBlockEngine plugin;
    private final ServicesCommandRouter services;

    public MultiblockCommand(MultiBlockEngine plugin) {
        this.plugin = plugin;
        this.services = new ServicesCommandRouter(plugin);
        this.services.registerInternal(new ItemsCommandService(plugin));
        this.services.registerInternal(new UiCommandService());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String[] safeArgs = args == null ? new String[0] : args;
        if (safeArgs.length > 0 && safeArgs[0].equalsIgnoreCase("services")) {
            return services.handle(sender, label, safeArgs);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Usage: /" + label + " <inspect|reload|status|debug|services>", NamedTextColor.YELLOW));
            return true;
        }

        if (safeArgs.length == 0) {
            player.sendMessage(Component.text("Usage: /mb <inspect|reload>", NamedTextColor.YELLOW));
            return true;
        }

        if (safeArgs[0].equalsIgnoreCase("inspect")) {
            handleInspect(player);
            return true;
        } else if (safeArgs[0].equalsIgnoreCase("status") || safeArgs[0].equalsIgnoreCase("stats")) {
            handleStatus(player);
            return true;
        } else if (safeArgs[0].equalsIgnoreCase("reload")) {
            handleReload(player);
            return true;
        } else if (safeArgs[0].equalsIgnoreCase("debug")) {
            handleDebug(player, safeArgs);
            return true;
        }

        player.sendMessage(Component.text("Unknown subcommand.", NamedTextColor.RED));
        return true;
    }

    private void handleDebug(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /mb debug <id> [player]", NamedTextColor.RED));
            return;
        }
        
        String id = args[1];
        Optional<MultiblockType> typeOpt = plugin.getManager().getType(id);
        
        if (typeOpt.isEmpty()) {
            player.sendMessage(Component.text("Multiblock type not found: " + id, NamedTextColor.RED));
            return;
        }
        MultiblockType type = typeOpt.get();
        
        // Target player
        Player targetPlayer = player;
        if (args.length >= 3) {
            targetPlayer = org.bukkit.Bukkit.getPlayer(args[2]);
            if (targetPlayer == null) {
                player.sendMessage(Component.text("Player not found: " + args[2], NamedTextColor.RED));
                return;
            }
        }
        
        // Raytrace for anchor
        Block targetBlock = targetPlayer.getTargetBlockExact(10);
        if (targetBlock == null || targetBlock.getType().isAir()) {
            player.sendMessage(Component.text("You must look at a block to use as the controller anchor.", NamedTextColor.RED));
            return;
        }
        
        // Start session
        plugin.getDebugManager().startSession(targetPlayer, type, targetBlock.getLocation());
    }

    private void handleReload(Player player) {
        player.sendMessage(Component.text("Reloading MultiBlockEngine...", NamedTextColor.YELLOW));
        
        // Reload Config
        plugin.reloadConfig();
        
        // Reload Types
        File multiblockDir = new File(plugin.getDataFolder(), "multiblocks");
        if (!multiblockDir.exists()) {
            multiblockDir.mkdirs();
        }
        
        MultiblockParser parser = plugin.getParser();
        List<MultiblockType> newTypes = parser.loadAll(multiblockDir);
        
        plugin.getManager().reloadTypes(newTypes);
        
        // Restart ticking with new config
        plugin.getManager().startTicking(plugin);
        
        player.sendMessage(Component.text("Reloaded " + newTypes.size() + " multiblock types.", NamedTextColor.GREEN));
        player.sendMessage(Component.text("Metrics and Ticking restarted.", NamedTextColor.GREEN));
    }
    
    private void handleStatus(Player player) {
        MultiblockManager manager = plugin.getManager();
        MetricsManager metrics = manager.getMetrics();
        
        player.sendMessage(Component.text("=== MultiBlockEngine Status ===", NamedTextColor.BLUE));
        player.sendMessage(Component.textOfChildren(
                Component.text("Loaded Types: ", NamedTextColor.GRAY),
                Component.text(String.valueOf(manager.getTypes().size()), NamedTextColor.WHITE)
        ));
        player.sendMessage(Component.textOfChildren(
                Component.text("Total Created: ", NamedTextColor.GRAY),
                Component.text(String.valueOf(metrics.getCreatedInstances()), NamedTextColor.WHITE)
        ));
        player.sendMessage(Component.textOfChildren(
                Component.text("Total Destroyed: ", NamedTextColor.GRAY),
                Component.text(String.valueOf(metrics.getDestroyedInstances()), NamedTextColor.WHITE)
        ));
        player.sendMessage(Component.textOfChildren(
                Component.text("Structure Checks: ", NamedTextColor.GRAY),
                Component.text(String.valueOf(metrics.getStructureChecks()), NamedTextColor.WHITE)
        ));
        player.sendMessage(Component.textOfChildren(
                Component.text("Avg Tick Time: ", NamedTextColor.GRAY),
                Component.text(String.format("%.4f ms", metrics.getAverageTickTimeMs()), NamedTextColor.WHITE)
        ));
    }

    private void handleInspect(Player player) {
        Block target = player.getTargetBlockExact(5);
        if (target == null) {
            player.sendMessage(Component.text("You must look at a block.", NamedTextColor.RED));
            return;
        }

        MultiblockManager manager = plugin.getManager();
        Optional<MultiblockInstance> instanceOpt = manager.getInstanceAt(target.getLocation());

        if (instanceOpt.isEmpty()) {
            player.sendMessage(Component.text("No multiblock structure found at this block.", NamedTextColor.YELLOW));
            return;
        }

        MultiblockInstance instance = instanceOpt.get();
        player.sendMessage(Component.text("=== Multiblock Info ===", NamedTextColor.GREEN));
        player.sendMessage(Component.textOfChildren(
                Component.text("Type: ", NamedTextColor.GOLD),
                Component.text(instance.type().id(), NamedTextColor.WHITE)
        ));
        player.sendMessage(Component.textOfChildren(
                Component.text("State: ", NamedTextColor.GOLD),
                Component.text(String.valueOf(instance.state()), NamedTextColor.WHITE)
        ));
        player.sendMessage(Component.textOfChildren(
                Component.text("Facing: ", NamedTextColor.GOLD),
                Component.text(String.valueOf(instance.facing()), NamedTextColor.WHITE)
        ));
        player.sendMessage(Component.textOfChildren(
                Component.text("Anchor: ", NamedTextColor.GOLD),
                Component.text(formatLoc(instance.anchorLocation()), NamedTextColor.WHITE)
        ));
        
        // Visualize
        highlightStructure(player, instance);
    }
    
    private void highlightStructure(Player player, MultiblockInstance instance) {
        // Simple particle visualization
        // Show particles at every block of the structure
        Location anchor = instance.anchorLocation();
        BlockFace facing = instance.facing();
        
        // We need access to rotateVector, but it is private in Manager.
        // We should probably expose a helper or duplicate logic.
        // For now, let's just duplicate logic or make it public static in Manager?
        // Let's duplicate for simplicity or check if we can make it public.
        // Or better, let's iterate the pattern and calculate.
        
        for (PatternEntry entry : instance.type().pattern()) {
            Vector offset = rotateVector(entry.offset(), facing);
            Location loc = anchor.clone().add(offset);
            
            player.spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0);
        }
        
        // Highlight anchor specifically
        player.spawnParticle(Particle.FLAME, anchor.clone().add(0.5, 0.5, 0.5), 10, 0.1, 0.1, 0.1, 0.05);
        
        player.sendMessage(Component.text("Structure highlighted.", NamedTextColor.AQUA));
    }

    private String formatLoc(Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }
    
    // Duplicate from Manager for now
    private Vector rotateVector(Vector v, BlockFace facing) {
        int x = v.getBlockX();
        int y = v.getBlockY();
        int z = v.getBlockZ();
        
        return switch (facing) {
            case NORTH -> new Vector(x, y, z);
            case EAST -> new Vector(-z, y, x);
            case SOUTH -> new Vector(-x, y, -z);
            case WEST -> new Vector(z, y, -x);
            default -> new Vector(x, y, z);
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String[] safeArgs = args == null ? new String[0] : args;
        if (safeArgs.length > 0 && safeArgs[0].equalsIgnoreCase("services")) {
            return services.tabComplete(sender, safeArgs);
        }

        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            subcommands.add("inspect");
            subcommands.add("reload");
            subcommands.add("status");
            subcommands.add("stats");
            subcommands.add("debug");
            subcommands.add("services");
            
            return filter(subcommands, args[0]);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            // Autocomplete multiblock types
            List<String> types = new ArrayList<>();
            for (MultiblockType type : plugin.getManager().getTypes()) {
                types.add(type.id());
            }
            return filter(types, args[1]);
        } else if (args.length == 3 && args[0].equalsIgnoreCase("debug")) {
            // Autocomplete players
            return null; // Bukkit default player completion
        }
        
        return Collections.emptyList();
    }
    
    private List<String> filter(List<String> list, String input) {
        List<String> filtered = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(s);
            }
        }
        Collections.sort(filtered);
        return filtered;
    }
}
