package com.darkbladedev.engine.api.wiring.debug;

import com.darkbladedev.engine.api.wiring.BlockPos;
import com.darkbladedev.engine.api.wiring.Direction;
import com.darkbladedev.engine.api.wiring.NetworkNode;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface NetworkDebugService {

    Optional<NetworkNode> nodeAt(BlockPos pos);

    Optional<NetworkSnapshot> inspect(BlockPos pos);

    boolean toggleEdge(NetworkNode node, Direction dir);

    boolean visualize(NetworkSnapshot snapshot, Duration time, UUID playerId);
}

