package com.darkbladedev.engine.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.darkbladedev.engine.api.item.ItemDefinition;
import com.darkbladedev.engine.api.item.ItemInstance;
import com.darkbladedev.engine.api.item.ItemKey;
import com.darkbladedev.engine.api.item.ItemKeys;
import com.darkbladedev.engine.api.wrench.WrenchDispatcher;
import com.darkbladedev.engine.item.DefaultItemService;
import com.darkbladedev.engine.item.bridge.PdcItemStackBridge;
import com.darkbladedev.engine.manager.MultiblockManager;
import com.darkbladedev.engine.model.DisplayNameConfig;
import com.darkbladedev.engine.model.MultiblockInstance;
import com.darkbladedev.engine.model.MultiblockType;
import com.darkbladedev.engine.model.action.Action;
import com.darkbladedev.engine.wrench.DefaultWrenchDispatcher;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class MultiblockListenerInteractCancelTest {

    private ServerMock server;
    private WorldMock world;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void interactIsCancelledWhenMenuActionConsumesVanilla() throws Exception {
        Location loc = new Location(world, 10, 64, 10);

        Action openMenu = new Action() {
            @Override
            public void execute(MultiblockInstance instance, Player player) {
            }

            @Override
            public boolean shouldExecuteOnInteract(org.bukkit.event.block.Action interactAction) {
                return interactAction == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
            }

            @Override
            public boolean cancelsVanillaOnInteract(org.bukkit.event.block.Action interactAction) {
                return interactAction == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
            }
        };

        MultiblockInstance instance = new MultiblockInstance(dummyType("custom:menu_block", List.of(openMenu)), loc, BlockFace.NORTH);

        TestManager manager = new TestManager(instance);
        WrenchTestHarness harness = new WrenchTestHarness(manager);
        MultiblockListener listener = new MultiblockListener(manager, e -> {
        }, harness.dispatcher);

        Block block = world.getBlockAt(10, 64, 10);
        block.setType(Material.STONE);
        PlayerInteractEvent interactEvent = newPlayerInteractEvent(player, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, null, block, EquipmentSlot.HAND);

        listener.onInteract(interactEvent);

        assertTrue(interactEvent.isCancelled());
    }

    @Test
    void interactOnBeaconIsCancelledWhenMenuActionConsumesVanilla() throws Exception {
        Location loc = new Location(world, 10, 64, 10);

        Action openMenu = new Action() {
            @Override
            public void execute(MultiblockInstance instance, Player player) {
            }

            @Override
            public boolean shouldExecuteOnInteract(org.bukkit.event.block.Action interactAction) {
                return interactAction == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
            }

            @Override
            public boolean cancelsVanillaOnInteract(org.bukkit.event.block.Action interactAction) {
                return interactAction == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
            }
        };

        MultiblockInstance instance = new MultiblockInstance(dummyType("custom:beacon_menu_block", List.of(openMenu)), loc, BlockFace.NORTH);

        TestManager manager = new TestManager(instance);
        WrenchTestHarness harness = new WrenchTestHarness(manager);
        MultiblockListener listener = new MultiblockListener(manager, e -> {
        }, harness.dispatcher);

        Block beacon = world.getBlockAt(10, 64, 10);
        beacon.setType(Material.BEACON);
        PlayerInteractEvent interactEvent = newPlayerInteractEvent(player, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, null, beacon, EquipmentSlot.HAND);

        listener.onInteract(interactEvent);

        assertTrue(interactEvent.isCancelled());
    }

    private static PlayerInteractEvent newPlayerInteractEvent(Player player, org.bukkit.event.block.Action action, ItemStack item, Block clickedBlock, EquipmentSlot hand) throws Exception {
        for (Constructor<?> c : PlayerInteractEvent.class.getConstructors()) {
            Class<?>[] p = c.getParameterTypes();
            if (p.length == 6
                    && p[0] == Player.class
                    && p[1] == org.bukkit.event.block.Action.class
                    && p[2].getName().equals("org.bukkit.inventory.ItemStack")
                    && p[3] == Block.class
                    && p[4] == BlockFace.class
                    && p[5] == EquipmentSlot.class) {
                return (PlayerInteractEvent) c.newInstance(player, action, item, clickedBlock, BlockFace.UP, hand);
            }
            if (p.length == 5
                    && p[0] == Player.class
                    && p[1] == org.bukkit.event.block.Action.class
                    && p[2].getName().equals("org.bukkit.inventory.ItemStack")
                    && p[3] == Block.class
                    && p[4] == BlockFace.class) {
                return (PlayerInteractEvent) c.newInstance(player, action, item, clickedBlock, BlockFace.UP);
            }
        }
        throw new IllegalStateException("No compatible PlayerInteractEvent constructor found");
    }

    private static final class WrenchTestHarness {
        private static final ItemKey WRENCH_KEY = ItemKeys.of("mbe:wrench", 0);

        private final DefaultItemService items;
        private final PdcItemStackBridge bridge;
        private final ItemStack wrench;
        private final WrenchDispatcher dispatcher;

        private WrenchTestHarness(MultiblockManager manager) {
            this.items = new DefaultItemService();
            this.bridge = new PdcItemStackBridge(items);
            this.items.registry().register(new ItemDefinition() {
                @Override
                public ItemKey key() {
                    return WRENCH_KEY;
                }

                @Override
                public String displayName() {
                    return "Wrench";
                }

                @Override
                public Map<String, Object> properties() {
                    return Map.of("material", "IRON_HOE");
                }
            });
            ItemInstance instance = items.factory().create(WRENCH_KEY);
            this.wrench = bridge.toItemStack(instance);
            this.dispatcher = new DefaultWrenchDispatcher(manager, bridge, null, e -> {
            });
        }
    }

    private static MultiblockType dummyType(String id, List<Action> onInteract) {
        return new MultiblockType(
                id,
                "1.0",
                new Vector(0, 0, 0),
                block -> false,
                List.of(),
                false,
                Map.of(),
                Map.of(),
                List.of(),
                List.of(),
                onInteract,
                List.of(),
                new DisplayNameConfig("", false, "hologram"),
                20,
                List.of()
        );
    }

    private static final class TestManager extends MultiblockManager {
        private final MultiblockInstance instance;

        private TestManager(MultiblockInstance instance) {
            this.instance = instance;
        }

        @Override
        public Optional<MultiblockInstance> getInstanceAt(Location loc) {
            return Optional.of(instance);
        }
    }
}
