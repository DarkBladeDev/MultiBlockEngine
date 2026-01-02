package com.darkbladedev.engine.listener;

import com.darkbladedev.engine.api.event.MultiblockBreakEvent;
import com.darkbladedev.engine.manager.MultiblockManager;
import com.darkbladedev.engine.model.DisplayNameConfig;
import com.darkbladedev.engine.model.MultiblockInstance;
import com.darkbladedev.engine.model.MultiblockType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class MultiblockListenerBreakEventTest {

    @Test
    void breakEventIsFiredAndInstanceDestroyedWhenNotCancelled() {
        UUID worldId = UUID.randomUUID();
        World world = worldProxy(worldId);
        Location loc = new Location(world, 10, 64, 10);

        MultiblockInstance instance = new MultiblockInstance(dummyType("storage:disk"), loc, BlockFace.NORTH);

        TestManager manager = new TestManager(instance);
        List<Event> events = new ArrayList<>();
        Consumer<Event> caller = events::add;
        MultiblockListener listener = new MultiblockListener(manager, caller);

        Block block = blockProxy(loc);
        Player player = playerProxy();
        BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);

        listener.onBlockBreak(breakEvent);

        assertTrue(manager.destroyed);
        assertFalse(breakEvent.isCancelled());
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof MultiblockBreakEvent);
        MultiblockBreakEvent fired = (MultiblockBreakEvent) events.get(0);
        assertSame(instance, fired.getMultiblock());
        assertFalse(fired.isCancelled());
    }

    @Test
    void breakCancellationPreventsDestroy() {
        UUID worldId = UUID.randomUUID();
        World world = worldProxy(worldId);
        Location loc = new Location(world, 10, 64, 10);

        MultiblockInstance instance = new MultiblockInstance(dummyType("storage:disk"), loc, BlockFace.NORTH);

        TestManager manager = new TestManager(instance);
        List<Event> events = new ArrayList<>();
        Consumer<Event> caller = e -> {
            events.add(e);
            if (e instanceof MultiblockBreakEvent mbe) {
                mbe.setCancelled(true);
            }
        };
        MultiblockListener listener = new MultiblockListener(manager, caller);

        Block block = blockProxy(loc);
        Player player = playerProxy();
        BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);

        listener.onBlockBreak(breakEvent);

        assertFalse(manager.destroyed);
        assertTrue(breakEvent.isCancelled());
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof MultiblockBreakEvent);
        assertTrue(((MultiblockBreakEvent) events.get(0)).isCancelled());
    }

    private static MultiblockType dummyType(String id) {
        return new MultiblockType(
                id,
                "1.0",
                new Vector(0, 0, 0),
                block -> false,
                List.of(),
                false,
                java.util.Map.of(),
                java.util.Map.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new DisplayNameConfig("", false, "hologram"),
                20,
                List.of()
        );
    }

    private static World worldProxy(UUID uid) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getUID") && method.getParameterCount() == 0) {
                        return uid;
                    }
                    if (method.getReturnType().isPrimitive()) {
                        if (method.getReturnType() == boolean.class) return false;
                        if (method.getReturnType() == int.class) return 0;
                        if (method.getReturnType() == long.class) return 0L;
                        if (method.getReturnType() == float.class) return 0f;
                        if (method.getReturnType() == double.class) return 0d;
                        if (method.getReturnType() == short.class) return (short) 0;
                        if (method.getReturnType() == byte.class) return (byte) 0;
                        if (method.getReturnType() == char.class) return (char) 0;
                    }
                    return null;
                }
        );
    }

    private static Block blockProxy(Location location) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getLocation") && method.getParameterCount() == 0) {
                        return location;
                    }
                    if (method.getReturnType().isPrimitive()) {
                        if (method.getReturnType() == boolean.class) return false;
                        if (method.getReturnType() == int.class) return 0;
                        if (method.getReturnType() == long.class) return 0L;
                        if (method.getReturnType() == float.class) return 0f;
                        if (method.getReturnType() == double.class) return 0d;
                        if (method.getReturnType() == short.class) return (short) 0;
                        if (method.getReturnType() == byte.class) return (byte) 0;
                        if (method.getReturnType() == char.class) return (char) 0;
                    }
                    return null;
                }
        );
    }

    private static Player playerProxy() {
        UUID uuid = UUID.randomUUID();
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getUniqueId") && method.getParameterCount() == 0) {
                        return uuid;
                    }
                    if (method.getReturnType() == String.class && method.getParameterCount() == 0) {
                        if (method.getName().equals("getName")) {
                            return "TestPlayer";
                        }
                    }
                    if (method.getReturnType().isPrimitive()) {
                        if (method.getReturnType() == boolean.class) return false;
                        if (method.getReturnType() == int.class) return 0;
                        if (method.getReturnType() == long.class) return 0L;
                        if (method.getReturnType() == float.class) return 0f;
                        if (method.getReturnType() == double.class) return 0d;
                        if (method.getReturnType() == short.class) return (short) 0;
                        if (method.getReturnType() == byte.class) return (byte) 0;
                        if (method.getReturnType() == char.class) return (char) 0;
                    }
                    return null;
                }
        );
    }

    private static final class TestManager extends MultiblockManager {
        private MultiblockInstance instance;
        private boolean destroyed;

        private TestManager(MultiblockInstance instance) {
            this.instance = instance;
        }

        @Override
        public Optional<MultiblockInstance> getInstanceAt(Location loc) {
            return Optional.ofNullable(instance);
        }

        @Override
        public void destroyInstance(MultiblockInstance instance) {
            this.destroyed = true;
            this.instance = null;
        }
    }
}

